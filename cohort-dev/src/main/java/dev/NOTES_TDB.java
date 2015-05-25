/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package dev;

import java.util.Iterator ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.core.DatasetGraphTriplesQuads ;
import org.apache.jena.sparql.core.Quad ;


public class NOTES_TDB {
    
    // ** NodeTableCache and aborts.
    // NodeTableCache + abort -> clean out?
    // Abort notification.
    
    // ** DatasetGraphTDB extends TransactionalBase / drop DatasetGraphCaching 
    //   Look to use default methods in the DatasetGraph stack
    //   Or very basic interface for DatasetGraphTDB (not a datasetGraph!) 
    //     Interface StorageRDF - Triples and Quads, minimal. Plug-in. Prefixes?
    //     and wrap to add DatasetGraph-ness 
    // 1/ Transaction state changes notification API (TripleTable, QuadTable, Prefixes -> NodeTables) 
    //   --> delete DatasetGraphTrackActive long term.
    // 2/ addGraph, removeGraph -> default methods (more default methods? DatasetGraphBaseFind
    // 3/ implements Jena Transactional (!!)  
    
    // Move the DSG hierarchy from Jena? as "experimental"?
    
    // DatasetGraphTDB has begin/commit/abort/end --> Not used?
    
    // Quack clean / split into general and TDB
    
    // DatasetGraph.exec(op)
    //   Interface ExecuteOp + generic registration.
    // DatasetGraph.getBaseDatasetGraph
    
    // ++ DatasetGraphTriplesQuads
}

interface StorageRDF {
    void add(Triple triple) ; // addToDftGraph -- DatasetGraphTriplesQuads
    void addTriple(Node s, Node p, Node o) ; // addToDftGraph -- DatasetGraphTriplesQuads
    
    void add(Quad quad) ;     // addToNamedGraph
    void addQuad(Node g, Node s, Node p, Node o) ;
    
    void delete(Triple triple) ;
    void delete(Quad quad) ;
    Iterator<Triple> find(Node s, Node p, Node o) ;
    Iterator<Quad> find(Node g, Node s, Node p, Node o) ;
}

class Foo extends DatasetGraphTriplesQuads {

    @Override
    public Iterator<Node> listGraphNodes() {
        return null ;
    }

    @Override
    protected void addToDftGraph(Node s, Node p, Node o) {}

    @Override
    protected void addToNamedGraph(Node g, Node s, Node p, Node o) {}

    @Override
    protected void deleteFromDftGraph(Node s, Node p, Node o) {}

    @Override
    protected void deleteFromNamedGraph(Node g, Node s, Node p, Node o) {}

    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o) {
        return null ;
    }

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o) {
        return null ;
    }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o) {
        return null ;
    }

    @Override
    public Graph getDefaultGraph() {
        return null ;
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return null ;
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {}

    @Override
    public void removeGraph(Node graphName) {}
    
}
