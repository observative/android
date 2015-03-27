package org.syncloud.platform.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.platform.ssh.ConnectionPointProvider;
import org.syncloud.platform.ssh.SshRunner;
import org.syncloud.platform.ssh.model.Credentials;
import org.syncloud.platform.ssh.model.SshResult;
import org.syncloud.platform.ssh.model.StringResult;

import java.io.IOException;

import static org.syncloud.platform.ssh.SshRunner.cmd;

public class Server {

    public static final String SYNCLOUD_CLI = "syncloud-cli";
    private static Logger logger = Logger.getLogger(Server.class);

    public static final ObjectMapper JSON = new ObjectMapper();


    private SshRunner ssh;

    public Server(SshRunner ssh) {
        this.ssh = ssh;
    }

    public Optional<Credentials> activate(
            ConnectionPointProvider connectionPoint,
            String version,
            String topLevelDomain,
            String apiUrl,
            String email,
            String pass,
            String userDomain) {

        logger.info("activating " + userDomain);

        String[] activateCmd = cmd(SYNCLOUD_CLI, "activate", version, topLevelDomain, apiUrl, email, pass, userDomain);

        Optional<String> run = ssh.run(connectionPoint, activateCmd);

        if (run.isPresent()) {
            try {
                SshResult<Credentials> reference = JSON.readValue(run.get(), new TypeReference<SshResult<Credentials>>() {});
                return Optional.of(reference.data);
            } catch (IOException e) {
                logger.error("unable to parse execute response", e);
            }
        } else {
            logger.error("unable to execute command");
        }

        return Optional.absent();
    }

    public Optional<Credentials> get_access(ConnectionPointProvider connectionPoint) {

        logger.info("getting access");

        Optional<String> run = ssh.run(connectionPoint, cmd(SYNCLOUD_CLI, "get_access"));

        if (run.isPresent()) {
            try {
                SshResult<Credentials> reference = JSON.readValue(run.get(), new TypeReference<SshResult<Credentials>>() {});
                return Optional.of(reference.data);
            } catch (IOException e) {
                logger.error("unable to parse execute response", e);
            }
        } else {
            logger.error("unable to execute command");
        }

        return Optional.absent();
    }

    public Optional<String> userDomain(ConnectionPointProvider connectionPoint) {
        Optional<String> execute = ssh.run(connectionPoint, cmd(SYNCLOUD_CLI, "user_domain"));
        if (execute.isPresent()) {
            try {
                return Optional.of(JSON.readValue(execute.get(), StringResult.class).data);
            } catch (IOException e) {
                logger.error("unable to parse user domain reply");
            }
        }

        logger.error("unable to get user domain reply");
        return Optional.absent();
    }
}
