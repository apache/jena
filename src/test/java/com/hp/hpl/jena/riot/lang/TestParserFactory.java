/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.lang;

import java.util.ArrayList ;
import java.util.List ;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.Sink ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.riot.RiotReader ;
import com.hp.hpl.jena.riot.tokens.Tokenizer ;
import com.hp.hpl.jena.riot.tokens.TokenizerFactory ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;

/** System-level testing of the parsers - tesing the parser plumbing, not the language details */
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
        parser.parse();
        assertEquals(1, sink.flushCalled) ;
        assertEquals(0, sink.closeCalled) ;
        assertEquals(1, sink.things.size()) ;
        
        Triple t = SSE.parseTriple("(<http://base/x> <http://base/p> <http://base/q>)") ;
        Quad q = new Quad(null, t) ;
        assertEquals(q, sink.getLast()) ;
    }

    
    //    private Triple triple(String sStr, String pStr, String oStr)
//    {
//        Node s = SSE.parseNode(sStr) ;
//        Node p = SSE.parseNode(pStr) ;
//        Node o = SSE.parseNode(oStr) ;
//        return new Triple(s,p,o) ;
//    }
//
//    private Quad quad(String gnStr, String sStr, String pStr, String oStr)
//    {
//        Node g = SSE.parseNode(gnStr) ;
//        Node s = SSE.parseNode(sStr) ;
//        Node p = SSE.parseNode(pStr) ;
//        Node o = SSE.parseNode(oStr) ;
//        return new Quad(g,s,p,o) ;
//    }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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