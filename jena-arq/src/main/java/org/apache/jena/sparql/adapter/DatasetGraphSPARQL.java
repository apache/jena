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

package org.apache.jena.sparql.adapter;

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
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.Prefixes;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.DatasetGraphBase;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalNull;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.modify.request.QuadDataAcc;
import org.apache.jena.sparql.modify.request.Target;
import org.apache.jena.sparql.modify.request.UpdateClear;
import org.apache.jena.sparql.modify.request.UpdateDataDelete;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.sparql.modify.request.UpdateDeleteWhere;
import org.apache.jena.sparql.modify.request.UpdateDrop;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;

/**
 * This class provides a base implementation of the Jena DatasetGraph interface
 * by means of SPARQL statement executions. Efficiency not guaranteed.
 *
 * <p>
 * This class also serves as a base for custom DatasetGraph implementations.
 * For such custom dataset graphs, specialized {@link SparqlAdapterProvider} implementations
 * can be created that deliver improved SPARQL update and query performance over direct use of the
 * DatasetGraph API.
 *
 * <p>
 * All returned iterators must be closed to free any allocated resources.
 *
 * <p>
 * This base class does not support transactions.
 *
 * <p>
 * All inserts are passed on as SPARQL update requests.
 * Beware that blank nodes are likely to become renamed across separate requests.
 *
 * <p>
 * All method invocations build single, possibly compound, update request.
 */
