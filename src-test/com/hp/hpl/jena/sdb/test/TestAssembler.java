/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;

import org.junit.Test;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;

import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.assembler.AssemblerVocab;
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
        Model assem = FileManager.get().loadModel(dir+"graph-dft.ttl") ;
        Resource root = AssemblerUtils.findRootByType(assem, AssemblerVocab.ModelType) ;
        Model model = (Model)Assembler.general.openModel(root) ;
        assertNotNull(model) ;
    }

    @Test public void model_2()
    {
        Model assem = FileManager.get().loadModel(dir+"graph-named.ttl") ;
        Resource root = AssemblerUtils.findRootByType(assem, AssemblerVocab.ModelType) ;
        Model model = (Model)Assembler.general.openModel(root) ;
        assertNotNull(model) ;
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