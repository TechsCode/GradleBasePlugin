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
            "compileOnly#org.spigotmc:spigot-api:1.21.5-R0.1-SNAPSHOT",
            "compileOnly#net.md-5:bungeecord-api:1.21-R0.2-SNAPSHOT"
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
        Logger.info(
                Color.BLUE_BOLD_BRIGHT + "Applying GradleBasePlugin to " + project.getName() + "...",
                "",
                "Project Information:"
        );
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

        File destinationDir = project.getBuildDir().toPath().toFile();
        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }

        // Setting up Shadow Plugin
        project.getPlugins().apply("com.gradleup.shadow");
        getShadowJar(project).getArchiveFileName().set(project.getName() + "-" + meta.pluginVersion + ".jar");
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
        project.setProperty("sourceCompatibility", "17");
        project.setProperty("targetCompatibility", "17");

        if (!meta.configValid()) {
            Logger.error(
                    " _____",
                    "| ____|_ __ _ __ ___  _ __",
                    "|  _| | '__| '__/ _ \\| '__|",
                    "| |___| |  | | | (_) | |",
                    "|_____|_|  |_|  \\___/|_|",
                    "--------------------------------------------------------------------------------",
                    "Failed to configure Gradle Project - Build Settings",
                    "",
                    "Please check the build.gradle of your project and make sure you have all the required fields in your 'meta' extension",
                    "--------------------------------------------------------------------------------"
            );
            return;
        }

        Logger.info(
                "Plugin: " + project.getName() + " on Version: " + meta.pluginVersion,
                "",
                "Configuring Gradle Project - Build Settings:"
        );

        ResourceResponse response = ResourceManager.loadBasePlugin(project, meta, username, password, meta.baseVersion);
        if (response == ResourceResponse.SUCCESS) {
            Logger.info(
                    "Successfully retrieved BasePlugin.jar from the techscode repo...",
                    "Adding BasePlugin.jar to the dependencies..."
            );
            project.getDependencies().add("implementation", project.files("libs/BasePlugin.jar"));
        } else if (response == ResourceResponse.FAIL_USERNAME) {
            Logger.error(
                    "Could not retrieve BasePlugin.jar from the techscode repo...",
                    "Make sure that you have set the TECHSCODE_USERNAME environment variable that has access to the maven-private repository!"
            );
            return;
        } else if (response == ResourceResponse.FAIL_PASSWORD) {
            Logger.error(
                    "Could not retrieve BasePlugin.jar from the techscode repo...",
                    "Make sure that you have set the TECHSCODE_PASSWORD environment variable that has access to the maven-private repository!"
            );
            return;
        } else if (response == ResourceResponse.FAIL) {
            Logger.error(
                    "Could not retrieve BasePlugin.jar from the techscode repo...",
                    "There was an error downloading the BasePlugin.jar from the techscode repo..."
            );
            return;
        } else if (response == ResourceResponse.NOT_FETCH) {
            Logger.warning(
                    "Fetching is disabled in your build.gradle...",
                    "Not fetching the build, if this is a mistake, please set fetch to true!"
            );
            project.getDependencies().add("implementation", project.files("libs/BasePlugin.jar"));
        } else {
            Logger.error(
                    " _____",
                    "| ____|_ __ _ __ ___  _ __",
                    "|  _| | '__| '__/ _ \\| '__|",
                    "| |___| |  | | | (_) | |",
                    "|_____|_|  |_|  \\___/|_|",
                    "--------------------------------------------------------------------------------",
                    "Failed to configure Gradle Project - Build Settings",
                    "",
                    "Please check the build.gradle of your project and make sure you have all the required fields in your 'meta' extension",
                    "--------------------------------------------------------------------------------"
            );
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
}