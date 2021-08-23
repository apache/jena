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

package org.apache.jena.rdflink;

import static org.apache.jena.riot.other.G.clear;
import static org.apache.jena.riot.other.G.copyGraphSrcToDst;

import java.util.Objects;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdfconnection.Isolation;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.DatasetGraphReadOnly;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecApp;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.UpdateExecDatasetBuilder;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.GraphReadOnly;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateRequest;

/**
 * Implement of {@link RDFLink} over a {@link Graph} in the same JVM.
 * <p>
 * Multiple levels of {@link Isolation} are provided, The default {@code COPY} level makes a local
 * {@link RDFLink} behave like a remote connection. This should be the normal use in
 * testing.
 * <ul>
 * <li>{@code COPY} &ndash; {@code Graph}s and {@code Dataset}s are copied.
 *     This is most like a remote connection.
 * <li>{@code READONLY} &ndash; Read-only wrappers are added but changes to
 *     the underlying graph or dataset will be seen.
 * <li>{@code NONE} (default) &ndash; Changes to the returned {@code Graph}s or {@code Dataset}s act on the original object.
 * </ul>
 */

public class RDFLinkDataset implements RDFLink {
    private ThreadLocal<Boolean> transactionActive = ThreadLocal.withInitial(()->false);

    private DatasetGraph dataset;
    private final Isolation isolation;

    private RDFLinkDataset(DatasetGraph dataset) {
        this(dataset, Isolation.NONE);
    }

    /*package*/ RDFLinkDataset(DatasetGraph dataset, Isolation isolation) {
        this.dataset = dataset;
        this.isolation = isolation;
    }

    @Override
    public QueryExec query(Query query) {
        checkOpen();
        //return QueryExec.newBuilder().dataset(dataset).query(query).build();
        // Delayed.
        return QueryExecApp.create(QueryExec.dataset(dataset).query(query),
                                   dataset,
                                   query,
                                   null);
    }

    @Override
    public QueryExecBuilder newQuery() {
        return QueryExec.dataset(dataset);
    }

    @Override
    public void update(UpdateRequest update) {
        checkOpen();
        Txn.executeWrite(dataset, ()->UpdateExecDatasetBuilder.create().update(update).execute(dataset));
    }

    @Override
    public void load(Node graphName, String file) {
        checkOpen();
        doPutPost(graphName, file, false);
    }

    @Override
    public void load(String file) {
        checkOpen();
        doPutPost(null, file, false);
    }

    @Override
    public void load(Node graphName, Graph graphSrc) {
        checkOpen();
        Txn.executeWrite(dataset, ()-> {
            Graph graphDst = graphFor(graphName);
            copyGraphSrcToDst(graphSrc, graphDst);
        });
    }

    @Override
    public void load(Graph graph) {
        load(null, graph);
    }

    @Override
    public Graph get(Node graphName) {
        return Txn.calculateRead(dataset, ()-> {
            Graph graph = graphFor(graphName);
            return isolate(graph);
        });
    }

    @Override
    public Graph get() {
        checkOpen();
        return get(null);
    }

    @Override
    public void put(String file) {
        checkOpen();
        doPutPost(null, file, true);
    }

    @Override
    public void put(Node graphName, String file) {
        checkOpen();
        doPutPost(graphName, file, true);
    }

    @Override
    public void put(Graph graph) {
        put(null, graph);
    }

    @Override
    public void put(Node graphName, Graph graph) {
        checkOpen();
        Txn.executeWrite(dataset, ()-> {
            Graph graphDst = graphFor(graphName);
            clear(graphDst);
            copyGraphSrcToDst(graph, graphDst);
        });
    }

    @Override
    public void delete(Node graphName) {
        checkOpen();
        Txn.executeWrite(dataset,() ->{
            if ( LibRDFLink.isDefault(graphName) )
                clear(dataset.getDefaultGraph());
            else
                clear(dataset.getGraph(graphName));
        });
    }

    @Override
    public void delete() {
        checkOpen();
        delete(null);
    }

