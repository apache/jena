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

package com.hp.hpl.jena.sparql.graph;

import org.junit.Test ;

import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;

/** Test the test for datasets and graphs */
public class TestGraphsMem extends GraphsTests
{
    //TODO reenable when memory DSGs support quads and union.
    @Override
    protected Dataset createDataset()
    {
        DatasetGraph dsg = DatasetGraphFactory.createMem() ;
        dsg.addGraph(NodeFactory.createURI(graph1), GraphFactory.createDefaultGraph()) ;
        dsg.addGraph(NodeFactory.createURI(graph2), GraphFactory.createDefaultGraph()) ;
        dsg.addGraph(NodeFactory.createURI(graph3), GraphFactory.createDefaultGraph()) ;
        return DatasetFactory.create(dsg) ;
    }
    
    @Override
    @Test public void graph4() {}           // Quad.unionGraph

    @Override
    @Test public void graph5() {}           // Quad.defaultGraphIRI

    @Override
    @Test public void graph6() {}           // defaultGraphNodeGenerated
    
    @Override
    @Test public void graph_api4() {}       // Quad.unionGraph
    
    @Override
    @Test public void graph_api5() {}       // defaultGraphIRI
    
    @Override
    @Test public void graph_api6() {}       // defaultGraphNodeGenerated
    
    @Override
    @Test public void graph_count4() {}       // Quad.unionGraph
    
    @Override
    @Test public void graph_count5() {}       // defaultGraphIRI
    
    @Override
    @Test public void graph_count6() {}       // defaultGraphNodeGenerated
    
}
