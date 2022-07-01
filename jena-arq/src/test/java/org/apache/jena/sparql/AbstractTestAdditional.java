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

package org.apache.jena.sparql;

import static org.junit.Assert.assertTrue;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.*;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSetOps;
import org.apache.jena.sparql.exec.RowSetRewindable;
import org.apache.jena.sparql.resultset.ResultSetCompare;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.junit.Test;

/** Tests about things discovered over time. */
public abstract class AbstractTestAdditional {
    private static final String PREFIXES = StrUtils.strjoinNL
            ("PREFIX : <http://example/>"
             )+"\n";

    protected abstract Dataset dataset();

    /** Check substitution into patterns. */
    @Test public void substitute_1() {
        Dataset dataset = dataset();
        String resultsStr = StrUtils.strjoinNL("(resultset (?s ?p ?o)"
                                               , "(row (?s :s1) (?p :p) (?o :o))"
                                              ,")" );
        RowSetRewindable expected = SSE.parseRowSet(resultsStr).rewindable();
        Txn.executeWrite(dataset, ()->{
            String data = StrUtils.strjoinNL("(dataset"
                                            ,"  (:g1 :s1 :p :o)"
                                            ,"  (:g1 :s2 :p :o)"
                                            ,"  (:g2 :s1 :p :o)"
                                            ,"  (:g2 :s2 :p :o)"
                                            ,")");
            DatasetGraph dsg = SSE.parseDatasetGraph(data);
            dataset.asDatasetGraph().addAll(dsg);
            String qs = PREFIXES+"SELECT * { VALUES ?s { :s1 } GRAPH <"+Quad.unionGraph+"> { ?s ?p ?o } }";
            Query query = QueryFactory.create(qs);
            try ( QueryExec qExec = QueryExec.dataset(dsg).query(query).build() ) {
                RowSetRewindable rs = qExec.select().rewindable();
                testRS(expected, rs);
            }
        });
    }

    private static void testRS(RowSetRewindable rsExpected, RowSetRewindable rsGot) {
        boolean b = ResultSetCompare.equalsByTerm(rsExpected, rsGot);
        if (! b ) {
            rsExpected.reset();
            rsGot.reset();
            RowSetOps.out(System.out, rsExpected);
            System.out.println();
            RowSetOps.out(System.out, rsGot);
        }
        assertTrue("result sets different", b);
    }
}
