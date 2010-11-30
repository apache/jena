/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.op;

import java.util.List ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

/** A "sequence" is a join-like operation where it is know that the 
 * the output of one step can be fed into the input of the next 
 * (that is, no scoping issues arise). */

public class OpSequence extends OpN
{
    public static OpSequence create() { return new OpSequence() ; } 
    
    public static Op create(Op left, Op right)
    { 
        // Avoid stages of nothing
        if ( left == null && right == null )
            return null ;
        // Avoid stages of one.
        if ( left == null )
            return right ;
        if ( right == null )
            return left ;
        // If left already an OpSequence ... maybe?
        if ( left instanceof OpSequence )
        {
            OpSequence opSequence = (OpSequence)left ;
            opSequence.add(right) ;
            return opSequence ; 
        }
//        if ( right instanceof OpSequence )
//        {
//            OpSequence opSequence = (OpSequence)right ;
//            // Add front.
//            opSequence.getElements().add(0, left) ;
//            return opSequence ; 
//        }
        
        OpSequence stage = new OpSequence() ;
        stage.add(left) ;
        stage.add(right) ;
        return stage ;
    }
    
    private OpSequence()           { super() ; }
    private OpSequence(List<Op> elts)  { super(elts) ; }
    
    public String getName() { return Tags.tagSequence ; }

    public void visit(OpVisitor opVisitor) { opVisitor.visit(this) ; }
    
    @Override
    public boolean equalTo(Op op, NodeIsomorphismMap labelMap)
    {
        if ( ! ( op instanceof OpSequence) ) return false ;
        OpSequence other = (OpSequence) op ;
        return super.equalsSubOps(other, labelMap) ;
    }

    @Override
    public Op apply(Transform transform, List<Op> elts)
    { return transform.transform(this, elts) ; }

    @Override
    public Op copy(List<Op> elts)
    {
        return new OpSequence(elts) ; 
    }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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