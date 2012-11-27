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

package org.openjena.riot.lang;

import java.io.StringReader ;

import org.apache.jena.atlas.lib.SinkCounting ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.Test ;
import org.openjena.riot.RiotException ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.ErrorHandlerTestLib.ErrorHandlerEx ;
import org.openjena.riot.ErrorHandlerTestLib.ExFatal ;
import org.openjena.riot.system.JenaReaderNTriples2 ;
import org.openjena.riot.system.RiotLib ;
import org.openjena.riot.tokens.Tokenizer ;

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
        Model m1 = parseToModel("<x> <p> \"abc-\\u00E9\". ") ;
        assertEquals(1, m1.size()) ;
        Model m2 = parseToModel("<x> <p> \"abc-\\u00E9\". ") ;
        assertTrue(m1.isIsomorphicWith(m2)) ;
        Graph g1 = SSE.parseGraph("(graph (triple <x> <p> \"abc-é\"))") ;
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

    @Test(expected=RiotException.class) 
    public void nt_only_5()
    {
        parseCount("<x> <p> \"é\" .") ; 
    }
    
    @Override
    protected long parseCount(String... strings)
    {
        String string = StrUtils.strjoin("\n", strings) ;
        Tokenizer tokenizer = tokenizer(string) ;
        SinkCounting<Triple> sink = new SinkCounting<Triple>() ;
        LangNTriples x = RiotReader.createParserNTriples(tokenizer, sink) ;
        x.getProfile().setHandler(new ErrorHandlerEx()) ;
        x.parse() ;
        return sink.getCount() ;
    }

    @Override
    protected void parseCheck(String... strings)
    {
        String string = StrUtils.strjoin("\n", strings) ;
        Tokenizer tokenizer = tokenizer(string) ;
        SinkCounting<Triple> sink = new SinkCounting<Triple>() ;
        LangNTriples x = RiotReader.createParserNTriples(tokenizer, sink) ;
        x.setProfile(RiotLib.profile(null, false, true, new ErrorHandlerEx())) ;
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
