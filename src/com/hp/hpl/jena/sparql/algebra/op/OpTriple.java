/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.op;

import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.sse.Tags;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;
import com.hp.hpl.jena.sparql.util.Utils;

/** Algebra operation for a single triple.  Not normally used - triples are
 * contained in basic graph patterns (which is the unit of extension in SPARQL, 
 * and also the unit for adapting to other data store in ARQ).  But for
 * experimentation, it can be useful to have a convenience direct triple access.

 * @see OpBGP
 */ 
public class OpTriple extends Op0
{
    private final Triple triple ;
    private OpBGP opBGP = null ;
    
    public OpTriple(Triple triple)
    {
        this.triple = triple ;
    }
    
    public final Triple getTriple() { return triple ; }
    
    public final OpBGP asBGP()
    { 
        if ( opBGP == null )
        {
            BasicPattern bp = new BasicPattern() ;
            bp.add(getTriple()) ;
            opBGP = new OpBGP(bp) ;
        }
        return opBGP ;
    }        
    
    @Override
    public Op apply(Transform transform)
    { return transform.transform(this) ; }

    @Override
    public Op copy()
    {
        return new OpTriple(triple) ;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! (other instanceof OpTriple) )
            return false ;
        OpTriple opTriple = (OpTriple)other ;
        return Utils.tripleIso(getTriple(), opTriple.getTriple(), labelMap) ;
    }

    @Override
    public int hashCode()
    {
        return OpBase.HashTriple ^ triple.hashCode() ;
    }

    public void visit(OpVisitor opVisitor)
    { opVisitor.visit(this) ; }

    public String getName()
    {
        return Tags.tagTriple ;
    }

    public boolean equivalent(OpBGP opBGP)
    {
        BasicPattern bgp = opBGP.getPattern() ;
        if ( bgp.size() != 1 ) return false ;
        Triple t = bgp.get(0) ;
        return triple.equals(t) ;  
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