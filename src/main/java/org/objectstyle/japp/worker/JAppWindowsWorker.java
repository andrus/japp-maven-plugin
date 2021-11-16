package org.objectstyle.japp.worker;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.filters.ReplaceTokens;
import org.apache.tools.ant.filters.ReplaceTokens.Token;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.objectstyle.japp.OS;

class JAppWindowsWorker extends JAppJavaWorker {

    protected String nsisExe;
    protected File nsiScript;

    public JAppWindowsWorker(JApp parent) {
        super(parent);
    }

    @Override
    public void execute() {
        // this only runs on Windows, as NSIS.exe execution is required... so do
        // a quick OS check
        if (OS.getCurrentOs() != OS.windows) {
            throw new BuildException("Windows packaging can only be done on Windows. We are currenlty on '"
                    + System.getProperty("os.name") + "'");
        }

        // build fat runnable jar
        super.execute();

        createNsisScript();
        initNsis();
        execNsis();
    }

    void execNsis() throws BuildException {
        ExecTask exec = createTask(ExecTask.class);

        exec.setDir(parent.getDestDir());
        exec.setExecutable(nsisExe);
        exec.setFailonerror(true);
        exec.createArg().setLine(nsiScript.getAbsolutePath());

        exec.execute();
    }

    void initNsis() throws BuildException {

        parent.getLogger().debug("Extracting embedded NSIS");

        File nsisDir = new File(scratchDir(), "nsis");

        // extract embedded NSIS into the scratch directory
        extractResource("bin/makensis.exe", nsisDir);
        extractResource("bin/zlib1.dll", nsisDir);
        extractResource("Stubs/bzip2-x86-ansi", nsisDir);
        extractResource("Stubs/bzip2-x86-unicode", nsisDir);
        extractResource("Stubs/bzip2_solid-x86-ansi", nsisDir);
        extractResource("Stubs/bzip2_solid-x86-unicode", nsisDir);
        extractResource("Stubs/lzma-x86-ansi", nsisDir);
        extractResource("Stubs/lzma-x86-unicode", nsisDir);
        extractResource("Stubs/lzma_solid-x86-ansi", nsisDir);
        extractResource("Stubs/lzma_solid-x86-unicode", nsisDir);
        extractResource("Stubs/uninst", nsisDir);
        extractResource("Stubs/zlib-x86-ansi", nsisDir);
        extractResource("Stubs/zlib-x86-unicode", nsisDir);
        extractResource("Stubs/zlib_solid-x86-ansi", nsisDir);
        extractResource("Stubs/zlib_solid-x86-unicode", nsisDir);

        this.nsisExe = new File(nsisDir, "bin/makensis.exe").getAbsolutePath();
    }

    void createNsisScript() throws BuildException {

        String targetIcon = parent.getIcon() != null && parent.getIcon().isFile() ? "Icon \""
                + parent.getIcon().getAbsolutePath() + "\"" : "";
        String jvm0Options = parent.getJvm0Options() != null ? parent.getJvm0Options() : "";
        String jvm1Options = parent.getJvm1Options() != null ? parent.getJvm1Options() : "";
        String outFile = new File(parent.getDestDir(), parent.getName() + ".exe").getAbsolutePath();

        ReplaceTokens filter = new ReplaceTokens();
        filter.addConfiguredToken(token("NAME", parent.getName()));
        filter.addConfiguredToken(token("LONG_NAME", parent.getLongName()));
        filter.addConfiguredToken(token("MAIN_CLASS", parent.getMainClass()));
        filter.addConfiguredToken(token("VERSION", parent.getVersion()));
        filter.addConfiguredToken(token("ICON", targetIcon));
        filter.addConfiguredToken(token("OUT_FILE", outFile));

        filter.addConfiguredToken(token("JVM0", parent.getJvm0()));
        filter.addConfiguredToken(token("JVM0_OPTIONS", jvm0Options));
        filter.addConfiguredToken(token("JVM1", parent.getJvm1()));
        filter.addConfiguredToken(token("JVM1_OPTIONS", jvm1Options));

        this.nsiScript = new File(scratchDir(), "app.nsi");
        extractCharResource("windows/app.nsi", nsiScript, filter);
    }

    private Token token(String key, String value) {
        Token t = new Token();
        t.setKey(key);
        t.setValue(value);
        return t;
    }

    private void extractResource(String resourceName, File dir) {
        String path = "windows/nsis-3.05/" + resourceName;

        String name = ('/' != File.separatorChar) ? resourceName.replace('/', File.separatorChar) : resourceName;
        extractBinResource(path, new File(dir, name));
    }
}
