/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot;

import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

import atlas.io.PeekReader;
import atlas.lib.Sink;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.n3.JenaReaderBase;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.riot.lang.LangTurtle;
import com.hp.hpl.jena.riot.lang.SinkToGraphTriples;
import com.hp.hpl.jena.riot.tokens.Tokenizer;
import com.hp.hpl.jena.riot.tokens.TokenizerText;
import com.hp.hpl.jena.tdb.graph.GraphFactory;
import com.hp.hpl.jena.util.FileUtils;


/** Jena's RDFReader interface for RIOT/Turtle */
public class JenaReaderTurtle2 extends JenaReaderBase
{
    @Override
    protected void readWorker(Model model, Reader reader, String base)
    {
        startRead(model, reader, base) ;
        parse() ;
        finishRead(model) ;
    }
    
    private LangTurtle parser = null ; 
    public void startRead(Model model, Reader reader, String base)
    {
        PeekReader peekReader = PeekReader.make(reader) ;
        Sink<Triple> sink = new SinkToGraphTriples(model.getGraph()) ;
        Tokenizer tokenizer = new TokenizerText(peekReader) ;
        parser = new LangTurtle(base, tokenizer, sink) ;
    }

    /** Access to the prefix map.  Valid after "startRead" */
    public PrefixMap getPrefixMap() { return parser.getPrefixMap() ; }
    public void parse() { parser.parse() ; }
    
    public void finishRead(Model model)
    {
        // Merge prefixes.
        for ( Map.Entry<String,IRI> e : getPrefixMap().getMapping().entrySet() )
            model.setNsPrefix(e.getKey(), e.getValue().toString()) ;
    }
    
    /** Parse - but do nothing else */
    public static void parse(InputStream input)
    {
        Reader reader = FileUtils.asUTF8(input) ;
        PeekReader peekReader = PeekReader.make(reader) ;
        Sink<Triple> sink = new SinkToGraphTriples(GraphFactory.sinkGraph()) ;
        Tokenizer tokenizer = new TokenizerText(peekReader) ;
        LangTurtle parser = new LangTurtle("http://test/base/", tokenizer, sink) ;
        parser.parse() ;
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