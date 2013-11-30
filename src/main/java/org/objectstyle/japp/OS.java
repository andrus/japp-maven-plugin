package org.objectstyle.japp;

/**
 * An enum of supported target operating systems.
 */
public enum OS {
    windows, mac, java;

    /**
     * Returns default operating system for a given platform. If no exact
     * platform match is found, java platform is returned.
     */
    public static OS getCurrentOs() {
        String vmOS = System.getProperty("os.name").toUpperCase();
        if (vmOS.startsWith("WINDOWS")) {
            return OS.windows;
        } else if (vmOS.startsWith("MAC")) {
            return OS.mac;
        } else {
            return OS.java;
        }
    }
}
