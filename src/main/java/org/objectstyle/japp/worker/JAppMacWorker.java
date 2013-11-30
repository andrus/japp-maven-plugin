package org.objectstyle.japp.worker;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;

import com.oracle.appbundler.AppBundlerTask;
import com.oracle.appbundler.Option;

/**
 * Packages OS X apps for Java 1.7+ using Oracle appbunder.
 */
class JAppMacWorker extends AbstractAntWorker {

    JAppMacWorker(JApp parent) {
        super(parent);
    }

    public void execute() {
        validateJavaVersion();
        createDirectories();
        bundle();
    }

    private void validateJavaVersion() {
        // not using commons SystemUtils... They are replying on static enums
        // and will become obsolete eventually
        String classVersion = System.getProperty("java.class.version");
        int dot = classVersion.indexOf('.');
        classVersion = dot > 0 ? classVersion.substring(0, dot) : classVersion;
        int classVersionInt;
        try {
            classVersionInt = Integer.parseInt(classVersion);
        } catch (Exception e) {
            // hmm..
            return;
        }

        if (classVersionInt < 51) {
            throw new BuildException("Minimal JDK requirement is 1.7. Got : " + System.getProperty("java.version"));
        }
    }

    void createDirectories() {

        File baseDir = parent.getDestDir();
        if (!baseDir.isDirectory() && !baseDir.mkdirs()) {
            throw new BuildException("Can't create directory " + baseDir.getAbsolutePath());
        }
    }

    void bundle() {

        String[] jvmOptions = parent.getJvmOptions() != null ? parent.getJvmOptions().split("\\s") : new String[0];

        AppBundlerTask bundler = createTask(AppBundlerTask.class);

        // TODO: hardcoded keys that are nice to support...

        // bundler.setApplicationCategory("public.app-category.developer-tools");
        // bundler.setCopyright("ASF");

        bundler.setDisplayName(parent.getLongName());
        bundler.setIcon(parent.getIcon());
        bundler.setIdentifier(parent.getName());
        bundler.setMainClassName(parent.getMainClass());
        bundler.setName(parent.getName());
        bundler.setOutputDirectory(parent.getDestDir());
        bundler.setShortVersion(parent.getVersion());

        for (String op : jvmOptions) {
            Option option = new Option();
            option.setValue(op);
            bundler.addConfiguredOption(option);
        }

        for (FileSet fs : parent.getLibs()) {
            bundler.addConfiguredClassPath(fs);
        }

        bundler.execute();
    }
}
