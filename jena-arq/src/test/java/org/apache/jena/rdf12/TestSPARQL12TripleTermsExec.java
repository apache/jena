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

package org.apache.jena.rdf12;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.Rename;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sys.JenaSystem;

/**
 * Execution of queries where a triple term is built by {@code BIND} and the value
 * crosses a sub-{@code SELECT} scope boundary.
 * <p>
 * Variables of a sub-{@code SELECT} that are not projected out are renamed
 * ({@code ?x} {@literal ->} {@code ?/x}) so that they do not clash with usage
 * outside the sub-{@code SELECT}. The renaming has to reach the variables inside a
 * triple term in an expression; otherwise the {@code BIND} sees an unbound
 * variable and the triple term is silently not created.
 *
 * @see Rename
 */
public class TestSPARQL12TripleTermsExec {

    static { JenaSystem.init(); }

    // ":" is "http://example/" for both SSE and the queries here.
    private static final String PREFIX = "PREFIX : <http://example/>\n";

    // :g1 has both properties and so survives the join; :g2 does not.
    private static final DatasetGraph dsg = SSE.parseDatasetGraph(StrUtils.strjoinNL
            ("(dataset"
            ,"  (graph"
            ,"    (:g1 :hasX 'x1')"
            ,"    (:g1 :hasB 'b1')"
            ,"    (:g2 :hasX 'x2')"
            ,"  ))"
            ));

    private static final Node tripleTerm_x1 = SSE.parseNode("<<( 'x1' :p :o )>>");

    /**
     * A sub-SELECT projecting a triple term, joined with a sibling triple pattern.
     * The triple term used to come back unbound.
     */
    @Test public void subSelect_join_pattern() {
        String qs = StrUtils.strjoinNL
                ("SELECT * {"
                ,"  { SELECT ?g ?tt { ?g :hasX ?x . BIND( <<( ?x :p :o )>> AS ?tt ) } }"
                ,"  ?g :hasB ?b ."
                ,"}");
        Binding row = execOne(qs);
        assertEquals(SSE.parseNode(":g1"), row.get(Var.alloc("g")));
        assertEquals(SSE.parseNode("'b1'"), row.get(Var.alloc("b")));
        assertEquals(tripleTerm_x1, row.get(Var.alloc("tt")));
    }

    /** Same, with the join partner being a sub-SELECT as well. */
    @Test public void subSelect_join_subSelect() {
        String qs = StrUtils.strjoinNL
                ("SELECT * {"
                ,"  { SELECT ?g ?tt { ?g :hasX ?x . BIND( <<( ?x :p :o )>> AS ?tt ) } }"
                ,"  { SELECT ?g ?b { ?g :hasB ?b } }"
                ,"}");
        Binding row = execOne(qs);
        assertEquals(SSE.parseNode(":g1"), row.get(Var.alloc("g")));
        assertEquals(SSE.parseNode("'b1'"), row.get(Var.alloc("b")));
        assertEquals(tripleTerm_x1, row.get(Var.alloc("tt")));
    }

    /** Both sides of the join build a triple term in a sub-SELECT. */
    @Test public void subSelect_join_subSelect_2() {
        String qs = StrUtils.strjoinNL
                ("SELECT * {"
                ,"  { SELECT ?g ?tt1 { ?g :hasX ?x . BIND( <<( ?x :p :o )>> AS ?tt1 ) } }"
                ,"  { SELECT ?g ?tt2 { ?g :hasB ?x . BIND( <<( ?x :p :o )>> AS ?tt2 ) } }"
                ,"}");
        Binding row = execOne(qs);
        assertEquals(SSE.parseNode(":g1"), row.get(Var.alloc("g")));
        assertEquals(tripleTerm_x1, row.get(Var.alloc("tt1")));
        assertEquals(SSE.parseNode("<<( 'b1' :p :o )>>"), row.get(Var.alloc("tt2")));
    }

    /**
     * The projected triple term used by the triple term accessor functions outside
     * the sub-SELECT. This is the shape where the row count is right but every
     * triple-term-derived column comes back unbound.
     */
    @Test public void subSelect_join_pattern_accessors() {
        String qs = StrUtils.strjoinNL
                ("SELECT * {"
                ,"  { SELECT ?g ?tt { ?g :hasX ?x . BIND( <<( ?x :p :o )>> AS ?tt ) } }"
                ,"  ?g :hasB ?b ."
                ,"  BIND( SUBJECT(?tt)   AS ?s1 )"
                ,"  BIND( PREDICATE(?tt) AS ?p1 )"
                ,"  BIND( OBJECT(?tt)    AS ?o1 )"
                ,"  BIND( ISTRIPLE(?tt)  AS ?isTT )"
                ,"}");
        Binding row = execOne(qs);
        assertEquals(tripleTerm_x1, row.get(Var.alloc("tt")));
        assertEquals(SSE.parseNode("'x1'"), row.get(Var.alloc("s1")));
        assertEquals(SSE.parseNode(":p"), row.get(Var.alloc("p1")));
        assertEquals(SSE.parseNode(":o"), row.get(Var.alloc("o1")));
        assertEquals(SSE.parseNode("true"), row.get(Var.alloc("isTT")));
    }

