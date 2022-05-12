package me.TechsCode.GradeBasePlugin.resource;

import me.TechsCode.GradeBasePlugin.GradleBasePlugin;
import me.TechsCode.GradeBasePlugin.extensions.MetaExtension;
import org.gradle.api.Project;

import javax.net.ssl.HttpsURLConnection;
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
        String userCredentials = username + ":" + password;
        String basicAuth = "Basic " + Base64.getEncoder().encodeToString(userCredentials.getBytes());

        try {
            URL url = new URL(RETRIEVE_RELEASES);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection(Proxy.NO_PROXY);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("x-authorization", basicAuth);
            connection.setRequestProperty("Accept", "application/java-archive");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36");

            ReadableByteChannel uChannel = Channels.newChannel(connection.getInputStream());
            FileOutputStream foStream = new FileOutputStream(libraryFile.getAbsolutePath());
            FileChannel fChannel = foStream.getChannel();
            fChannel.transferFrom(uChannel, 0, Long.MAX_VALUE);
            uChannel.close();
            foStream.close();
            fChannel.close();
        } catch (IOException e) {
            e.printStackTrace();

            return ResourceResponse.FAIL;
        }

        return ResourceResponse.SUCCESS;
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
