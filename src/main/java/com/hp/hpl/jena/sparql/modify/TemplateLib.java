/**
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

import java.util.ArrayList ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;
import org.openjena.atlas.lib.MultiMap ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Substitute ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

public class TemplateLib
{
    // See also Substitute -- combine?
    // Or is this specifc enough to CONSTRUCT/Update template processing? 
    
    /** Take a template, as a list of quad patterns, a default graph, and a list of bindings,
     *  and produce a map of graph name to lists of triples.
     */
    public static MultiMap<Node, Triple> template(List<Quad> quads, final Node dftGraph, List<Binding> bindings)
    {
        if ( quads == null || quads.isEmpty() ) return null ; 
        
        quads = remapDefaultGraph(quads, dftGraph);
        MultiMap<Node, Triple> acc = calcTriples(quads, bindings) ;
        return acc ;
    }
    
    /**
     * Take a template, as a list of quad patterns, a default graph, and an iterator of bindings,
     * and produce an iterator of quads that result from applying the template to the bindings.
     */
    public static Iterator<Quad> template(List<Quad> quads, final Node dftGraph, Iterator<Binding> bindings)
    {
        if ( quads == null || quads.isEmpty() ) return null ;
        
        quads = remapDefaultGraph(quads, dftGraph);
        return calcQuads(quads, bindings);
    }
    
    protected static List<Quad> remapDefaultGraph(List<Quad> quads, final Node dftGraph)
    {
        // The default graph has been set to something else.
        if ( dftGraph != null )
        {
            Transform<Quad, Quad> nt = new Transform<Quad, Quad>() {
                public Quad convert(Quad quad)
                {
                    if ( ! quad.isDefaultGraph() ) return quad ;
                    
                    return new Quad(dftGraph, quad.getSubject(), quad.getPredicate(), quad.getObject()) ;
                }
            };
            quads = Iter.map(quads, nt) ;
        }
        return quads;
    }
    
