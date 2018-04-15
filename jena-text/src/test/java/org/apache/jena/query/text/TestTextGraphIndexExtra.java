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

package org.apache.jena.query.text;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.* ;
import org.apache.jena.query.text.EntityDefinition ;
import org.apache.jena.query.text.TextDatasetFactory ;
import org.apache.jena.query.text.TextIndex ;
import org.apache.jena.query.text.TextIndexConfig ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sys.Txn ;
import org.apache.jena.tdb.TDBFactory ;
import org.apache.jena.vocabulary.RDFS ;
import org.apache.lucene.store.RAMDirectory ;
import org.junit.Assert ;
import org.junit.Test ;

public class TestTextGraphIndexExtra {
    // JENA-1372
    // from https://lists.apache.org/thread.html/7c185d3666af3fbf1559b27129b32e2984582413f2331535ee653a83@%3Cusers.jena.apache.org%3E 
    
    static Node rdfsLabel     = RDFS.Nodes.label;

    static Dataset textDataset(Dataset dataset) {
        EntityDefinition entdef = new EntityDefinition("uri", "text", "graph", rdfsLabel);
        TextIndex textIndex = TextDatasetFactory.createLuceneIndex(new RAMDirectory(), new TextIndexConfig(entdef));
        return TextDatasetFactory.create(dataset, textIndex, true);
    }
    
    static void populate(Dataset dataset) {
        Txn.executeWrite(dataset, ()->{
            Quad q1 = SSE.parseQuad("(<x:/graph1> <x:/thing1> rdfs:label 'test1')");
            dataset.asDatasetGraph().add(q1);
            Quad q2 = SSE.parseQuad("(<x:/graph1> <x:/thing2> rdfs:label 'abcd1')");
            dataset.asDatasetGraph().add(q2);
            
            Quad q3 = SSE.parseQuad("(<x:/graph2> <x:/thing3> rdfs:label 'abcd2')");
            dataset.asDatasetGraph().add(q3);
        });
    }

    String textQuery  = StrUtils.strjoinNL
        ("PREFIX text: <http://jena.apache.org/text#>"
        ,"SELECT * WHERE {"
        ,"  graph ?g {"
        ,"    ?thing text:query 'test1' ."
        // This would check in the graph the literal is present.
        // Then test_mem_link_ds returns 1.
        //,"    ?thing ?p 'test1' ."  
        ,"  }"
        ,"}"
        );

    String textQuery2  = StrUtils.strjoinNL
        ("PREFIX text: <http://jena.apache.org/text#>"
        ,"SELECT * WHERE {"
        ,"  graph ?g {"
        ,"    ?thing text:query 'test1' ."
        // This checks in the graph that the literal is present.
        ,"    ?thing ?p 'test1' ."  
        ,"  }"
        ,"}"
        );

    int sparqlQuery(Dataset dataset, String queryStr) {
        return Txn.calculateRead(dataset, ()->{
            QueryExecution qExec = QueryExecutionFactory.create(queryStr, dataset);
            ResultSet rs = qExec.execSelect();
            return ResultSetFormatter.consume(rs);
        });
    }
    
    @Test public void test_mem_copy_ds () {
        test(DatasetFactory.create(), textQuery, 1);
        test(DatasetFactory.create(), textQuery2, 1);
    }
    
    @Test public void test_mem_link_ds () {
        // The general dataset contains graphs as given by linking to the graph object.
        // It does not provide their name in getGraph() and so the answer is 2 hits,
        // one for each of <graph1> and <graph2> whereas it is 1 otherwise.
        test(DatasetFactory.createGeneral(), textQuery, 2);
        test(DatasetFactory.createGeneral(), textQuery2, 1);
    }

    @Test public void test_mem_txn_ds () {
        test(DatasetFactory.createTxnMem(), textQuery, 1);
        test(DatasetFactory.createTxnMem(), textQuery2, 1);
    }

    @Test public void test_tdb_ds () {
        test(TDBFactory.createDataset(), textQuery, 1);
        test(TDBFactory.createDataset(), textQuery2, 1);
    }

    private void test(Dataset ds, String queryStr, int expected) {
        ds = textDataset(ds);
        populate(ds);
        int x = sparqlQuery(ds, queryStr);
        Assert.assertEquals(expected, x);
    }
    
}
