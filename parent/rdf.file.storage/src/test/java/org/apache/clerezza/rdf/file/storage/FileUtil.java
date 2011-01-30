/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.clerezza.rdf.file.storage;

import java.io.File;

/**
 *
 * @author mir
 */
public class FileUtil {

     /**
     * Sets up a new <code>SesameMGraph</code>.
     * @param testName  Name of the test to support.
     * @throws RepositoryException  If it failed to activate the graph.
     */
    public static void setUpEmptyDirectory(String directory) {
        final File dataDir= new File(directory);
        dataDir.mkdirs();
        cleanDirectory(dataDir);
    }


    /**
     * Cleans the content of the specified directory recursively.
     * @param dir  Abstract path denoting the directory to clean.
     */
    public static void cleanDirectory(File dir) {
        File[] files= dir.listFiles();
        if (files!=null && files.length>0) {
            for (File file: files) {
                delete(file);
            }
        }
    }

    /**
     * Deletes the specified file or directory.
     * @param file  Abstract path denoting the file or directory to clean.
     */
    public static void delete(File file) {
        if (file.isDirectory()) {
            cleanDirectory(file);
        }
        file.delete();
    }

}
