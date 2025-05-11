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

import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.rdflink.dataset.DatasetGraphOverRDFLink;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;

/** TODO Get rid of dependency to external service. */
public class TestGraphOverRDFLink {
    // @Test
    public void test() throws InterruptedException {
        DatasetGraph dsg = new DatasetGraphOverRDFLink(() ->
            RDFLinkHTTP.newBuilder()
                .destination("http://dbpedia.org/sparql")
                .build());

        QueryExec qe = QueryExec.dataset(dsg)
                // .timeout(10000, TimeUnit.MILLISECONDS)
                // .query("SELECT * { ?s rdfs:label ?o . ?o bif:contains 'Leipzig' }")
                .query("""
                    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                    SELECT (count(*) AS ?cxx) WHERE {
                      GRAPH <http://dbpedia.org> {
                      ?x ?y ?z. ?a ?b ?c .
                      }
                    } LIMIT 10
                """)
                .build();

        Thread txx = new Thread(() -> {
            try (QueryExec q = qe) {
                RowSet rs = q.select();
                Table table = TableFactory.create(rs);
                table.iterator(null).forEachRemaining(System.out::println);
            }
        });

        txx.start();
        Thread.sleep(1000);
        System.out.println("Aborting");
        qe.abort();
        System.out.println("Aborted");

        txx.join();
        System.out.println("joined");

        // table.iterator(null).forEachRemaining(System.out::println);

        if (false) {
            Iterator<Quad> it = dsg.find();
            try {
                while (it.hasNext()) {
                    Quad t = it.next();
                    System.out.println(t);
                }
            } finally {
                Iter.close(it);
            }
        }
    }
}
