package me.TechsCode.GradeBasePlugin.deploy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.gradle.api.Project;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import me.TechsCode.GradeBasePlugin.Color;
import me.TechsCode.GradeBasePlugin.GradleBasePlugin;
import me.TechsCode.GradeBasePlugin.resource.ResourceManager;

public class DeploymentFile {
    
    private static JsonObject root;
    
    public DeploymentFile(Project project) {
        File global = new File(System.getProperty("user.home") + "/deployment.json");
        File local = new File(project.getProjectDir().getAbsolutePath() + "/deployment.json");
        
        File file = local;
        
        if (global.exists() && !local.exists()) {
            file = global;
        }
        if (!file.exists()) {
            try {
                InputStream src = ResourceManager.class.getResourceAsStream("/deployment.json");
                Files.copy(src, Paths.get(file.toURI()), StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            root = new Gson().fromJson(new FileReader(file), JsonObject.class);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String getLocalOutputPath() {
        return root.getAsJsonObject("local").get("outputPath").getAsString();
    }
    
    public List<Remote> getRemotes() {
        List<Remote> remotes = new ArrayList<>();
        
        for (JsonElement element : root.getAsJsonArray("remotes")) {
            remotes.add(new Remote(element.getAsJsonObject()));
        }
        return remotes;
    }
    
    public static class Remote {
        
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
}
