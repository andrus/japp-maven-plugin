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
        extractResource("makensis.exe", nsisDir);
        extractResource("Stubs/bzip2", nsisDir);
        extractResource("Stubs/bzip2_solid", nsisDir);
        extractResource("Stubs/lzma", nsisDir);
        extractResource("Stubs/lzma_solid", nsisDir);
        extractResource("Stubs/uninst", nsisDir);
        extractResource("Stubs/zlib", nsisDir);
        extractResource("Stubs/zlib_solid", nsisDir);

        this.nsisExe = new File(nsisDir, "makensis.exe").getAbsolutePath();
    }

    void createNsisScript() throws BuildException {

        String targetIcon = parent.getIcon() != null && parent.getIcon().isFile() ? "Icon \""
                + parent.getIcon().getAbsolutePath() + "\"" : "";
        String jvmOptions = parent.getJvmOptions() != null ? parent.getJvmOptions() : "";
        String outFile = new File(parent.getDestDir(), parent.getName() + ".exe").getAbsolutePath();

        ReplaceTokens filter = new ReplaceTokens();
        filter.addConfiguredToken(token("NAME", parent.getName()));
        filter.addConfiguredToken(token("LONG_NAME", parent.getLongName()));
        filter.addConfiguredToken(token("MAIN_CLASS", parent.getMainClass()));
        filter.addConfiguredToken(token("ICON", targetIcon));
        filter.addConfiguredToken(token("JVM_OPTIONS", jvmOptions));
        filter.addConfiguredToken(token("OUT_FILE", outFile));

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
        String path = "windows/nsis-2.20/" + resourceName;

        String name = ('/' != File.separatorChar) ? resourceName.replace('/', File.separatorChar) : resourceName;
        extractBinResource(path, new File(dir, name));
    }
}
