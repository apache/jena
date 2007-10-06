/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.test;

import static org.junit.Assert.*;

import java.sql.Connection;

import org.junit.Test;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.sparql.core.DatasetGraph;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;

import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.store.DatasetStoreGraph;


public class TestAssembler
{
    static final String dir = "testing/Assembler/" ;
    
    @Test public void dataset_1()
    {
        Dataset ds = DatasetFactory.assemble(dir+"dataset.ttl") ;
        assertNotNull(ds) ;
        // Check it will be dispatched to SDB
        DatasetGraph dsg = ds.asDatasetGraph() ;
        assertTrue( dsg instanceof DatasetStoreGraph ) ;
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
        Store store = ((DatasetStoreGraph)ds.asDatasetGraph()).getStore() ;
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

}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */