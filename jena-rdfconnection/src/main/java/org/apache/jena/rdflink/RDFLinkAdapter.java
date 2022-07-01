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

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphZero;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.update.UpdateRequest;

public class RDFLinkAdapter implements RDFLink {

    public static RDFLink adapt(RDFConnection conn) {
        if ( conn instanceof RDFConnectionAdapter )
            return ((RDFConnectionAdapter)conn).getLink();
        return new RDFLinkAdapter(conn);
    }

    private final RDFConnection conn ;
    // Class/subclass access. Ideally, call only once per method (for swappable links).
    protected RDFConnection getConnection() { return conn; }

    public RDFLinkAdapter(RDFConnection conn) {
        this.conn = conn;
    }

    @Override
    public void begin(TxnType type) { conn.begin(type); }

    @Override
    public void begin(ReadWrite readWrite) { conn.begin(readWrite); }

    @Override
    public boolean promote(Promote mode) { return conn.promote(mode) ; }

    @Override
    public void commit() { conn.commit(); }

    @Override
    public void abort() { conn.abort(); }

    @Override
    public void end() { conn.end(); }

    @Override
    public ReadWrite transactionMode() { return conn.transactionMode(); }

    @Override
    public TxnType transactionType() { return conn.transactionType(); }

    @Override
    public boolean isInTransaction() { return conn.isInTransaction(); }

    @Override
    public DatasetGraph getDataset() { return conn.fetchDataset().asDatasetGraph(); }

    @Override
    public QueryExec query(Query query) { return QueryExec.adapt(conn.query(query)); }

    @Override
    public QueryExec query(String queryString) { return QueryExec.adapt(conn.query(queryString)); }

    @Override
    public QueryExecBuilder newQuery() {
        // Can't adapt a previously wrapped RDFLink via RDFConnectionAdapter
        throw new UnsupportedOperationException("RDFLinkAdapter.newQuery");
    }

    @Override
    public UpdateExecBuilder newUpdate() {
        // Can't adapt a previously wrapped RDFLink via RDFConnectionAdapter
        throw new UnsupportedOperationException("RDFLinkAdapter.newUpdate");
    }

    @Override
    public void update(UpdateRequest update) {
        conn.update(update);
    }

    @Override
    public void update(String update) {
        conn.update(update);
    }

    @Override
    public Graph get() {
        Model m = conn.fetch();
        if ( m == null ) return null;
        return m.getGraph();
    }

    private static String uri(Node node) {
        return ( node == null ) ? null : node.getURI();
    }

    @Override
    public Graph get(Node graphName) {
        Model m = conn.fetch(uri(graphName));
        if ( m == null ) return null;
        return m.getGraph();
    }

    @Override
    public void load(String file) { conn.load(file); }

    @Override
    public void load(Node graphName, String file) {
        conn.load(uri(graphName), file);
    }

    @Override
    public void load(Graph graph) {
        conn.load(ModelFactory.createModelForGraph(graph));
    }

    @Override
    public void load(Node graphName, Graph graph) {
        conn.load(uri(graphName), ModelFactory.createModelForGraph(graph));
    }

    @Override
    public void put(String file) { conn.put(file); }

    @Override
    public void put(Node graphName, String file) {
        conn.put(uri(graphName), file);
    }

    @Override
    public void put(Graph graph) {
        conn.put(ModelFactory.createModelForGraph(graph));
    }

    @Override
    public void put(Node graphName, Graph graph) {
        conn.put(uri(graphName), ModelFactory.createModelForGraph(graph));
    }

    @Override
    public void delete(Node graphName) { conn.delete(uri(graphName)); }

    @Override
    public void delete() { conn.delete(); }

    @Override
    public void loadDataset(String file) { conn.loadDataset(file); }

    @Override
    public void loadDataset(DatasetGraph dataset) { conn.loadDataset(DatasetFactory.wrap(dataset)); }

    @Override
    public void putDataset(String file) { conn.putDataset(file); }

    @Override
    public void putDataset(DatasetGraph dataset) { conn.putDataset(DatasetFactory.wrap(dataset)); }

    @Override
    public void clearDataset() { DatasetFactory.wrap(DatasetGraphZero.create()); }

    @Override
    public boolean isClosed() { return conn.isClosed(); }

    @Override
    public void close() { conn.close(); }
}
