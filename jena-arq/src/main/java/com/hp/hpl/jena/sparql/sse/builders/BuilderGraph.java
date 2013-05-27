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

package com.hp.hpl.jena.sparql.sse.builders;


import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.NodeUtils ;
import com.hp.hpl.jena.util.FileManager ;

public class BuilderGraph
{
    public static Graph buildGraph(Item item)
    { 
        Graph graph = GraphFactory.createDefaultGraph() ;
        buildGraph(graph, item) ;
        return graph ;
    }
    
    public static Graph buildGraph(ItemList itemList)
    { 
        Graph graph = GraphFactory.createDefaultGraph() ;
        buildGraph(graph, itemList) ;
        return graph ;
    }
    
    public static void buildGraph(Graph graph, Item item)
    {
        if (item.isNode() )
            BuilderLib.broken(item, "Attempt to build graph from a plain node") ;

        if (item.isSymbol() )
            BuilderLib.broken(item, "Attempt to build graph from a bare symbol") ;

        if ( item.isTagged(Tags.tagGraph) )
        {
            buildGraph(graph, item.getList()) ;
            return ;
        }
        
        if ( item.isTagged(Tags.tagLoad) )
        {
           loadGraph(graph, item.getList()) ;
           return ;
        }
        
        BuilderLib.broken(item, "Wanted ("+Tags.tagGraph+"...) or ("+Tags.tagLoad+"...)");
    }
    
    public static Graph buildGraph(Graph graph, ItemList list)
    {
        if ( ! list.isEmpty() && list.get(0).isSymbol() )
        {
            if ( list.get(0).isSymbol(Tags.tagGraph) )
                list = list.cdr();
        }
        
        for (Item item : list)
        {
            BuilderLib.checkList(item) ;
            Triple triple = buildTriple(item.getList()) ;
            graph.add(triple) ;
        }
        return graph ;
    }
    
    /** Format:
     * (dataset
     *    (graph ...))
     *    (graph IRIa ...))
     *    (graph IRIb ...))
     *    )
     * (graph ...) is an abbrevaition for a dataset with a default graph and no named graphs.
     */

    public static DatasetGraph buildDataset(Item item)
    {
        return buildDataset(DatasetGraphFactory.createMem(), item) ; 
    }
    
    public static DatasetGraph buildDataset(ItemList list)
    {
        return buildDataset(DatasetGraphFactory.createMem(), list) ; 
    }

    public static DatasetGraph buildDataset(DatasetGraph dsg, Item item)
    {
        if (item.isNode() )
            BuilderLib.broken(item, "Attempt to build dataset from a plain node") ;

        if (item.isSymbol() )
            BuilderLib.broken(item, "Attempt to build dataset from a bare symbol") ;

        if ( item.isTagged(Tags.tagGraph) )
        {
            Graph g = BuilderGraph.buildGraph(item.getList()) ;
            return DatasetGraphFactory.create(g) ;
        }
        
        if ( ! item.isTagged(Tags.tagDataset) )
            BuilderLib.broken(item, "Wanted ("+Tags.tagDataset+"...)" );
        return buildDataset(dsg, item.getList()) ;
    }
    
    public static DatasetGraph buildDataset(DatasetGraph dsg, ItemList list)
    {
        BuilderLib.checkTag(list, Tags.tagDataset) ;
        list = list.cdr();
        
        for (Item item : list)
        {
            if ( ! item.isTagged(Tags.tagGraph) )
                BuilderLib.broken(item, "Expected (graph ...) as elements of a dataset") ;
            
            Node name = null ;
            ItemList graphContent = item.getList().cdr();

            if ( !graphContent.isEmpty() && graphContent.car().isNode() )
            {
                name = graphContent.car().getNode();
                graphContent = graphContent.cdr() ;
            }
            
            Graph g ;
            if ( name == null )
            {
                g = dsg.getDefaultGraph() ;
                if ( g == null )
                {
                    g = GraphFactory.createDefaultGraph() ;
                    dsg.setDefaultGraph(g) ;
                }
            }
            else
            {
                g = dsg.getGraph(name) ;
                if ( g == null )
                {
                    g = GraphFactory.createDefaultGraph() ;
                    dsg.addGraph(name, g) ;
                }
            }
            BuilderGraph.buildGraph(g, graphContent) ;
        }
        return dsg ;
    }
    
    
    private static void loadGraph(Graph graph, ItemList list)
    {
        BuilderLib.checkLength(2, list, Tags.tagLoad ) ;
        Item item = list.get(1) ;
        if ( ! item.isNode() )
            BuilderLib.broken(item, "Expected: ("+Tags.tagLoad+" 'filename')") ;
        String s = NodeUtils.stringLiteral(item.getNode()) ;
        if ( s == null )
            BuilderLib.broken(item, "Expected: ("+Tags.tagLoad+" 'filename')") ;
        Model model = ModelFactory.createModelForGraph(graph) ;
        FileManager.get().readModel(model, s) ;
    }
    
    public static Triple buildTriple(ItemList list)
    {
        if ( list.size() != 3 && list.size() != 4 )
            BuilderLib.broken(list, "Not a triple", list) ;
        if ( list.size() == 4 )
        {
            if ( ! list.get(0).isSymbol(Tags.tagTriple) )
                BuilderLib.broken(list, "Not a triple") ;
            list = list.cdr() ;
        }
        return _buildNode3(list) ;
    }

    public static Triple buildNode3(ItemList list)
    {
        BuilderLib.checkLength(3, list, null) ;
        return _buildNode3(list) ;
    }
    
    private static Triple _buildNode3(ItemList list)
    {
        Node s = BuilderNode.buildNode(list.get(0)) ;
        Node p = BuilderNode.buildNode(list.get(1)) ;
        Node o = BuilderNode.buildNode(list.get(2)) ;
        return new Triple(s, p, o) ; 
    }
   
    public static Quad buildQuad(ItemList list)
    {
        if ( list.size() != 4 && list.size() != 5 )
            BuilderLib.broken(list, "Not a quad") ;
        if ( list.size() == 5 )
        {
            if ( ! list.get(0).isSymbol(Tags.tagQuad) )
                BuilderLib.broken(list, "Not a quad") ;
            list = list.cdr() ;
        }
        return _buildNode4(list) ;
    }
    
    public static Quad buildNode4(ItemList list)
    {
        BuilderLib.checkLength(4, list, null) ;
        return _buildNode4(list) ;
    }
    
    private static Quad _buildNode4(ItemList list)
    {
        Node g = null ;
        if ( list.get(0).equals(Item.defaultItem)) 
            g = Quad.defaultGraphNodeGenerated ;
        else
            g = BuilderNode.buildNode(list.get(0)) ;
        Node s = BuilderNode.buildNode(list.get(1)) ;
        Node p = BuilderNode.buildNode(list.get(2)) ;
        Node o = BuilderNode.buildNode(list.get(3)) ;
        return new Quad(g, s, p, o) ; 
    }


}
