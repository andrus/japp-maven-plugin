/*****************************************************************
 *   Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 ****************************************************************/
package org.objectstyle.japp.worker;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.filters.ChainableReader;

class AbstractAntWorker {

    protected JApp parent;
    protected Project project;

    private File scratchDir;

    public AbstractAntWorker(JApp parent) {
        this.parent = parent;
        this.project = new Project();
    }

    /**
     * Returns a directory where this worker can place temp files, creating it
     * lazily if needed.
     */
    protected File scratchDir() {

        if (scratchDir == null) {
            File newScratchDir = null;
            String prefix = getClass().getSimpleName() + "-" + System.currentTimeMillis();

            for (int i = 0; i < 10; i++) {
                File dir = new File(parent.getBuildDir(), prefix + i);
                if (!dir.exists() && dir.mkdirs()) {
                    newScratchDir = dir;
                    break;
                }
            }

            if (newScratchDir == null || !newScratchDir.isDirectory()) {
                throw new BuildException("Can't create scratch directory");
            }

            this.scratchDir = newScratchDir;
        }

        return scratchDir;
    }

    /**
     * A utility method to create Ant worker tasks.
     */
    protected <T extends Task> T createTask(Class<T> type) {

        if (type == null) {
            throw new NullPointerException("Null task class");
        }

        T task;
        try {
            task = type.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't create Ant task: " + type.getName());
        }

        task.setProject(project);
        task.setTaskName(type.getSimpleName());
        task.setLocation(Location.UNKNOWN_LOCATION);
        return task;
    }

    /**
     * Extracts a char resource from plugin to a file applying a token filter.
     */
    protected void extractCharResource(String fromResource, File to, ChainableReader filter) {
        int bufferSize = 8 * 1024;

        InputStream in = getClass().getClassLoader().getResourceAsStream(fromResource);

        if (in == null) {
            throw new BuildException("Resource not found: " + fromResource);
        }

        try {
            ensureParentDirExists(to);

            Reader reader = filter.chain(new InputStreamReader(in, "UTF-8"));

            char[] buffer = new char[bufferSize];
            int read;
            Writer out = new BufferedWriter(new FileWriter(to), bufferSize);

            try {

                while ((read = reader.read(buffer, 0, bufferSize)) >= 0) {
                    out.write(buffer, 0, read);
                }

                out.flush();

            } finally {
                try {
                    out.close();
                } catch (IOException ioex) {
                    // ignore
                }
            }

        } catch (IOException e) {
            throw new BuildException("Error copying resource " + fromResource, e);
        } finally {
            try {
                in.close();
            } catch (IOException ioex) {
                // ignore
            }
        }
    }

    /**
     * Extracts a binary resource from plugin to a file.
     */
    protected void extractBinResource(String fromResource, File to) {

        int bufferSize = 8 * 1024;

        InputStream in = getClass().getClassLoader().getResourceAsStream(fromResource);

        if (in == null) {
            throw new BuildException("Resource not found: " + fromResource);
        }

        ensureParentDirExists(to);

        try {
            in = new BufferedInputStream(in, bufferSize);

            byte[] buffer = new byte[bufferSize];
            int read;
            OutputStream out = new BufferedOutputStream(new FileOutputStream(to), bufferSize);

            try {

                while ((read = in.read(buffer, 0, bufferSize)) >= 0) {
                    out.write(buffer, 0, read);
                }

                out.flush();

            } finally {
                try {
                    out.close();
                } catch (IOException ioex) {
                    // ignore
                }
            }

        } catch (IOException e) {
            throw new BuildException("Error copying resource " + fromResource, e);
        } finally {
            try {
                in.close();
            } catch (IOException ioex) {
                // ignore
            }
        }
    }

    private void ensureParentDirExists(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new BuildException("Failed to create directory " + parent.getAbsolutePath());
        }
    }
}
