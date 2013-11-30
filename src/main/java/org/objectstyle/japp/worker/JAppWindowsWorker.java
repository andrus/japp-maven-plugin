package org.objectstyle.japp.worker;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.filters.ReplaceTokens;
import org.apache.tools.ant.filters.ReplaceTokens.Token;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.ExecTask;

class JAppWindowsWorker extends JAppJavaWorker {

    static final String EMBEDDED_NSIS_PATH = "japplication/windows/nsis-2.20";

    protected String nsisExe;
    protected File nsiScript;

    public JAppWindowsWorker(JApp parent) {
        super(parent);
    }

    @Override
    protected void executeInternal() throws BuildException {
        // build fat runnable jar
        super.executeInternal();

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

        File nsisDir = new File(scratchDir, "nsis");

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

        ReplaceTokens tokenFilter = new ReplaceTokens();
        tokenFilter.addConfiguredToken(token("@NAME@", parent.getName()));
        tokenFilter.addConfiguredToken(token("@LONG_NAME@", parent.getLongName()));
        tokenFilter.addConfiguredToken(token("@MAIN_CLASS@", parent.getMainClass()));
        tokenFilter.addConfiguredToken(token("@ICON@", targetIcon));
        tokenFilter.addConfiguredToken(token("@JVM_OPTIONS@", jvmOptions));
        tokenFilter.addConfiguredToken(token("@OUT_FILE@", outFile));

        // TODO: extract "japplication/windows/app.nsi" using 'extractResource'
        // to copy
        Copy copy = createTask(Copy.class);
        copy.createFilterChain().add(tokenFilter);
        copy.setTodir(scratchDir);
        copy.execute();

        this.nsiScript = new File(scratchDir, "app.nsi");
    }

    private Token token(String key, String value) {
        Token t = new Token();
        t.setKey(key);
        t.setValue(value);
        return t;
    }

    void extractResource(String resourceName, File dir) {
        String path = EMBEDDED_NSIS_PATH + '/' + resourceName;

        String name = ('/' != File.separatorChar) ? resourceName.replace('/', File.separatorChar) : resourceName;
        AntUtil.copy(path, new File(dir, name));
    }
}
