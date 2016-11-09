/*
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
 
package projects.dsg2;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.* ;
import projects.dsg2.storage.StorageRDF ;

/** DatasetGraph over RDFStoreage, using DatasetGraphBaseFind.
<pre>
DatasetGraph
  DatasetGraphBase
    DatasetGraphBaseFind
      DatasetGraphTriplesQuads
        DatasetGraphStorage
</pre>
 * <p>
/**
 * A DatasetGraph base class for triples+quads storage. The machinary is really
 * the spliting between default and named graphs. This happens in two classes,
 * {@link DatasetGraphBaseFind} (for find splitting) and 
 * {@link DatasetGraphTriplesQuads} add/delete splitting (it inherits
 * {@link DatasetGraphBaseFind}).
 * <p>
 * Because storage is usually decomposing quads and triples, the default
 * behaviour is to work in s/p/o and g/s/p/o.  
 */

public class DatasetGraphStorage extends DatasetGraphTriplesQuads
{
    // Temporary fill-in
    private final Transactional txn;
    @Override public void begin(ReadWrite mode)         { txn.begin(mode) ; }
    @Override public void commit()                      { txn.commit() ; }
    @Override public void abort()                       { txn.abort() ; }
    @Override public boolean isInTransaction()          { return txn.isInTransaction() ; }
    @Override public void end()                         { txn.end(); }
    @Override public boolean supportsTransactions()     { return true ; }
    @Override public boolean supportsTransactionAbort() { return false ; }
    
    private final StorageRDF storage ;
    
    public DatasetGraphStorage(StorageRDF storage) {
        this(storage, TransactionalLock.createMRSW());
    }
    
    public DatasetGraphStorage(StorageRDF storage, Transactional txn) {
        this.storage = storage ;
        this.txn = txn;
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        Iterator<Quad> iter = findInAnyNamedGraphs(null, null, null);
        return Iter.iter(iter).map(Quad::getGraph).distinct() ;
    }

    @Override
    protected void addToDftGraph(Node s, Node p, Node o) {
        storage.add(s, p, o);
    }
    
    @Override
    protected void addToNamedGraph(Node g, Node s, Node p, Node o) {
        storage.add(g, s, p, o);
    }
    
    @Override
    protected void deleteFromDftGraph(Node s, Node p, Node o) {
        storage.delete(s, p, o);
    }
    
    @Override
    protected void deleteFromNamedGraph(Node g, Node s, Node p, Node o) {
        storage.delete(g, s, p, o);
    }
    
    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o) {
        return storage.find(s, p, o).map(t -> Quad.create(Quad.defaultGraphIRI, t)).iterator() ;
    }

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o) {
        return storage.find(g, s, p, o).iterator() ;
    }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o) {
        return storage.find(Node.ANY, s, p, o).iterator() ;
    }

    @Override
    public Graph getDefaultGraph() {
        return GraphView.createDefaultGraph(this) ;
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return GraphView.createNamedGraph(this, graphNode) ;
    }

//    @Override
//    public void addGraph(Node graphName, Graph graph) {
//        graph.find(null,null,null).forEachRemaining(t->add(graphName, t.getSubject(), t.getPredicate(), t.getObject())) ;
//    }

    @Override
    public void removeGraph(Node graphName) {
        storage.removeAll(graphName, null, null, null) ;
        // Prefixes.
    }
}
