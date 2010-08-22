/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.modify.request;

import java.util.Iterator ;

import org.openjena.atlas.lib.Pair ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.TriplePath ;
import com.hp.hpl.jena.sparql.syntax.TripleCollector ;

/** Accumulate quads (including allowing variables) during parsing. */
public class QuadPatternAcc
{
    // A lists of Pairs: Node and Triple connector
    private Node currentNode ;
    private TripleCollector currentTripleCollector ;
    
    public QuadPatternAcc()
    {
        currentNode = null ;
        currentTripleCollector = new TripleAcc() ;
    }
    
    public void addTriple(Triple t) { currentTripleCollector.addTriple(t) ; }
    
    
    private static class X extends Pair<Node, TripleCollector> {

        public X(Node a, TripleCollector b)
        {
            super(a, b) ;
        }}
    
    private static class TripleAcc implements TripleCollector {
        private BasicPattern pattern = new BasicPattern() ; 

        public TripleAcc()
        {  }

        public boolean isEmpty() { return pattern.isEmpty() ; }
        
        public void addTriple(Triple t)
        { pattern.add(t) ; }
        
        public int mark() { return pattern.size() ; }
        
        public void addTriple(int index, Triple t)
        { pattern.add(index, t) ; }
        
        public void addTriplePath(TriplePath path)
        { throw new ARQException("Triples-only collector") ; }

        public void addTriplePath(int index, TriplePath path)
        { throw new ARQException("Triples-only collector") ; }
        
        public BasicPattern getPattern() { return pattern ; }
        public Iterator<Triple> patternElts() { return pattern.iterator(); }
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