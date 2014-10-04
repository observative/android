package org.syncloud.ssh;

import com.google.common.io.ByteStreams;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.apache.commons.lang3.StringUtils;
import org.syncloud.ssh.model.Device;
import org.syncloud.ssh.model.DirectEndpoint;
import org.syncloud.common.model.Result;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static java.util.Arrays.asList;

public class Ssh {

    public static final int SSH_SERVER_PORT = 22;
    public static final String SSH_TYPE = "_ssh._tcp";

    public static Result<String> execute(Device device, String command) {
        return execute(device, asList(command));
    }

    public static Result<String> execute(Device device, List<String> commands) {

        String error = "";

        try {
            return run(device.getLocalEndpoint(), commands);
        } catch (Exception localException) {
            error += localException.getMessage();
        }

        EndpointResolver resolver = new EndpointResolver(new Dns());
        Result<DirectEndpoint> remote = resolver.dnsService(device.getUserDomain(), SSH_TYPE, device.getLocalEndpoint().getKey());
        if (remote.hasError())
            return Result.error(remote.getError());

        try {
            return run(remote.getValue(), commands);
        } catch (Exception remoteException) {
            error += remoteException.getMessage();
        }

        return Result.error(error);
    }

    private static Result<String> run(DirectEndpoint endpoint, List<String> commands) throws JSchException, IOException {

        JSch jsch = new JSch();

        Session session = jsch.getSession(endpoint.getLogin(), endpoint.getHost(), endpoint.getPort());
        session.setTimeout(3000);
        if (endpoint.getKey() == null) {
            session.setPassword(endpoint.getPassword());
        } else {
            jsch.addIdentity(endpoint.getLogin(), endpoint.getKey().getBytes(), null, new byte[0]);
            session.setUserInfo(new EmptyUserInfo());
        }

        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        try {
            session.connect();

            ChannelExec channel = (ChannelExec) session.openChannel("exec");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            channel.setOutputStream(baos);

            ByteArrayOutputStream err = new ByteArrayOutputStream();
            channel.setErrStream(err);

            channel.setCommand(StringUtils.join(commands, "; "));
            InputStream inputStream = channel.getInputStream();

            try {
                channel.connect();
                String otput = new String(ByteStreams.toByteArray(inputStream));
                while (channel.getExitStatus() == -1) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ignored) {
                    }
                }
                //TODO: export output stream for progress monitor
                String message = baos.toString() + otput + err.toString();
                if (channel.getExitStatus() == 0)
                    return Result.value(message);
                else
                    return Result.error(message);
            } finally {
                if (channel.isConnected())
                    channel.disconnect();
            }
        } finally {
            if (session.isConnected())
                session.disconnect();
        }
    }

}
