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

package org.apache.jena.tdb1.assembler;

import org.apache.jena.rdf.model.Property ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.sparql.core.assembler.AssemblerUtils ;
import org.apache.jena.system.Vocab;
import org.apache.jena.tdb1.TDB1;

/** @deprecated Migrate to TDB2. */
@Deprecated(forRemoval = true)
public class VocabTDB1
{
    private static final String NS = TDB1.namespace ;

    public static String getURI() { return NS ; }

    // Types
    public static final Resource tDatasetTDB        = Vocab.type(NS, "DatasetTDB") ;
    public static final Resource tGraphTDB          = Vocab.type(NS, "GraphTDB") ;

    // Alternative type names.
    public static final Resource tDatasetTDB1       = Vocab.type(NS, "DatasetTDB1") ;
    public static final Resource tGraphTDB1         = Vocab.type(NS, "GraphTDB1") ;

    public static final Resource tNodeTable         = Vocab.type(NS, "NodeTable") ;

    public static final Property pLocation          = Vocab.property(NS, "location") ;
    public static final Property pUnionDefaultGraph = Vocab.property(NS, "unionDefaultGraph") ;

    public static final Property pIndex             = Vocab.property(NS, "index") ;
    public static final Property pGraphName1        = Vocab.property(NS, "graphName") ;
    public static final Property pGraphName2        = Vocab.property(NS, "namedGraph") ;
    public static final Property pDataset           = Vocab.property(NS, "dataset") ;

    public static final Property pNodes             = Vocab.property(NS, "nodes") ;

    // Indexes
    public static final Property pDescription       = Vocab.property(getURI(), "description") ;
    public static final Property pFile              = Vocab.property(getURI(), "file") ;

//    // Nodes
//    public static final Property pNodeIndex         = Vocab.property(getURI(), "nodeIndex") ;
//    public static final Property pNodeData          = Vocab.property(getURI(), "nodeData") ;

    // Setting
    public static final Property pSetting           = Vocab.property(getURI(), "setting") ;
    public static final Property pName              = Vocab.property(getURI(), "name") ;
    public static final Property pValue             = Vocab.property(getURI(), "value") ;

    private static boolean initialized = false ;

    static { init() ; }

    static synchronized public void init() {
        if ( initialized )
            return;
        initialized = true;
        AssemblerUtils.registerDataset(tDatasetTDB,     new DatasetAssemblerTDB1());
        AssemblerUtils.registerDataset(tDatasetTDB1,    new DatasetAssemblerTDB1());
        AssemblerUtils.registerModel(tGraphTDB,         new TDB1GraphAssembler());
        AssemblerUtils.registerModel(tGraphTDB1,        new TDB1GraphAssembler());
    }
}
