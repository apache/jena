/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.n3.turtle;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;


public class TurtleRDFGraphInserter implements TurtleEventHandler
{
    Graph graph = null ;
    public TurtleRDFGraphInserter(Graph graph) { this.graph = graph ; }
    
    @Override
    public void triple(int line, int col, Triple triple)
    {
        //Check it's valid triple.
        Node s = triple.getSubject() ;
        Node p = triple.getPredicate() ;
        Node o = triple.getObject() ;
        
        if ( ! ( s.isURI() || s.isBlank() ) )
            throw new TurtleParseException("["+line+", "+col+"] : Error: Subject is not a URI or blank node") ;
        if ( ! p.isURI() )
            throw new TurtleParseException("["+line+", "+col+"] : Error: Predicate is not a URI") ;
        if ( ! ( o.isURI() || o.isBlank() || o.isLiteral() ) ) 
            throw new TurtleParseException("["+line+", "+col+"] : Error: Object is not a URI, blank node or literal") ;
        
        graph.add(triple) ;
    }

    @Override
    public void startFormula(int line, int col)
    { throw new TurtleParseException("["+line+", "+col+"] : Error: Formula found") ; }

    @Override
    public void endFormula(int line, int col)
    { throw new TurtleParseException("["+line+", "+col+"] : Error: Formula found") ; }

    @Override
    public void prefix(int line, int col, String prefix, String iri)
    { graph.getPrefixMapping().setNsPrefix(prefix, iri) ; }
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