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

import static org.apache.jena.riot.system.ErrorHandlerFactory.errorHandlerNoLogging ;
import static org.apache.jena.riot.system.ErrorHandlerFactory.getDefaultErrorHandler ;
import static org.apache.jena.riot.system.ErrorHandlerFactory.setDefaultErrorHandler ;

import java.io.Reader ;
import java.io.StringReader ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.riot.ErrorHandlerTestLib.ErrorHandlerEx ;
import org.apache.jena.riot.ErrorHandlerTestLib.ExFatal ;
import org.apache.jena.riot.ErrorHandlerTestLib.ExWarning ;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.SinkRDF ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class TestLangTurtle extends BaseTest
{
    @Test public void blankNodes1()
    {
        String s = "_:a <http://example/p> 'foo' . " ;
        StringReader r = new StringReader(s) ;
        Model m = ModelFactory.createDefaultModel() ;
        RDFDataMgr.read(m, r, null, RDFLanguages.Turtle) ;
        assertEquals(1, m.size()) ;
        
        String x = m.listStatements().next().getSubject().getId().getLabelString() ;
        assertNotEquals(x, "a") ;

        // reset - reread - new bNode.
        r = new StringReader(s) ;
        RDFDataMgr.read(m, r, null, RDFLanguages.Turtle) ;
        assertEquals(2, m.size()) ;
    }
    
    @Test public void blankNodes2()
    {
        // Duplicate.
        String s = "_:a <http://example/p> 'foo' . _:a <http://example/p> 'foo' ." ;
        StringReader r = new StringReader(s) ;
        Model m = ModelFactory.createDefaultModel() ;
        RDFDataMgr.read(m, r, null, RDFLanguages.Turtle) ;
        assertEquals(1, m.size()) ;
    }

    
    @Test public void updatePrefixMapping()
    {
        Model model = ModelFactory.createDefaultModel() ;
        StringReader reader = new StringReader("@prefix x: <http://example/x>.") ;
        RDFDataMgr.read(model, reader, null, RDFLanguages.Turtle) ;
        assertEquals(1, model.getNsPrefixMap().size()) ;
        assertEquals("http://example/x", model.getNsPrefixURI("x")) ;
    }
    
    private static ErrorHandler errorhandler = null ;
    @BeforeClass public static void beforeClass()
    { 
        errorhandler = getDefaultErrorHandler() ;
        setDefaultErrorHandler(errorHandlerNoLogging) ;
    }

    @AfterClass public static void afterClass()
    { 
        setDefaultErrorHandler(errorhandler) ;
    }
    
    // Call parser directly.
    
    private static Graph parse(String ...strings)
    {
        String string = StrUtils.strjoin("\n", strings) ;
        Reader reader = new StringReader(string) ;
        String baseIRI = "http://base/" ;
        Tokenizer tokenizer = TokenizerFactory.makeTokenizer(reader) ;
        
        Graph graph = GraphFactory.createDefaultGraph() ;
        SinkRDF sink = RDFParserOutputLib.graph(graph) ;
        LangTurtle parser = RiotReader.createParserTurtle(tokenizer, "http://base/", sink) ;
        parser.getProfile().setHandler(new ErrorHandlerEx()) ;
        parser.parse() ; 
        return graph ;
    }
    
    private static Triple parseOneTriple(String ...strings)
    {
        Graph graph = parse(strings) ;
        assertEquals(1, graph.size()) ;
        return graph.find(null, null, null).next();
    }
    
//    private static void parseSilent(String string)
//    {
//        Reader reader = new StringReader(string) ;
//        Tokenizer tokenizer = TokenizerFactory.makeTokenizer(reader) ;
//        String baseIRI = "http://base/" ;
//        LangTurtle parser = RiotReader.createParserTurtle(tokenizer, baseIRI, new SinkNull<Triple>()) ;
//        parser.setProfile(RiotReader.profile(RDFLanguages.Turtle, baseIRI, ErrorHandlerLib.errorHandlerNoLogging)) ;
//        parser.parse() ;
//    }

    @Test
    public void triple()                    { parse("<s> <p> <o> .") ; }
    
    @Test(expected=ExFatal.class)
    public void errorJunk_1()               { parse("<p>") ; }
    
    @Test(expected=ExFatal.class)
    public void errorJunk_2()               { parse("<r> <p>") ; }

    @Test(expected=ExFatal.class)
    public void errorNoPrefixDef()          { parse("x:p <p> 'q' .") ; }
    
    @Test(expected=ExFatal.class)
    public void errorNoPrefixDefDT()        { parse("<p> <p> 'q'^^x:foo .") ; }

    @Test(expected=ExFatal.class)
    public void errorBadDatatype()          { parse("<p> <p> 'q'^^.") ; }
    
    @Test(expected=ExWarning.class)
    public void errorBadURI_1()
    { parse("<http://example/a b> <http://example/p> 123 .") ; }

    @Test(expected=ExWarning.class)
    public void errorBadURI_2()
    { parse("<http://example/a%XAb> <http://example/p> 123 .") ; }

    @Test //(expected=ExWarning.class)
    // No check for escape sequence case.
    public void errorBadURI_3()
    { parse("<http://example/a%Aab> <http://example/p> 123 .") ; }

    @Test
    public void turtle_01()         
    { 
        Triple t = parseOneTriple("<s> <p> 123 . ") ;
        Triple t2 = SSE.parseTriple("(<http://base/s> <http://base/p> 123)") ;
        assertEquals(t2, t) ;
    }

    @Test
    public void turtle_02()         
    { 
        Triple t = parseOneTriple("@base <http://example/> . <s> <p> 123 . ") ;
        Triple t2 = SSE.parseTriple("(<http://example/s> <http://example/p> 123)") ;
        assertEquals(t2, t) ;
    }

    @Test
    public void turtle_03()         
    { 
        Triple t = parseOneTriple("@prefix ex: <http://example/x/> . ex:s ex:p 123 . ") ;
        Triple t2 = SSE.parseTriple("(<http://example/x/s> <http://example/x/p> 123)") ;
        assertEquals(t2, t) ;
    }
    
    // No Formulae. Not trig.
    @Test (expected=ExFatal.class)
    public void turtle_10()     { parse("@prefix ex:  <http://example/> .  { ex:s ex:p 123 . } ") ; }
    
    // Bad terms.
    @Test (expected=ExWarning.class)
    public void turtle_20()     { parse("@prefix ex:  <bad iri> .  ex:s ex:p 123 ") ; }
    
    @Test (expected=ExWarning.class)
    public void turtle_21()     { parse("@prefix ex:  <http://example/> . ex:s <http://example/broken p> 123") ; }
    
    @Test (expected=ExWarning.class)
    public void turtle_22()     { parse("<x> <p> 'number'^^<bad uri> ") ; }

    @Test (expected=ExWarning.class)
    public void turtle_23()     { parse("@prefix xsd:  <http://www.w3.org/2001/XMLSchema#> . <x> <p> 'number'^^xsd:byte }") ; }

}
