package me.TechsCode.GradeBasePlugin.tasks;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import me.TechsCode.GradeBasePlugin.Logger;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import me.TechsCode.GradeBasePlugin.Color;
import me.TechsCode.GradeBasePlugin.GradleBasePlugin;
import me.TechsCode.GradeBasePlugin.extensions.MetaExtension;

public class GenerateMetaFilesTask extends DefaultTask {

    @TaskAction
    public void generateMetaFiles() {
        Logger.info(Color.BLUE_BRIGHT + "Generating Plugin.yml & Bungee.yml......");

        File resourcesFolder = new File(getProject().getBuildDir().getAbsolutePath() + "/resources/main");
        resourcesFolder.mkdirs();

        try {
            MetaExtension meta = getProject().getExtensions().getByType(MetaExtension.class);
            int buildNumber = getBuildNumber();

            createPluginYml(resourcesFolder, getProject().getName(), meta.pluginVersion, buildNumber, meta.loadAfter, meta.loadBefore, meta.load, meta.libraries);
            createBungeeYml(resourcesFolder, getProject().getName(), meta.pluginVersion, buildNumber, meta.libraries);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createPluginYml(File resourcesFolder, String projectName, String projectVersion, int buildNumber, String loadAfter, String loadBefore, String load, ArrayList<String> libraries) throws IOException {
        File file = new File(resourcesFolder, "plugin.yml");
        file.createNewFile();

        PrintWriter writer = new PrintWriter(file, "UTF-8");

        writer.println("name: " + projectName);
        writer.println("version: " + projectVersion);
        writer.println("author: Tech");
        writer.println("website: " + projectName + ".com");
        writer.println("build: " + buildNumber);
        writer.println("main: me.TechsCode." + getProject().getName() + ".base.loader.SpigotLoader");
        writer.println("api-version: 1.13");

        if (loadAfter != null) {
            writer.println("softdepend: " + loadAfter);
        }
        if (loadBefore != null) {
            writer.println("loadbefore: " + loadBefore);
        }
        if (load != null) {
            writer.println("load: " + load);
        }
        if (libraries != null && !libraries.isEmpty()){
            writer.println("libraries:");
            libraries.stream().map(library -> "- " + library).forEach(writer::println);
        } else {
            writer.println("libraries: []");
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
        writer.println("author: Tech");

        if (libraries != null && !libraries.isEmpty()) {
            writer.println("libraries:");
            libraries.stream().map(library -> "- " + library).forEach(writer::println);
        } else {
            writer.println("libraries: []");
        }
        writer.close();
    }

    private int getBuildNumber() {
        String buildNumber = System.getenv("BUILD_NUMBER");

        return buildNumber != null ? Integer.parseInt(buildNumber) : 0;
    }
}