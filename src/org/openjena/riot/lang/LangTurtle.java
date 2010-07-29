/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.lang;

import static org.openjena.riot.tokens.TokenType.DOT ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.riot.Lang ;
import org.openjena.riot.system.ParserProfile ;
import org.openjena.riot.tokens.Tokenizer ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
/** Turtle language */
public class LangTurtle extends LangTurtleBase<Triple>
{
    public LangTurtle(String baseURI, Tokenizer tokens, 
                      ParserProfile profile, 
                      Sink<Triple> sink) 
    {
        super(baseURI, tokens, profile, sink) ;
        setCurrentGraph(null) ;
    }

    //@Override
    public Lang getLang()   { return Lang.TURTLE ; }
    
    @Override
    protected final void oneTopLevelElement()
    {
        triplesSameSubject() ;
    }
    
    @Override
    protected void expectEndOfTriples()
    {
        // The DOT is required by Turtle (strictly).
        // It is not in N3 and SPARQL.
        if ( strict )
            expect("Triples not terminated by DOT", DOT) ;
        else
            expectOrEOF("Triples not terminated by DOT", DOT) ;
    }
    
    @Override
    protected void emit(Node subject, Node predicate, Node object)
    {
        Triple t = profile.createTriple(subject, predicate, object, currLine, currCol) ;
        sink.send(t) ;
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