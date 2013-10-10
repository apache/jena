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

package com.hp.hpl.jena.tdb.assembler;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.FileOps ;
import org.junit.AfterClass ;
import org.junit.Before ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.assembler.JA ;
import com.hp.hpl.jena.assembler.exceptions.AssemblerException ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils ;
import com.hp.hpl.jena.sparql.core.assembler.DatasetAssemblerVocab ;
import com.hp.hpl.jena.tdb.ConfigTest ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.store.* ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTransaction ;

public class TestTDBAssembler extends BaseTest
{
    // Can be slow - explicitly closes the dataset.
    static String dirAssem      = null ;
    static final String dirDB   = ConfigTest.getTestingDir()+"/DB" ;

    @BeforeClass static public void beforeClass()
    {
        dirAssem = ConfigTest.getTestingDataRoot()+"/Assembler" ;
        FileOps.ensureDir(dirDB) ;
    }
    
    @Before public void before()
    {
        StoreConnection.reset() ;
        FileOps.clearDirectory(dirDB) ;
    }
    
    @AfterClass static public void afterClass()
    {
        StoreConnection.reset() ;
        FileOps.clearDirectory(dirDB) ;
    }
    
    @Test public void createDatasetDirect()
    {
        createTest(dirAssem+"/tdb-dataset.ttl", VocabTDB.tDatasetTDB) ;
    }
    
    @Test public void createDatasetEmbed()
    {
        createTest(dirAssem+"/tdb-dataset-embed.ttl", DatasetAssemblerVocab.tDataset) ;
    }
    
    private void createTest(String filename, Resource type)
    {
        Object thing = AssemblerUtils.build(filename, type) ; 
        assertTrue(thing instanceof Dataset) ;
        Dataset ds = (Dataset)thing ;
        assertTrue(ds.asDatasetGraph() instanceof DatasetGraphTransaction) ;
        assertTrue(ds.supportsTransactions()) ;
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

        DatasetGraphTDB ds = ((GraphTDB)graph).getDSG() ;
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
        
        DatasetGraphTDB ds = ((GraphTDB)graph).getDSG() ;
        if ( ds != null )
            ds.close();
    }
}
