package org.objectstyle.japp.worker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import org.apache.maven.plugin.logging.Log;
import org.apache.tools.ant.types.FileSet;
import org.objectstyle.japp.Flavor;
import org.objectstyle.japp.OS;

public class JApp {

    private Log logger;

    private String name;
    private String mainClass;
    private Flavor flavor;
    private OS os;
    private File destDir;
    private String longName;
    private File icon;
    private String jvm0;
    private String jvm0Options;
    private String jvm1;
    private String jvm1Options;
    private String version;
    private Collection<FileSet> libs;
    private File buildDir;

    public JApp(Log logger, File buildDir) {
        this.logger = logger;
        this.buildDir = buildDir;
        this.libs = new ArrayList<>();
    }

    public File getBuildDir() {
        return buildDir;
    }

    public Collection<FileSet> getLibs() {
        return libs;
    }

    public void execute() {
        validate();
        initDefaults();

        logger.info("Building Java Application '" + name + "', os: " + os + ", dir: " + destDir);

        switch (os) {
        case mac:
            if(flavor == Flavor.osx_legacy) {
                new JAppLegacyMacWorker(this).execute();
            } else {
                new JAppMacWorker(this).execute();
            }
         
            break;
        case windows:
            new JAppWindowsWorker(this).execute();
            break;
        case java:
            new JAppJavaWorker(this).execute();
            break;
        default:
            // a safeguard against us adding another OS without creating a
            // worker...
            throw new IllegalStateException("Unsupported OS: " + os);
        }

    }

    protected void validate() {

        // theoretically calling Mojo should ensure that all required attributes
        // are set, so doing just quick sanity checks...

        if (isBlankString(name)) {
            throw new IllegalStateException("'name' is required");
        }

        if (destDir == null) {
            throw new IllegalStateException("'destDir' is required");
        }

        if (os == null) {
            throw new IllegalStateException("'os' is required");
        }

        validateMainClass();
    }

    protected void validateMainClass() {
        if (isBlankString(mainClass)) {
            throw new IllegalStateException("'mainClass' is required");
        }

        StringTokenizer classToks = new StringTokenizer(mainClass, ".");
        while (classToks.hasMoreTokens()) {
            String tok = classToks.nextToken();
            for (int i = 0; i < tok.length(); i++) {

                if (i == 0) {
                    if (!Character.isJavaIdentifierStart(tok.charAt(0))) {
                        throw new IllegalStateException("Invalid java class name: " + mainClass);
                    }
                } else {
                    if (!Character.isJavaIdentifierPart(tok.charAt(i))) {
                        throw new IllegalStateException("Invalid java class name: " + mainClass);
                    }
                }
            }
        }
    }

    protected void initDefaults() {
        if (longName == null) {
            longName = name;
        }

        if (version == null) {
            version = "0.0";
        }

        if (jvm0 == null) {
            jvm0 = "8";
        }

        if (jvm1 == null) {
            jvm1 = "0";
        }
    }

    private boolean isBlankString(String string) {
        return string == null || string.trim().length() == 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public OS getOs() {
        return os;
    }

    public void setOs(OS os) {
        this.os = os;
    }

    public File getDestDir() {
        return destDir;
    }

    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public File getIcon() {
        return icon;
    }

    public void setIcon(File icon) {
        this.icon = icon;
    }

    public String getJvm0() {
        return jvm0;
    }

    public void setJvm0(String jvm0) {
        this.jvm0 = jvm0;
    }

    public String getJvm0Options() {
        return jvm0Options;
    }

    public void setJvm0Options(String jvm0Options) {
        this.jvm0Options = jvm0Options;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Log getLogger() {
        return logger;
    }

    public Flavor getFlavor() {
        return flavor;
    }

    public void setFlavor(Flavor flavor) {
        this.flavor = flavor;
    }

    public String getJvm1Options() {
        return jvm1Options;
    }

    public void setJvm1Options(String jvm1Options) {
        this.jvm1Options = jvm1Options;
    }

    public String getJvm1() {
        return jvm1;
    }

    public void setJvm1(String jvm1) {
        this.jvm1 = jvm1;
    }
}
