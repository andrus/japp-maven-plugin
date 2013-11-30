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

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

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
}
