package org.objectstyle.japp;

import java.io.File;
import java.util.ArrayList;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.types.FileSet;
import org.objectstyle.japp.worker.JApp;
import org.objectstyle.japp.worker.OS;

/**
 * Maven plugin to to assemble desktop Java applications for different
 * platforms.
 */
@Mojo(name = "japp", requiresDependencyResolution = ResolutionScope.COMPILE)
public class JAppMojo extends AbstractMojo {

    /**
     * The name of the application without OS-specific extension
     */
    @Parameter(defaultValue = "${project.artifact.artifactId}")
    protected String name;

    /**
     * Main Java class that will be used to start packaged Java application.
     */
    @Parameter(required = true)
    protected String mainClass;

    /**
     * A family of operating systems. Currently supported values are "mac",
     * "windows" and "java". Default is the current OS where the build is run.
     */
    @Parameter
    protected OS os;

    /**
     * An optional string identifying the application human-readable name.
     */
    @Parameter(defaultValue = "${project.artifact.artifactId}-${project.artifact.version}")
    protected String longName;

    /**
     * A destination directory where the application launcher should be
     * installed.
     */
    @Parameter(defaultValue = "${project.build.directory}")
    protected File destDir;

    /**
     * Platform-specific icon file. Usually "*.ico" on Windows and "*.icns" on
     * Mac.
     */
    @Parameter
    protected File icon;

    /**
     * Minimal version of the Java Virtual machine required.
     */
    @Parameter
    protected String jvm;

    /**
     * Optional parameters to pass to the JVM, such as memory settings, etc.
     */
    @Parameter
    protected String jvmOptions;

    /**
     * A String identifying the version of the assembled package.
     */
    @Parameter(defaultValue = "${project.artifact.version}")
    protected String version;

    /**
     * An array of included artifact items used to filter the list of
     * dependencies. Pattern matching is done via a simple String.startsWith()
     * check on an artifact name in the form of groupid:artifactid:version.
     */
    @Parameter
    protected ArrayList<String> includes;

    /**
     * An array of excluded artifact items used to filter the list of
     * dependencies. Pattern matching is done via a simple String.startsWith()
     * check on an artifact name in the form of groupid:artifactid:version.
     */
    @Parameter
    protected ArrayList<String> excludes;

    @Component
    protected MavenProject project;

    public void execute() throws MojoExecutionException, MojoFailureException {

        JApp task = new JApp(getLog());

        task.setName(name);
        task.setMainClass(mainClass);
        task.setDestDir(destDir);
        task.setOs(os != null ? os : getCurrentOs());
        task.setLongName(longName);
        task.setIcon(icon);
        task.setJvm(jvm);
        task.setJvmOptions(jvmOptions);
        task.setVersion(version);

        ArtifactMatchPattern includesMatcher = new ArtifactMatchPattern(includes);
        ArtifactMatchPattern excludesMatcher = new ArtifactMatchPattern(excludes);

        // add compile dependencies...
        for (Artifact a : project.getArtifacts()) {
            if (a.getArtifactHandler().isAddedToClasspath()) {
                if (Artifact.SCOPE_COMPILE.equals(a.getScope()) || Artifact.SCOPE_PROVIDED.equals(a.getScope())
                        || Artifact.SCOPE_SYSTEM.equals(a.getScope())) {
                    addArtifact(task, a, includesMatcher, excludesMatcher);
                }
            }
        }

        // add current project's own artifact
        addArtifact(task, project.getArtifact(), includesMatcher, excludesMatcher);

        try {
            task.execute();
        } catch (Exception e) {
            throw new MojoExecutionException("Error packaging the app: " + e.getMessage(), e);
        }
    }

    private void addArtifact(JApp task, Artifact artifact, ArtifactMatchPattern includesMatcher,
            ArtifactMatchPattern excludesMatcher) {

        if (includesMatcher.matchInclude(artifact) && !excludesMatcher.matchExclude(artifact)) {

            getLog().debug("packaging artifact '" + artifact.getId() + "'...");

            FileSet fs = new FileSet();
            fs.setFile(artifact.getFile());
            task.getLibs().add(fs);
        }
    }

    /**
     * Returns default operating system for a given platform. If no exact
     * platform match is found, java platform is returned.
     */
    private OS getCurrentOs() {
        String vmOS = System.getProperty("os.name").toUpperCase();
        if (vmOS.startsWith("WINDOWS")) {
            return OS.windows;
        } else if (vmOS.startsWith("MAC")) {
            return OS.mac;
        } else {
            return OS.java;
        }
    }
}
