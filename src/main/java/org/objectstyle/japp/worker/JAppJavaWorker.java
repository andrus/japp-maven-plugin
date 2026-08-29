package org.objectstyle.japp.worker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.ManifestException;
import org.apache.tools.ant.taskdefs.ManifestTask;
import org.apache.tools.ant.types.FileSet;

class JAppJavaWorker extends AbstractAntWorker {

    private static final String MAIN_CLASS = "Main-Class";

    protected Collection<File> unpackedJarDirs;
    protected File manifestFile;

    JAppJavaWorker(JApp parent) {
        super(parent);
        this.unpackedJarDirs = new ArrayList<>();
    }

    public void execute() {
        createDirectories();
        createManifest();
        unpackJars();
        createFatJar();
    }

    void createDirectories() {

        File baseDir = parent.getDestDir();
        if (!baseDir.isDirectory() && !baseDir.mkdirs()) {
            throw new BuildException("Can't create directory " + baseDir.getAbsolutePath());
        }
    }

    void createManifest() {
        this.manifestFile = new File(scratchDir(), "MANIFEST.MF");

        ManifestTask manifest = createTask(ManifestTask.class);
        manifest.setFile(manifestFile);

        try {
            manifest.addConfiguredAttribute(attribute(MAIN_CLASS, parent.getMainClass()));

            for (Map.Entry<String, String> e : parent.getManifestEntries().entrySet()) {

                String name = e.getKey();
                if (name == null || name.trim().length() == 0) {
                    throw new BuildException("Empty manifest entry name");
                }

                if (MAIN_CLASS.equalsIgnoreCase(name.trim())) {
                    // "Main-Class" is derived from the "mainClass" parameter, and can't be overridden
                    parent.getLogger().warn("Ignoring '" + MAIN_CLASS + "' manifest entry. Use 'mainClass' instead");
                    continue;
                }

                manifest.addConfiguredAttribute(attribute(name.trim(), e.getValue()));
            }
        } catch (ManifestException e) {
            throw new BuildException("Manifest error", e);
        }

        manifest.execute();
    }

    private Manifest.Attribute attribute(String name, String value) {
        Manifest.Attribute attribute = new Manifest.Attribute();
        attribute.setName(name);
        attribute.setValue(value != null ? value : "");
        return attribute;
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

                File unpackDir = new File(scratchDir(), jarId++ + "");
                unpackDir.mkdirs();
                unpackedJarDirs.add(unpackDir);

                unjar.setDest(unpackDir);
                unjar.setSrc(new File(scanner.getBasedir(), file));
                unjar.execute();
            }
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
