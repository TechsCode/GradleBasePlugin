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
        Files.copy(ResourceManager.class.getResourceAsStream("/gitignore.file"), 
                Paths.get(new File(project.getProjectDir().getAbsolutePath() + "/.gitignore").toURI()),
                StandardCopyOption.REPLACE_EXISTING);
    }
    
    public static void createWorkflow(Project project, boolean isApi) throws IOException {
        File destination;

        if(isApi){
            destination = new File(project.getProjectDir().getAbsolutePath() + "/.github/workflows/build/api.yml");
        }else{
            destination = new File(project.getProjectDir().getAbsolutePath() + "/.github/workflows/build/plugin.yml");
        }
        destination.mkdirs();
        
        InputStream src = ResourceManager.class.getResourceAsStream("/workflows/build.yml");
        Files.copy(src, Paths.get(destination.toURI()), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void createGradleFiles(Project project) throws IOException {
        File buildGradleFile = new File(project.getProjectDir().getAbsolutePath() + "/gradle/build.gradle");
        InputStream buildGradleSrc = ResourceManager.class.getResourceAsStream("/build.gradle");
        Files.copy(buildGradleSrc, Paths.get(buildGradleFile.toURI()), StandardCopyOption.REPLACE_EXISTING);

        File settingsGradleFile = new File(project.getProjectDir().getAbsolutePath() + "/gradle/plugin.properties");
        InputStream settingsGradleSrc = ResourceManager.class.getResourceAsStream("/plugin-test.properties");

        // Don't copy if src file exists
        if(settingsGradleSrc == null){
            Files.copy(settingsGradleSrc, Paths.get(settingsGradleFile.toURI()), StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
