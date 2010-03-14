/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.lang;

import java.util.Iterator ;

import org.openjena.atlas.lib.Sink ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.riot.Checker ;
import com.hp.hpl.jena.riot.tokens.Token ;
import com.hp.hpl.jena.riot.tokens.TokenType ;
import com.hp.hpl.jena.riot.tokens.Tokenizer ;

/** N-Quads, N-triples parser framework, with both push and pull interfaces.
 * 
 * <ul>
 * <li>The {@link #parse} method processes the whole stream of tokens, 
 *   sending each to a {@link org.openjena.atlas.lib.Sink} object.</li>
 * <li>The <tt>Iterator&lt;X&gt;</tt> interface yields triples one-by-one.</li>
 *  </ul> 
 * 
 * Normally, bad terms causes the parser to stop (i.e. treat them as errors).
 * In addition, the NTuples subsystem allows triples/quads with "bad" terms
 * to be skipped.
 * 
 * Checking can be switched off completely. if the dat is known to be correct,
 * no checking can be a large performance gain. <i>Caveat emptor</i>.
 */

public abstract class LangNTuple<X> extends LangBase<X> implements Iterator<X>
{
    private static Logger log = LoggerFactory.getLogger(LangNTuple.class) ;
    
    public static final boolean STRICT = false ;
    protected boolean skipOnBadTerm = false ;
    
    protected LangNTuple(Tokenizer tokens,
                         Checker checker,
                         Sink<X> sink)
    { 
        super(tokens, sink, checker, LabelToNode.createScopeByDocument()) ;
    }

    /** Method to parse the whole stream of triples, sending each to the sink */ 
    @Override
    protected final void runParser()
    {
        while(hasNext())
        {
            X x = parseOne() ;
            if ( x != null )
                sink.send(x) ;
        }
    }

    // Assumes no syntax errors.
    //@Override
    public final boolean hasNext()
    {
        return super.moreTokens() ;
    }
    
    //@Override
    public final X next()
    {
        return parseOne() ;
    }
    
    //@Override
    public final void remove()
    { throw new UnsupportedOperationException(); }

    /** Parse one tuple - return object to be sent to the sink or null for none */ 
    protected abstract X parseOne() ;
    
    /** Note a tuple not being output */
    protected void skipOne(X object)
    {
        checker.getHandler().warning("Skip: "+object, -1, -1) ;
    }

    
    protected final Node parseIRIOrBNode(Token token)
    {
        if ( ! ( token.hasType(TokenType.BNODE) ||
                 token.hasType(TokenType.IRI) ) )
            exception("Expected BNode or IRI", token) ;
        return tokenAsNode(token) ;
    }

    protected final Node parseIRI(Token token)
    {
        if ( ! token.hasType(TokenType.IRI) )
            exception("Expected IRI", token) ;
        return tokenAsNode(token) ;
    }

    protected final Node parseRDFTerm(Token token)
    {
        switch(token.getType())
        {
            case IRI:
            case BNODE:
            case STRING2:
            case LITERAL_DT:
            case LITERAL_LANG:
                break ;
            case STRING1:
                if ( STRICT )
                    exception("Illegal single quoted string", token) ;
                break ;
            default:
                exception("Illegal object", token) ;
        }
            
        return tokenAsNode(token) ;
    }

    /** SkipOnBadTerm - do not output tuples with bad RDF terms */ 
    public boolean  getSkipOnBadTerm()                      { return skipOnBadTerm ; }
    /** SkipOnBadTerm - do not output tuples with bad RDF terms */ 
    public void     setSkipOnBadTerm(boolean skipOnBadTerm) { this.skipOnBadTerm = skipOnBadTerm ; }
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