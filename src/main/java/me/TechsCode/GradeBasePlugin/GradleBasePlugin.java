package me.TechsCode.GradeBasePlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

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

        // Setting up Shadow Plugin
        project.getPlugins().apply("com.github.johnrengelman.shadow");
        getShadowJar(project).getArchiveFileName().set(project.getName() + ".jar");
        getShadowJar(project).setProperty("destinationDir", project.file("build"));
        getShadowJar(project).dependsOn("generateMetaFiles");
        getShadowJar(project).setProperty("archiveClassifier", "");

        // project.getTasks().getByName("build").dependsOn("shadowJar");
        project.getTasks().getByName("build").doLast(this::uploadToRemotes);

        // Add onProjectEvaluation hook
        project.afterEvaluate(this::onProjectEvaluation);
    }

    private void onProjectEvaluation(Project project) {
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

        // Setting properties
        project.setProperty("version", meta.pluginVersion);
        project.setProperty("sourceCompatibility", "1.8");
        project.setProperty("targetCompatibility", "1.8");

        // Setting up repositories
        project.getRepositories().mavenLocal();
        project.getRepositories().mavenCentral();
        Arrays.stream(repositories)
                .forEach(url -> project.getRepositories().maven((maven) -> maven.setUrl(url)));
        if(meta.fetch){
            project.getRepositories().maven((maven) ->
                    maven.setUrl("https://repo.techscode.com/repository/maven-private/")
            );
        }

        // Setting up dependencies
        Arrays.stream(dependencies).map(entry -> entry.split("#"))
                .forEach(confAndUrl -> project.getDependencies().add(confAndUrl[0], confAndUrl[1]));
        if(meta.fetch){
            project.getDependencies().add("implementation", "me.TechsCode:BasePlugin:" + meta.baseVersion);
        }else{
            project.getDependencies().add("implementation", project.files("libs/BasePlugin.jar"));
        }

        // Setting up relocations
        Arrays.stream(relocations).map(entry -> entry.split("#"))
                .forEach(fromTo -> getShadowJar(project).relocate(fromTo[0],
                        fromTo[1].replace("PROJECT_NAME", project.getName())));
    }

    /* After the build prcoess is completed, the file will be uploaded to all remotes */
    private void uploadToRemotes(Task buildTask) {
        File file = new File(meta.localDeploymentPath + '/'
                + buildTask.getProject().getName() + "-" + meta.pluginVersion + ".jar");

//        meta.remotes.stream().filter(Remote::isEnabled)
//                .forEach(all -> all.uploadFile(file));
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