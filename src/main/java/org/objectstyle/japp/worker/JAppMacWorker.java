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

    private static final String STUB = "/System/Library/Frameworks/JavaVM.framework/Versions/Current/Resources/MacOS/JavaApplicationStub";

    protected JApp parent;
    protected File contentsDir;
    protected File resourcesDir;
    protected File javaDir;
    protected File macOSDir;
    protected File stub;

    public JAppMacWorker(JApp parent) {
        this.parent = parent;
    }

    public void execute() {

        File baseDir = new File(parent.getDestDir(), parent.getName() + ".app");
        this.contentsDir = new File(baseDir, "Contents");
        this.macOSDir = new File(contentsDir, "MacOS");
        this.resourcesDir = new File(contentsDir, "Resources");
        this.javaDir = new File(resourcesDir, "Java");

        this.stub = new File(STUB);

        // sanity check...
        if (!stub.isFile()) {
            throw new BuildException("Java stub file not found. Is this a Mac? " + STUB);
        }

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

        ReplaceTokens tokenFilter = new ReplaceTokens();
        tokenFilter.addConfiguredToken(token("@NAME@", parent.getName()));
        tokenFilter.addConfiguredToken(token("@VERSION@", parent.getVersion()));
        tokenFilter.addConfiguredToken(token("@LONG_NAME@", parent.getLongName()));
        tokenFilter.addConfiguredToken(token("@MAIN_CLASS@", parent.getMainClass()));
        tokenFilter.addConfiguredToken(token("@ICON@", targetIcon));
        tokenFilter.addConfiguredToken(token("@JVM@", parent.getJvm()));
        tokenFilter.addConfiguredToken(token("@JVM_OPTIONS@", jvmOptions));
        tokenFilter.addConfiguredToken(token("@JARS@", jars.toString()));

        // TODO: extract "japplication/mac/Info.plist" using 'extractResource'
        // to copy
        Copy copy = createTask(Copy.class);
        copy.createFilterChain().add(tokenFilter);
        copy.setTodir(contentsDir);
        copy.execute();
    }

    private Token token(String key, String value) {
        Token t = new Token();
        t.setKey(key);
        t.setValue(value);
        return t;
    }

    void copyStub() throws BuildException {
        Copy cp = createTask(Copy.class);
        cp.setTodir(macOSDir);
        cp.setFile(stub);
        cp.execute();

        Chmod chmod = createTask(Chmod.class);
        chmod.setPerm("755");
        chmod.setFile(new File(macOSDir, "JavaApplicationStub"));
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
