/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot;

import java.io.InputStream ;

import atlas.lib.NotImplemented ;
import atlas.lib.Sink ;
import atlas.lib.SinkNull ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.riot.lang.LangNQuads ;
import com.hp.hpl.jena.riot.tokens.Tokenizer ;
import com.hp.hpl.jena.riot.tokens.TokenizerFactory ;
import com.hp.hpl.jena.sparql.core.Quad ;


/** Jena reader for RIOT N-Triples */
public class JenaReaderNQuads extends JenaReaderRIOT
{
    @Override
    protected void readWorker(Model model, Tokenizer tokenizer, String base)
    {
        throw new NotImplemented() ;
//        Sink<Triple> sink = new SinkToGraphTriples(model.getGraph()) ;
//        LangNQuads parser = new LangNQuads(tokenizer, sink);
//        parser.parse() ;
//        tokenizer.close() ;
    }
    
    /** Parse - but do nothing else */
    public static void parse(InputStream input)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizer(input) ;
        Sink<Quad> sink = new SinkNull<Quad>() ;
        LangNQuads parser = new LangNQuads(tokenizer, sink) ;
        parser.parse() ;
        tokenizer.close();
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * (c) Copyright 2010 Talis Information Ltd.
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