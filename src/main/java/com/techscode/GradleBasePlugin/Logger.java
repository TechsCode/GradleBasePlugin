package com.techscode.GradleBasePlugin;

/**
 * Logging util making it easier to log messages to the console
 */
public class Logger {

    /**
     * Info message
     * @param messages
     */
    public static void info(String... messages) {
        for(String message : messages) {
            info(message);
        }
    }

    /**
     * Info message
     * @param message
     */
    public static void info(String message) {
        System.out.println(Color.GREEN + "[INFO] " + Color.RESET + message);
    }

    /**
     * Warning message
     * @param messages
     */
    public static void warning(String... messages) {
        for(String message : messages) {
            error(message);
        }
    }

    /**
     * Warning message
     * @param message
     */
    public static void warning(String message) {
        System.err.println(Color.GREEN + "[WARN] " + Color.RESET + message);
    }

    /**
     * Error message
     * @param messages
     */
    public static void error(String... messages) {
        for(String message : messages) {
            error(message);
        }
    }

    /**
     * Error message
     * @param message
     */
    public static void error(String message) {
        System.err.println(Color.GREEN + "[ERROR] " + Color.RESET + message);
    }
}
