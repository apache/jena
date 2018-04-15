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

package org.apache.jena.test.assembler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetOne;
import org.apache.jena.sparql.core.TxnDataset2Graph;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab;
import org.apache.jena.system.JenaSystem;
import org.apache.jena.sys.Txn;
import org.apache.jena.test.txn.TestDataset2Graph;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests of building datasets with assemblers.
 */
public class TestDatasetAssembler {
    static { JenaSystem.init(); } 
    
    @SuppressWarnings("deprecation")
    @BeforeClass public static void beforeClass() {
        if ( ! TxnDataset2Graph.TXN_DSG_GRAPH )
            Log.warn(TestDataset2Graph.class, "**** TxnDataset2Graph.TXN_DSG_GRAPH is false in the system setup ****");
    }
    
    protected static String DIR = "testing/Assembler/";
    
    static private Model     data = RDFDataMgr.loadModel(DIR + "data.ttl");
    static private Resource  s    = data.createResource("http://example/data/s");
    static private Property  p    = data.createProperty("http://example/data/p");
    static private Resource  o    = data.createResource("http://example/data/o");
    static private Statement stmt = data.createStatement(s, p, o);

    // See also jena-arq/etc/...
    
    // ---- Null dataset assemblers

    @Test public void dsg_zero() {
        Dataset ds = (Dataset)AssemblerUtils.build(DIR+"assem_dsg_zero.ttl", DatasetAssemblerVocab.tDatasetZero);
        assertNotNull(ds);
        try { 
            ds.getDefaultModel().add(stmt);
        } catch (UnsupportedOperationException ex) {}
    }
    
    @Test public void dsg_sink() {
        Dataset ds = (Dataset)AssemblerUtils.build(DIR+"assem_dsg_sink.ttl", DatasetAssemblerVocab.tDatasetSink);
        assertNotNull(ds);
        assertTrue(ds.getContext().isDefined(ARQ.queryTimeout)); 
        ds.getDefaultModel().add(stmt);
        assertEquals(0, ds.getDefaultModel().size());
    }

    // ---- DatasetOneAssembler
    
    @Test public void dsg1_1() {
        Dataset ds = (Dataset)AssemblerUtils.build(DIR+"assem_dsg1_1.ttl", DatasetAssemblerVocab.tDatasetOne);
        assertNotNull(ds);
        assertNotNull(ds.getDefaultModel());
        assertTrue(ds instanceof DatasetOne);
        useIt(ds);
    }
    
    @Test public void dsg1_2() {
        Dataset ds = (Dataset)AssemblerUtils.build(DIR+"assem_dsg1_2.ttl", DatasetAssemblerVocab.tDatasetOne);
        assertNotNull(ds);
        assertNotNull(ds.getDefaultModel());
        assertTrue(ds instanceof DatasetOne);
        readIt(ds);
    }
    
    @Test public void dsg1_3() {
        Dataset ds = (Dataset)AssemblerUtils.build(DIR+"assem_dsg1_3.ttl", DatasetAssemblerVocab.tDatasetOne);
        assertNotNull(ds);
        assertNotNull(ds.getDefaultModel());
        assertTrue(ds instanceof DatasetOne);
        readIt(ds);
    }
    
    @Test(expected=AssemblerException.class)
    public void dsg1_bad_1() { 
        Dataset ds = (Dataset)AssemblerUtils.build(DIR+"assem_dsg1_bad_1.ttl", DatasetAssemblerVocab.tDatasetOne);
        assertNotNull(ds);
    } 
    
    @Test public void dsg1_inf_tdb1_1() {
        Dataset ds = (Dataset)AssemblerUtils.build(DIR+"assem_dsg1_inf_tdb1.ttl", DatasetAssemblerVocab.tDatasetOne);
        assertNotNull(ds);
        assertNotNull(ds.getDefaultModel());
        assertTrue(ds instanceof DatasetOne);
        useIt(ds);
    }
    
    @Test public void dsg1_inf_tdb1_2() {
        Dataset ds = (Dataset)AssemblerUtils.build(DIR+"assem_dsg1_inf_tdb2.ttl", DatasetAssemblerVocab.tDatasetOne);
        assertNotNull(ds);
        assertNotNull(ds.getDefaultModel());
        assertTrue(ds instanceof DatasetOne);
        useIt(ds);
    }
    
    private void readIt(Dataset ds) {
        Txn.exec(ds, TxnType.READ, ()->{
            assertTrue(ds.getDefaultModel().contains(stmt));
        });
    }
    
    private void useIt(Dataset ds) {
        Txn.executeWrite(ds, ()->{
            ds.getDefaultModel().add(data);
        });
        readIt(ds);
    }
}
