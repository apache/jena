/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.modify.request.QuadDataAcc;
import org.apache.jena.sparql.modify.request.Target;
import org.apache.jena.sparql.modify.request.UpdateClear;
import org.apache.jena.sparql.modify.request.UpdateDataDelete;
import org.apache.jena.sparql.modify.request.UpdateDeleteWhere;
import org.apache.jena.sparql.modify.request.UpdateDrop;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;

/**
 * Static helper methods that build SPARQL queries and turn the results into the
 * iterator types required by {@link DatasetGraphSparql}.
 */
final class DsgSparqlExecUtils {
    static final Var vg = Var.alloc("g");
    static final Var vs = Var.alloc("s");
    static final Var vp = Var.alloc("p");
    static final Var vo = Var.alloc("o");
    static final Var vc = Var.alloc("c");

    static final Query graphsQuery           = QueryFactory.create("SELECT ?g { GRAPH ?g { } }");
    static final Query graphsCountQuery      = QueryFactory.create("SELECT (COUNT(*) AS ?c) { GRAPH ?g { } }");
    static final Query defaultGraphSizeQuery = QueryFactory.create("SELECT (COUNT(*) AS ?c) { ?s ?p ?o }");

    static IteratorCloseable<Node> listGraphNodes(
            Function<Query, ? extends QueryExec> executor) {
        QueryExec qExec = executor.apply(graphsQuery);
        return listNodesNotNull(qExec, vg);
    }

    private static IteratorCloseable<Node> listNodesNotNull(QueryExec qExec, Var var) {
        return Iter.onClose(
                Iter.map(qExec.select(), b -> {
                    Node r = b.get(var);
                    return Objects.requireNonNull(r);
                }),
                qExec::close);
    }

    static IteratorCloseable<Triple> findTriples(
            Function<Query, ? extends QueryExec> executor,
            Node s, Node p, Node o) {
        Triple pattern = matchTriple(s, p, o);
        Query query = createQueryTriple(pattern);
        QueryExec qExec = executor.apply(query);
        return Iter.onClose(
                Iter.map(qExec.select(), b -> Substitute.substitute(pattern, b)),
                qExec::close);
    }

    static IteratorCloseable<Quad> findQuads(
            Function<Query, ? extends QueryExec> executor,
            Node g, Node s, Node p, Node o) {
        Quad pattern = matchQuad(g, s, p, o);
        Query query = createQueryQuad(pattern);
        QueryExec qExec = executor.apply(query);
        return Iter.onClose(
                Iter.map(qExec.select(), b -> Substitute.substitute(pattern, b)),
                qExec::close);
    }

    static IteratorCloseable<Quad> findTriplesOrQuads(
            Function<Query, ? extends QueryExec> executor,
            Node s, Node p, Node o) {
        Quad pattern = matchQuad(vg, s, p, o);
        Query query = createQueryTriplesAndQuads(vg, s, p, o);
        QueryExec qExec = executor.apply(query);
        return Iter.onClose(Iter.map(qExec.select(),
            b -> {
                if (!b.contains(vg)) {
                    b = BindingFactory.binding(b, vg, Quad.defaultGraphIRI);
                }
                return Substitute.substitute(pattern, b);
            }), qExec::close);
    }

    static boolean contains(
            Function<Query, ? extends QueryExec> executor,
            Node g, Node s, Node p, Node o) {
        Query baseQuery = DsgSparqlExecUtils.createQueryTriplesAndQuads(g, s, p, o);
        baseQuery.setQueryAskType();
        QueryExec qExec = executor.apply(baseQuery);
        return qExec.ask();
    }

    static boolean containsGraph(
            Function<Query, ? extends QueryExec> executor,
            Node g) {
        Query query = QueryFactory.create();
        query.setQueryAskType();
        Element element = new ElementNamedGraph(g, new ElementGroup());
        query.setQueryPattern(element);
        QueryExec qExec = executor.apply(query);
        return qExec.ask();
    }

    static long fetchLong(
            Function<Query, ? extends QueryExec> executor,
            Query query,
            Var numberVar) {
        try (QueryExec qExec = executor.apply(query)) {
            Binding b = qExec.select().next();
            Node node = b.get(numberVar);
            Number n = (Number)node.getLiteralValue();
            return n.longValue();
        }
    }

    public static long fetchGraphCount(Function<Query, ? extends QueryExec> executor) {
        long count =  fetchLong(executor, graphsCountQuery, vc);
        return count;
    }

    public static long fetchDefaultGraphSize(Function<Query, ? extends QueryExec> executor) {
        long size = fetchLong(executor, defaultGraphSizeQuery, vc);
        return size;
    }

    public static long fetchGraphSize(Function<Query, ? extends QueryExec> executor, Node g) {
        Query q = createQueryNamedGraphSize(g, vc);
        long size = fetchLong(executor, q, vc);
        return size;
    }

