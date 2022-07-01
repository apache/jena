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

import static org.apache.jena.sparql.util.graph.GraphUtils.getAsStringValue;
import static org.apache.jena.sparql.util.graph.GraphUtils.getResourceValue;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfs.RDFSFactory;
import org.apache.jena.rdfs.SetupRDFS;
import org.apache.jena.riot.RDFDataMgr;

public class GraphRDFSAssembler extends AssemblerBase implements Assembler {

    public static Resource getType() {
        return VocabRDFS.tGraphRDFS;
    }

    @Override
    public Object open(Assembler a, Resource root, Mode mode) {
        Graph graph = createGraph(a, root, mode) ;
        return ModelFactory.createModelForGraph(graph);
    }

    /**
     * <pre>
     * &lt;#rdfsGraph&gt; rdf:type ja:GraphRDFS ;
     *      ja:rdfs "vocab.ttl";
     *      ja:graph &lt;#baseGraph&gt; ;
     *      .
     *
     * &lt;#baseGraph&gt; rdf:type ja:MemoryModel ;
     *     ja:data "data1.trig";
     *     &lt;#baseGraph&gt; rdf:type ja:MemoryModel;
     *     ja:content [ja:externalContent &lt;data.ttl&gt; ];
     *     .
     * </pre>
     */
    public Graph createGraph(Assembler a, Resource root, Mode mode) {

        Resource graph = getResourceValue(root, VocabRDFS.pGraph) ;
        if ( graph == null )
            throw new AssemblerException(root, "Required base graph missing: "+VocabRDFS.tGraphRDFS) ;

        Model base = (Model)Assembler.general.open(graph);

        String schemaFile = getAsStringValue(root, VocabRDFS.pRdfsSchemaFile);
        if ( schemaFile == null )
            throw new AssemblerException(root, "Required property missing: "+VocabRDFS.pRdfsSchemaFile) ;

        Graph schema = RDFDataMgr.loadGraph(schemaFile);
        SetupRDFS setup = RDFSFactory.setupRDFS(schema);
        Graph graphRDFS = RDFSFactory.graphRDFS(base.getGraph(), setup);
        return graphRDFS;
    }
}
