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

package org.apache.jena.fuseki.access;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.system.Vocab;

public class VocabSecurity {
    private static final String NS = "http://jena.apache.org/access#";

    public static String getURI() { return NS; }

    // Types
    public static final Resource tAccessControlledDataset = Vocab.type(NS, "AccessControlledDataset");
    public static final Resource tSecurityRegistry        = Vocab.type(NS, "SecurityRegistry");

    // Make subproperty of.
    public static final Property pDataset                 = Vocab.property(NS, "dataset");
    public static final Property pSecurityRegistry        = Vocab.property(NS, "registry");

    public static final Property pEntry                   = Vocab.property(NS, "entry");
    public static final Property pUser                    = Vocab.property(NS, "user");
    public static final Property pGraphs                  = Vocab.property(NS, "graphs");

    private static boolean initialized = false;

    static { init(); }

    static synchronized public void init() {
        if ( initialized )
            return;
        initialized = true;
        // AssemblerUtils.subProperty
        AssemblerUtils.registerDataset(tAccessControlledDataset, new AssemblerAccessDataset());
        AssemblerUtils.registerModel(tSecurityRegistry, new AssemblerSecurityRegistry());
    }
}