//    /** Take a template, as a list of quad patterns, but only on one graph,  
//     *  a default graph, and a list of bindings,
//     *  and produce a lists of triples.
//     */
//    public static List<Triple> templateTriples(List<Quad> quads, List<Binding> bindings)
//    {
//        if ( quads == null || quads.isEmpty() ) return null ; 
//        MultiMap<Node, Triple> acc = calcTriples(quads, bindings) ;
//        return acc ;
//    }

    /** Substitute into quad patterns, and build a map of graph name to lists of triples */
    public static MultiMap<Node, Triple> calcTriples(List<Quad> quads, List<Binding> bindings)
    {
        QueryIterator qIter = new QueryIterPlainWrapper(bindings.iterator()) ;
        return subst(quads, qIter) ;
    }

    /** Substitute into quad patterns, and build a map of graph name to lists of triples */
    protected static MultiMap<Node, Triple> subst(List<Quad> quads, QueryIterator qIter)
    {
        MultiMap<Node, Triple> acc = MultiMap.createMapList() ;
    
        for ( ; qIter.hasNext() ; )
        {
            Map<Node, Node> bNodeMap = new HashMap<Node, Node>() ;
            Binding b = qIter.nextBinding() ;
            for ( Quad quad : quads )
                subst(acc, quad, b, bNodeMap) ;
        }
        return acc ;
    }

    static void subst(MultiMap<Node, Triple> acc, Quad quad, Binding b, Map<Node, Node> bNodeMap)
    {
        Quad q = subst(quad, b, bNodeMap) ;
        if ( ! q.isConcrete() )
        {
            Log.warn(TemplateLib.class, "Unbound quad: "+FmtUtils.stringForQuad(quad)) ;
            return ;
        }
        acc.put(q.getGraph(), q.asTriple()) ;
    }
    
    /** Substitute into quad patterns */
    public static Iterator<Quad> calcQuads(final List<Quad> quads, Iterator<Binding> bindings)
    {
        return Iter.mapMany(bindings, new Transform<Binding, Iterator<Quad>>()
        {
            public Iterator<Quad> convert(final Binding b)
            {
                // Iteration is a new mapping of bnodes. 
                final Map<Node, Node> bNodeMap = new HashMap<Node, Node>() ;

                List<Quad> quadList = new ArrayList<Quad>(quads.size());
                for (Quad quad : quads)
                {
                    Quad q = subst(quad, b, bNodeMap) ;
                    if ( ! q.isConcrete() )
                    {
                        Log.warn(TemplateLib.class, "Unbound quad: "+FmtUtils.stringForQuad(quad)) ;
                        continue ;
                    }
                    quadList.add(q);
                }
                return quadList.iterator();
                
                // Alternative implementation (doesn't need to create a new arraylist)
//                Iterator<Quad> it = Iter.map(quads.iterator(), new Transform<Quad, Quad>()
//                {
//                    public Quad convert(Quad quad)
//                    {
//                        Quad q = subst(quad, b, bNodeMap) ;
//                        if ( ! q.isConcrete() )
//                        {
//                            Log.warn(TemplateLib.class, "Unbound quad: "+FmtUtils.stringForQuad(quad)) ;
//                            return null;
//                        }
//                        return q;
//                    }
//                });
//                // Filter out any of the nulls that were introduced above
//                return Iter.removeNulls(it);
            }
        });
    }

    /** Substitute into a quad, with rewriting of bNodes */ 
    public static Quad subst(Quad quad, Binding b, Map<Node, Node> bNodeMap)
    {
        Node g = quad.getGraph() ;
        Node s = quad.getSubject() ; 
        Node p = quad.getPredicate() ;
        Node o = quad.getObject() ;
    
        Node g1 = g ;
        Node s1 = s ; 
        Node p1 = p ;
        Node o1 = o ;
        
        // replace blank nodes. 
        if ( g1.isBlank() || Var.isBlankNodeVar(g1) )
            g1 = newBlank(g1, bNodeMap) ;
        
        if ( s1.isBlank() || Var.isBlankNodeVar(s1) )
            s1 = newBlank(s1, bNodeMap) ;
    
        if ( p1.isBlank() || Var.isBlankNodeVar(p1) )
            p1 = newBlank(p1, bNodeMap) ;
    
        if ( o1.isBlank() || Var.isBlankNodeVar(o1) )
            o1 = newBlank(o1, bNodeMap) ;
    
        Quad q = quad ;
        if ( s1 != s || p1 != p || o1 != o || g1 != g )
            q = new Quad(g1, s1, p1, o1) ;
        
        Quad q2 = Substitute.substitute(q, b) ;
        return q2 ;
    }
    
    /** Substitute into a triple, with rewriting of bNodes */ 
    public static Triple subst(Triple triple, Binding b, Map<Node, Node> bNodeMap)
    {
        Node s = triple.getSubject() ; 
        Node p = triple.getPredicate() ;
        Node o = triple.getObject() ;
    
        Node s1 = s ; 
        Node p1 = p ;
        Node o1 = o ;
        
        if ( s1.isBlank() || Var.isBlankNodeVar(s1) )
            s1 = newBlank(s1, bNodeMap) ;
    
        if ( p1.isBlank() || Var.isBlankNodeVar(p1) )
            p1 = newBlank(p1, bNodeMap) ;
    
        if ( o1.isBlank() || Var.isBlankNodeVar(o1) )
            o1 = newBlank(o1, bNodeMap) ;
    
        Triple t = triple ;
        if ( s1 != s || p1 != p || o1 != o )
            t = new Triple(s1, p1, o1) ;
        
        Triple t2 = Substitute.substitute(t, b) ;
        return t2 ;
    }

    /** generate a blank node consistently */
    private static Node newBlank(Node n, Map<Node, Node> bNodeMap)
    {
        if ( ! bNodeMap.containsKey(n) ) 
            bNodeMap.put(n, Node.createAnon() );
        return bNodeMap.get(n) ;
    }
}