    /** A nested triple term, so that the renaming has to recurse. */
    @Test public void subSelect_join_pattern_nested() {
        String qs = StrUtils.strjoinNL
                ("SELECT * {"
                ,"  { SELECT ?g ?tt { ?g :hasX ?x . BIND( <<( :s :p <<( ?x :p :o )>> )>> AS ?tt ) } }"
                ,"  ?g :hasB ?b ."
                ,"}");
        Binding row = execOne(qs);
        assertEquals(SSE.parseNode("<<( :s :p <<( 'x1' :p :o )>> )>>"), row.get(Var.alloc("tt")));
    }

    /**
     * The variable used in the triple term is itself projected by the sub-SELECT,
     * so it is not renamed.
     */
    @Test public void subSelect_join_pattern_projectedVar() {
        String qs = StrUtils.strjoinNL
                ("SELECT * {"
                ,"  { SELECT ?g ?x ?tt { ?g :hasX ?x . BIND( <<( ?x :p :o )>> AS ?tt ) } }"
                ,"  ?g :hasB ?b ."
                ,"}");
        Binding row = execOne(qs);
        assertEquals(SSE.parseNode("'x1'"), row.get(Var.alloc("x")));
        assertEquals(tripleTerm_x1, row.get(Var.alloc("tt")));
    }

    /** The projected triple term used by a FILTER outside the sub-SELECT. */
    @Test public void subSelect_join_pattern_filter() {
        String qs = StrUtils.strjoinNL
                ("SELECT * {"
                ,"  { SELECT ?g ?tt { ?g :hasX ?x . BIND( <<( ?x :p :o )>> AS ?tt ) } }"
                ,"  ?g :hasB ?b ."
                ,"  FILTER ( SUBJECT(?tt) = 'x1' )"
                ,"}");
        Binding row = execOne(qs);
        assertEquals(tripleTerm_x1, row.get(Var.alloc("tt")));
    }

    // -- Negative controls: these did not go wrong and must not start to.

    /** The sub-SELECT on its own: no join, so no scope renaming. */
    @Test public void subSelect_noJoin() {
        String qs = "SELECT ?g ?tt { ?g :hasX ?x . BIND( <<( ?x :p :o )>> AS ?tt ) }";
        List<Binding> rows = exec(qs);
        assertEquals(2, rows.size());
        Set<Node> tripleTerms = rows.stream().map(b->b.get(Var.alloc("tt"))).collect(Collectors.toSet());
        assertEquals(Set.of(tripleTerm_x1, SSE.parseNode("<<( 'x2' :p :o )>>")), tripleTerms);
    }

    /** The same BIND in a plain group, not a sub-SELECT. */
    @Test public void group_join_pattern() {
        String qs = StrUtils.strjoinNL
                ("SELECT * {"
                ,"  { ?g :hasX ?x . BIND( <<( ?x :p :o )>> AS ?tt ) }"
                ,"  ?g :hasB ?b ."
                ,"}");
        Binding row = execOne(qs);
        assertEquals(SSE.parseNode("'x1'"), row.get(Var.alloc("x")));
        assertEquals(tripleTerm_x1, row.get(Var.alloc("tt")));
    }

    /** A sub-SELECT binding a value that is not a triple term. */
    @Test public void subSelect_join_pattern_noTripleTerm() {
        String qs = StrUtils.strjoinNL
                ("SELECT * {"
                ,"  { SELECT ?g ?z { ?g :hasX ?x . BIND( CONCAT(?x, '!') AS ?z ) } }"
                ,"  ?g :hasB ?b ."
                ,"}");
        Binding row = execOne(qs);
        assertEquals(SSE.parseNode("'x1!'"), row.get(Var.alloc("z")));
    }

    // -- The renaming step itself.

    /** Variable renaming reaches inside a triple term in an expression. */
    @Test public void renameVars_tripleTerm() {
        Op op = SSE.parseOp(StrUtils.strjoinNL
                ("(extend ((?tt (tripleterm ?x :p :o)))"
                ,"  (bgp (triple ?g :hasX ?x)))"
                ));
        Op expected = SSE.parseOp(StrUtils.strjoinNL
                ("(extend ((?tt (tripleterm ?/x :p :o)))"
                ,"  (bgp (triple ?g :hasX ?/x)))"
                ));
        Op op2 = Rename.renameVars(op, Set.of(Var.alloc("g"), Var.alloc("tt")));
        assertEquals(expected, op2);
    }

    /** ... and the reverse renaming undoes it. */
    @Test public void reverseRenameVars_tripleTerm() {
        Op op = SSE.parseOp(StrUtils.strjoinNL
                ("(extend ((?tt (tripleterm ?/x :p :o)))"
                ,"  (bgp (triple ?g :hasX ?/x)))"
                ));
        Op expected = SSE.parseOp(StrUtils.strjoinNL
                ("(extend ((?tt (tripleterm ?x :p :o)))"
                ,"  (bgp (triple ?g :hasX ?x)))"
                ));
        Op op2 = Rename.reverseVarRename(op, true);
        assertEquals(expected, op2);
    }

    // ----

    private static List<Binding> exec(String queryString) {
        Query query = QueryFactory.create(PREFIX+queryString, Syntax.syntaxSPARQL_12);
        try ( QueryExec qExec = QueryExec.dataset(dsg).query(query).build() ) {
            return Iter.toList(qExec.select());
        }
    }

    /** Execute, expecting exactly one row. */
    private static Binding execOne(String queryString) {
        List<Binding> rows = exec(queryString);
        assertEquals(1, rows.size(), ()->"Expected one row: got "+rows);
        return rows.get(0);
    }
}
