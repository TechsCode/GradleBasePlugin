package me.TechsCode;

import groovy.lang.Closure;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.internal.impldep.org.eclipse.jgit.api.Git;
import org.gradle.internal.impldep.org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.internal.impldep.org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;

public class GradleBasePlugin implements Plugin<Project> {

    public static void log(String message){
        System.out.println(Color.BLACK_BOLD+message);
    }

    public static void log(){
        System.out.println();
    }

    @Override
    public void apply(Project project) {
        project.getExtensions().create("meta", MetaExtension.class);
        project.getExtensions().create("upload", UploadExtension.class);

        MetaExtension meta = project.getExtensions().getByType(MetaExtension.class);
        UploadExtension uploadExtension = project.getExtensions().getByType(UploadExtension.class);

        System.out.println(meta.version);
        System.out.println(uploadExtension.host);

        if(meta.validate()){
            log();
            log(Color.RED+"Please check the GitHub page of GradleBasePlugin for more information");
            return;
        }

        if(uploadExtension.validate()){
            log();
            log(Color.RED+"Please check your SFTP Upload Settings and retry.");
            return;
        }

        GitInteractor.initialize(project.getProjectDir());

        log(Color.GREEN_BOLD_BRIGHT+"Configuring Gradle Project - Build Settings...");
        log();
        log("Project Info:");
        log("Plugin: "+project.getName()+" on Version: "+meta.version);

        project.setProperty("version", meta.version);
        project.setProperty("sourceCompatibility", "1.8");
        project.setProperty("targetCompatibility", "1.8");

        project.getTasks().create("upload", UploadTask.class);
        project.getTasks().create("dev", DevTask.class);

        project.getPlugins().apply("com.github.johnrengelman.shadow");
        project.getTasksByName("build", false).stream().findFirst().get().dependsOn("shadowJar");


        /*

        String[] repositories = new String[]{
                "https://hub.spigotmc.org/nexus/content/repositories/snapshots/",
                "https://oss.sonatype.org/content/repositories/snapshots",
                "https://jitpack.io"
        };

        project.getRepositories().maven((maven) -> maven.setArtifactUrls(new HashSet<String>(Arrays.asList(repositories))));
        project.getRepositories().jcenter();
        project.getRepositories().mavenLocal();
        project.getRepositories().mavenCentral();

        String[] compileOnlyDependencies = new String[]{
                "org.spigotmc:spigot:1.12.2-R0.1-SNAPSHOT",
                "org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT",
                "net.md-5:bungeecord-api:1.12-SNAPSHOT"
        };

        project.getDependencies().add("compileOnly", compileOnlyDependencies);*/
    }


}
