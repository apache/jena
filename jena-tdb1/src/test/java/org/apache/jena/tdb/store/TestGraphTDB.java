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

package org.apache.jena.tdb.store;

import org.apache.jena.graph.Graph ;
import org.apache.jena.sparql.graph.AbstractTestGraphAddDelete ;
import org.apache.jena.tdb.ConfigTest ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.junit.GraphLocation ;
import org.apache.jena.tdb.sys.TDBInternal;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/** Programmatic tests on persistent graph */
public class TestGraphTDB extends AbstractTestGraphAddDelete
{
    static GraphLocation graphLocation = null ;
    
    @BeforeClass public static void beforeClass()
    {
        TDBInternal.reset() ;
        graphLocation = new GraphLocation(Location.create(ConfigTest.getCleanDir())) ;
        graphLocation.release() ;
        graphLocation.clearDirectory() ;
        graphLocation.createGraph() ;
        graph = graphLocation.getGraph() ;
    }
    // ----------
    
    @AfterClass public static void afterClass()
    { 
        graphLocation.release() ;
        TDBInternal.reset() ;
        graphLocation.clearDirectory() ;
        ConfigTest.deleteTestingDirDB() ;
    }
    
    static Graph graph = null ;
    @Before public void before()
    { 
        if ( graph != null )
            graph.clear() ;
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
