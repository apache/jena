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

package com.hp.hpl.jena.tdb.store;

import org.apache.jena.atlas.logging.Log ;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.sparql.graph.AbstractTestGraph2 ;
import com.hp.hpl.jena.tdb.ConfigTest;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.junit.GraphLocation;

/** Programmatic tests on persistent graph */
public class TestGraphTDB extends AbstractTestGraph2
{
    static GraphLocation graphLocation = null ;
    
    @BeforeClass public static void beforeClass()
    {
        StoreConnection.reset() ;
        graphLocation = new GraphLocation(new Location(ConfigTest.getCleanDir())) ;
        graphLocation.release() ;
        graphLocation.clearDirectory() ;
        graphLocation.createGraph() ;
        graph = graphLocation.getGraph() ;
    }
    // ----------
    
    @AfterClass public static void afterClass()
    { 
        graphLocation.release() ;
        graphLocation.clearDirectory() ;
        ConfigTest.deleteTestingDirDB() ;
    }
    
    static Graph graph = null ;
    @Before public void before()
    { 
        try {
            graph.clear() ;
        } catch (Exception ex)
        {
            Log.warn(this, "before() : "+ex.getMessage(), ex) ;
            // Problem - reset.
            beforeClass() ;
        }
        
    }
            
    @After public void after()   
    { 
    }
    
    @Override
    protected Graph emptyGraph()
    {
        return graph ;
    }
    
    @Override
    protected void returnGraph(Graph g)
    {}
}
