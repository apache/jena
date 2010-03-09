/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.lang;

import java.io.StringReader ;

import org.junit.Test ;
import atlas.lib.Sink ;
import atlas.lib.SinkCounting ;
import atlas.test.BaseTest ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.RDFReader ;
import com.hp.hpl.jena.riot.JenaReaderNTriples2 ;
import com.hp.hpl.jena.riot.ParseException ;
import com.hp.hpl.jena.riot.ParserFactory ;
import com.hp.hpl.jena.riot.tokens.Tokenizer ;
import com.hp.hpl.jena.riot.tokens.TokenizerFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.tdb.lib.DatasetLib ;


public class TestLangNQuads extends BaseTest
{
    // TODO Quads
    @Test public void nt0()
    {
        SinkCounting<Quad> sink = parseToSink("") ;
        assertEquals(0, sink.getCount()) ;
    }
    
    @Test public void nt1()
    {
        SinkCounting<Quad> sink = parseToSink("<x> <y> <z>.") ;
        assertEquals(1, sink.getCount()) ;
    }
    
    @Test public void nt2()
    {
        SinkCounting<Quad> sink = parseToSink("<x> <y> \"z\".") ;
        assertEquals(1, sink.getCount()) ;
    }
    
    @Test public void nt3()
    {
        SinkCounting<Quad> sink = parseToSink("<x> <y> <z>. <x> <y> <z>.") ;
        assertEquals(2, sink.getCount()) ;
    }

    @Test public void nt4()
    {
        SinkCounting<Quad> sink = parseToSink("<x> <y> \"123\"^^<int>.") ;
        assertEquals(1, sink.getCount()) ;
    }

    @Test public void nt5()
    {
        SinkCounting<Quad> sink = parseToSink("<x> <y> \"123\"@lang.") ;
        assertEquals(1, sink.getCount()) ;
    }
    
    @Test public void nt_reader_twice()
    {
        String s = "_:a <p> 'foo' . " ;
        StringReader r = new StringReader(s) ;
        Model m = ModelFactory.createDefaultModel() ;
        
        RDFReader reader = new JenaReaderNTriples2() ;
        reader.read(m, r, null) ;
        assertEquals(1, m.size()) ;
        
        String x = m.listStatements().next().getSubject().getId().getLabelString() ;
        assertNotEquals(x, "a") ;
        

        // reset - reread -  new bNode.
        r = new StringReader(s) ;
        reader.read(m, r, null) ;
        assertEquals(2, m.size()) ;
    }
    
    // Test iterator interface.

    // Test parse errors interface.
    @Test(expected=ParseException.class)
    public void nt_bad_01()
    {
        parseToSink("<x> <y> <z>") ;          // No DOT
    }
    
    @Test(expected=ParseException.class)
    public void nt_bad_02()
    {
        parseToSink("<x> _:a <z> .") ;        // Bad predicate
    }

    @Test(expected=ParseException.class)
    public void nt_bad_03()
    {
        parseToSink("<x> \"p\" <z> .") ;      // Bad predicate 
    }

    @Test(expected=ParseException.class)
    public void nt_bad_4()
    {
        parseToSink("\"x\" <p> <z> .") ;      // Bad subject
    }

    @Test(expected=ParseException.class)
    public void nt_bad_5()
    {
        parseToSink("<x> <p> ?var .") ;        // No variables 
    }
    
    @Test(expected=ParseException.class)
    public void nt_bad_6()
    {
        parseToSink("<x> <p> 123 .") ;        // No abbreviations. 
    }
    
    @Test(expected=ParseException.class)
    public void nt_bad_7()
    {
        parseToSink("<x> <p> x:y .") ;        // No prefixed names 
    }
    
    @Test
    public void quad_1()
    {
        parseToSink("<x> <p> <s> <g> .") ; 
    }
    
    @Test(expected=ParseException.class)
    public void quad_2()
    {
        parseToSink("<x> <p> <s> <g>") ;        // No trailing DOT
    }
    
    @Test
    public void dataset_1()
    {
        DatasetGraph dsg = parseToDataset("<x> <p> <s> <g> .") ;
        assertEquals(1,dsg.size()) ;
        assertEquals(1, dsg.getGraph(Node.createURI("g")).size()) ;
        assertEquals(0, dsg.getDefaultGraph().size()) ;
    }

    private static SinkCounting<Quad> parseToSink(String string)
    {
        
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(string) ;
        SinkCounting<Quad> sink = new SinkCounting<Quad>() ;
        
        LangNQuads x = ParserFactory.createParserNQuads(tokenizer, sink) ;
        x.parse() ;
        return sink ;
    }
    
    private static DatasetGraph parseToDataset(String string)
    {
        DatasetGraph dsg = DatasetLib.createDatasetGraphMem() ;
        Sink<Quad> sink = DatasetLib.datasetSink(dsg) ;
        
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(string) ;
        LangRIOT parser = ParserFactory.createParserNQuads(tokenizer, sink) ;
        parser.parse() ;
        sink.flush();
        return dsg ;
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