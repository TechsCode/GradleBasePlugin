package me.TechsCode.GradeBasePlugin.extensions;

import java.util.ArrayList;

import me.TechsCode.GradeBasePlugin.Color;
import me.TechsCode.GradeBasePlugin.GradleBasePlugin;

public class MetaExtension {

    public String pluginVersion;
    public String baseVersion;
    public String loadAfter, loadBefore, load;
    public ArrayList<String> libraries;
    public boolean fetch;
    public boolean isAPI;

    public boolean configValid() {
        if (pluginVersion == null) {
            GradleBasePlugin.log("Could not find a 'pluginVersion' field in your build.gradle");
            GradleBasePlugin.log();
            GradleBasePlugin.log(Color.RED + "Please check the build.gradle of your project");
            return false;
        }
        if (baseVersion == null) {
            GradleBasePlugin.log("Could not find a 'baseVersion' field in your build.gradle");
            GradleBasePlugin.log();
            GradleBasePlugin.log(Color.RED + "Please check the build.gradle of your project");
            return false;
        }
        if (loadAfter == null) {
            GradleBasePlugin.log("Could not find a 'loadAfter' field in your build.gradle");
            GradleBasePlugin.log();
            GradleBasePlugin.log(Color.RED + "Please check the build.gradle of your project");
            return false;
        }
        if (loadBefore == null) {
            GradleBasePlugin.log("Could not find a 'loadBefore' field in your build.gradle");
            GradleBasePlugin.log();
            GradleBasePlugin.log(Color.RED + "Please check the build.gradle of your project");
            return false;
        }
        if (load == null) {
            GradleBasePlugin.log("Could not find a 'load' field in your build.gradle");
            GradleBasePlugin.log();
            GradleBasePlugin.log(Color.RED + "Please check the build.gradle of your project");
            return false;
        }
            return false;
        }
        if (fetch) {
            if(System.getenv("TECHSCODE_USERNAME") == null || System.getenv("TECHSCODE_PASSWORD") == null) {
                GradleBasePlugin.log("Could not find a 'TECHSCODE_USERNAME' or 'TECHSCODE_PASSWORD' environment variable");
                GradleBasePlugin.log();
                GradleBasePlugin.log(Color.RED + "Please set the 'TECHSCODE_USERNAME' and 'TECHSCODE_PASSWORD' environment variables");
                return false;
            }
        }

        return true;
    }
}