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

package org.apache.jena.rdflink.dataset;

import static org.apache.jena.query.ReadWrite.WRITE;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.Prefixes;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.DatasetGraphBaseFind;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalNull;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.dispatch.SparqlDispatcherRegistry;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.modify.request.QuadDataAcc;
import org.apache.jena.sparql.modify.request.UpdateDataDelete;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.sparql.modify.request.UpdateDeleteWhere;
import org.apache.jena.sparql.modify.request.UpdateDrop;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;

/**
 * DatasetGraph implementation that implements all methods
 * against an RDFLink.
 * All returned iterators are backed by a fresh RDFLink instance.
 * The iterators must be closed to free the resources.
 */
public abstract class DatasetGraphOverRDFLink
    extends DatasetGraphBaseFind
{
    private PrefixMap prefixes = PrefixMapFactory.create();

    /** Batch size for inserting via SPARQL INSERT. */
    private int bufferSize = StreamRDFToRDFLink.DFT_BUFFER_SIZE;

    private Transactional transactional = TransactionalNull.create();

    /** This method must be implemented. */
    public abstract RDFLink newLink();

    public DatasetGraphOverRDFLink() {
        initContext();
    }

    protected PrefixMap getPrefixes() {
        return prefixes;
    }

    protected Transactional getTransactional() {
        return transactional;
    }

    protected int getInsertBatchSize() {
        return bufferSize;
    }

    protected void initContext() {
        Context cxt = getContext();
        // Use the context to advertise that SPARQL statements should not be parsed.
        SparqlDispatcherRegistry.setParseCheck(cxt, false);
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        RDFLink link = newLink();
        QueryExec qExec = link.query(graphsQuery);
        return Iter.onClose(Iter.onClose(
            Iter.map(qExec.select(), b -> b.get(vg)),
            qExec::close), link::close);
    }

    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o) {
        RDFLink link = newLink();
        Iterator<Triple> base = findTriples(link::query, s, p, o);
        Iterator<Quad> result = Iter.onClose(
            Iter.map(base, t -> Quad.create(Quad.defaultGraphIRI, t)),
            link::close);
        return result;
    }

    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o) {
        RDFLink link = newLink();
        Iterator<Quad> result = Iter.onClose(findQuads(link::query, g, s, p, o), link::close);
        return result;
    }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o) {
        RDFLink link = newLink();
        Iterator<Quad> result = Iter.onClose(findQuads(link::query, Node.ANY, s, p, o), link::close);
        return result;
    }

    @Override
    public Graph getDefaultGraph() {
        return GraphView.createDefaultGraph(this);
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return GraphView.createNamedGraph(this, graphNode);
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {
        try (RDFLink link = newLink()) {
            StreamRDF sink = new StreamRDFToRDFLink(link, Prefixes.adapt(getPrefixes()), getInsertBatchSize());
            sink.start();
            StreamRDFToRDFLink.sendGraphTriplesToStream(graph, graphName, sink);
            sink.finish();
        }
    }

    @Override
    public void removeGraph(Node graphName) {
        Objects.requireNonNull(graphName);
        UpdateRequest ur = new UpdateRequest(new UpdateDrop(graphName));
        try (RDFLink link = newLink()) {
            link.update(ur);
        }
    }

    @Override
    public void add(Quad quad) {
        if (!quad.isConcrete()) {
            throw new IllegalArgumentException("Concrete quad expected.");
        }
        Update update = new UpdateDataInsert(new QuadDataAcc(List.of(quad)));
        try (RDFLink link = newLink()) {
            link.update(update);
        }
    }

    @Override
    public void delete(Quad quad) {
        if (!quad.isConcrete()) {
            throw new IllegalArgumentException("Concrete quad expected.");
        }
        Update update = new UpdateDataDelete(new QuadDataAcc(List.of(quad)));
        try (RDFLink link = newLink()) {
            link.update(update);
        }
    }

    @Override
    public void deleteAny(Node g, Node s, Node p, Node o) {
        Quad quad = matchQuad(Quad.create(g, s, p, o));
        Update update = quad.isConcrete()
            ? new UpdateDataDelete(new QuadDataAcc(List.of(quad)))
            : new UpdateDeleteWhere(new QuadAcc(List.of(quad)));
        try (RDFLink link = newLink()) {
            link.update(update);
        }
    }

    @Override
    public boolean supportsTransactions() {
        return false;
    }

    @Override
    public void abort() {
        getTransactional().abort();
    }

    @Override
    public void begin(ReadWrite readWrite) {
        getTransactional().begin(readWrite);
    }

    @Override
    public void commit() {
        getTransactional().commit();
    }

    @Override
    public void end() {
        // AbstractTestRDFConnection.transaction_bad_01() expects
        // a JenaTransactionException to be thrown if the
        // following conditions are met.

        // getTransactional().end();
        if (isInTransaction()) {
            if (transactionMode().equals(WRITE)) {
                String msg = "end() called for WRITE transaction without commit or abort having been called. This causes a forced abort.";
                throw new JenaTransactionException(msg);
            }
        }
    }

    @Override
    public boolean isInTransaction() {
        return getTransactional().isInTransaction();
    }

    @Override
    public void begin(TxnType type) {
        getTransactional().begin(type);
    }

    @Override
    public boolean promote(Promote mode) {
        return getTransactional().promote(mode);
    }

    @Override
    public ReadWrite transactionMode() {
        return getTransactional().transactionMode();
    }

    @Override
    public TxnType transactionType() {
        return getTransactional().transactionType();
    }

    @Override
    public PrefixMap prefixes() {
        return prefixes;
    }

    private static final Var vg = Var.alloc("g");
    private static final Var vs = Var.alloc("s");
    private static final Var vp = Var.alloc("p");
    private static final Var vo = Var.alloc("o");
    private static final Query graphsQuery = QueryFactory.create("SELECT ?g { GRAPH ?g { } }");

    private static IteratorCloseable<Triple> findTriples(Function<Query, ? extends QueryExec> executor, Node s, Node p, Node o) {
        Triple triple = matchTriple(Triple.create(s, p, o));
        Query query = createQueryTriple(triple);
        QueryExec qExec = executor.apply(query);
        return Iter.onClose(
                Iter.map(qExec.select(), b -> Substitute.substitute(triple, b)),
                qExec::close);
    }

    private static IteratorCloseable<Quad> findQuads(Function<Query, ? extends QueryExec> executor, Node g, Node s, Node p, Node o) {
        Quad quad = matchQuad(Quad.create(g, s, p, o));
        Query query = createQueryQuad(quad);
        QueryExec qExec = executor.apply(query);
        return Iter.onClose(
            Iter.map(qExec.select(), b -> Substitute.substitute(quad, b)),
            qExec::close);
    }

    private static Node matchNode(Node n, Node d) {
        return n == null || n.equals(Node.ANY) ? d : n;
    }

    private static Triple matchTriple(Triple triple) {
        return Triple.create(
            matchNode(triple.getSubject(), vs),
            matchNode(triple.getPredicate(), vp),
            matchNode(triple.getObject(), vo));
    }

    private static Quad matchQuad(Quad quad) {
        return Quad.create(
            matchNode(quad.getGraph(), vg),
            matchTriple(quad.asTriple()));
    }

    private static Query createQueryQuad(Quad quad) {
        Query query = new Query();
        query.setQuerySelectType();
        query.setQueryResultStar(true);

        BasicPattern bgp = new BasicPattern();
        bgp.add(quad.asTriple());

        Element element = new ElementTriplesBlock(bgp);
        element = new ElementNamedGraph(quad.getGraph(), element);

        query.setQueryPattern(element);
        return query;
    }

    private static Query createQueryTriple(Triple m) {
        Query query = new Query();
        query.setQuerySelectType();
        query.setQueryResultStar(true);

        BasicPattern bgp = new BasicPattern();
        bgp.add(m);

        Element element = new ElementTriplesBlock(bgp);
        query.setQueryPattern(element);
        return query;
    }
}
