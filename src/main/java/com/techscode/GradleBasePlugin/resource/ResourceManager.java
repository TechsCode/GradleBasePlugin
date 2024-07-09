package com.techscode.GradleBasePlugin.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.techscode.GradleBasePlugin.Color;
import com.techscode.GradleBasePlugin.Logger;
import com.techscode.GradleBasePlugin.extensions.Downloader;
import com.techscode.GradleBasePlugin.extensions.MetaExtension;
import org.gradle.api.Project;

public class ResourceManager {

    public static ResourceResponse loadBasePlugin(Project project, MetaExtension meta, String username, String password, String version) {
        if (!meta.fetch) {
            return ResourceResponse.NOT_FETCH;
        }
        if (username == null) {
            return ResourceResponse.FAIL_USERNAME;
        }
        if (password == null) {
            return ResourceResponse.FAIL_PASSWORD;
        }

        File libraryFolder = new File(project.getProjectDir().getAbsolutePath() + "/libs");
        libraryFolder.mkdirs();

        File libraryFile = new File(libraryFolder.getAbsolutePath() + "/BasePlugin.jar");
        libraryFile.delete();

        String RETRIEVE_RELEASES;
        if(!meta.isAPI){
            RETRIEVE_RELEASES = "https://repo.techscode.com/repository/maven-private/me/TechsCode/BasePlugin/"+version+"/BasePlugin-"+version+"-all.jar?enable-custom-header=true";
        }else{
            RETRIEVE_RELEASES = "https://repo.techscode.com/repository/maven-private/me/TechsCode/BasePluginAPI/"+version+"/BasePluginAPI-"+version+"-all.jar?enable-custom-header=true";
        }

        try {
            Downloader downloader = new Downloader();
            downloader.authorize(username, password);
            downloader.download(new URL(RETRIEVE_RELEASES), libraryFile);
            return ResourceResponse.SUCCESS;
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResourceResponse.FAIL;
        }
    }
    
    public static void createGitIgnore(Project project) throws IOException {
        File gitIgnoreDestination = new File(project.getProjectDir().getPath() + "/.gitignore");
        gitIgnoreDestination.mkdirs();

        InputStream src = ResourceManager.class.getResourceAsStream("/gitignore.file");
        if(src == null) throw new IOException("Gitignore file not found in resources");

        Files.copy(src, Paths.get(gitIgnoreDestination.toURI()), StandardCopyOption.REPLACE_EXISTING);
        Logger.info(Color.GREEN + "Copied .gitignore file to " + gitIgnoreDestination.getPath());
    }
    
    public static void createWorkflow(Project project, boolean isApi) throws IOException {
        File destination = new File(project.getProjectDir().getPath() + "/.github/workflows/build.yml");
        destination.mkdirs();

        String workflowFile;
        if(isApi){
            workflowFile = "/workflows/api.yml";
            Logger.info(Color.BLUE + "Using API workflow file");
        }else{
            workflowFile = "/workflows/plugin.yml";
            Logger.info(Color.BLUE + "Using Plugin workflow file");
        }

        InputStream src = ResourceManager.class.getResourceAsStream(workflowFile);
        if(src == null) throw new IOException("Workflow file not found in resources");

        Files.copy(src, Paths.get(destination.toURI()), StandardCopyOption.REPLACE_EXISTING);
        Logger.info(Color.GREEN + "Copied workflow file to " + destination.getPath());
    }

}
