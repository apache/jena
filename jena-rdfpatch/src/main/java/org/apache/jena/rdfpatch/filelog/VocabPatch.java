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

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab;
import org.apache.jena.system.Vocab;

public class VocabPatch {

    private static final String NS = "http://jena.apache.org/rdf-patch#";

    public static String getURI() { return NS ; }

    // Type
    public static final Resource tLoggedDataset = Vocab.type(getURI(), "LoggedDataset");

    // Add feature to another (sub) dataset.
    // This is ja:dataset.
    public static final Property pDataset       = Vocab.property(DatasetAssemblerVocab.getURI(), "dataset");

    // Name of the patch log.
    public static final Property pPatchLog      = Vocab.property(getURI(), "patchlog");

    /** Name of a file to append change logs to. */
    public static final Property pLogFile       = Vocab.property(getURI(), "log");

    /** Name of a file rotation policy . */
    public static final Property pLogPolicy     = Vocab.property(getURI(), "logPolicy");

    private static volatile boolean initialized = false ;

    static { init() ; }

    static synchronized public void init() {
        if ( initialized )
            return;
        initialized = true;
    }

    static {
        AssemblerUtils.registerDataset(tLoggedDataset, new AssemblerFileLog());
    }
}
