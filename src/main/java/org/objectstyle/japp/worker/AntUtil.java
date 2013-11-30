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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.tools.ant.BuildException;

class AntUtil {

    public static void copy(String fromResource, File to) {

        int bufferSize = 8 * 1024;

        ensureParentDirExists(to);

        InputStream in = AntUtil.class.getClassLoader().getResourceAsStream(fromResource);

        if (in == null) {
            throw new BuildException("Resource not found: " + fromResource);
        }

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

    private static void ensureParentDirExists(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new BuildException("Failed to create directory " + parent.getAbsolutePath());
        }
    }

    private AntUtil() {
    }

}
