package me.TechsCode.GradeBasePlugin.resource;

import me.TechsCode.GradeBasePlugin.GradleBasePlugin;
import me.TechsCode.GradeBasePlugin.extensions.Downloader;
import me.TechsCode.GradeBasePlugin.extensions.MetaExtension;
import org.gradle.api.Project;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

public class ResourceManager {

    public static ResourceResponse loadBasePlugin(Project project, MetaExtension meta, String username, String password, String version) {
        if (!meta.fetch)
            return ResourceResponse.NOT_FETCH;
        if (username == null)
            return ResourceResponse.FAIL_USERNAME;
        if (password == null)
            return ResourceResponse.FAIL_PASSWORD;

        File libraryFolder = new File(project.getProjectDir().getAbsolutePath() + "/libs");
        libraryFolder.mkdirs();

        File libraryFile = new File(libraryFolder.getAbsolutePath() + "/BasePlugin.jar");
        libraryFile.delete();

        String RETRIEVE_RELEASES = "https://repo.techscode.com/repository/maven-private/me/TechsCode/BasePlugin/"+version+"/BasePlugin-"+version+"-all.jar?enable-custom-header=true";

        try{
            Downloader downloader = new Downloader();
            downloader.authorize(username, password);
            File jarFile = downloader.download(new URL(RETRIEVE_RELEASES), libraryFile);
            return ResourceResponse.SUCCESS;
        }catch (Exception e){
            e.printStackTrace();
            return ResourceResponse.FAIL;
        }
    }

    public static void createGitIgnore(Project project) throws IOException {
        InputStream src = ResourceManager.class.getResourceAsStream("/gitignore.file");
        Files.copy(src, Paths.get(new File(project.getProjectDir().getAbsolutePath() + "/.gitignore").toURI()), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void createWorkflow(Project project) throws IOException {
        File destination = new File(project.getProjectDir().getAbsolutePath() + "/.github/workflows/build.yml");
        destination.mkdirs();

        InputStream src = ResourceManager.class.getResourceAsStream("/workflows/build.yml");
        Files.copy(src, Paths.get(destination.toURI()), StandardCopyOption.REPLACE_EXISTING);
    }
}
