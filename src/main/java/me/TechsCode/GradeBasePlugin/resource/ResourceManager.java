package me.TechsCode.GradeBasePlugin.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import me.TechsCode.GradeBasePlugin.Color;
import me.TechsCode.GradeBasePlugin.GradleBasePlugin;
import net.rubygrapefruit.platform.terminal.TerminalOutput;
import org.gradle.api.Project;

import me.TechsCode.GradeBasePlugin.extensions.Downloader;
import me.TechsCode.GradeBasePlugin.extensions.MetaExtension;

public class ResourceManager {
    
    public static void createGitIgnore(Project project) throws IOException {
        File gitIgnoreDestination = new File(project.getProjectDir().getAbsolutePath() + "/.gitignore");
        gitIgnoreDestination.mkdirs();

        InputStream src = ResourceManager.class.getResourceAsStream("/gitignore.file");
        if(src == null) throw new IOException("Gitignore file not found in resources");

        Files.copy(src, Paths.get(gitIgnoreDestination.toURI()), StandardCopyOption.REPLACE_EXISTING);
        GradleBasePlugin.log(Color.GREEN + "Copied .gitignore file to " + gitIgnoreDestination.getAbsolutePath());
    }
    
    public static void createWorkflow(Project project, boolean isApi) throws IOException {
        File destination = new File(project.getProjectDir().getAbsolutePath() + "/.github/workflows/build.yml");
        destination.mkdirs();

        String workflowFile;
        if(isApi){
            workflowFile = "/workflows/api.yml";
            GradleBasePlugin.log(Color.BLUE + "Using API workflow file");
        }else{
            workflowFile = "/workflows/plugin.yml";
            GradleBasePlugin.log(Color.BLUE + "Using Plugin workflow file");
        }

        InputStream src = ResourceManager.class.getResourceAsStream(workflowFile);
        if(src == null) throw new IOException("Workflow file not found in resources");

        Files.copy(src, Paths.get(destination.toURI()), StandardCopyOption.REPLACE_EXISTING);
        GradleBasePlugin.log(Color.GREEN + "Copied workflow file to " + destination.getAbsolutePath());
    }

}
