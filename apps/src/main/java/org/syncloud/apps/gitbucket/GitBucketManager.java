package org.syncloud.apps.gitbucket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

import org.apache.log4j.Logger;
import org.syncloud.ssh.ConnectionPointProvider;
import org.syncloud.ssh.SshRunner;
import org.syncloud.ssh.model.StringResult;

import java.io.IOException;

public class GitBucketManager {
    private static Logger logger = Logger.getLogger(GitBucketManager.class);

    private static String CTL_BIN = "gitbucket-ctl";
    public static final ObjectMapper JSON = new ObjectMapper();
    private SshRunner ssh;

    public GitBucketManager() {
        this.ssh = new SshRunner();
    }

    public Optional<String> enable(ConnectionPointProvider connectionPoint, String login, String password) {
        return ssh.run(connectionPoint, String.format("%s enable %s %s", CTL_BIN, login, password));
    }

    public Optional<String> disable(ConnectionPointProvider connectionPoint) {
        return ssh.run(connectionPoint, String.format("%s disable", CTL_BIN));
    }

    public Optional<String> url(ConnectionPointProvider connectionPoint) {

        Optional<String> execute = ssh.run(connectionPoint, String.format("%s url", CTL_BIN));
        if (execute.isPresent())
            try {
                return Optional.of(JSON.readValue(execute.get(), StringResult.class).data);
            } catch (IOException e) {
                logger.error("unable to parse gitbucket url response");
            }
        return Optional.absent();
    }
}