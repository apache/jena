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
package org.apache.jena.hadoop.rdf.io.input.bnodes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.graph.GraphFactory ;

/**
 *
 */
public abstract class AbstractTripleBlankNodeTests extends AbstractBlankNodeTests<Triple, TripleWritable> {
    
    /**
     * Gets the language to use
     * 
     * @return Language
     */
    protected abstract Lang getLanguage();

    @Override
    protected Triple createTuple(Node s, Node p, Node o) {
        return new Triple(s, p, o);
    }

    @Override
    protected void writeTuples(File f, List<Triple> tuples) throws FileNotFoundException {
        Graph g = GraphFactory.createGraphMem();
        for (Triple t : tuples) {
            g.add(t);
        }
        RDFDataMgr.write(new FileOutputStream(f), g, getLanguage());
    }

    @Override
    protected Node getSubject(Triple value) {
        return value.getSubject();
    }

}
