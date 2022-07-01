/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.riot.lang;

import static org.junit.Assert.assertEquals;

import java.io.StringReader ;
import java.util.List ;

import org.apache.jena.graph.Triple ;
import org.apache.jena.irix.IRIs;
import org.apache.jena.irix.IRIxResolver;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFParser ;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.system.*;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerText;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.Test ;

/** System-level testing of the parsers - testing the parser plumbing, not the language details */
public class TestParserFactory
{
    @Test public void ntriples_01()
    {
        {
            String s = "<x> <p> <q> ." ;
            CatchParserOutput sink = parseCapture(s, Lang.NT) ;
            assertEquals(1, sink.startCalled) ;
            assertEquals(1, sink.finishCalled) ;
            assertEquals(1, sink.triples.size()) ;
            assertEquals(0, sink.quads.size()) ;
            Triple t = SSE.parseTriple("(<x> <p> <q>)") ;
            assertEquals(t, last(sink.triples)) ;
        }

        // Old style, direct to LangRIOT -- very deprecated.
        // NQ version tests that relative URIs remain relative.
        Tokenizer tokenizer = TokenizerText.create().fromString("<x> <p> <q> .").build();
        CatchParserOutput sink = new CatchParserOutput() ;
        IRIxResolver resolver = IRIs.relativeResolver();
        ParserProfile profile = makeParserProfile(IRIs.relativeResolver(), null, false);
        LangRIOT parser = RiotParsers.createParserNTriples(tokenizer, sink, profile) ;
        parser.parse();
        assertEquals(1, sink.startCalled) ;
        assertEquals(1, sink.finishCalled) ;
        assertEquals(1, sink.triples.size()) ;
        assertEquals(0, sink.quads.size()) ;
        assertEquals(SSE.parseTriple("(<x> <p> <q>)"), last(sink.triples)) ;
    }

    @Test public void turtle_01()
    {
        // Verify the expected output works.
        String s = "<x> <p> <q> ." ;
        CatchParserOutput sink = parseCapture(s, Lang.TTL) ;
        assertEquals(1, sink.startCalled) ;
        assertEquals(1, sink.finishCalled) ;
        assertEquals(1, sink.triples.size()) ;
        assertEquals(0, sink.quads.size()) ;
        Triple t = SSE.parseTriple("(<http://base/x> <http://base/p> <http://base/q>)") ;
        assertEquals(t, last(sink.triples)) ;
    }

    private ParserProfile makeParserProfile(IRIxResolver resolver, ErrorHandler errorHandler, boolean checking) {
        if ( errorHandler == null )
            errorHandler = ErrorHandlerFactory.errorHandlerStd;
        return new ParserProfileStd(RiotLib.factoryRDF(), errorHandler,
                                    resolver, PrefixMapFactory.create(),
                                    RIOT.getContext().copy(),
                                    checking, false);
    }

    @Test public void nquads_01()
    {
        String s = "<x> <p> <q> <g> ." ;
        CatchParserOutput sink = parseCapture(s, Lang.NQ) ;
        assertEquals(1, sink.startCalled) ;
        assertEquals(1, sink.finishCalled) ;
        assertEquals(0, sink.triples.size()) ;
        assertEquals(1, sink.quads.size()) ;
        Quad q = SSE.parseQuad("(<g> <x> <p> <q>)") ;
        assertEquals(q, last(sink.quads)) ;
    }

    @Test public void nquads_dft_triple() {
        // JENA-1854
        String s = "<x> <p> <q> ." ;
        CatchParserOutput sink = parseCapture(s, Lang.NQ) ;
        assertEquals(1, sink.startCalled) ;
        assertEquals(1, sink.finishCalled) ;
        assertEquals(0, sink.triples.size()) ;
        assertEquals(1, sink.quads.size()) ;

        Triple t = SSE.parseTriple("(<x> <p> <q>)") ;
        Quad q = new Quad(Quad.defaultGraphNodeGenerated, t) ;
        assertEquals(q, last(sink.quads)) ;
    }


    @Test public void trig_dft_triple() {
        // JENA-1854
        String s = "{ <x> <p> <q> }" ;
        CatchParserOutput sink = parseCapture(s, Lang.TRIG) ;
        assertEquals(1, sink.startCalled) ;
        assertEquals(1, sink.finishCalled) ;
        assertEquals(0, sink.triples.size()) ;
        assertEquals(1, sink.quads.size()) ;

        Triple t = SSE.parseTriple("(<http://base/x> <http://base/p> <http://base/q>)") ;
        Quad q = new Quad(Quad.defaultGraphNodeGenerated, t) ;
        assertEquals(q, last(sink.quads)) ;
    }

    @Test public void trig_02()
    {
        String s = "<g> { <x> <p> <q> }" ;
        CatchParserOutput sink = parseCapture(s, Lang.TRIG) ;
        assertEquals(1, sink.startCalled) ;
        assertEquals(1, sink.finishCalled) ;
        assertEquals(0, sink.triples.size()) ;
        assertEquals(1, sink.quads.size()) ;

        Quad q = SSE.parseQuad("(<http://base/g> <http://base/x> <http://base/p> <http://base/q>)") ;
        assertEquals(q, last(sink.quads)) ;
    }

    private CatchParserOutput parseCapture(String s, Lang lang) {
        CatchParserOutput sink = new CatchParserOutput() ;
        RDFParser.create().source(new StringReader(s)).base("http://base/").lang(lang).parse(sink);
        return sink ;
    }

    private static <T> T last(List<T> list)
    {
        if ( list.isEmpty() ) return null ;
        return list.get(list.size()-1) ;
    }
}
