package me.TechsCode.GradeBasePlugin.extensions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.TechsCode.GradeBasePlugin.Color;
import me.TechsCode.GradeBasePlugin.GradleBasePlugin;
import me.TechsCode.GradeBasePlugin.deploy.Remote;
import org.gradle.internal.impldep.com.google.api.client.json.Json;

public class MetaExtension {

    public String gradleBasePluginVersion;
    public String pluginVersion;
    public String baseVersion;
    public String load;
    public ArrayList<String> loadBefore, loadAfter;
    public boolean fetch;
    public boolean isApi = false;

    public ArrayList<String> libraries;
    public HashMap<String, String> repositories;
    public HashMap<String, String[]> dependencies;

    public String localDeploymentPath;
    public List<Remote> remotes;

    public static MetaExtension fromFile(File file) {
        try{
            Path path = Paths.get(file.getAbsolutePath());
            byte[] fileBytes = Files.readAllBytes(path);
            String fileContent = new String(fileBytes);

            JsonObject json = new Gson().fromJson(fileContent, JsonObject.class);
            MetaExtension meta = new MetaExtension();
            meta.gradleBasePluginVersion = json.get("gradleBasePluginVersion").getAsString();
            meta.pluginVersion = json.get("pluginVersion").getAsString();
            meta.baseVersion = json.get("baseVersion").getAsString();
            meta.load = json.get("load").getAsString();

            meta.loadBefore = new ArrayList<>();
            if(json.has("loadBefore")){
                JsonArray loadBefore = json.get("loadBefore").getAsJsonArray();
                for (JsonElement loadBeforeElement : loadBefore) {
                    meta.loadBefore.add(loadBeforeElement.getAsString());
                }
            }

            meta.loadAfter = new ArrayList<>();
            if(json.has("loadAfter")){
                JsonArray loadAfter = json.get("loadAfter").getAsJsonArray();
                for (JsonElement loadAfterElement : loadAfter) {
                    meta.loadAfter.add(loadAfterElement.getAsString());
                }
            }

            meta.fetch = json.get("fetch").getAsBoolean();
            meta.isApi = json.get("isApi").getAsBoolean();

            meta.libraries = new ArrayList<>();
            if(json.has("libraries")) {
                JsonArray libraries = json.get("libraries").getAsJsonArray();
                for (JsonElement library : libraries) {
                    meta.libraries.add(library.getAsString());
                }
            }

            meta.repositories = new HashMap<>();
            if(json.has("repositories")) {
                JsonObject repositories = json.get("repositories").getAsJsonObject();
                for (String repository : repositories.keySet()) {
                    meta.repositories.put(repository, repositories.get(repository).getAsString());
                }
            }

            meta.dependencies = new HashMap<>();
            if(json.has("dependencies")) {
                JsonObject dependencies = json.get("dependencies").getAsJsonObject();
                for (String dependency : dependencies.keySet()) {
                    JsonObject dependencyObject = dependencies.get(dependency).getAsJsonObject();
                    String scope = dependencyObject.get("scope").getAsString();
                    String url = dependencyObject.get("url").getAsString();
                    meta.dependencies.put(dependency, new String[]{scope, url});
                }
            }

            JsonObject deployment = json.get("deployment").getAsJsonObject();
            JsonObject local = deployment.get("local").getAsJsonObject();
            meta.localDeploymentPath = local.get("path").getAsString();

            meta.remotes = new ArrayList<>();
            if(deployment.has("remotes")){
                JsonArray remotes = deployment.get("remotes").getAsJsonArray();
                for (JsonElement remote : remotes) {
                    meta.remotes.add(new Remote(remote.getAsJsonObject()));
                }
            }

            return meta;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean validate() {
        if (pluginVersion == null) {
            GradleBasePlugin.log("Could not find a 'meta' section with a 'version' field in your build.gradle");
            GradleBasePlugin.log();
            GradleBasePlugin.log(Color.RED + "Please check the GitHub page of GradleBasePlugin for more information");
            return true;
        }
        return false;
    }
}
