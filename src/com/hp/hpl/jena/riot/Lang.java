/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot;

import java.io.InputStream ;

import atlas.lib.Sink ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.riot.lang.LangRIOT ;
import com.hp.hpl.jena.riot.lang.LangNTriples ;
import com.hp.hpl.jena.riot.lang.LangTurtle ;
import com.hp.hpl.jena.riot.tokens.Tokenizer ;
import com.hp.hpl.jena.riot.tokens.TokenizerFactory ;


public class Lang
{
    /** Create a parser for N-Triples, with default behaviour */
    public static LangTurtle createParserTurtle(String baseIRI, InputStream input, Sink<Triple> sink)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizer(input) ;
        LangTurtle parser = new LangTurtle(baseIRI, tokenizer, new Checker(), sink, false, true) ; ;
        return parser ;
    }
    
    /** Create a parser forTurtle, with default behaviour */
    public static LangRIOT createParserNTriples(InputStream input, Sink<Triple> sink)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizer(input) ;
        LangNTriples parser = new LangNTriples(tokenizer, sink, new Checker(), false, true) ; ;
        return parser ;
    }
}

/*
 * (c) Copyright 2010 Talis Information Ltd.
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