/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.path;

import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

public class P_NegPropSet extends PathBase
{
    List<P_Path0> nodes ;
    List<Node> forwardNodes ;
    List<Node> backwardNodes ;
    
    public P_NegPropSet()
    {
        nodes = new ArrayList<P_Path0>() ;
        forwardNodes = new ArrayList<Node>() ;
        backwardNodes = new ArrayList<Node>() ;
    }
    
    // addFwd, addBkwd?
    public void add(P_Path0 p)
    {
        nodes.add(p) ;
        if ( p.isForward() )
            forwardNodes.add(p.getNode()) ;
        else
            backwardNodes.add(p.getNode()) ;
    }

    //public List<Node> getExcludedNodes() { return forwardNodes ; }

    public List<P_Path0> getNodes() { return nodes ; }
    public List<Node> getFwdNodes() { return forwardNodes ; }
    public List<Node> getBwdNodes() { return backwardNodes ; }

    //@Override
    public void visit(PathVisitor visitor)
    { visitor.visit(this) ; }

    @Override
    public boolean equalTo(Path path2, NodeIsomorphismMap isoMap)
    {
        if ( ! ( path2 instanceof P_NegPropSet ) ) return false ;
        P_NegPropSet other = (P_NegPropSet)path2 ;
        return nodes.equals(other.nodes) ;
    }

    @Override
    public int hashCode()
    {
        return nodes.hashCode() ^ hashNegPropClass  ;
    }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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