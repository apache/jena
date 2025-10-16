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

package org.apache.jena.rdflink.dataset.assembler;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.JA;
import org.apache.jena.assembler.assemblers.AssemblerGroup;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.system.Vocab;

public class VocabAssemblerHTTP
{
    private static final String NS = JA.getURI();

    public static String getURI() { return NS; }

    // Types
    public static final Resource tDatasetHTTP        = Vocab.type(NS, "DatasetHTTP");

    // Properties
    public static final Property pUser               = Vocab.property(NS, "user");
    public static final Property pPass               = Vocab.property(NS, "pass");

    public static final Property pDestination        = Vocab.property(NS, "destination");
    public static final Property pQueryEndpoint      = Vocab.property(NS, "queryEndpoint");
    public static final Property pUpdateEndpoint     = Vocab.property(NS, "updateEndpoint");
    public static final Property pGspEndpoint        = Vocab.property(NS, "gspEndpoint");

    private static boolean initialized = false;

    static { init(); }

    static public synchronized void init() {
        if ( initialized )
            return;
        registerWith(Assembler.general());
        initialized = true;
    }

    static void registerWith(AssemblerGroup g) {
        // Wire in the assemblers.
        AssemblerUtils.registerAssembler(g, tDatasetHTTP, new DatasetAssemblerHTTP());
    }
}
