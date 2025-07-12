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

package org.apache.jena.rdf12;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Graph;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetOps;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.sse.SSE_ParseException;
import org.apache.jena.system.Txn;

/**
 * Basic evaluation tests.
 * <p>
 * Evaluation is properly test by the rdf-tests CG scripted test suite.
 */
public abstract class AbstratTestSPARQL12Eval {

    /** Get the dataset implementation. This should be same object each time. */
    protected abstract DatasetGraph dsg();

    /** Fill the dataset */
    protected static DatasetGraph data(DatasetGraph dsg) {
        Graph g = SSE.parseGraph("""
                (graph
                   (:s :p :o)
                   (:s :q <<( :s :p 'TripleTerm' )>> )
                )""");
        Txn.executeWrite(dsg,  ()->{
            addQuad(dsg, "( _ :s :p :o)");
            addQuad(dsg, "( _ :x :q <<( :s1 :p 'TripleTerm1' )>> )");
            addQuad(dsg, "( _ :x :q <<( :s2 :p 'TripleTerm2' )>> )");
            addQuad(dsg, "( _ :r rdf:reifies <<( :x :y :z )>>)");
        });
        return dsg;
    }

    private static void addQuad(DatasetGraph dsg, String str) {
        try {
            Quad quad = SSE.parseQuad(str);
            dsg.add(quad);
        } catch (SSE_ParseException ex) {
            System.err.println("Inout: "+str);
            System.err.println(ex.getMessage());
            throw ex;
        }
    }

    @Test public void eval_sparql12_01() {
        test("SELECT * { ?s ?p ?o }", 4);
    }

    @Test public void eval_sparql12_02() {
        test("SELECT * { ?x ?q <<( ?a ?b ?c )>> }", 3);
    }

    @Test public void eval_sparql12_03() {
        test("SELECT * { ?x ?q <<( ?a ?b 'TripleTerm1' )>> }", 1);
    }

    @Test public void eval_sparql12_04() {
        test("SELECT * { << ?x ?y ?z >> . }", 1);
    }

    private void test(String queryString, int expected) {
        DatasetGraph dsg = dsg();
        Txn.executeRead(dsg, ()->{
            try( QueryExec qExec = QueryExec.dataset(dsg).query(queryString).build() ) {
                RowSet rowSet = qExec.select();
                long x1 = RowSetOps.count(rowSet);
                assertEquals(expected, x1);
            }
        });
    }



}
