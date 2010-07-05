/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.lang;

import java.io.StringReader ;

import org.junit.Test ;
import org.openjena.atlas.lib.SinkCounting ;
import org.openjena.atlas.lib.StrUtils ;
import org.openjena.riot.JenaReaderNTriples2 ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.ErrorHandlerTestLib.* ;
import org.openjena.riot.lang.LangNTriples ;
import org.openjena.riot.tokens.Tokenizer ;
import org.openjena.riot.tokens.TokenizerFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.RDFReader ;
import com.hp.hpl.jena.sparql.sse.SSE ;

/** Test of syntax by a triples parser (does not include node validitiy checking) */ 

public class TestLangNTriples extends TestLangNTuples
{
    // Test streaming interface.
    
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

    @Test
    public void nt_model_1()
    {
        Model m1 = parseToModel("<x> <p> \"abc\". ") ;
        assertEquals(1, m1.size()) ;
        Model m2 = parseToModel("<x> <p> \"abc\". ") ;
        assertTrue(m1.isIsomorphicWith(m2)) ;
        Graph g1 = SSE.parseGraph("(graph (triple <x> <p> \"abc\"))") ;
        assertTrue(g1.isIsomorphicWith(m1.getGraph())) ;
    }

    @Test(expected=ExFatal.class) 
    public void nt_only_1()
    {
        parseCount("<x> <p> <s> <g> .") ; 
    }

    @Test(expected=ExFatal.class) 
    public void nt_only_2()
    {
        parseCount("@base <http://example/> . <x> <p> <s> .") ; 
    }

    @Override
    protected long parseCount(String... strings)
    {
        String string = StrUtils.join("\n", strings) ;
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(string) ;
        SinkCounting<Triple> sink = new SinkCounting<Triple>() ;
        LangNTriples x = RiotReader.createParserNTriples(tokenizer, sink) ;
        x.getProfile().setHandler(new ErrorHandlerEx()) ;
        x.parse() ;
        return sink.getCount() ;
    }

    @Override
    protected void parseCheck(String... strings)
    {
        String string = StrUtils.join("\n", strings) ;
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(string) ;
        SinkCounting<Triple> sink = new SinkCounting<Triple>() ;
        LangNTriples x = RiotReader.createParserNTriples(tokenizer, sink) ;
        x.setProfile(RiotReader.profile(null, false, true, new ErrorHandlerEx())) ;
        x.parse() ;
    }

    protected Model parseToModel(String string)
    {
        StringReader r = new StringReader(string) ;
        Model model = ModelFactory.createDefaultModel() ;
        RDFReader reader = new JenaReaderNTriples2() ;
        reader.read(model, r, null) ;
        return model ;
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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