public abstract class DatasetGraphSPARQL
    extends DatasetGraphBase
{
    private PrefixMap prefixes = PrefixMapFactory.create();
    private Transactional transactional = TransactionalNull.create();

    public DatasetGraphSPARQL() {
        super();
        initContext();
    }

    protected PrefixMap getPrefixes() {
        return prefixes;
    }

    protected Transactional getTransactional() {
        return transactional;
    }

    protected void initContext() {
        Context cxt = getContext();
        // Use the context to advertise that SPARQL statements should not be parsed.
        ParseCheckUtils.setParseCheck(cxt, false);
    }

    protected abstract QueryExec query(Query query);
    protected abstract UpdateExec update(UpdateRequest UpdateRequest);

    protected void execUpdate(Update update) {
        execUpdate(new UpdateRequest(update));
    }

    protected void execUpdate(UpdateRequest updateRequest) {
        UpdateExec uExec = update(updateRequest);
        uExec.execute();
    }

    /**
     * This method must return a StreamRDF instance that handles bulk inserts of RDF tuples (triples or quads).
     * The default implementation transfers all data as a unit.
     * Alternative implementations could e.g. flush every 1000 triples or by the string length of the update request.
     */
    protected StreamRDF newUpdateSink() {
        StreamRDF sink = new StreamRDFToUpdateRequest(this::execUpdate, Prefixes.adapt(getPrefixes()), Integer.MAX_VALUE);
        return sink;
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        QueryExec qExec = query(graphsQuery);
        return Iter.onClose(
            Iter.map(qExec.select(), b -> b.get(vg)),
            qExec::close);
    }

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        Iterator<Quad> result;
        if (g == null || Node.ANY.equals(g)) {
            result = findTriplesOrQuads(this::query, s, p, o);
        } else if (Quad.isDefaultGraph(g)) {
            Iterator<Triple> base = findTriples(this::query, s, p, o);
            result = Iter.map(base, t -> Quad.create(Quad.defaultGraphIRI, t));
        } else {
            result = findQuads(this::query, g, s, p, o);
        }
        return result;
    }

    @Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
        Iterator<Quad> result = findQuads(this::query, g, s, p, o);
        return result;
    }

    @Override
    public Graph getDefaultGraph() {
        DatasetGraphSPARQL self = this;
        return new GraphView(this, Quad.defaultGraphNodeGenerated) {
            @Override
            protected int graphBaseSize() {
                long size = sizeLong();
                return (size < Integer.MAX_VALUE) ? (int)size : Integer.MAX_VALUE;
            }

            @Override
            public long sizeLong() {
                long result = fetchLong(self::query, defaultGraphSizeQuery, vc);
                return result;
            }
        };
    }

    @Override
    public Graph getGraph(Node graphNode) {
        DatasetGraphSPARQL self = this;
        return new GraphView(this, graphNode) {
            @Override
            protected int graphBaseSize() {
                long size = sizeLong();
                return (size < Integer.MAX_VALUE) ? (int)size : Integer.MAX_VALUE;
            }

            @Override
            public long sizeLong() {
                Query q = createQueryNamedGraphSize(graphNode, vc);
                long result = fetchLong(self::query, q, vc);
                return result;
            }
        };
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {
        StreamRDF sink = newUpdateSink();
        try {
            sink.start();
            StreamRDFToUpdateRequest.sendGraphTriplesToStream(graph, graphName, sink);
        } finally {
            sink.finish();
        }
    }

    @Override
    public void removeGraph(Node graphName) {
        Objects.requireNonNull(graphName);
        delete(graphName, Node.ANY, Node.ANY, Node.ANY);
        // UpdateRequest ur = new UpdateRequest(new UpdateDrop(graphName, true));
        // execUpdate(ur);
    }

    @Override
    public void add(Quad quad) {
        Quad q = harmonizeTripleInQuad(quad);
        if (!q.isConcrete()) {
            throw new IllegalArgumentException("Concrete quad expected.");
        }
        Update update = new UpdateDataInsert(new QuadDataAcc(List.of(q)));
        execUpdate(new UpdateRequest(update));
    }

    @Override
    public void delete(Quad quad) {
        Quad q = harmonizeTripleInQuad(quad);
        if (!q.isConcrete()) {
            throw new IllegalArgumentException("Concrete quad expected.");
        }
        Update update = new UpdateDataDelete(new QuadDataAcc(List.of(q)));
        execUpdate(update);
    }

    @Override
    public void deleteAny(Node g, Node s, Node p, Node o) {
        boolean allowDrop = true;
        UpdateRequest updateRequest;
        if (allowDrop && isWildcard(s) && isWildcard(p) && isWildcard(o)) {
            updateRequest = new UpdateRequest(buildDeleteByGraph(g));
        } else {
            updateRequest = buildDeleteByPattern(g, s, p, o);
        }
        execUpdate(updateRequest);
    }

    @Override
    public long size() {
        long result = fetchLong(this::query, graphsCountQuery, vc);
        return result;
    }

    @Override
    public boolean supportsTransactions() {
        return false;
    }

    @Override
    public boolean supportsTransactionAbort() {
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
        // Note: AbstractTestRDFConnection.transaction_bad_01() expects
        //   a JenaTransactionException to be thrown if the
        //   conditions of the if-statement below are satisfied.
        try {
            if (isInTransaction()) {
                if (transactionMode().equals(WRITE)) {
                    String msg = "end() called for WRITE transaction without commit or abort having been called. This causes a forced abort.";
                    throw new JenaTransactionException(msg);
                }
            }
        } finally {
            getTransactional().end();
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

    // ----- SPARQL Statement Generation -----

    private static final Var vg = Var.alloc("g");
    private static final Var vs = Var.alloc("s");
    private static final Var vp = Var.alloc("p");
    private static final Var vo = Var.alloc("o");
    private static final Query graphsQuery = QueryFactory.create("SELECT ?g { GRAPH ?g { } }");

    private static final Var vc = Var.alloc("c");
    private static final Query graphsCountQuery = QueryFactory.create("SELECT (COUNT(*) AS ?c) { GRAPH ?g { } }");

    private static final Query defaultGraphSizeQuery = QueryFactory.create("SELECT (COUNT(*) AS ?c) { ?s ?p ?o }");

    private static IteratorCloseable<Triple> findTriples(Function<Query, ? extends QueryExec> executor, Node s, Node p, Node o) {
        Triple triple = matchTriple(Triple.create(s, p, o));
        Query query = createQueryTriple(triple);
        QueryExec qExec = executor.apply(query);
        return Iter.onClose(
                Iter.map(qExec.select(), b -> Substitute.substitute(triple, b)),
                qExec::close);
    }

    private static IteratorCloseable<Quad> findQuads(Function<Query, ? extends QueryExec> executor, Node g, Node s, Node p, Node o) {
        Quad quad = matchQuad(g, s, p, o);
        Query query = createQueryQuad(quad);
        QueryExec qExec = executor.apply(query);
        return Iter.onClose(
            Iter.map(qExec.select(), b -> Substitute.substitute(quad, b)),
            qExec::close);
    }

    private static IteratorCloseable<Quad> findTriplesOrQuads(Function<Query, ? extends QueryExec> executor, Node s, Node p, Node o) {
        Quad quad = matchQuad(vg, s, p, o);
        Query query = createQueryTriplesAndQuads(s, p, o);
        QueryExec qExec = executor.apply(query);
        return Iter.onClose(
            Iter.map(qExec.select(), b -> {
                if (!b.contains(vg)) {
                    // Unbound graph variable -> default graph.
                    b = BindingFactory.binding(b, vg, Quad.defaultGraphIRI);
                }
                return Substitute.substitute(quad, b);
            }),
            qExec::close);
    }

    private static long fetchLong(Function<Query, ? extends QueryExec> executor, Query query, Var numberVar) {
        long result;
        try (QueryExec qExec = executor.apply(query)) {
            Binding b = qExec.select().next();
            Number number = (Number)b.get(numberVar).getLiteralValue();
            result = number.longValue();
        }
        return result;
    }

    private static Node matchNode(Node n, Node d) {
        return n == null || n.equals(Node.ANY) ? d : n;
    }

    private static Triple matchTriple(Triple triple) {
        return matchTriple(triple.getSubject(), triple.getPredicate(), triple.getObject());
    }

    private static Triple matchTriple(Node s, Node p, Node o) {
        return Triple.create(matchNode(s, vs), matchNode(p, vp), matchNode(o, vo));
    }

    private static Quad harmonizeTripleInQuad(Quad quad) {
        Quad result = quad.isTriple() ? new Quad(Quad.defaultGraphIRI, quad.asTriple()) : quad;
        return result;
    }

    private static Quad matchQuad(Node g, Node s, Node p, Node o) {
        return Quad.create(matchNode(g, vg), matchNode(s, vs), matchNode(p, vp), matchNode(o, vo));
    }

    /**
     * Generates the query:
     * <pre>
     * SELECT * {
     *     { ?s ?p ?o }
     *   UNION
     *     { GRAPH ?g { ?s ?p ?o } }
     * }
     * </pre>
     */
    private static Query createQueryTriplesAndQuads(Node s, Node p, Node o) {
        BasicPattern bgpTriples = new BasicPattern();
        bgpTriples.add(matchTriple(s, p, o));

        Quad quad = matchQuad(vg, s, p, o);
        BasicPattern bgpQuads = new BasicPattern();
        bgpQuads.add(quad.asTriple());

        ElementUnion union = new ElementUnion();
        union.addElement(new ElementTriplesBlock(bgpTriples));
        union.addElement(new ElementNamedGraph(vg, new ElementTriplesBlock(bgpQuads)));

        Query query = new Query();
        query.setQuerySelectType();
        query.setQueryResultStar(true);
        query.setQueryPattern(union);
        return query;
    }

    /**
     * Generates the query:
     * <pre>
     * SELECT * { GRAPH ?g { ?s ?p ?o } }
     * </pre>
     */
    private static Query createQueryQuad(Quad quad) {
        BasicPattern bgp = new BasicPattern();
        bgp.add(quad.asTriple());
        Element element = new ElementTriplesBlock(bgp);
        element = new ElementNamedGraph(quad.getGraph(), element);

        Query query = new Query();
        query.setQuerySelectType();
        query.setQueryResultStar(true);
        query.setQueryPattern(element);
        return query;
    }

    /**
     * Generates the query:
     * <pre>
     * SELECT * { ?s ?p ?o }
     * </pre>
     */
    private static Query createQueryTriple(Triple m) {
        BasicPattern bgp = new BasicPattern();
        bgp.add(m);
        Element element = new ElementTriplesBlock(bgp);

        Query query = new Query();
        query.setQuerySelectType();
        query.setQueryResultStar(true);
        query.setQueryPattern(element);
        return query;
    }

    /**
     * Generates the query:
     * <pre>
     * SELECT (COUNT(*) AS ?c) { GRAPH &lt;g&gt; { ?s ?p ?o } }
     * </pre>
     */
    private static Query createQueryNamedGraphSize(Node graphName, Var outputVar) {
        BasicPattern bgp = new BasicPattern();
        bgp.add(Triple.create(vs, vp, vo));
        Element element = new ElementNamedGraph(graphName, new ElementTriplesBlock(bgp));

        Query query = new Query();
        query.setQuerySelectType();
        query.setQueryPattern(element);
        Expr exprAgg = query.allocAggregate(new AggCount());
        query.getProject().add(outputVar, exprAgg);
        return query;
    }

    private static Update buildDelete(Node g, Node s, Node p, Node o) {
        Quad quad = matchQuad(g, s, p, o);
        Update update = quad.isConcrete()
            ? new UpdateDataDelete(new QuadDataAcc(List.of(quad)))
            : new UpdateDeleteWhere(new QuadAcc(List.of(quad)));
        return update;
    }

    private static UpdateRequest buildDeleteByPattern(Node g, Node s, Node p, Node o) {
        UpdateRequest updateRequest = new UpdateRequest();
        if (isWildcard(g)) {
            updateRequest.add(buildDelete(Quad.defaultGraphIRI, s, p, o));
            updateRequest.add(buildDelete(g, s, p, o));
        } else {
            updateRequest.add(buildDelete(g, s, p, o));
        }
        return updateRequest;
    }

    private static Update buildDeleteByGraph(Node g) {
        Target target = chooseTarget(g);
        boolean silent = true;
        boolean useDrop = true;
        Update update = useDrop
            ? new UpdateDrop(target, silent)
            : new UpdateClear(target, silent);
        return update;
    }

    private static Target chooseTarget(Node g) {
        Target target = Quad.isDefaultGraph(g)
            ? Target.DEFAULT
            : Quad.isUnionGraph(g)
                ? Target.NAMED
                : (g == null || Node.ANY.equals(g))
                    ? Target.ALL
                    : Target.create(g);
        return target;
    }
}
