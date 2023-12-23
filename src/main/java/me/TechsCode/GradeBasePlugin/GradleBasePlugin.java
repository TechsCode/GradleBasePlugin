package me.TechsCode.GradeBasePlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import me.TechsCode.GradeBasePlugin.deploy.Remote;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;

import me.TechsCode.GradeBasePlugin.extensions.MetaExtension;
import me.TechsCode.GradeBasePlugin.resource.ResourceManager;
import me.TechsCode.GradeBasePlugin.resource.ResourceResponse;
import me.TechsCode.GradeBasePlugin.tasks.GenerateMetaFilesTask;

public class GradleBasePlugin implements Plugin<Project> {


    private static final String[] repositories = new String[] {
            "https://hub.spigotmc.org/nexus/content/repositories/snapshots/",
            "https://oss.sonatype.org/content/repositories/snapshots", "https://jitpack.io",
            "https://repo.codemc.io/repository/maven-public/" };
    
    private static final String[] dependencies = new String[] {
            "compileOnly#org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT",
            "compileOnly#net.md-5:bungeecord-api:1.20-R0.1-SNAPSHOT" };
    
    private static final String[] relocations = new String[] {
            "me.TechsCode.base#me.TechsCode.PROJECT_NAME.base",
            "me.TechsCode.tpl#me.TechsCode.PROJECT_NAME.tpl",
            "me.TechsCode.dependencies#me.TechsCode.PROJECT_NAME.dependencies" };

    public static String propertiesFileName = "properties.json";
    private MetaExtension meta;
    private String username;
    private String password;
    
    @Override
    public void apply(Project project) {
        File propertiesFile = new File(project.getProjectDir(), propertiesFileName);
        if(!propertiesFile.exists()){
            log();
            log(Color.RED + "Could not find properties.json file!");
            log(Color.RED_BRIGHT + "Make sure that you have a " + Color.BLUE + "properties.json" + Color.RED_BRIGHT + " file in the root directory of your project!");
            return;
        }

        this.meta = MetaExtension.fromFile(propertiesFile);

        this.username = System.getenv("TECHSCODE_USERNAME");
        this.password = System.getenv("TECHSCODE_PASSWORD");
        
        // Registering GradleBasePlugin tasks
        // run GenerateMetaFilesTask and pass in the project
        project.getTasks().register("generateMetaFiles", GenerateMetaFilesTask.class, (task) -> {
            task.setProject(project);
        });
        
        // Setting up Shadow Plugin
        project.getPlugins().apply("com.github.johnrengelman.shadow");
        getShadowJar(project).getArchiveFileName().set(project.getName() + "-" + project.getVersion() + ".jar");
        getShadowJar(project).setProperty("destinationDir", project.file(meta.localDeploymentPath));
        getShadowJar(project).dependsOn("generateMetaFiles");
        
        // project.getTasks().getByName("build").dependsOn("shadowJar");
        project.getTasks().getByName("build").doLast(this::uploadToRemotes);
        
        // Add onProjectEvaluation hook
        project.afterEvaluate(this::onProjectEvaluation);
    }
    
    private void onProjectEvaluation(Project project) {
        if (meta.validate()) {
            return;
        }
        log(Color.BLUE_BOLD_BRIGHT + "Configuring Gradle Project - Build Settings...");
        log();
        log("Project Info");
        log("Plugin: " + project.getName() + " on Version: " + meta.pluginVersion);
        log();

        log("Generating and copying files...");
        try {
            ResourceManager.createGitIgnore(project);
            ResourceManager.createWorkflow(project, meta.isApi);
            ResourceManager.createGradleFiles(project);
        }
        catch (IOException e) {
            log(Color.RED + "Could not generate and copy files...");
            log(Color.RED_BRIGHT + "There was an error generating and copying files...");
            e.printStackTrace();
            return;
        }
        log();

        project.afterEvaluate((p) -> {
            log("Setting up repositories...");
            project.getRepositories().mavenLocal();

            meta.repositories.put("TechsCode", "https://repo.techscode.com/repository/maven-releases/");
            meta.repositories.forEach((name, url) -> project.getRepositories().maven((maven) -> {
                log(Color.BLUE + "Adding repository: " + name + " with url: " + url + "...");
                maven.setName(name);
                maven.setUrl(url);
                maven.setAllowInsecureProtocol(url.startsWith("http://"));
            }));

            log();
            log("Setting up dependencies...");
            meta.dependencies.forEach((name, confAndUrl) -> {
                String scope = confAndUrl[0];
                String url = confAndUrl[1];

                log(Color.BLUE + "Adding dependency: " + name + " with configuration: " + scope + " and url: " + url + "...");
                if(!url.contains(":")){
                    project.getDependencies().add(scope, project.files(url));
                }else{
                    project.getDependencies().add(scope, url);
                }
            });
        });
        log();

        if (this.username == null || this.password == null) {
            log(Color.RED + "Missing TECHSCODE_USERNAME and/or TECHSCODE_PASSWORD environment variables!");
            log(Color.RED_BRIGHT + "Make sure that you have set the TECHSCODE_USERNAME and TECHSCODE_PASSWORD environment variables that has access to the maven-private repository!");
            return;
        }
        if (!meta.baseVersion.equalsIgnoreCase("none")) {
            ResourceResponse response = ResourceManager.loadBasePlugin(project, meta, username, password, meta.baseVersion);
            
            if (response == ResourceResponse.SUCCESS) {
                log("Successfully retrieved BasePlugin.jar from the techscode repo...");
                project.getDependencies().add("implementation", project.files("libs/BasePlugin.jar"));
            }
            else if (response == ResourceResponse.FAIL_USERNAME) {
                log(Color.RED + "Could not retrieve BasePlugin.jar from the techscode repo...");
                log(Color.RED_BRIGHT + "Make sure that you have set the TECHSCODE_USERNAME environment variable that has access to the maven-private repository!");
            }
            else if (response == ResourceResponse.FAIL_PASSWORD) {
                log(Color.RED + "Could not retrieve BasePlugin.jar from the techscode repo...");
                log(Color.RED_BRIGHT + "Make sure that you have set the TECHSCODE_PASSWORD environment variable that has access to the maven-private repository!");
            }
            else if (response == ResourceResponse.FAIL) {
                log(Color.RED + "Could not retrieve BasePlugin.jar from the techscode repo...");
                log(Color.RED_BRIGHT + "There was an error downloading the BasePlugin.jar from the techscode repo...");
            }
            else if (response == ResourceResponse.NOT_FETCH) {
                log(Color.YELLOW + "Not fetching the build, if this is a mistake, please set fetch to true!");
                project.getDependencies().add("implementation", project.files("libs/BasePlugin.jar"));
            }
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
        
        // Setting up dependencies
        Arrays.stream(dependencies).map(entry -> entry.split("#"))
                .forEach(confAndUrl -> project.getDependencies().add(confAndUrl[0], confAndUrl[1]));
        
        // Setting up relocations
        Arrays.stream(relocations).map(entry -> entry.split("#"))
                .forEach(fromTo -> getShadowJar(project).relocate(fromTo[0],
                        fromTo[1].replace("PROJECT_NAME", project.getName())));
    }
    
    /* After the build prcoess is completed, the file will be uploaded to all remotes */
    private void uploadToRemotes(Task buildTask) {
        File file = new File(meta.localDeploymentPath + '/'
                + buildTask.getProject().getName() + "-" + buildTask.getProject().getVersion() + ".jar");

        meta.remotes.stream().filter(Remote::isEnabled)
                .forEach(all -> all.uploadFile(file));
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
