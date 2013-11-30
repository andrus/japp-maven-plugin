package org.objectstyle.japp.worker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.ManifestException;
import org.apache.tools.ant.taskdefs.ManifestTask;
import org.apache.tools.ant.types.FileSet;

class JAppJavaWorker extends AbstractAntWorker {

    protected JApp parent;
    protected File scratchDir;
    protected Collection<File> unpackedJarDirs;
    protected File manifestFile;

    JAppJavaWorker(JApp parent) {
        this.parent = parent;
        this.unpackedJarDirs = new ArrayList<File>();
    }

    public void execute() {

        try {
            executeInternal();
        } finally {
            // must clean the tempdir
            if (!recursiveDelete(scratchDir)) {
                throw new RuntimeException("Failed to clean up temp directory: " + scratchDir);
            }
        }
    }

    protected void executeInternal() {
        createDirectories();
        createManifest();
        unpackJars();
        createFatJar();
    }

    void createManifest() {
        this.manifestFile = new File(scratchDir, "MANIFEST.MF");

        Manifest.Attribute mainClass = new Manifest.Attribute();
        mainClass.setName("Main-Class");
        mainClass.setValue(parent.getMainClass());

        ManifestTask manifest = createTask(ManifestTask.class);
        manifest.setFile(manifestFile);
        try {
            manifest.addConfiguredAttribute(mainClass);
        } catch (ManifestException e) {
            throw new BuildException("Manifest error", e);
        }

        manifest.execute();
    }

    void createFatJar() {
        File fatJar = new File(parent.getDestDir(), parent.getName() + ".jar");
        fatJar.delete();

        Jar jar = createTask(Jar.class);
        jar.setDestFile(fatJar);
        jar.setManifest(manifestFile);

        for (File jarDir : unpackedJarDirs) {

            FileSet fs = new FileSet();
            fs.setDir(jarDir);
            jar.addFileset(fs);
        }

        jar.execute();
    }

    void unpackJars() {
        // unpack in separate dirs, to allow multiple instances of the same
        // resource from different jars.

        int jarId = 0;

        for (FileSet fs : parent.getLibs()) {
            DirectoryScanner scanner = fs.getDirectoryScanner(project);

            Expand unjar = createTask(Expand.class);

            for (String file : scanner.getIncludedFiles()) {

                File unpackDir = new File(scratchDir, jarId++ + "");
                unpackDir.mkdirs();
                unpackedJarDirs.add(unpackDir);

                unjar.setDest(unpackDir);
                unjar.setSrc(new File(scanner.getBasedir(), file));
                unjar.execute();
            }
        }
    }

    void createDirectories() {

        File baseDir = parent.getDestDir();
        if (!baseDir.isDirectory() && !baseDir.mkdirs()) {
            throw new BuildException("Can't create directory " + baseDir.getAbsolutePath());
        }

        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        String label = "japplication-" + System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            File dir = new File(tmpDir, label + i);
            if (!dir.exists() && dir.mkdirs()) {
                this.scratchDir = dir;
                break;
            }
        }

        if (!scratchDir.isDirectory()) {
            throw new BuildException("Can't create scratch directory");
        }
    }

    boolean recursiveDelete(File file) {
        if (!file.exists()) {
            return true;
        }

        if (!file.isDirectory()) {
            return file.delete();
        }

        for (String childFile : file.list()) {
            if (!recursiveDelete(new File(file, childFile))) {
                return false;
            }

        }

        return file.delete();
    }
}