    /* --------------------------------------------------- */
    /*  Private helpers                                    */
    /* --------------------------------------------------- */

    private static Triple matchTriple(Node s, Node p, Node o) {
        return Triple.create(matchNode(s, vs), matchNode(p, vp), matchNode(o, vo));
    }

    static Quad matchQuad(Node g, Node s, Node p, Node o) {
        return Quad.create(matchNode(g, vg), matchNode(s, vs),
                           matchNode(p, vp), matchNode(o, vo));
    }

    private static Node matchNode(Node supplied, Node defaultVar) {
        return supplied == null || supplied.equals(Node.ANY) ? defaultVar : supplied;
    }

    private static Query createQueryTriple(Triple m) {
        BasicPattern bgp = new BasicPattern();
        bgp.add(m);
        Element element = new ElementTriplesBlock(bgp);

        Query q = QueryFactory.create();
        q.setQuerySelectType();
        q.setQueryResultStar(true);
        q.setQueryPattern(element);
        return q;
    }

    private static Query createQueryQuad(Quad quad) {
        BasicPattern bgp = new BasicPattern();
        bgp.add(quad.asTriple());

        Element element = new ElementNamedGraph(quad.getGraph(),
                                                new ElementTriplesBlock(bgp));

        Query q = QueryFactory.create();
        q.setQuerySelectType();
        q.setQueryResultStar(true);
        q.setQueryPattern(element);
        return q;
    }

    /**
     * Create a SELECT query that matches across the default graph and the named graphs.
     *
     * If g is a variable then match triples and quads with a UNION graph pattern.
     * If g is concrete then match either triples or quads as appropriate.
     */
    private static Query createQueryTriplesAndQuads(Node g, Node s, Node p, Node o) {
        List<Element> elts = new ArrayList<>(2);
        Node matchG = matchNode(g, vg); // ANY -> vg
        if (matchG.isVariable() || Quad.isDefaultGraph(matchG)) {
            BasicPattern bgpDefault = new BasicPattern();
            bgpDefault.add(matchTriple(s, p, o));
            elts.add(new ElementTriplesBlock(bgpDefault));
        }

        if (matchG.isVariable() || !Quad.isDefaultGraph(matchG)) {
            Quad quad = matchQuad(matchG, s, p, o);
            BasicPattern bgpNamed = new BasicPattern();
            bgpNamed.add(quad.asTriple());
            elts.add(new ElementNamedGraph(matchG,
                    new ElementTriplesBlock(bgpNamed)));
        }

        Element finalElt;
        if (elts.size() == 1) {
            finalElt = elts.get(0);
        } else {
            ElementUnion union = new ElementUnion();
            elts.forEach(union::addElement);
            finalElt = union;
        }
        Query q = QueryFactory.create();
        q.setQuerySelectType();
        q.setQueryResultStar(true);
        q.setQueryPattern(finalElt);
        return q;
    }

    static Query createQueryNamedGraphSize(Node graphName, Var outputVar) {
        BasicPattern bgp = new BasicPattern();
        bgp.add(Triple.create(vs, vp, vo));

        Element element = new ElementNamedGraph(graphName,
                new ElementTriplesBlock(bgp));

        Query q = QueryFactory.create();
        q.setQuerySelectType();
        q.setQueryPattern(element);
        Expr agg = q.allocAggregate(new AggCount());
        q.getProject().add(outputVar, agg);
        return q;
    }

    /* --------------------------------------------------- */
    /*  Triple/Quad-level deletion                         */
    /* --------------------------------------------------- */

    static UpdateRequest buildDeleteByPattern(Node g, Node s, Node p, Node o) {
        UpdateRequest updateRequest = new UpdateRequest();
        if (isWildcard(g)) {
            updateRequest.add(buildDelete(Quad.defaultGraphIRI, s, p, o));
            updateRequest.add(buildDelete(g, s, p, o));
        } else {
            updateRequest.add(buildDelete(g, s, p, o));
        }
        return updateRequest;
    }

    static boolean isWildcard(Node g) {
        return g == null || Node.ANY.equals(g) || g.isVariable();
    }

    private static Update buildDelete(Node g, Node s, Node p, Node o) {
        Quad quad = DsgSparqlExecUtils.matchQuad(g, s, p, o);
        Update update = quad.isConcrete()
            ? new UpdateDataDelete(new QuadDataAcc(List.of(quad)))
            : new UpdateDeleteWhere(new QuadAcc(List.of(quad)));
        return update;
    }

    /* --------------------------------------------------- */
    /*  Graph removal                                      */
    /* --------------------------------------------------- */

    static Update buildGraphRemoval(Node g, boolean useDrop, boolean silent) {
        Target target = chooseTarget(g);
        Update update = useDrop
            ? new UpdateDrop(target, silent)
            : new UpdateClear(target, silent);
        return update;
    }

    static Target chooseTarget(Node g) {
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
