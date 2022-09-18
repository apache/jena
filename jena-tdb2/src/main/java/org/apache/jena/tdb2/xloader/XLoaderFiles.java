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

package org.apache.jena.tdb2.xloader;

import java.nio.file.Path;
import java.util.Objects;

/** File setup */
public class XLoaderFiles {
    // Fixed file names in temporary directory
    static final String nameTriplesFile = "triples.tmp";
    static final String nameQuadsFile = "quads.tmp";
    static final String nameLoadInfo = "load.json";

    // Names.
    public final String TMPDIR;
    public final String triplesFile;
    public final String quadsFile;
    public final String loadInfo;
    public XLoaderFiles(String TMPDIR) {
        String ext = BulkLoaderX.CompressDataFiles ? ".gz" : "";

        this.TMPDIR = Objects.requireNonNull(TMPDIR);
        Path loc = Path.of(TMPDIR);
        triplesFile = loc.resolve(nameTriplesFile).toString()+ext;
        quadsFile = loc.resolve(nameQuadsFile).toString()+ext;
        loadInfo = loc.resolve(nameLoadInfo).toString();
    }
}