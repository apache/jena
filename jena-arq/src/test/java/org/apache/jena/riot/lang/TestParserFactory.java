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

import java.io.StringReader ;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.Test ;

/** System-level testing of the parsers - testing the parser plumbing, not the language details */
public class TestParserFactory extends BaseTest
{
    static class CatchParserOutput implements StreamRDF
    {
        List<Triple>      triples     = new ArrayList<>() ;
        List<Quad>        quads       = new ArrayList<>() ;
        List<Pair<String,String>>     prefixes     = new ArrayList<>() ;
        List<String>     bases       = new ArrayList<>() ;
        
        int startCalled = 0 ;
        
        int finishCalled = 0 ;
        
        @Override public void start()   { startCalled++ ; }
        
        @Override public void triple(Triple triple)     { triples.add(triple) ; }
        
        @Override public void quad(Quad quad)           { quads.add(quad) ; }
        
        @Override public void base(String base)         { bases.add(base) ; }
        
        @Override public void prefix(String prefix, String iri) { prefixes.add(Pair.create(prefix, iri)) ; }
        
        @Override public void finish()  { finishCalled++ ; }
    }
    
    @Test public void ntriples_01() 
    {
        {
            String s = "<http://base/x> <http://base/p> <http://base/q> ." ;
            CatchParserOutput sink = parseCapture(s, Lang.NT) ;
            assertEquals(1, sink.startCalled) ;
            assertEquals(1, sink.finishCalled) ;
            assertEquals(1, sink.triples.size()) ;
            assertEquals(0, sink.quads.size()) ;
            Triple t = SSE.parseTriple("(<http://base/x> <http://base/p> <http://base/q>)") ;
            assertEquals(t, last(sink.triples)) ;
        }

        // Old style, deprecated.
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString("<x> <p> <q> .") ;
        CatchParserOutput sink = new CatchParserOutput() ;
        @SuppressWarnings("deprecation")
        LangRIOT parser = RiotParsers.createParserNTriples(tokenizer, sink) ;
        parserSetup(parser) ;
        parser.parse();
        assertEquals(1, sink.startCalled) ;
        assertEquals(1, sink.finishCalled) ;
        assertEquals(1, sink.triples.size()) ;
        assertEquals(0, sink.quads.size()) ;
        assertEquals(SSE.parseTriple("(<x> <p> <q>)"), last(sink.triples)) ;
    }
    
    @Test public void turtle_01() 
    {
        {
            String s = "<x> <p> <q> ." ;
            CatchParserOutput sink = parseCapture(s, Lang.TTL) ;
            assertEquals(1, sink.startCalled) ;
            assertEquals(1, sink.finishCalled) ;
            assertEquals(1, sink.triples.size()) ;
            assertEquals(0, sink.quads.size()) ;
            Triple t = SSE.parseTriple("(<http://base/x> <http://base/p> <http://base/q>)") ;
            assertEquals(t, last(sink.triples)) ;
        }

        // Old style, deprecated.
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString("<x> <p> <q> .") ; 
        CatchParserOutput sink = new CatchParserOutput() ;
        @SuppressWarnings("deprecation")
        LangRIOT parser = RiotParsers.createParserTurtle(tokenizer, "http://base/", sink) ;
        parserSetup(parser) ;
        parser.parse();
        assertEquals(1, sink.startCalled) ;
        assertEquals(1, sink.finishCalled) ;
        assertEquals(1, sink.triples.size()) ;
        assertEquals(0, sink.quads.size()) ;
        assertEquals(SSE.parseTriple("(<http://base/x> <http://base/p> <http://base/q>)"), last(sink.triples)) ;
    }
    
    @Test public void nquads_01() 
    {
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
        // Old style, deprecated.
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString("<x> <p> <q> <g>.") ; 
        CatchParserOutput sink = new CatchParserOutput() ;
        @SuppressWarnings("deprecation")
        LangRIOT parser = RiotParsers.createParserNQuads(tokenizer, sink) ;
        parserSetup(parser) ;
        parser.parse();
        assertEquals(1, sink.startCalled) ;
        assertEquals(1, sink.finishCalled) ;
        assertEquals(0, sink.triples.size()) ;
        assertEquals(1, sink.quads.size()) ;
        Quad q = SSE.parseQuad("(<g> <x> <p> <q>)") ;
        assertEquals(q, last(sink.quads)) ;
    }

    @Test public void trig_01() 
    {
        String s = "{ <x> <p> <q> }" ; 
        CatchParserOutput sink = parseCapture(s, Lang.TRIG) ;
        assertEquals(1, sink.startCalled) ;
        assertEquals(1, sink.finishCalled) ;
        assertEquals(0, sink.triples.size()) ;
        assertEquals(1, sink.quads.size()) ;
        
        Triple t = SSE.parseTriple("(<http://base/x> <http://base/p> <http://base/q>)") ;
        Quad q = new Quad(Quad.tripleInQuad, t) ;
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
        RDFDataMgr.parse(sink, new StringReader(s), "http://base/", lang) ;
        return sink ;
    }

    private static <T> T last(List<T> list) 
    { 
        if ( list.isEmpty() ) return null ;
        return list.get(list.size()-1) ;
    }
    
    private static void parserSetup(LangRIOT parser)
    {
//        ParserProfile profile = new ParserProfileBase(null) ;
//        parser.setProfile(profile) ;
    }
}
