package me.TechsCode.GradeBasePlugin.extensions;

import java.util.ArrayList;

import me.TechsCode.GradeBasePlugin.Color;
import me.TechsCode.GradeBasePlugin.GradleBasePlugin;

public class MetaExtension {

    public String version;
    public String baseVersion;
    public String loadAfter, loadBefore, load;
    public ArrayList<String> libraries;
    public boolean fetch;

    public boolean validate() {
        if (version == null) {
            GradleBasePlugin.log("Could not find a 'meta' section with a 'version' field in your build.gradle");
            GradleBasePlugin.log();
            GradleBasePlugin.log(Color.RED + "Please check the GitHub page of GradleBasePlugin for more information");
            return true;
        }
        return false;
    }
}
