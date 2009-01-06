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
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.sse.Tags;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

/** General procedure in algebra evaluation (a stored procedure facility)
 *  Syntax (ARQ extension): CALL <iri>(?x, ?y+3)
 *  
 *  See also the similary algebra form for property functions.  The difference is in argument handling.
 *  A property function has a URI and two argment lists, one for subject, one for objects.
 *  A procedure is a URI and a list of arguments.
 * 
 * @author Andy Seaborne
 */
public class OpProcedure extends Op1
{
    private Node procId ;
    private ExprList args = null ;

    public OpProcedure(Node procId, ExprList args, Op op)
    {
        super(op) ;   
        this.args = args ;
        this.procId = procId ;
    }
    
    public OpProcedure(String iri, ExprList args, Op op)
    {
        this(Node.createURI(iri), args, op) ;
    }
    
    public String getName()
    {
        return Tags.tagProc ;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if (other == this) return true;
        if ( ! (other instanceof OpProcedure) ) return false ;
        OpProcedure proc = (OpProcedure)other ;
        
        if ( ! procId.equals(proc.procId) ) return false ;
        if ( ! args.equals(proc.args) ) return false ;
        
        return getSubOp().equalTo(proc.getSubOp(), labelMap) ;
    }

    @Override
    public int hashCode()
    {
        int x = procId.hashCode() ;
        x ^= args.hashCode() ;
        x ^= getSubOp().hashCode() ;
        return x ;
    }

    public void visit(OpVisitor opVisitor)
    { opVisitor.visit(this) ; }

    @Override
    public Op apply(Transform transform, Op subOp)
    {
        return transform.transform(this, subOp) ;
    }

    @Override
    public Op copy(Op subOp)
    {
        return new OpProcedure(procId, args, getSubOp()) ;
    }

    public Node getProcId()
    {
        return procId ;
    }

    public String getURI()
    {
        return procId.getURI() ;
    }
    
    public ExprList getArgs()
    {
        return args ;
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