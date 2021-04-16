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

package org.apache.jena.rdfs.assembler;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.JA;
import org.apache.jena.irix.IRIException;
import org.apache.jena.irix.IRIx;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab;
import org.apache.jena.sys.JenaSystem;

/** Vocabulary */
public class VocabRDFS {
    static { JenaSystem.init(); }

    public static final String NS = JA.getURI() ;
    public static String getURI() { return NS ; }

    public static final Resource tDatasetRDFS = resource("DatasetRDFS");
    public static final Resource tGraphRDFS = resource("GraphRDFS");
    public static final Resource tModelRDFS = resource("ModelRDFS");

    public static final Property pRdfsSchemaFile = property("rdfsSchema");
    public static final Property pDataset = DatasetAssemblerVocab.pDataset;
    public static final Property pGraph = DatasetAssemblerVocab.pGraph;

    private static boolean initialized = false ;

    static { init() ; }

    static synchronized public void init() {
        if ( initialized )
            return;
        initialized = true;
        AssemblerUtils.registerDataset(tDatasetRDFS, new DatasetRDFSAssembler());
        Assembler a = new GraphRDFSAssembler();
        AssemblerUtils.registerModel(tGraphRDFS, a);
        AssemblerUtils.registerModel(tModelRDFS, a);
    }

    private static Resource resource(String localname) { return ResourceFactory.createResource(iri(localname)); }
    private static Property property(String localname) { return ResourceFactory.createProperty(iri(localname)); }

    private static String iri(String localname) {
        String uri = NS + localname;
        try {
            IRIx iri = IRIx.create(uri);
            if ( ! iri.isReference() )
                throw new JenaException("Bad IRI (relative): "+uri);
            return uri;
        } catch (IRIException ex) {
            throw new JenaException("Bad IRI: "+uri);
        }
    }
}
