/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver.stats;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.sse.Item;

public class ReorderWeighted implements ReorderPattern
{
    private StatsMatcher stats ;

    public ReorderWeighted(StatsMatcher stats)
    {
        this.stats = stats ;
    }
    
    
    
    @Override
    public List<Triple> reorder(Graph graph, List<Triple> triples)
    {
        int N = triples.size() ;

        List<Item[]> triples1 = new ArrayList<Item[]>() ;
        for ( Triple triple : triples )
        {
            Item[] t = new Item[3] ;
            t[0] = Item.createNode(triple.getSubject()) ;
            t[1] = Item.createNode(triple.getPredicate()) ;
            t[2] = Item.createNode(triple.getObject()) ;
            triples1.add(t) ;
        }        
        
        List<Triple> triples2 = new ArrayList<Triple>() ;
        Set<Var> bindings = new HashSet<Var>() ; 
        
        for ( int i = 0 ; i < N ; i++ )
        {
            int j = minimum(triples1) ;
            if ( j < 0 )
                System.err.println("No weight") ;
            Triple triple = triples.get(j) ; 
            triples2.add(triple) ;
            System.out.println("Choose: "+triple) ;
            // Update bound variables.
            for ( Item[] elts : triples1 )
                if ( elts != null ) update(triple, elts) ;
            triples1.set(j, null) ;
        }
        // Check
        return triples2 ;
    }

    private int minimum(List<Item[]> triples1)
    {
        int idx = -1 ;
        double w = Double.MAX_VALUE ;
        for ( int i = 0 ; i < triples1.size() ; i++ ) 
        {
            if ( triples1.get(i) == null )
                continue ;
            double w2 = stats.match(triples1.get(i)) ;
            if ( w2 >= 0 && w > w2 )
            {
                w = w2 ;
                idx = i ;
            }
        }
        return idx ;
    }



    private void update(Triple triple, Item[] elts)
    {
        update(triple.getSubject(), elts) ;
        update(triple.getPredicate(), elts) ;
        update(triple.getObject(), elts) ;
    }

    private void update(Node node, Item[] elts)
    {
        if ( Var.isVar(node) )
        {
            update(node, elts, 0) ;
            update(node, elts, 1) ;
            update(node, elts, 2) ;
        }
           
    }

    private void update(Node var, Item[] elts, int i)
    {
        Node n = elts[i].getNode() ;
        if ( n == null )
            return ;
        if ( n.equals(var) )
            elts[i] = StatsMatcher.TERM ;
            
    }

//    private double[] init(List<Triple> triples)
//    {
//        int N = triples.size() ;
//        double[] weights = new double[N] ;
//
//        for ( int i = 0 ; i < N ; i++ )
//            weights[i] = stats.match(triples.get(i)) ;
//        return weights ;
//    }
    
//    private int minimum(Item[] triple)
//    {
//        stats.ma
//        
//        double w = Double.MAX_VALUE ;
//        int j = -1 ;
//        for ( int i = 0 ; i < weights.length ; i++ )
//        {
//            double w2 = weights[i] ;
//            if ( w2 >= 0 && w > w2 )
//            {
//                w = weights[i] ;
//                j = i ;
//            }
//        }
//        return j ;
//    }
//
//    @Override
//    public Triple first(Graph graph, List<Triple> triples)
//    {
//        double[] weights = init(triples) ;
//        int idx = minimum(weights) ;
//        return triples.get(idx) ;
//    }
}
/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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