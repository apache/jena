/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.op;

import java.util.List;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.sse.Tags;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

/** A "sequence" is a join-like operation where it is know that the 
 * the output of one step can be fed into the input of the next 
 * (that is, no scoping issues arise). 
 * 
 * @author Andy Seaborne
 */

public class OpSeq extends OpN
{
    public static OpSeq create() { return new OpSeq() ; } 
    
    public static Op create(Op left, Op right)
    { 
        // Avoid stages of one.
        if ( left == null && right == null )
            return null ;
        if ( left == null )
            return right ;
        if ( right == null )
            return left ;
        // If left already an OpSeq ... maybe?
        if ( left instanceof OpSeq )
        {
            OpSeq opSeq = (OpSeq)left ;
            opSeq.add(right) ;
            return opSeq ; 
        }
        // Not a stage .. yet
        OpSeq stage = new OpSeq() ;
        stage.add(left) ;
        stage.add(right) ;
        return stage ;
    }
    
    private OpSeq()           { super() ; }
    private OpSeq(List elts)  { super(elts) ; }
    
    public String getName() { return Tags.tagSequence ; }

    public void visit(OpVisitor opVisitor) { opVisitor.visit(this) ; }
    
    public boolean equalTo(Op op, NodeIsomorphismMap labelMap)
    {
        if ( ! ( op instanceof OpSeq) ) return false ;
        OpSeq other = (OpSeq) op ;
        return super.sameAs(other, labelMap) ;
    }

    public Op apply(Transform transform, List elts)
    { return transform.transform(this, elts) ; }

    public Op copy(List elts)
    {
        return new OpSeq(elts) ; 
    }
}

/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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