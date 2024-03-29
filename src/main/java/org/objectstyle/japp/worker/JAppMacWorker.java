package org.objectstyle.japp.worker;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Objects;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.filters.ReplaceTokens;
import org.apache.tools.ant.taskdefs.Chmod;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

/**
 * @since 3.1
 */
class JAppMacWorker extends AbstractAntWorker {

    protected File contentsDir;
    protected File resourcesDir;
    protected File javaDir;
    protected File macOSDir;
    protected File pluginsDir;

    public JAppMacWorker(JApp parent) {
        super(parent);
    }

    public void execute() {

        File baseDir = new File(parent.getDestDir(), parent.getName() + ".app");
        this.contentsDir = new File(baseDir, "Contents");
        this.macOSDir = new File(contentsDir, "MacOS");
        this.resourcesDir = new File(contentsDir, "Resources");
        this.javaDir = new File(contentsDir, "Java");
        this.pluginsDir = new File(contentsDir, "PlugIns");

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
        createDirectory(pluginsDir);
    }

    void createDirectory(File file) throws BuildException {
        if (!file.isDirectory() && !file.mkdirs()) {
            throw new BuildException("Can't create directory " + file.getAbsolutePath());
        }
    }

    void createInfoPlist() throws BuildException {

        String targetIcon = parent.getIcon() != null && parent.getIcon().isFile() ? parent.getIcon().getName() : "";
        String jvm0Options = parent.getJvm0Options() != null ? parent.getJvm0Options() : "";

        StringBuilder jars = new StringBuilder();
        String[] jarFiles = javaDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        for (String jar : Objects.requireNonNull(jarFiles)) {
            jars.append("\n          <string>").append(jar).append("</string>");
        }

        ReplaceTokens filter = new ReplaceTokens();
        filter.addConfiguredToken(token("NAME", parent.getName()));
        filter.addConfiguredToken(token("VERSION", parent.getVersion()));
        filter.addConfiguredToken(token("LONG_NAME", parent.getLongName()));
        filter.addConfiguredToken(token("MAIN_CLASS", parent.getMainClass()));
        filter.addConfiguredToken(token("ICON", targetIcon));
        // force JVM0 version as a min version not a strict match
        String minVersion = parent.getJvm0().endsWith("+")
                ? parent.getJvm0()
                : parent.getJvm0() + "+";
        filter.addConfiguredToken(token("JVM0_VERSION", minVersion));
        filter.addConfiguredToken(token("JVM0_OPTIONS", jvm0Options));
        filter.addConfiguredToken(token("JARS", jars.toString()));
        filter.addConfiguredToken(token("EXECUTION_NAME", "MacOS/"+parent.getName()));
        filter.addConfiguredToken(token("JVM1_OPTIONS", parent.getJvm1Options()));
        filter.addConfiguredToken(token("JVM1_VERSION", parent.getJvm1()));

        File infoPlist = new File(contentsDir, "Info.plist");
        extractCharResource("mac/Info.plist.tpl", infoPlist, filter);
    }

    ReplaceTokens.Token token(String key, String value) {
        ReplaceTokens.Token t = new ReplaceTokens.Token();
        t.setKey(key);
        t.setValue(value);
        return t;
    }

    void copyStub() throws BuildException {

        File stub = new File(macOSDir, parent.getName());
        extractBinResource("mac/universalJavaApplicationStub", stub);

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
