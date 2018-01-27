package org.objectstyle.japp.worker;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.filters.ReplaceTokens;
import org.apache.tools.ant.taskdefs.Chmod;

/**
 * Packages OS X apps for Java 1.6 and older, targeting Apple Java.
 * 
 * @see JAppMacWorker
 */
class JAppLegacyMacWorker extends JAppMacWorker {

    public JAppLegacyMacWorker(JApp parent) {
        super(parent);
    }

    void createInfoPlist() throws BuildException {

        String targetIcon = parent.getIcon() != null && parent.getIcon().isFile() ? parent.getIcon().getName() : "";
        String jvmOptions = parent.getJvmOptions() != null ? parent.getJvmOptions() : "";

        StringBuffer jars = new StringBuffer();
        String[] jarFiles = javaDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        for (String jar : jarFiles) {
            jars.append("          <string>$JAVAROOT/").append(jar).append("</string>\n");
        }

        ReplaceTokens filter = new ReplaceTokens();
        filter.addConfiguredToken(token("NAME", parent.getName()));
        filter.addConfiguredToken(token("VERSION", parent.getVersion()));
        filter.addConfiguredToken(token("LONG_NAME", parent.getLongName()));
        filter.addConfiguredToken(token("MAIN_CLASS", parent.getMainClass()));
        filter.addConfiguredToken(token("ICON", targetIcon));
        filter.addConfiguredToken(token("JVM", parent.getJvm()));
        filter.addConfiguredToken(token("JVM_OPTIONS", jvmOptions));
        filter.addConfiguredToken(token("JARS", jars.toString()));

        File infoPlist = new File(contentsDir, "Info.plist");
        extractCharResource("mac/Info.plist.legacy.tpl", infoPlist, filter);
    }

    void copyStub() throws BuildException {

        File stub = new File(macOSDir, "JavaApplicationStub");
        extractBinResource("mac/JavaApplicationStub_1.6", stub);

        Chmod chmod = createTask(Chmod.class);
        chmod.setPerm("755");
        chmod.setFile(stub);
        chmod.execute();
    }
}
