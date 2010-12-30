/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.modify.request;

import java.util.ArrayList ;
import java.util.Collections ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.TriplePath ;
import com.hp.hpl.jena.sparql.syntax.TripleCollector ;

/** Accumulate quads (including allowing variables) during parsing. */
public class QuadsAcc implements TripleCollector
{
    // A lists of Pairs: Node and Triple connector
    
    private Node graphNode = Quad.defaultGraphNodeGenerated ;
    private List<Quad> quads = new ArrayList<Quad>() ;
    private List<Quad> quadsView = Collections.unmodifiableList(quads) ;
    
    public QuadsAcc()     {}
    
    protected void check(Triple triple) {} 
    protected void check(Quad quad) {} 
    
    public void setGraph(Node n) 
    { 
        graphNode = n ;
    }
    
    public Node getGraph()    { return graphNode ; }
    
    public List<Quad> getQuads()
    {
        return quadsView ;
    }
    
    //@Override
    public void addQuad(Quad quad)
    {
        check(quad) ;
        quads.add(quad) ;
    }

    //@Override
    public void addTriple(Triple triple)
    {
        check(triple) ;
        quads.add(new Quad(graphNode, triple)) ;
    }

    public void addTriple(int index, Triple triple)
    {
        check(triple) ;
        quads.add(index, new Quad(graphNode, triple)) ;
    }

    public void addTriplePath(TriplePath tPath)
    { throw new UnsupportedOperationException("Can't add paths to quads") ; }

    public void addTriplePath(int index, TriplePath tPath)
    { throw new UnsupportedOperationException("Can't add paths to quads") ; }

    public int mark()
    {
        return quads.size() ;
    }
    
    @Override
    public int hashCode() { return quads.hashCode() ; }

    @Override
    public boolean equals(Object other)
    {
        if ( ! ( other instanceof QuadsAcc ) ) return false ;
        QuadsAcc acc = (QuadsAcc)other ;
        return quads.equals(acc.quads) ; 
    }

}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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