/**
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

package org.openjena.riot.lang;

import java.util.ArrayList ;
import java.util.List ;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.tokens.Tokenizer ;
import org.openjena.riot.tokens.TokenizerFactory ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;

/** System-level testing of the parsers - testing the parser plumbing, not the language details */
public class TestParserFactory extends BaseTest
{
    static class CatchSink<T> implements Sink<T>
    {
        List<T> things = new ArrayList<T>() ;
        int flushCalled = 0 ;
        int closeCalled = 0 ;

        public void send(T item)
        { things.add(item) ; }

        public void flush() { flushCalled ++ ; }
        public void close() { closeCalled++ ; }
        
        public T getLast()
        { 
            if ( things.size() == 0 ) return null ;
            return things.get(things.size()-1) ;
        }
    }
    
    @Test public void ntriples_01() 
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString("<x> <p> <q> .") ;
        CatchSink<Triple> sink = new CatchSink<Triple>() ;
        
        LangRIOT parser = RiotReader.createParserNTriples(tokenizer, sink) ;
        parserSetup(parser) ;
        parser.parse();
        assertEquals(1, sink.flushCalled) ;
        assertEquals(0, sink.closeCalled) ;
        assertEquals(1, sink.things.size()) ;
        assertEquals(SSE.parseTriple("(<x> <p> <q>)"), sink.getLast()) ;
    }
    
    @Test public void turtle_01() 
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString("<x> <p> <q> .") ; 
        CatchSink<Triple> sink = new CatchSink<Triple>() ;
        LangRIOT parser = RiotReader.createParserTurtle(tokenizer, "http://base/", sink) ;
        parserSetup(parser) ;
        parser.parse();
        assertEquals(1, sink.flushCalled) ;
        assertEquals(0, sink.closeCalled) ;
        assertEquals(1, sink.things.size()) ;
        assertEquals(SSE.parseTriple("(<http://base/x> <http://base/p> <http://base/q>)"), sink.getLast()) ;
    }
    
    @Test public void nquads_01() 
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString("<x> <p> <q> <g>.") ; 
        CatchSink<Quad> sink = new CatchSink<Quad>() ;
        LangRIOT parser = RiotReader.createParserNQuads(tokenizer, sink) ;
        parserSetup(parser) ;
        parser.parse();
        assertEquals(1, sink.flushCalled) ;
        assertEquals(0, sink.closeCalled) ;
        assertEquals(1, sink.things.size()) ;
        
        Quad q = SSE.parseQuad("(<g> <x> <p> <q>)") ;
        assertEquals(q, sink.getLast()) ;
    }

    @Test public void trig_01() 
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString("{ <x> <p> <q> }") ; 
        CatchSink<Quad> sink = new CatchSink<Quad>() ;
        LangRIOT parser = RiotReader.createParserTriG(tokenizer, "http://base/", sink) ;
        parserSetup(parser) ;
        parser.parse();
        assertEquals(1, sink.flushCalled) ;
        assertEquals(0, sink.closeCalled) ;
        assertEquals(1, sink.things.size()) ;
        
        Triple t = SSE.parseTriple("(<http://base/x> <http://base/p> <http://base/q>)") ;
        Quad q = new Quad(Quad.tripleInQuad, t) ;
        assertEquals(q, sink.getLast()) ;
    }
    
    @Test public void trig_02() 
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString("<g> { <x> <p> <q> }") ; 
        CatchSink<Quad> sink = new CatchSink<Quad>() ;
        LangRIOT parser = RiotReader.createParserTriG(tokenizer, "http://base/", sink) ;
        parserSetup(parser) ;
        parser.parse();
        assertEquals(1, sink.flushCalled) ;
        assertEquals(0, sink.closeCalled) ;
        assertEquals(1, sink.things.size()) ;
        
        Quad q = SSE.parseQuad("(<http://base/g> <http://base/x> <http://base/p> <http://base/q>)") ;
        assertEquals(q, sink.getLast()) ;
    }
    
    private static void parserSetup(LangRIOT parser)
    {
//        ParserProfile profile = new ParserProfileBase(null) ;
//        parser.setProfile(profile) ;
    }
}
