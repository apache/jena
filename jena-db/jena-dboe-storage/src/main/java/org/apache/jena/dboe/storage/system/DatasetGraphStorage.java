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

package org.apache.jena.dboe.storage.system;

import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.dboe.storage.DatabaseRDF;
import org.apache.jena.dboe.storage.StoragePrefixes;
import org.apache.jena.dboe.storage.StorageRDF;
import org.apache.jena.dboe.transaction.txn.IteratorTxnTracker;
import org.apache.jena.dboe.transaction.txn.TransactionalSystem;
import org.apache.jena.dboe.transaction.txn.TxnId;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.*;

/** Alternative: DatasetGraph over RDFStorage, using DatasetGraphBaseFind
 *  Collapses DatasetGraphTriplesQuads into this adapter class.
<pre>
DatasetGraph
  DatasetGraphBase
    DatasetGraphBaseFind
      DatasetGraphStorageDirect
</pre>
/**
 * A DatasetGraph base class for triples+quads storage. The machinery is really
 * the splitting between default and named graphs. This happens in two classes,
 * {@link DatasetGraphBaseFind} (for find splitting) and
 * {@link DatasetGraphTriplesQuads} add/delete splitting (it inherits
 * {@link DatasetGraphBaseFind}).
 * <p>
 * Because storage is usually decomposing quads and triples, the default
 * behaviour is to work in s/p/o and g/s/p/o.
 */

public class DatasetGraphStorage extends DatasetGraphBaseFind implements DatabaseRDF
{
    private final Transactional txn;
    @Override public void begin()                       { txn.begin(); }
    @Override public void begin(TxnType txnType)        { txn.begin(txnType); }
    @Override public void begin(ReadWrite mode)         { txn.begin(mode); }
    @Override public boolean promote(Promote mode)      { return txn.promote(mode); }
    @Override public void commit()                      { txn.commit(); }
    @Override public void abort()                       { txn.abort(); }
    @Override public boolean isInTransaction()          { return txn.isInTransaction(); }
    @Override public void end()                         { txn.end(); }
    @Override public ReadWrite transactionMode()        { return txn.transactionMode(); }
    @Override public TxnType transactionType()          { return txn.transactionType(); }
    @Override public boolean supportsTransactions()     { return true; }
    @Override public boolean supportsTransactionAbort() { return false; }

    private final StorageRDF storage;
    private final StoragePrefixes prefixes;

    public DatasetGraphStorage(StorageRDF storage, StoragePrefixes prefixes, Transactional transactional) {
        this.storage = storage;
        this.prefixes = prefixes;
        this.txn = transactional;
    }

    @Override public StorageRDF getData()             { return storage; }
    @Override public StoragePrefixes getPrefixes()    { return prefixes; }
    @Override public Transactional getTransactional() { return txn; }

    @Override
    public Iterator<Node> listGraphNodes() {
        Iterator<Quad> iter = find(null, null, null, null);
        return Iter.iter(iter).map(Quad::getGraph).distinct();
    }

    private <T> Iterator<T> isolate(Iterator<T> iterator) {
        if ( txn.isInTransaction() && txn instanceof TransactionalSystem) {
            // Needs TxnId to track.
            TransactionalSystem txnSystem = (TransactionalSystem)txn;
            TxnId txnId = txnSystem.getThreadTransaction().getTxnId();
            // Add transaction protection.
            return new IteratorTxnTracker<>(iterator, txnSystem, txnId);
        }
        return Iter.iterator(iterator);
    }

    private Iterator<Triple> findStorage(Node s, Node p, Node o) {
        return isolate(storage.find(s, p, o));
    }

    private Iterator<Quad> findStorage(Node g, Node s, Node p, Node o) {
        return isolate(storage.find(g, s, p, o));
    }

    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o) {
        return Iter.map(findStorage(s, p, o), t -> Quad.create(Quad.defaultGraphIRI, t));
    }

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o) {
        return findStorage(g, s, p, o);
    }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o) {
        // Implementations may wish to do better.
        return findStorage(Node.ANY, s, p, o);
    }

    @Override
    public Graph getDefaultGraph() {
        return GraphViewStorage.createDefaultGraphStorage(this, prefixes);
    }

    @Override
    public Graph getUnionGraph() {
        return GraphViewStorage.createUnionGraphStorage(this, prefixes);
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return GraphViewStorage.createNamedGraphStorage(this,  graphNode, prefixes);
    }

    @Override
    public void add(Quad quad) {
        if ( Quad.isDefaultGraph(quad.getGraph()) )
            storage.add(quad.getSubject(), quad.getPredicate(), quad.getObject());
        else
            storage.add(quad);
    }

    @Override
    public void delete(Quad quad) {
        if ( Quad.isDefaultGraph(quad.getGraph()) )
            storage.delete(quad.getSubject(), quad.getPredicate(), quad.getObject());
        else
            storage.delete(quad);
    }

    @Override
    public void add(Node g, Node s, Node p, Node o) {
        if ( g == null || Quad.isDefaultGraph(g) )
            storage.add(s,p,o);
        else
            storage.add(g,s,p,o);
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        if ( g == null || Quad.isDefaultGraph(g) )
            storage.delete(s,p,o);
        else
            storage.delete(g,s,p,o);
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {
        graph.find(null,null,null).forEachRemaining(t->add(graphName, t.getSubject(), t.getPredicate(), t.getObject()));
    }

    @Override
    public void removeGraph(Node graphName) {
        storage.removeAll(graphName, null, null, null);
        prefixes.deleteAll(graphName);
    }
}
