/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.op;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.sse.Tags;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;
import com.hp.hpl.jena.sparql.util.Utils;

/** Do-nothing class that means that tags/labels/comments can be left in the algebra tree.
 * If serialized, toString called on the object, reparsing yields a string.
 *  Can have zero one one sub ops.
 * @author Andy Seaborne
 */

public class OpLabel extends Op1
{
    // Beware : while this is a Op1, it might have no sub operation.
    // (label "foo") and (label "foo" (other ...)) are legal.
    // OpNull?
    
    // Better: string+(object for internal use only)+op?
    public static Op create(Object label, Op op) { return new OpLabel(label, op) ; }
    
    private Object object ;

    private OpLabel(Object thing) { this(thing, null) ; }
    
    private OpLabel(Object thing, Op op)
    {
        super(op) ;
        this.object = thing ;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! ( other instanceof OpLabel) )
            return false ;
        OpLabel opLabel = (OpLabel)other ;
        if ( ! Utils.equal(object, opLabel.object) )
            return false ;
        
        return Utils.equal(getSubOp(), opLabel.getSubOp()) ;
    }

    @Override
    public int hashCode()
    {
        int x = HashLabel ;
        x ^= Utils.hashCodeObject(object, 0) ;
        x ^= Utils.hashCodeObject(getSubOp(), 0) ;
        return x ;
    }

    public void visit(OpVisitor opVisitor)
    { opVisitor.visit(this) ; }

    public Object getObject() { return object ; } 
    
    public boolean hasSubOp() { return getSubOp() != null ; } 
    
    public String getName()
    {
        return Tags.tagLabel ;
    }

    @Override
    public Op apply(Transform transform, Op subOp)
    { return transform.transform(this, subOp) ; }

    @Override
    public Op copy(Op subOp)
    {
        return new OpLabel(object, subOp) ; 
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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