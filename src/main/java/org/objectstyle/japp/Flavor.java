package org.objectstyle.japp;

/**
 * Optional flavors of the produced app.
 */
public enum Flavor {

    /**
     * A flavor corresponding to a default package for each {@link OS}.
     */
    osdefault,

    /**
     * A flavor that is only applicable for {@link OS#mac} platform, that will
     * result in an app bundle working with older Apple Java 6.
     */
    osx_legacy;
}
