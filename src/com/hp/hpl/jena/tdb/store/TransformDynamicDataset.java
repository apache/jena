/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import java.util.Set ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.TransformCopy ;
import com.hp.hpl.jena.sparql.algebra.op.OpAssign ;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph ;
import com.hp.hpl.jena.sparql.algebra.op.OpNull ;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern ;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;

public class TransformDynamicDataset extends TransformCopy
{
    private Set<Node> defaultGraph ;
    private Set<Node> namedGraphs ;

    public TransformDynamicDataset(Set<Node> defaultGraph, Set<Node> namedGraphs)
    {
        this.defaultGraph = defaultGraph ;
        this.namedGraphs = namedGraphs ;
    }
    
    @Override
    public Op transform(OpGraph opGraph, Op x)
    { 
        Node gn = opGraph.getNode() ;
        if ( namedGraphs.size() == 0 )
            return OpNull.create() ;
        
        if ( gn.isVariable() )
        {
            // AND REWRITE ?g everywhere.
            
            Var v = Var.alloc(gn) ; 
            // Assume few named graphs over a large collection.
            // rewrite as union of
            Op union = null ;
            for ( Node n : namedGraphs )
            {
                Op op = OpAssign.assign(x, v, NodeValue.makeNode(n)) ;
                union = OpUnion.create(union, op) ;
            }
            return union ;
        }

        // Not a variable.
        if ( ! namedGraphs.contains(gn) )
            // No match. 
            return OpNull.create() ;
        // Nothing to do.
        return super.transform(opGraph, x) ;
    }

    @Override
    public Op transform(OpQuadPattern opQuadPattern)
    {
        Node gn = opQuadPattern.getGraphNode() ;
        if ( gn.isVariable() )
        {
            // AND REWRITE ?g everywhere.

            Var v = Var.alloc(gn) ; 
            
            // Assume few named graphs over a large collection.
            // rewrite as union of
            Op union = null ;
            
            BasicPattern bgp = opQuadPattern.getBasicPattern() ;
            
            for ( Node n : namedGraphs )
            {
                Op quads2 = new OpQuadPattern(n, bgp) ;
                Op op = OpAssign.assign(quads2, v, NodeValue.makeNode(n)) ;
                union = OpUnion.create(union, op) ;
            }
            return union ;
        }

        // Not a variable.
        if ( ! namedGraphs.contains(gn) )
            // No match. 
            return OpNull.create() ;
        // Nothing to do.
        return super.transform(opQuadPattern) ;
    }
}

/*
 * (c) Copyright 2010 Talis Information Ltd.
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