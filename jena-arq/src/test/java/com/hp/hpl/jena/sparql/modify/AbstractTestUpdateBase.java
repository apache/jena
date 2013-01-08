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

package com.hp.hpl.jena.sparql.modify;

import org.apache.jena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.GraphUtil ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.ARQTestSuite ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.UpdateAction ;

public abstract class AbstractTestUpdateBase extends BaseTest
{
    protected abstract GraphStore getEmptyGraphStore() ; 
    
    protected void defaultGraphData(GraphStore gStore, Graph data)
    {
        Graph g = gStore.getDefaultGraph() ;
        g.clear() ;
        GraphUtil.addInto(g, data) ;
    }
    
    protected void namedGraphData(GraphStore gStore, Node uri, Graph data)
    {
        Graph g = gStore.getGraph(uri) ;
        if ( g == null )
        {
            gStore.addGraph(uri, GraphFactory.createJenaDefaultGraph()) ;
            g = gStore.getGraph(uri) ;
        }
        else
            g.clear() ;
        GraphUtil.addInto(g,data) ;
    }
    
    protected static final String FileBase = ARQTestSuite.testDirUpdate ;
    
    protected static void script(GraphStore gStore, String filename)
    {
        UpdateAction.readExecute(FileBase+"/"+filename, gStore) ;
    }
    
    protected static boolean graphEmpty(Graph graph)
    {
        return graph.isEmpty() ; 
    }
    
    protected static boolean graphContains(Graph graph, Triple triple)
    {
        return graph.contains(triple) ; 
    }
}
