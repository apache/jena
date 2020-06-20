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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.rdf.model.Property ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.riot.ErrorHandlerTestLib.ExFatal ;
import org.apache.jena.riot.ErrorHandlerTestLib.ExWarning ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

public class TestLangTurtle
{
    @Test public void blankNodes1()
    {
        String s = "_:a <http://example/p> 'foo' . " ;
        StringReader r = new StringReader(s) ;
        Model m = ModelFactory.createDefaultModel() ;
        RDFDataMgr.read(m, r, null, RDFLanguages.TURTLE) ;
        assertEquals(1, m.size()) ;
        
        String x = m.listStatements().next().getSubject().getId().getLabelString() ;
        assertNotEquals(x, "a") ;

        // reset - reread - new bNode.
        r = new StringReader(s) ;
        RDFDataMgr.read(m, r, null, RDFLanguages.TURTLE) ;
        assertEquals(2, m.size()) ;
    }
    
    @Test public void blankNodes2()
    {
        // Duplicate.
        String s = "_:a <http://example/p> 'foo' . _:a <http://example/p> 'foo' ." ;
        StringReader r = new StringReader(s) ;
        Model m = ModelFactory.createDefaultModel() ;
        RDFDataMgr.read(m, r, null, RDFLanguages.TURTLE) ;
        assertEquals(1, m.size()) ;
    }

    
    @Test public void updatePrefixMapping1()
    {
        Model model = ModelFactory.createDefaultModel() ;
        StringReader reader = new StringReader("@prefix x: <http://example/x>.") ;
        RDFDataMgr.read(model, reader, null, RDFLanguages.TURTLE) ;
        assertEquals(1, model.getNsPrefixMap().size()) ;
        assertEquals("http://example/x", model.getNsPrefixURI("x")) ;
    }
    
    @Test public void updatePrefixMapping2()
    {
        // Test that prefixes are resolved
        Model model = ModelFactory.createDefaultModel() ;
        StringReader reader = new StringReader("BASE <http://example/> PREFIX x: <abc>") ;
        RDFDataMgr.read(model, reader, null, RDFLanguages.TURTLE) ;
        assertEquals(1, model.getNsPrefixMap().size()) ;
        assertEquals("http://example/abc", model.getNsPrefixURI("x")) ;
    }
    

    @Test public void optionalDotInPrefix()
    {
        Model model = ModelFactory.createDefaultModel() ;
        StringReader reader = new StringReader("@prefix x: <http://example/x>") ;
        RDFDataMgr.read(model, reader, null, RDFLanguages.TURTLE) ;
        assertEquals(1, model.getNsPrefixMap().size()) ;
        assertEquals("http://example/x", model.getNsPrefixURI("x")) ;
    }

    @Test public void optionalDotInBase()
    {
        Model model = ModelFactory.createDefaultModel() ;
        StringReader reader = new StringReader("@base <http://example/> <x> <p> <o> .") ;
        RDFDataMgr.read(model, reader, null, RDFLanguages.TURTLE) ;
        assertEquals(1, model.size()) ;
        Resource r = model.createResource("http://example/x") ;
        Property p = model.createProperty("http://example/p") ;
        assertTrue(model.contains(r,p)) ;
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
    
    private static Graph parse(String ...strings) {
        return ParserTestBaseLib.parseGraph(Lang.TURTLE, strings) ;
    }
    
    private static Triple parseOneTriple(String ...strings)
    {
        Graph graph = parse(strings) ;
        assertEquals(1, graph.size()) ;
        return graph.find(null, null, null).next();
    }
    
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
    
    @Test(expected=ExFatal.class)
    public void errorBadURI_1()
    { parse("<http://example/a b> <http://example/p> 123 .") ; }

    @Test(expected=ExWarning.class)
    public void errorBadURI_2()
    { parse("<http://example/a%XAb> <http://example/p> 123 .") ; }

    @Test //(expected=ExWarning.class)
    // No check for escape sequence case.
    public void errorBadURI_3()
    { parse("<http://example/a%Aab> <http://example/p> 123 .") ; }

    // Bad URIs
    @Test (expected=ExFatal.class)
    public void errorBadURI_4()     { parse("@prefix ex:  <bad iri> .  ex:s ex:p 123 ") ; }
    
    @Test (expected=ExFatal.class)
    public void errorBadURI_5()     { parse("<x> <p> 'number'^^<bad uri> ") ; }
    
    @Test (expected=ExFatal.class)
    public void errorBadList_1()     { parse("<x> <p> (") ; }

    @Test (expected=ExFatal.class)
    public void errorBadList_2()     { parse("<x> <p> ( <z>") ; }

    @Test
    public void turtle_01() {
        Triple t = parseOneTriple("<s> <p> 123 . ") ;
        Triple t2 = SSE.parseTriple("(<http://base/s> <http://base/p> 123)") ;
        assertEquals(t2, t) ;
    }

    @Test
    public void turtle_02() {
        Triple t = parseOneTriple("@base <http://example/> . <s> <p> 123 . ") ;
        Triple t2 = SSE.parseTriple("(<http://example/s> <http://example/p> 123)") ;
        assertEquals(t2, t) ;
    }

    @Test
    public void turtle_03() {
        Triple t = parseOneTriple("@prefix ex: <http://example/x/> . ex:s ex:p 123 . ") ;
        Triple t2 = SSE.parseTriple("(<http://example/x/s> <http://example/x/p> 123)") ;
        assertEquals(t2, t) ;
    }
    
    // No Formulae. Not trig.
    @Test (expected=ExFatal.class)
    public void turtle_10()     { parse("@prefix ex:  <http://example/> .  { ex:s ex:p 123 . } ") ; }
    
    @Test (expected=ExWarning.class)
    public void turtle_20()     { parse("@prefix xsd:  <http://www.w3.org/2001/XMLSchema#> . <x> <p> 'number'^^xsd:byte }") ; }
}