    private void doPutPost(Node graphName, String file, boolean replace) {
        Objects.requireNonNull(file);
        Lang lang = RDFLanguages.filenameToLang(file);

        Txn.executeWrite(dataset,() ->{
            if ( RDFLanguages.isTriples(lang) ) {
                Graph graph = LibRDFLink.isDefault(graphName) ? dataset.getDefaultGraph() : dataset.getGraph(graphName);
                if ( replace )
                    clear(graph);
                RDFDataMgr.read(graph, file);
            }
            else if ( RDFLanguages.isQuads(lang) ) {
                if ( replace )
                    dataset.clear();
                // Try to POST to the dataset.
                RDFDataMgr.read(dataset, file);
            }
            else
                throw new ARQException("Not an RDF format: "+file+" (lang="+lang+")");
        });
    }

    /**
     * Called to isolate a graph from it's storage.
     * Must be inside a transaction.
     */
    private Graph isolate(Graph graph) {
        switch(isolation) {
            case COPY: {
                // Copy - the graph is completely isolated from the original.
                Graph graph2 = GraphFactory.createDefaultGraph();
                copyGraphSrcToDst(graph, graph2);
                return graph2;
            }
            case READONLY : {
                Graph graph2 = new GraphReadOnly(graph);
                return graph2;
            }
            case NONE :
                return graph;
        }
        throw new InternalErrorException();
    }

    /**
     * Called to isolate a dataset from it's storage.
     * Must be inside a transaction.
     */
    private DatasetGraph isolate(DatasetGraph dataset) {
        switch(isolation) {
            case COPY: {
                DatasetGraph dsg2 = DatasetGraphFactory.create();
                dataset.find().forEachRemaining(q -> dsg2.add(q) );
                return dsg2;
            }
            case READONLY : {
                DatasetGraph dsg = new DatasetGraphReadOnly(dataset);
                return dsg;
            }
            case NONE :
                return dataset;
        }
        throw new InternalErrorException();
    }

    private Graph graphFor(Node graphName) {
        if ( LibRDFLink.isDefault(graphName))
            return dataset.getDefaultGraph();
        return dataset.getGraph(graphName);
    }

    @Override
    public DatasetGraph getDataset() {
        checkOpen();
        return Txn.calculateRead(dataset,() -> isolate(dataset));
    }

    @Override
    public void loadDataset(String file) {
        checkOpen();
        Txn.executeWrite(dataset,() ->{
            RDFDataMgr.read(dataset, file);
        });
    }

    @Override
    public void loadDataset(DatasetGraph srcDataset) {
        checkOpen();
        srcDataset.executeRead(()->{
            dataset.executeWrite(()->{
                srcDataset.find().forEachRemaining(q->dataset.add(q));
            });
        });
    }

    @Override
    public void putDataset(String file) {
        checkOpen();
        Txn.executeWrite(dataset,() ->{
            dataset.clear();
            RDFDataMgr.read(dataset, file);
        });
    }

    @Override
    public void putDataset(DatasetGraph dataset) {
        checkOpen();
        Txn.executeWrite(dataset,() ->{
            this.dataset = isolate(dataset);
        });
    }

    @Override
    public void clearDataset() {
        checkOpen();
        Txn.executeWrite(dataset, dataset::clear);
    }

    @Override
    public void close() {
        dataset = null;
    }

    @Override
    public boolean isClosed() {
        return dataset == null;
    }

    private void checkOpen() {
        if ( dataset == null )
            throw new ARQException("closed");
    }

    @Override public void begin()                       { dataset.begin(); }
    @Override public void begin(TxnType txnType)        { dataset.begin(txnType); }
    @Override public void begin(ReadWrite mode)         { dataset.begin(mode); }
    @Override public boolean promote(Promote promote)   { return dataset.promote(promote); }
    @Override public void commit()                      { dataset.commit(); }
    @Override public void abort()                       { dataset.abort(); }
    @Override public boolean isInTransaction()          { return dataset.isInTransaction(); }
    @Override public void end()                         { dataset.end(); }
    @Override public ReadWrite transactionMode()        { return dataset.transactionMode(); }
    @Override public TxnType transactionType()          { return dataset.transactionType(); }
}
