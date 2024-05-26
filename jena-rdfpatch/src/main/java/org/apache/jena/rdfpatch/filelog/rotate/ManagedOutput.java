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

package org.apache.jena.rdfpatch.filelog.rotate;

import java.io.OutputStream;
import java.nio.file.Path;

import org.apache.jena.rdfpatch.filelog.FilePolicy;
import org.apache.jena.rdfpatch.filelog.OutputMgr;

/** Interface to managed output streams.
 *
 * @see OutputMgr
 * @see FilePolicy
 */
public interface ManagedOutput {
    /** Get an OutputStream; use with try-resources or similar usage pattern.
     *  Closing the OutputStream returns it to the manager.
     */
    public OutputStream output();

    /** Current output stream, or null if there hasn't been one yet */
    public OutputStream currentOutput();

    /** The most recent output file name, only valid during an output section, else null. */
    public Path currentFilename();

    /** The latest output file name, or null if there hasn't been one yet */
    public Path latestFilename();

    /** Request file rotation */
    public void rotate();

    /** Get rotation engine */
    public Roller roller();
}
