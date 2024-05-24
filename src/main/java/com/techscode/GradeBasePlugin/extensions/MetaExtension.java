package com.techscode.GradeBasePlugin.extensions;

import java.util.ArrayList;

import com.techscode.GradeBasePlugin.Logger;

public class MetaExtension {

    public String pluginVersion;
    public String baseVersion;
    public String loadAfter, loadBefore, load;
    public ArrayList<String> libraries;
    public boolean fetch;
    public boolean isAPI;

    public boolean configValid() {
        if (pluginVersion == null) {
            Logger.error(
                    "Could not find a 'pluginVersion' field in your build.gradle",
                    "",
                    "Please check the build.gradle of your project and make sure you have a 'pluginVersion' field in your 'meta' extension"
            );
            return false;
        }
        if (baseVersion == null) {
            Logger.error(
                    "Could not find a 'baseVersion' field in your build.gradle",
                    "",
                    "Please check the build.gradle of your project and make sure you have a 'baseVersion' field in your 'meta' extension"
            );
            return false;
        }
        if (loadAfter == null) {
            Logger.error(
                    "Could not find a 'loadAfter' field in your build.gradle",
                    "",
                    "Please check the build.gradle of your project and make sure you have a 'loadAfter' field in your 'meta' extension"
            );
            return false;
        }
        if (loadBefore == null) {
            Logger.error(
                    "Could not find a 'loadBefore' field in your build.gradle",
                    "",
                    "Please check the build.gradle of your project and make sure you have a 'loadBefore' field in your 'meta' extension"
            );
            return false;
        }
        if (load == null) {
            Logger.error(
                    "Could not find a 'load' field in your build.gradle",
                    "",
                    "Please check the build.gradle of your project and make sure you have a 'load' field in your 'meta' extension"
            );
            return false;
        }
        if (libraries == null) {
            Logger.error(
                    "Could not find a 'libraries' field in your build.gradle",
                    "",
                    "Please check the build.gradle of your project and make sure you have a 'libraries' field in your 'meta' extension"
            );
            return false;
        }
        if (fetch) {
            if(System.getenv("TECHSCODE_USERNAME") == null || System.getenv("TECHSCODE_PASSWORD") == null) {
                Logger.error(
                        "Could not find a 'TECHSCODE_USERNAME' or 'TECHSCODE_PASSWORD' environment variable",
                        "",
                        "Please make sure you have set the 'TECHSCODE_USERNAME' and 'TECHSCODE_PASSWORD' environment variables"
                );
                return false;
            }
        }

        return true;
    }
}