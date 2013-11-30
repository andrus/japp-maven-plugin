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
        // TODO: check that Java version is 1.7+...

        createDirectories();
        bundle();
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

    }
}
