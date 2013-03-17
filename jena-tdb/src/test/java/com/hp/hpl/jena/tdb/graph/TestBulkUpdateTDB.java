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

package com.hp.hpl.jena.tdb.graph;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;
import com.hp.hpl.jena.tdb.TDBFactory;

@SuppressWarnings("deprecation")
// Bulk update handlering is on its way out from Jena.
public class TestBulkUpdateTDB extends BaseTest
{
    //private static PrefixMapping 
    
    private static String graphName = "http://graph/";
    
    private static Node n0 = NodeFactoryExtra.parseNode("<http://example/n0>") ; 
    private static Node n1 = NodeFactoryExtra.parseNode("<http://example/n1>") ;
    private static Node n2 = NodeFactoryExtra.parseNode("<http://example/n2>") ;
    
    private static Triple t1 = SSE.parseTriple("(<x> <y> <z>)") ;
    
    private static Graph create()
    {
        Graph g = TDBFactory.createDatasetGraph().getDefaultGraph() ;
        return g ;
    }
    
    @Test public void update1()
    {
        Graph g = create() ;
        g.getBulkUpdateHandler().add(new Triple[]{t1}) ;
        assertTrue(g.contains(t1)) ;
        g.getBulkUpdateHandler().delete(new Triple[]{t1}) ;
        assertFalse(g.contains(t1)) ;
    }

    @Test public void update2()
    {
        Graph g = create() ;
        g.getBulkUpdateHandler().add(new Triple[]{t1}) ;
        assertTrue(g.contains(t1)) ;
        g.getBulkUpdateHandler().removeAll() ;
        assertFalse(g.contains(t1)) ;
    }


    @Test public void update3()
    {
        Dataset ds = TDBFactory.createDataset() ;
        
        ds.asDatasetGraph().getDefaultGraph().add(t1) ;
        ds.getNamedModel(graphName).getGraph().add(t1) ;
        
        Model m = ds.getDefaultModel() ;
        m.removeAll() ;
        assertEquals(0, m.size()) ;
        
        // But still in the other graph
        assertTrue(ds.getNamedModel(graphName).getGraph().contains(t1)) ;
    }

    @Test public void update4()
    {
        Dataset ds = TDBFactory.createDataset() ;
        
        ds.asDatasetGraph().getDefaultGraph().add(t1) ;
        ds.getNamedModel(graphName).getGraph().add(t1) ;
        
        Model m = ds.getNamedModel(graphName) ;
        m.removeAll() ;
        assertEquals(0, m.size()) ;
        
        // But still in the other graph
        assertTrue(ds.getDefaultModel().getGraph().contains(t1)) ;
    }

    
}
