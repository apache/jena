/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.builders;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.core.DataSourceGraphImpl;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.ItemList;
import com.hp.hpl.jena.sparql.util.GraphUtils;
import com.hp.hpl.jena.sparql.util.NodeUtils;
import com.hp.hpl.jena.util.FileManager;

public class BuilderGraph
{
    static protected final String symGraph      = "graph" ;
    static protected final String symLoad       = "graph@" ;
    static protected final String symTriple     = "triple" ;
    static protected final String symQuad       = "quad" ;

    static protected final String symDataset    = "dataset" ;
    static protected final String symDefault    = "default" ;
    static protected final String symNamedGraph = "namedgraph" ;

    public static Graph buildGraph(Item item)
    {
        if (item.isNode() )
            BuilderBase.broken(item, "Attempt to build graph from a plain node") ;

        if (item.isSymbol() )
            BuilderBase.broken(item, "Attempt to build graph from a bare symbol") ;

        if ( item.isTagged(symGraph) )
            return buildGraph(item.getList()) ;
        if ( item.isTagged(symLoad) )
            return loadGraph(item.getList()) ;
        throw new BuildException("Wanted ("+symGraph+"...) or ("+symLoad+"...) : got: "+BuilderBase.shortPrint(item));
    }
    
    public static Graph buildGraph(ItemList list)
    {
        BuilderBase.checkTag(list, symGraph) ;
        list = list.cdr();
        Graph graph = Factory.createDefaultGraph() ;
        
        for ( Iterator iter = list.iterator() ; iter.hasNext() ; )
        {
            Item item = (Item)iter.next();
            BuilderBase.checkList(item) ;
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
            BuilderBase.broken(item, "Attempt to build dataset from a plain node") ;

        if (item.isSymbol() )
            BuilderBase.broken(item, "Attempt to build dataset from a bare symbol") ;

        if ( item.isTagged(BuilderGraph.symGraph) )
        {
            Graph g = BuilderGraph.buildGraph(item.getList()) ;
            DataSourceGraphImpl ds = new DataSourceGraphImpl(g) ;
            return ds ;
        }
        
        if ( ! item.isTagged(symDataset) )
            BuilderBase.broken(item, 
                               "Wanted ("+symDataset+"...) : got: "+BuilderBase.shortPrint(item));
        return buildDataset(item.getList()) ;
    }
    
    public static DatasetGraph buildDataset(ItemList list)
    {
        BuilderBase.checkTag(list, symDataset) ;
        list = list.cdr();
        DataSourceGraphImpl ds = new DataSourceGraphImpl((Graph)null) ;
        
        for ( Iterator iter = list.iterator() ; iter.hasNext() ; )
        {
            Item item = (Item)iter.next();
            if ( item.isTagged(symDefault) )
            {
                if ( ds.getDefaultGraph() != null )
                    BuilderBase.broken(item, "Multiple default graphs") ;
                // (default (graph ...))
                BuilderBase.checkLength(2, item.getList(), "Expected (default (graph...))") ;
                Graph g = BuilderGraph.buildGraph(item.getList().get(1)) ;
                ds.setDefaultGraph(g) ;
                continue ;
            }
            if ( item.isTagged(symNamedGraph) )
            {
                ItemList ngList = item.getList() ;
                BuilderBase.checkLength(3, ngList, "Expected (namedgraph IRI (graph...))") ;
                Node n = BuilderNode.buildNode(ngList.get(1)) ;
                Graph g = BuilderGraph.buildGraph(item.getList().get(2)) ;
                ds.addGraph(n, g) ;
                continue ;
            }
            BuilderBase.broken(item, "Not expected in dataset: "+BuilderBase.shortPrint(item)) ;
        }
        if ( ds.getDefaultGraph() == null )
            ds.setDefaultGraph(GraphUtils.makeDefaultGraph()) ;
            
        return ds ;
    }
    
    
    private static Graph loadGraph(ItemList list)
    {
        BuilderBase.checkLength(2, list, symLoad ) ;
        Item item = list.get(1) ;
        if ( ! item.isNode() )
            BuilderBase.broken(item, "Expected: ("+symLoad+" 'filename') : Got: "+BuilderBase.shortPrint(item)) ;
        String s = NodeUtils.stringLiteral(item.getNode()) ;
        if ( s == null )
            BuilderBase.broken(item, "Expected: ("+symLoad+" 'filename') : Got: "+BuilderBase.shortPrint(item)) ;
        return FileManager.get().loadModel(s).getGraph() ;
    }
    
    public static Triple buildTriple(ItemList list)
    {
        if ( list.size() != 3 && list.size() != 4 )
            BuilderBase.broken(list, "Not a triple: "+BuilderBase.shortPrint(list)) ;
        if ( list.size() == 4 )
        {
            if ( ! list.get(0).isSymbol(symTriple) )
                BuilderBase.broken(list, "Not a triple: "+BuilderBase.shortPrint(list)) ;
            list = list.cdr() ;
        }
        return _buildNode3(list) ;
    }

    public static Triple buildNode3(ItemList list)
    {
        BuilderBase.checkLength(3, list, null) ;
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
            BuilderBase.broken(list, "Not a quad: "+BuilderBase.shortPrint(list)) ;
        if ( list.size() == 5 )
        {
            if ( ! list.get(0).isSymbol(symQuad) )
                BuilderBase.broken(list, "Not a quad: "+BuilderBase.shortPrint(list)) ;
            list = list.cdr() ;
        }
        return _buildNode4(list) ;
    }
    
    public static Quad buildNode4(ItemList list)
    {
        BuilderBase.checkLength(4, list, null) ;
        return _buildNode4(list) ;
    }
    
    private static Quad _buildNode4(ItemList list)
    {
        Node g = null ;
        if ( "_".equals(list.get(0).getSymbol()) )
            g = Quad.defaultGraphNode ;
        else
            g = BuilderNode.buildNode(list.get(0)) ;
        Node s = BuilderNode.buildNode(list.get(1)) ;
        Node p = BuilderNode.buildNode(list.get(2)) ;
        Node o = BuilderNode.buildNode(list.get(3)) ;
        return new Quad(g, s, p, o) ; 
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