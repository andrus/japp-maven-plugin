package org.objectstyle.japp.worker;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.filters.ReplaceTokens;
import org.apache.tools.ant.filters.ReplaceTokens.Token;
import org.apache.tools.ant.taskdefs.Chmod;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

class JAppMacWorker extends AbstractAntWorker {

    protected File contentsDir;
    protected File resourcesDir;
    protected File javaDir;
    protected File macOSDir;

    public JAppMacWorker(JApp parent) {
        super(parent);
    }

    public void execute() {

        File baseDir = new File(parent.getDestDir(), parent.getName() + ".app");
        this.contentsDir = new File(baseDir, "Contents");
        this.macOSDir = new File(contentsDir, "MacOS");
        this.resourcesDir = new File(contentsDir, "Resources");
        this.javaDir = new File(resourcesDir, "Java");

        createDirectories();
        copyStub();
        copyIcon();
        copyJars();

        // do this AFTER the jars, as we need to list them in the Info.plist
        createInfoPlist();
    }

    void createDirectories() throws BuildException {
        createDirectory(parent.getDestDir());
        createDirectory(resourcesDir);
        createDirectory(javaDir);
        createDirectory(macOSDir);
    }

    void createDirectory(File file) throws BuildException {
        if (!file.isDirectory() && !file.mkdirs()) {
            throw new BuildException("Can't create directory " + file.getAbsolutePath());
        }
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
        extractCharResource("mac/Info.plist", infoPlist, filter);
    }

    private Token token(String key, String value) {
        Token t = new Token();
        t.setKey(key);
        t.setValue(value);
        return t;
    }

    void copyStub() throws BuildException {

        File stub = new File(macOSDir, "JavaApplicationStub");
        extractBinResource("mac/JavaApplicationStub_1.6", stub);

        Chmod chmod = createTask(Chmod.class);
        chmod.setPerm("755");
        chmod.setFile(stub);
        chmod.execute();
    }

    void copyIcon() throws BuildException {
        if (parent.getIcon() != null && parent.getIcon().isFile()) {
            Copy cp = createTask(Copy.class);
            cp.setTodir(resourcesDir);
            cp.setFile(parent.getIcon());
            cp.execute();
        }
    }

    void copyJars() {
        if (!parent.getLibs().isEmpty()) {
            Copy cp = createTask(Copy.class);
            cp.setTodir(javaDir);
            cp.setFlatten(true);

            for (FileSet fs : parent.getLibs()) {
                cp.addFileset(fs);
            }

            cp.execute();
        }
    }
}
