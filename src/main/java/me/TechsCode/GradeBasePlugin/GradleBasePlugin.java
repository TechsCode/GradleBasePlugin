package me.TechsCode.GradeBasePlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import me.TechsCode.GradeBasePlugin.resource.ResourceResponse;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;

import me.TechsCode.GradeBasePlugin.extensions.MetaExtension;
import me.TechsCode.GradeBasePlugin.resource.ResourceManager;
import me.TechsCode.GradeBasePlugin.tasks.GenerateMetaFilesTask;

public class GradleBasePlugin implements Plugin<Project> {

    private static final String[] repositories = new String[] {
            "https://hub.spigotmc.org/nexus/content/repositories/snapshots/",
            "https://oss.sonatype.org/content/repositories/snapshots",
            "https://jitpack.io",
            "https://repo.codemc.io/repository/maven-public/",
            "https://repo.techscode.com/repository/maven-releases/"
    };
    
    private static final String[] dependencies = new String[] {
            "compileOnly#org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT",
            "compileOnly#net.md-5:bungeecord-api:1.20-R0.1-SNAPSHOT"
    };
    
    private static final String[] relocations = new String[] {
            "me.TechsCode.base#me.TechsCode.PROJECT_NAME.base",
            "me.TechsCode.tpl#me.TechsCode.PROJECT_NAME.tpl",
            "me.TechsCode.dependencies#me.TechsCode.PROJECT_NAME.dependencies"
    };

    private MetaExtension meta;
    private String username;
    private String password;
    
    @Override
    public void apply(Project project) {
        log(Color.BLUE_BOLD_BRIGHT + "Applying GradleBasePlugin to " + project.getName() + "...");
        log();
        log("Project Info");
        this.meta = project.getExtensions().create("meta", MetaExtension.class);

        this.username = System.getenv("TECHSCODE_USERNAME");
        this.password = System.getenv("TECHSCODE_PASSWORD");

        try {
            ResourceManager.createGitIgnore(project);
            ResourceManager.createWorkflow(project, meta.isAPI);
        }
        catch (IOException ignored) {}

        // Registering GradleBasePlugin tasks
        project.getTasks().create("generateMetaFiles", GenerateMetaFilesTask.class);

        File destinationDir = project.getBuildDir().toPath().resolve("libs").toFile();
        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }

        // Setting up Shadow Plugin
        project.getPlugins().apply("com.github.johnrengelman.shadow");
        getShadowJar(project).getArchiveFileName().set(project.getName() + ".jar");
        getShadowJar(project).getDestinationDirectory().set(destinationDir);
        getShadowJar(project).dependsOn("generateMetaFiles");
        getShadowJar(project).setProperty("archiveClassifier", "");

        project.getTasks().getByName("build").dependsOn("shadowJar");

        // Add projectEvaluation hooks
        project.afterEvaluate(this::afterProjectEvaluation);
    }

    private void afterProjectEvaluation(Project project) {
        // Setting properties
        project.setProperty("version", meta.pluginVersion);
        project.setProperty("sourceCompatibility", "1.8");
        project.setProperty("targetCompatibility", "1.8");

        if (!meta.configValid()) {
            return;
        }
        log(Color.BLUE_BOLD_BRIGHT + "Configuring Gradle Project - Build Settings...");
        log();
        log("Project Info");
        log("Plugin: " + project.getName() + " on Version: " + meta.pluginVersion);
        log();

        if (this.username == null || this.password == null) {
            log(Color.RED + "Missing TECHSCODE_USERNAME and/or TECHSCODE_PASSWORD environment variables!");
            log(Color.RED_BRIGHT + "Make sure that you have set the TECHSCODE_USERNAME and TECHSCODE_PASSWORD environment variables that has access to the maven-private repository!");
            return;
        }
        if(meta.baseVersion.isEmpty() && !meta.fetch){
            log(Color.RED + "Missing baseVersion in the gradle.properties file!");
            log(Color.RED_BRIGHT + "Make sure that you have set the baseVersion in the gradle.properties file!");
            return;
        }

        ResourceResponse response = ResourceManager.loadBasePlugin(project, meta, username, password, meta.baseVersion);
        if (response == ResourceResponse.SUCCESS) {
            log("Successfully retrieved BasePlugin.jar from the techscode repo...");
            project.getDependencies().add("implementation", project.files("libs/BasePlugin.jar"));
        } else if (response == ResourceResponse.FAIL_USERNAME) {
            log(Color.RED + "Could not retrieve BasePlugin.jar from the techscode repo...");
            log(Color.RED_BRIGHT + "Make sure that you have set the TECHSCODE_USERNAME environment variable that has access to the maven-private repository!");
        } else if (response == ResourceResponse.FAIL_PASSWORD) {
            log(Color.RED + "Could not retrieve BasePlugin.jar from the techscode repo...");
            log(Color.RED_BRIGHT + "Make sure that you have set the TECHSCODE_PASSWORD environment variable that has access to the maven-private repository!");
        } else if (response == ResourceResponse.FAIL) {
            log(Color.RED + "Could not retrieve BasePlugin.jar from the techscode repo...");
            log(Color.RED_BRIGHT + "There was an error downloading the BasePlugin.jar from the techscode repo...");
        } else if (response == ResourceResponse.NOT_FETCH) {
            log(Color.YELLOW + "Not fetching the build, if this is a mistake, please set fetch to true!");
            project.getDependencies().add("implementation", project.files("libs/BasePlugin.jar"));
        } else {
            log(Color.RED + "Could not retrieve BasePlugin.jar from the techscode repo...");
            log(Color.RED_BRIGHT + "There was an error downloading the BasePlugin.jar from the techscode repo...");
            log(Color.RED_BRIGHT + "Error: " + response.name());
        }

        // Setting up repositories
        project.getRepositories().mavenLocal();
        project.getRepositories().mavenCentral();
        Arrays.stream(repositories)
                .forEach(url -> project.getRepositories().maven((maven) -> maven.setUrl(url)));

        // Setting up dependencies
        Arrays.stream(dependencies).map(entry -> entry.split("#"))
                .forEach(confAndUrl -> project.getDependencies().add(confAndUrl[0], confAndUrl[1]));

        // Setting up relocations
        Arrays.stream(relocations).map(entry -> entry.split("#"))
                .forEach(fromTo -> getShadowJar(project).relocate(fromTo[0],
                        fromTo[1].replace("PROJECT_NAME", project.getName())));
    }

    private ShadowJar getShadowJar(Project project) {
        return (ShadowJar) project.getTasks().getByName("shadowJar");
    }

    public static void log(String message) {
        System.out.println(Color.RESET + message + Color.RESET);
    }

    public static void log() {
        System.out.println();
    }
}