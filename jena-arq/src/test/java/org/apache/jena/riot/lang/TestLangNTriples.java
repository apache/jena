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

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotReader ;
import org.apache.jena.riot.ErrorHandlerTestLib.ErrorHandlerEx ;
import org.apache.jena.riot.ErrorHandlerTestLib.ExFatal ;
import org.apache.jena.riot.out.CharSpace;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
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
        
        RDFDataMgr.read(m, r, null, RDFLanguages.NTRIPLES) ;
        assertEquals(1, m.size()) ;
        
        String x = m.listStatements().next().getSubject().getId().getLabelString() ;
        assertNotEquals(x, "a") ;
        
        // reset - reread -  new bNode.
        r = new StringReader(s) ;
        RDFDataMgr.read(m, r, null, RDFLanguages.NTRIPLES) ;
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

    @Test
    public void nt_only_5()
    {
        parseCount("<x> <p> \"é\" .") ; 
    }
    
    @Test(expected = RiotException.class)
    public void nt_only_5b()
    {
        parseCount(CharSpace.ASCII, "<x> <p> \"é\" .") ; 
    }
    
    @Override
    protected long parseCount(String... strings) {
        return parseCount(CharSpace.UTF8, strings);
    }
    
    private long parseCount(CharSpace charSpace, String... strings)
    {
        String string = StrUtils.strjoin("\n", strings) ;
        Tokenizer tokenizer = tokenizer(charSpace, string) ;
        StreamRDFCounting sink = StreamRDFLib.count() ;
        LangNTriples x = RiotReader.createParserNTriples(tokenizer, sink) ;
        x.getProfile().setHandler(new ErrorHandlerEx()) ;
        x.parse() ;
        return sink.count() ;
    }

    @Override
    protected LangRIOT createParser(Tokenizer tokenizer, StreamRDF sink)
    {
        return RiotReader.createParserNTriples(tokenizer, sink) ;
    } 

    protected Model parseToModel(String string)
    {
        StringReader r = new StringReader(string) ;
        Model model = ModelFactory.createDefaultModel() ;
        RDFDataMgr.read(model, r, null, RDFLanguages.NTRIPLES) ;
        return model ;
    }
}
