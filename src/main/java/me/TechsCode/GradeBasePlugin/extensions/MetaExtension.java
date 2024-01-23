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
    public boolean isAPI = false;

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

        return true;
    }
}