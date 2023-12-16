package me.TechsCode.GradeBasePlugin.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.gradle.api.Project;

import me.TechsCode.GradeBasePlugin.extensions.Downloader;
import me.TechsCode.GradeBasePlugin.extensions.MetaExtension;

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
        if(!meta.isApi){
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
        Files.copy(ResourceManager.class.getResourceAsStream("/gitignore.file"), 
                Paths.get(new File(project.getProjectDir().getAbsolutePath() + "/.gitignore").toURI()),
                StandardCopyOption.REPLACE_EXISTING);
    }
    
    public static void createWorkflow(Project project, boolean isApi) throws IOException {
        File destination = new File(project.getProjectDir().getAbsolutePath() + "/.github/workflows/build.yml");
        destination.mkdirs();

        String workflowFile;
        if(isApi){
            workflowFile = "/workflows/api.yml";
        }else{
            workflowFile = "/workflows/plugin.yml";
        }

        InputStream src = ResourceManager.class.getResourceAsStream(workflowFile);
        if(src == null) throw new IOException("Workflow file not found in resources");

        Files.copy(src, Paths.get(destination.toURI()), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void createGradleFiles(Project project) throws IOException {
        // build.gradle
        File buildGradleDestination = new File(project.getProjectDir().getAbsolutePath() + "/build.gradle");
        buildGradleDestination.mkdirs();

        InputStream buildGradleSrc = ResourceManager.class.getResourceAsStream("/gradle/build.gradle");
        if(buildGradleSrc == null) throw new IOException("build.gradle file not found in resources");

        Files.copy(buildGradleSrc, Paths.get(buildGradleDestination.toURI()), StandardCopyOption.REPLACE_EXISTING);
    }

}
