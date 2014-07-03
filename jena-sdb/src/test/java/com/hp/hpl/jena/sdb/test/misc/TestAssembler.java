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

package com.hp.hpl.jena.sdb.test.misc;

import static org.junit.Assert.assertFalse ;
import static org.junit.Assert.assertNotNull ;
import static org.junit.Assert.assertTrue ;

import java.sql.Connection ;

import org.junit.Test ;

import com.hp.hpl.jena.assembler.Assembler ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.rdf.model.Literal ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.Property ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sdb.SDBFactory ;
import com.hp.hpl.jena.sdb.Store ;
import com.hp.hpl.jena.sdb.StoreDesc ;
import com.hp.hpl.jena.sdb.store.DatasetGraphSDB ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.util.FileManager ;


public class TestAssembler
{
    static final String dir = "testing/Assembler/" ;
    
    @Test public void dataset_1()
    {
        Dataset ds = DatasetFactory.assemble(dir+"dataset.ttl") ;
        assertNotNull(ds) ;
        // Check it will be dispatched to SDB
        DatasetGraph dsg = ds.asDatasetGraph() ;
        assertTrue( dsg instanceof DatasetGraphSDB ) ;
    }
    
    @Test public void connection_1()
    {
        Connection jdbc = SDBFactory.createSqlConnection(dir+"connection.ttl") ;
        assertNotNull(jdbc) ;
    }
    
    @Test public void store_1()
    {
        Store store = SDBFactory.connectStore(dir+"store.ttl") ;
        assertNotNull(store) ;
    }
    
    @Test public void model_1()
    {
        Model assem = FileManager.get().loadModel(dir+"graph-assembler.ttl") ;
        Resource x = assem.getResource("http://example/test#graphDft") ;
        // Model for default graph
        Model model = (Model)Assembler.general.open(x) ;
        assertNotNull(model) ;
    }

    @Test public void model_2()
    {
        Model assem = FileManager.get().loadModel(dir+"graph-assembler.ttl") ;
        Resource x = assem.getResource("http://example/test#graphNamed") ;
        // Model for default graph
        Model model = (Model)Assembler.general.open(x) ;
        assertNotNull(model) ;
    }
    
    private Store create(Model assem)
    {
        // Create a store and format
        Dataset ds = DatasetFactory.assemble(assem) ;
        Store store = ((DatasetGraphSDB)ds.asDatasetGraph()).getStore() ;
        store.getTableFormatter().create() ;
        return store ;
    }
    
    @Test public void model_3()
    {
        Model assem = FileManager.get().loadModel(dir+"graph-assembler.ttl") ;
        Resource xDft = assem.getResource("http://example/test#graphDft") ;
        Resource xNamed = assem.getResource("http://example/test#graphNamed") ;
        
        Store store = create(assem) ;
        
        Model model1 = (Model)Assembler.general.open(xDft) ;
        Model model2 = (Model)Assembler.general.open(xNamed) ;
        assertNotNull(model1 != model2) ;
    }
        
    @Test public void model_4()
    {
        Model assem = FileManager.get().loadModel(dir+"graph-assembler.ttl") ;
        Resource xDft = assem.getResource("http://example/test#graphDft") ;
        Resource xNamed = assem.getResource("http://example/test#graphNamed") ;
        
        Store store = create(assem) ;
        
        Model model1 = (Model)Assembler.general.open(xDft) ;
        Model model2 = (Model)Assembler.general.open(xNamed) ;
        // Check they are not connected to the same place in the store 
        Resource s = model1.createResource() ;
        Property p = model1.createProperty("http://example/p") ;
        Literal o = model1.createLiteral("foo") ;
        
        model1.add(s,p,o) ;
        assertTrue(model1.contains(s, p, o)) ;
        
        assertTrue(model1.size() == 1 ) ;
        assertTrue(model2.size() == 0 ) ;
        
        assertFalse(model1.isIsomorphicWith(model2)) ;
    }
        
    @Test public void model_5()
    {
        Model assem = FileManager.get().loadModel(dir+"graph-assembler.ttl") ;
        Resource xDft = assem.getResource("http://example/test#graphDft") ;
        
        Store store = create(assem) ;
        
        // Default graph: Check they are connected to the same place in the store 
        Model model2 = (Model)Assembler.general.open(xDft) ;
        Model model3 = (Model)Assembler.general.open(xDft) ;
        
        Resource s = model2.createResource() ;
        Property p = model2.createProperty("http://example/p") ;
        // Check two models connected to the same graph 
        Literal o2 = model2.createLiteral("xyz") ;
        model2.add(s,p,o2) ;
        assertTrue(model3.contains(s,p,o2)) ;
    }
    
    @Test public void model_6()
    {
        Model assem = FileManager.get().loadModel(dir+"graph-assembler.ttl") ;
        Resource xNamed = assem.getResource("http://example/test#graphNamed") ;
        
        Store store = create(assem) ;
        
        // Named graph: Check they are connected to the same place in the store 
        Model model2 = (Model)Assembler.general.open(xNamed) ;
        Model model3 = (Model)Assembler.general.open(xNamed) ;
        
        Resource s = model2.createResource() ;
        Property p = model2.createProperty("http://example/p") ;
        // Check two models connected to the same graph 
        Literal o2 = model2.createLiteral("xyz") ;
        model2.add(s,p,o2) ;
        assertTrue(model3.contains(s,p,o2)) ;
    }
    
    @Test public void pool_1()
    {
        // Connection
        Connection conn = SDBFactory.createSqlConnection(dir+"connection.ttl") ;
        
        // Store
        StoreDesc desc = StoreDesc.read(dir+"dataset2.ttl") ;
        Store store = SDBFactory.connectStore(conn, desc) ;
        
    } 

}
