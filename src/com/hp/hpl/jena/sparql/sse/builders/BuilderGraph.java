/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.builders;


import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.core.DataSourceGraphImpl ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.NodeUtils ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;
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
        BuilderLib.checkTag(list, Tags.tagGraph) ;
        list = list.cdr();
        
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
     *    (default (graph ...))
     *    (namedgraph IRIa (graph ...))
     *    (namedgraph IRIb (graph ...))
     *    )
     * (graph ...) is an abbrevaition for a dataset with a default graph and no named graphs.
     */

    public static DatasetGraph buildDataset(Item item)
    {
        if (item.isNode() )
            BuilderLib.broken(item, "Attempt to build dataset from a plain node") ;

        if (item.isSymbol() )
            BuilderLib.broken(item, "Attempt to build dataset from a bare symbol") ;

        if ( item.isTagged(Tags.tagGraph) )
        {
            Graph g = BuilderGraph.buildGraph(item.getList()) ;
            DataSourceGraphImpl ds = new DataSourceGraphImpl(g) ;
            return ds ;
        }
        
        if ( ! item.isTagged(Tags.tagDataset) )
            BuilderLib.broken(item, "Wanted ("+Tags.tagDataset+"...)" );
        return buildDataset(item.getList()) ;
    }
    
    public static DatasetGraph buildDataset(ItemList list)
    {
        BuilderLib.checkTag(list, Tags.tagDataset) ;
        list = list.cdr();
        DataSourceGraphImpl ds = new DataSourceGraphImpl((Graph)null) ;
        
        for (Item item : list)
        {
            if ( item.isTagged(Tags.tagDefault) )
            {
                if ( ds.getDefaultGraph() != null )
                    BuilderLib.broken(item, "Multiple default graphs") ;
                // (default (graph ...))
                BuilderLib.checkLength(2, item.getList(), "Expected (default (graph...))") ;
                Graph g = BuilderGraph.buildGraph(item.getList().get(1)) ;
                ds.setDefaultGraph(g) ;
                continue ;
            }
            if ( item.isTagged(Tags.tagNamedGraph) )
            {
                ItemList ngList = item.getList() ;
                BuilderLib.checkLength(3, ngList, "Expected (namedgraph IRI (graph...))") ;
                Node n = BuilderNode.buildNode(ngList.get(1)) ;
                Graph g = BuilderGraph.buildGraph(item.getList().get(2)) ;
                ds.addGraph(n, g) ;
                continue ;
            }
            BuilderLib.broken(item, "Not expected in dataset") ;
        }
        if ( ds.getDefaultGraph() == null )
            ds.setDefaultGraph(GraphFactory.createDefaultGraph()) ;
            
        return ds ;
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

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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