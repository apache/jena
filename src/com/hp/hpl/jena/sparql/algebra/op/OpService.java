/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.op;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.sse.Tags;
import com.hp.hpl.jena.sparql.syntax.ElementService ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

public class OpService extends Op1
{
    Node serviceNode = null ;
    private ElementService serviceElement = null ;    
    public OpService(Node serviceNode, Op subOp)
    {
        super(subOp) ;
        this.serviceNode = serviceNode ;
    }

    public OpService(Node serviceNode, Op subOp, ElementService elt)
    {
        super(subOp) ;
        this.serviceNode = serviceNode ;
        this.serviceElement = elt ;
    }
    
    @Override
    public Op apply(Transform transform, Op subOp)  { return transform.transform(this, subOp) ; }

    @Override
    public Op copy(Op newOp)                    { return new OpService(serviceNode, newOp) ; }
    public String getName()                     { return Tags.tagService ; }
    public void visit(OpVisitor opVisitor)      { opVisitor.visit(this) ; }
    public Node getService()                    { return serviceNode ;  }
    public ElementService getServiceElement()   { return serviceElement ;  }

    @Override
    public int hashCode()
    { return serviceNode.hashCode() ^ getSubOp().hashCode() ; }
    
    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! (other instanceof OpService) )
            return false ;
        OpService opService = (OpService)other ;
        if ( ! ( serviceNode.equals(opService.serviceNode) ) )
            return false ;
        return getSubOp().equalTo(opService.getSubOp(), labelMap) ;
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