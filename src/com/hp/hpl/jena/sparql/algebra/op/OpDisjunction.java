/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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

/** N-way disjunction.
 *  OpUnion remains as the strict SPARQL algebra operator.
 */
public class OpDisjunction extends OpN
{
    public static OpDisjunction create() { return new OpDisjunction() ; }
    
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

        if ( left instanceof OpDisjunction )
        {
            OpDisjunction opDisjunction = (OpDisjunction)left ;
            opDisjunction.add(right) ;
            return opDisjunction ; 
        }
        
//        if ( right instanceof OpDisjunction )
//        {
//            OpDisjunction opSequence = (OpDisjunction)right ;
//            // Add front.
//            opDisjunction.getElements().add(0, left) ;
//            return opDisjunction ; 
//        }
        
        OpDisjunction stage = new OpDisjunction() ;
        stage.add(left) ;
        stage.add(right) ;
        return stage ;
    }

    private OpDisjunction(List<Op> elts) { super(elts) ; }
    private OpDisjunction() { super() ; }
    
    public String getName() { return Tags.tagDisjunction ; }
    
    public void visit(OpVisitor opVisitor)
    { opVisitor.visit(this) ; }
    //{ throw new ARQNotImplemented("OpDisjunction.visit") ; }

    
    @Override
    public boolean equalTo(Op op, NodeIsomorphismMap labelMap)
    {
        if ( ! ( op instanceof OpDisjunction) ) return false ;
        OpDisjunction other = (OpDisjunction) op ;
        return super.equalsSubOps(other, labelMap) ;
    }

    
    
    @Override
    public Op apply(Transform transform, List<Op> elts)
    { return transform.transform(this, elts) ; }
    //{ throw new ARQNotImplemented("OpDisjunction.apply") ; }

    @Override
    public Op copy(List<Op> elts)
    {
        return new OpDisjunction(elts) ; 
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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