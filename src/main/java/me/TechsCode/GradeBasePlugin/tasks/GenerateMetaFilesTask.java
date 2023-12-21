package me.TechsCode.GradeBasePlugin.tasks;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.tasks.TaskAction;

import me.TechsCode.GradeBasePlugin.Color;
import me.TechsCode.GradeBasePlugin.GradleBasePlugin;
import me.TechsCode.GradeBasePlugin.extensions.MetaExtension;

public class GenerateMetaFilesTask extends DefaultTask {

    private Project project;

    public void setProject(Project project) {
        this.project = project;
    }

    @TaskAction
    public void generateMetaFiles() {
        GradleBasePlugin.log(Color.BLUE_BRIGHT + "Generating Plugin.yml & Bungee.yml......");

        File resourcesFolder = new File(getProject().getBuildDir().getAbsolutePath() + "/resources/main");
        resourcesFolder.mkdirs();

        try {
            File propertiesFile = new File(project.getProjectDir(), GradleBasePlugin.propertiesFileName);
            if(!propertiesFile.exists()){
                GradleBasePlugin.log();
                GradleBasePlugin.log(Color.RED + "Could not find properties.json file!");
                GradleBasePlugin.log(Color.RED_BRIGHT + "Make sure that you have a " + Color.BLUE + "properties.json" + Color.RED_BRIGHT + " file in the root directory of your project!");
                return;
            }

            MetaExtension meta = MetaExtension.fromFile(propertiesFile);
            int buildNumber = getBuildNumber();

            createPluginYml(resourcesFolder, getProject().getName(), meta.pluginVersion, buildNumber, meta.loadAfter, meta.loadBefore, meta.load, meta.libraries);
            createBungeeYml(resourcesFolder, getProject().getName(), meta.pluginVersion, buildNumber, meta.libraries);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createPluginYml(File resourcesFolder, String projectName, String projectVersion, int buildNumber, ArrayList<String> loadAfter, ArrayList<String> loadBefore, String load, ArrayList<String> libraries) throws IOException {
        File file = new File(resourcesFolder, "plugin.yml");
        file.createNewFile();

        PrintWriter writer = new PrintWriter(file, "UTF-8");

        writer.println("name: " + projectName);
        writer.println("version: " + projectVersion);
        writer.println("author: TechsCode");
        writer.println("website: " + projectName + ".com");
        writer.println("build: " + buildNumber);
        writer.println("main: me.TechsCode." + getProject().getName() + ".base.loader.SpigotLoader");
        writer.println("api-version: 1.13");

        if (!loadAfter.isEmpty()) {
            writer.println("softdepend: " + "[" + loadAfter.stream().map(Object::toString).reduce((a, b) -> a + ", " + b).get() + "]");
        }
        if (!loadBefore.isEmpty()) {
            writer.println("loadbefore: " + "[" + loadBefore.stream().map(Object::toString).reduce((a, b) -> a + ", " + b).get() + "]");
        }
        if (load != null) {
            writer.println("load: " + load);
        }
        if (!libraries.isEmpty()) {
            writer.println("libraries:");
            libraries.stream().map(library -> "- " + library).forEach(writer::println);
        }
        writer.close();
    }

    private void createBungeeYml(File resourcesFolder, String projectName, String projectVersion, int buildNumber, ArrayList<String> libraries) throws IOException {
        File file = new File(resourcesFolder, "bungee.yml");
        file.createNewFile();
        
        PrintWriter writer = new PrintWriter(file, "UTF-8");

        writer.println("name: " + projectName);
        writer.println("version: " + projectVersion);
        writer.println("build: " + buildNumber);
        writer.println("main: me.TechsCode." + getProject().getName() + ".base.loader.BungeeLoader");
        writer.println("author: TechsCode");
        
        if (!libraries.isEmpty()) {
            writer.println("libraries:");
            libraries.stream().map(library -> "- " + library).forEach(writer::println);
        }
        writer.close();
    }

    private int getBuildNumber() {
        String buildNumber = System.getenv("BUILD_NUMBER");
        
        return buildNumber != null ? Integer.parseInt(buildNumber) : 0;
    }
}
