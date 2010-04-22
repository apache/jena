/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.assembler;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.FileOps ;

import com.hp.hpl.jena.assembler.JA;
import com.hp.hpl.jena.assembler.exceptions.AssemblerException ;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils;
import com.hp.hpl.jena.sparql.core.assembler.DatasetAssemblerVocab;
import com.hp.hpl.jena.tdb.ConfigTest;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;
import com.hp.hpl.jena.tdb.store.GraphNamedTDB;
import com.hp.hpl.jena.tdb.store.GraphTDB;
import com.hp.hpl.jena.tdb.store.GraphTDBBase;
import com.hp.hpl.jena.tdb.store.GraphTriplesTDB;

public class TestTDBAssembler extends BaseTest
{
    // Can be slow - explicitly closes the dataset.
    static final String dirAssem    = "testing/Assembler" ;
    static final String dirDB       = ConfigTest.getTestingDir()+"/DB" ;

    @BeforeClass static public void beforeClass()
    {
        FileOps.ensureDir(dirDB) ;
    }
    
    @Before public void before()
    {
        FileOps.clearDirectory(dirDB) ;
    }
    
    @AfterClass static public void afterClass()
    {
        FileOps.clearDirectory(dirDB) ;
    }
    
    @Test public void createDatasetDirect()
    {
        String f = dirAssem+"/tdb-dataset.ttl" ;
        Object thing = AssemblerUtils.build( f, VocabTDB.tDatasetTDB) ;
        assertTrue(thing instanceof Dataset) ;
        Dataset ds = (Dataset)thing ;
        ds.asDatasetGraph() ;
        assertTrue(((Dataset)thing).asDatasetGraph() instanceof DatasetGraphTDB) ;
        ds.close() ;
    }
    
    @Test public void createDatasetEmbed()
    {
        String f = dirAssem+"/tdb-dataset-embed.ttl" ;
        Object thing = AssemblerUtils.build( f, DatasetAssemblerVocab.tDataset) ;
        assertTrue(thing instanceof Dataset) ;
        Dataset ds = (Dataset)thing ;
        assertTrue(ds.asDatasetGraph() instanceof DatasetGraphTDB) ;
        ds.close();
    }

    @Test public void createGraphDirect()
    {
        testGraph(dirAssem+"/tdb-graph.ttl", false) ;
    }
    
    @Test public void createGraphEmbed()
    {
        String f = dirAssem+"/tdb-graph-embed.ttl" ;
        Object thing = null ;
        try { thing = AssemblerUtils.build( f, JA.Model) ; }
        catch (AssemblerException e)
        { 
            e.getCause().printStackTrace(System.err) ;
            throw e ;
        }
        
        assertTrue(thing instanceof Model) ;
        Graph graph = ((Model)thing).getGraph() ;
        
        assertTrue(graph instanceof GraphTDB) ; 
        assertTrue(graph instanceof GraphTriplesTDB) ;
        assertFalse(graph instanceof GraphNamedTDB) ;

        DatasetGraphTDB ds = ((GraphTDBBase)graph).getDataset() ;
        if ( ds != null )
            ds.close();
    }
    
    @Test public void createNamedGraph1()
    {
        testGraph(dirAssem+"/tdb-named-graph-1.ttl", true) ;
    }
    
    @Test public void createNamedGraph2()
    {
        testGraph(dirAssem+"/tdb-named-graph-2.ttl", true) ;
    }
    
    @Test public void createNamedGraphViaDataset()
    {
        testGraph(dirAssem+"/tdb-graph-ref-dataset.ttl",false) ;
    }

    private static void testGraph(String assemblerFile, boolean named) 
    {
        Object thing = null ;
        try { thing = AssemblerUtils.build( assemblerFile, VocabTDB.tGraphTDB) ; }
        catch (AssemblerException e)
        { 
            e.getCause().printStackTrace(System.err) ;
            throw e ;
        }


        assertTrue(thing instanceof Model) ;
        Graph graph = ((Model)thing).getGraph() ;
        
        assertTrue(graph instanceof GraphTDB) ; 
        if ( named )
        {
            assertFalse( graph instanceof GraphTriplesTDB) ;
            assertTrue(graph instanceof GraphNamedTDB) ;
        }
        else
        {
            assertTrue( graph instanceof GraphTriplesTDB) ;
            assertFalse(graph instanceof GraphNamedTDB) ;
        }
        
        DatasetGraphTDB ds = ((GraphTDBBase)graph).getDataset() ;
        if ( ds != null )
            ds.close();
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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