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
     * A "flavor" of the produced. CUrrently supported flavors are "osdefault"
     * (which the the default), and "osx_legacy".
     */
    @Parameter(defaultValue = "osdefault")
    protected Flavor flavor;

    /**
     * An optional string identifying the application human-readable name.
     */
    @Parameter(defaultValue = "${project.artifact.artifactId}-${project.artifact.version}")
    protected String longName;

    /**
     * A destination directory where the application launcher should be
     * installed. By default goes to "target".
     */
    @Parameter(defaultValue = "${project.build.directory}")
    protected File destDir;

    /**
     * Not settable by user.
     */
    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    protected File buildDir;

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
     * Optional parameters to pass to the JVM, such as memory settings, etc.
     */
    @Parameter
    protected String additionalJvmOptions;

    /**
     * Min JVM version to pass additional params
     */
    @Parameter
    protected String additionalJvmVersion;


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

    @Parameter( defaultValue = "${project}", readonly = true )
    protected MavenProject project;

    public void execute() throws MojoExecutionException, MojoFailureException {

        JApp task = new JApp(getLog(), buildDir);

        task.setName(name);
        task.setMainClass(mainClass);
        task.setDestDir(destDir);
        task.setOs(os != null ? os : OS.getCurrentOs());
        task.setLongName(longName);
        task.setIcon(icon);
        task.setJvm(jvm);
        task.setJvmOptions(jvmOptions);
        task.setVersion(version);
        task.setFlavor(flavor);
        task.setAdditionalJvmOptions(additionalJvmOptions);
        task.setAdditionalJvmVersion(additionalJvmVersion);

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

            File file = artifact.getFile();
            if (file == null) {
                getLog().debug("skipping empty artifact '" + artifact.getId() + "'...");
            } else {
                getLog().debug("packaging artifact '" + artifact.getId() + "'...");

                FileSet fs = new FileSet();
                fs.setFile(file);
                task.getLibs().add(fs);
            }
        }
    }

}
