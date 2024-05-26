/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.rdfpatch.filelog;

import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.jena.rdfpatch.filelog.rotate.ManagedOutput;
import org.apache.jena.rdfpatch.filelog.rotate.OutputFixed;
import org.apache.jena.rdfpatch.filelog.rotate.OutputManagedFile;

/** Create a managed output stream handler for a file. */
public class OutputMgr {

    /** Create a {@link ManagedOutput managed output stream} handler for a file.
     * The {@link FilePolicy} determines how the file is rotated which
     * may be automatic (such as by {@link FilePolicy#DATE})
     * or by external control ({@link ManagedOutput#rotate()})
     * @param directoryName
     * @param baseFilename
     * @param strategy
     * @return ManagedOutput
     */
    public static ManagedOutput create(String directoryName, String baseFilename, FilePolicy strategy) {
        Objects.requireNonNull(directoryName);
        Objects.requireNonNull(baseFilename);
        Objects.requireNonNull(strategy);
        return new OutputManagedFile(Paths.get(directoryName), baseFilename, strategy);
    }

    /** Create a {@link ManagedOutput managed output stream} handler for a file.
     * The {@link FilePolicy} determines how the file is rotated which
     * may be automatic (such as by {@link FilePolicy#DATE})
     * or by external control ({@link ManagedOutput#rotate()})
     * @param directory
     * @param baseFilename
     * @param strategy
     * @return ManagedOutput
     */
    public static ManagedOutput create(Path directory, String baseFilename, FilePolicy strategy) {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(baseFilename);
        Objects.requireNonNull(strategy);
        return new OutputManagedFile(directory, baseFilename, strategy);
    }

    /** Create a {@link ManagedOutput managed output stream} handler for a file.
     * The {@link FilePolicy} determines how the file is rotated which
     * may be automatic (such as by {@link FilePolicy#DATE})
     * or by external control ({@link ManagedOutput#rotate()})
     * @param pathName
     * @param strategy
     * @return ManagedOutput
     */
    public static ManagedOutput create(String pathName, FilePolicy strategy) {
        Objects.requireNonNull(pathName);
        Objects.requireNonNull(strategy);
        if ( pathName.equals("-") )
            return new OutputFixed(System.out);
        Path p = Paths.get(pathName).toAbsolutePath();
        return new OutputManagedFile(p.getParent(), p.getFileName().toString(), strategy);
    }

    /** Create a managed output stream handler for fixed {@link OutputStream}.
     * The {@code OutputStream} is fixed; there is no rotation policy.
     * This is an adapter from {@code OutputStream} to {@code ManagedOutput}.
     * @param outputStream
     * @return ManagedOutput
     */
    public static ManagedOutput create(OutputStream outputStream) {
        return new OutputFixed(outputStream);
    }
}
