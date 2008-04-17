/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.tdb.pgraph.PGraphBase;

public class ReorderLib
{
    
    interface Weighter { float[] weights(PGraphBase graph, List<Triple> triples) ; }
    
    static ReorderPattern std = new NullReorder() ;
    
    /** Return the default pattern reorder engine. */  
    public static ReorderPattern get()
    {
        return std ;
    }

    // --------------------------------------
    // Identity reorder function
    static class NullReorder implements ReorderPattern
    {
        @Override
        public final List<Triple> reorder(PGraphBase graph, List<Triple> triples)
        {
            return triples ;
        }
    }
    
    // --------------------------------------
    // Per-triple weighting engine
    // (NB: Special case ?x rdf:type rdf:Resource for inference query)
    
    static abstract class WeighterBase implements ReorderPattern
    {
        @Override
        public final List<Triple> reorder(PGraphBase graph, List<Triple> triples)
        {
            // Higher weights are good (more selective)
            float[] weight = weights(graph, triples) ;

            List<Triple> reorderedTriples = new ArrayList<Triple>(triples.size()) ;
            for ( int i = 0 ; i < triples.size() ; i++ )
            {
                // Find largest.
                int j = next(weight) ;
                reorderedTriples.add(triples.get(j)) ;
                weight[j] = -1 ;
            }
            
            return reorderedTriples ;
        }
        
        public final float[] weights(PGraphBase graph, List<Triple> triples)
        {
            float[] weights = new float[triples.size()] ;
            for ( int i = 0 ; i < triples.size() ; i++ )
                weights[i] = weight(graph, triples.get(i)) ;
            return weights ;
        }

        /** Calculate the weight of a triple (heavier = more selective = good) */
        abstract protected float weight(PGraphBase graph, Triple triple) ;

        // Inline me.
        private static int next(float[] weight)
        {
            int idx = 0 ;
            float w = 0 ;
            for ( int i = 0 ; i < weight.length ; i++ )
            {
                if ( weight[i] > w )
                {
                    idx = i ;
                    w = weight[i] ;
                }
            }
            return idx ;
        }
    }
    
    // --------------------------------------
    // Some triple weighting algorithms
    
    static class CountVarBindings extends WeighterBase
    {
        @Override
        protected float weight(PGraphBase graph, Triple triple)
        {
            int x = 3 ;
            if ( Var.isVar(triple.getSubject()) )   x-- ;
            if ( Var.isVar(triple.getPredicate()) ) x-- ;
            if ( Var.isVar(triple.getObject()) )    x-- ;
            
            return (float)x ;
        }
    }
    
    // Property weighted variable counting
    static class PropertyAndCountVar extends WeighterBase
    {
        @Override
        protected float weight(PGraphBase graph, Triple triple)
        {
            //h = graph.getStatisticsHandler() ;
            // graph get couints
            throw new UnsupportedOperationException("PropertyAndCountVar.weight") ;
        }
    }
    
//    
//    // The main weighting functions 
//    
//
//    private float weightProperty(PGraphBase graph, Node property)
//    {
//        return 0 ;
//    }
//    
//    private float weightSubjectProperty(PGraphBase graph, Node subject, Node property)
//    {
//        return 0 ;
//    }
//    
//    private float weightPropertyObject(PGraphBase graph, Node subject, Node property)
//    {
//        return 0 ;
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