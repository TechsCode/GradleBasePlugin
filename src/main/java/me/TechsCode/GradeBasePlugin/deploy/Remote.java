package me.TechsCode.GradeBasePlugin.deploy;

import com.google.gson.JsonObject;
import com.jcraft.jsch.*;
import me.TechsCode.GradeBasePlugin.Color;
import me.TechsCode.GradeBasePlugin.GradleBasePlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

public class Remote {
    private final boolean enabled;
    private final String hostname, username, password, path;
    private final int port;

    public Remote(JsonObject jsonObject) {
        this.enabled = jsonObject.get("enabled").getAsBoolean();
        this.hostname = jsonObject.get("hostname").getAsString();
        this.port = jsonObject.has("port") ? jsonObject.get("port").getAsInt() : 22;
        this.username = jsonObject.get("username").getAsString();
        this.password = jsonObject.get("password").getAsString();
        this.path = jsonObject.get("path").getAsString();
    }

    public void uploadFile(File file) {
        try {
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");

            JSch jsch = new JSch();
            Session session = jsch.getSession(username, hostname, port);
            session.setPassword(password);
            session.setConfig(config);
            session.connect();

            ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect();
            sftp.cd(path);
            sftp.put(new FileInputStream(file), file.getName(), ChannelSftp.OVERWRITE);
            sftp.exit();

            session.disconnect();
        }
        catch (JSchException | SftpException | FileNotFoundException e) {
            GradleBasePlugin.log(Color.RED_BOLD_BRIGHT + "Couldn't upload file to remote '"
                    + hostname + "':");
            e.printStackTrace();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
