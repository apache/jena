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

package org.apache.jena.test.txn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb2.TDB2Factory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

// Tests - programmatic construct, assembler construct.  Good and bad.
//   TestDataset2Graph - TS_Transactions.
//   DatasetFactory.wrap
//   DatasetOne, DatasetGraphOne. <-- Flag needed.
//   DatasetImpl

/** Additional testing for "Dataset over Graph" transaction mapping */

@RunWith(Parameterized.class)
public class TestDataset2Graph {

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        Creator<Dataset> datasetPlainMaker = ()-> DatasetFactory.createGeneral() ;
        Creator<Dataset> datasetTxnMemMaker = ()-> DatasetFactory.createTxnMem() ;
        Creator<Dataset> datasetTDB1 = ()-> TDBFactory.createDataset();
        Creator<Dataset> datasetTDB2 = ()-> TDB2Factory.createDataset();


        return Arrays.asList(new Object[][] {
            { "Plain", datasetPlainMaker },
            { "TIM",   datasetTxnMemMaker },
            { "TDB1",  datasetTDB1 },
            { "TDB2",  datasetTDB2 }
        });
    }

    private final Creator<Dataset> creator;

    public TestDataset2Graph(String name, Creator<Dataset> creator) {
        this.creator = creator;
    }

    @Test public void dsgGraphTxn_infModel() {
        testInfModel(creator.create());
    }

    @Test public void dsgGraphTxn_dataset_wrap() {
        testOverDS(creator.create(), true);
    }

    @Test public void dsgGraphTxn_dataset_create() {
        testOverDS(creator.create(), false);
    }

    private static void testInfModel(Dataset ds0) {
        Txn.executeWrite(ds0, ()->{});
        Model baseModel = ds0.getDefaultModel();
        Model model = ModelFactory.createInfModel(RDFSRuleReasonerFactory.theInstance().create(null), baseModel);
        if ( model.getGraph().getTransactionHandler().transactionsSupported() ) {
            // InfModels do not support transactions per se - they participate if included in a suitable dataset.
            model.begin();
            long x = Iter.count(model.listStatements());
            model.commit();
            assertTrue(x > 10);
        }
    }

    private static void testOverDS(Dataset ds0, boolean wrap) {
        // Force to transactions / verify the DSG is transactional.
        Txn.executeWrite(ds0, ()->{});
        Model baseModel = ds0.getDefaultModel();
        Model model = ModelFactory.createInfModel(RDFSRuleReasonerFactory.theInstance().create(null), baseModel);
        Dataset ds1 = wrap ? DatasetFactory.wrap(model) : DatasetFactory.create(model);

        try ( RDFConnection conn = RDFConnection.connect(ds1) ) {

            //conn.querySelect("SELECT (count(*) AS ?C) { ?s ?p ?o } HAVING (?C = 0)", (qs)-> fail("Didn't expect any query solutions"));

            // Necessary
            Txn.exec(conn, TxnType.READ, ()->{
                try ( QueryExecution qExec = conn.query("SELECT * { ?s ?p ?o }") ) {
                    long x = ResultSetFormatter.consume(qExec.execSelect());
                    assertTrue(x > 10); // About106
                }
            });
        }

        Triple t = SSE.parseTriple("(:s :p :o)");
        Quad q = Quad.create(Quad.defaultGraphIRI, t);

        // Now write via top.
        Txn.executeWrite(ds1, ()->{
            ds1.asDatasetGraph().add(q);
        });

        // And get it back again from storage.
        Txn.exec(ds0, TxnType.READ, ()->{
            assertEquals(1, ds0.asDatasetGraph().getDefaultGraph().size());
            assertTrue(ds0.getDefaultModel().getGraph().contains(t));
        });
    }
}
