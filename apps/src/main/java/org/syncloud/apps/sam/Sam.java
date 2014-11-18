package org.syncloud.apps.sam;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.syncloud.common.progress.Progress;
import org.syncloud.common.model.Result;
import org.syncloud.ssh.Ssh;
import org.syncloud.ssh.model.Device;

import java.util.List;

import static org.syncloud.apps.sam.Command.List;
import static org.syncloud.apps.sam.Command.Update;
import static org.syncloud.common.model.Result.value;

public class Sam {
    public static final ObjectMapper JSON = new ObjectMapper();
    public static final String RELEASE = "0.7";
    private Ssh ssh;
    private Progress progress;

    public Sam(Ssh ssh, Progress progress) {
        this.ssh = ssh;
        this.progress = progress;
    }

    public Result<String> run(Device device, Command command, String... arguments) {

        return ssh.execute(device, command.cmd(arguments));
    }

    public Result<List<AppVersions>> update(Device device) {
        progress.title("Checking for updates");
        return appList(device, Update, "--release", RELEASE);
    }

    public Result<List<AppVersions>> list(Device device) {
        progress.title("Refreshing app list");
        return appList(device, List);
    }

    private Result<List<AppVersions>> appList(Device device, Command command, String... arguments) {
        return run(device, command, arguments).flatMap(new Result.Function<String, Result<List<AppVersions>>>() {
            @Override
            public Result<List<AppVersions>> apply(String v) throws Exception {
                return value(JSON.readValue(v, AppListReply.class).data);
            }
        });
    }

}
