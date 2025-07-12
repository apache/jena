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

import static org.apache.jena.riot.system.ErrorHandlerFactory.errorHandlerNoLogging;
import static org.apache.jena.riot.system.ErrorHandlerFactory.getDefaultErrorHandler;
import static org.apache.jena.riot.system.ErrorHandlerFactory.setDefaultErrorHandler;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.io.StringReader;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.ErrorHandlerTestLib.ExError;
import org.apache.jena.riot.ErrorHandlerTestLib.ExFatal;
import org.apache.jena.riot.ErrorHandlerTestLib.ExWarning;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.sparql.sse.SSE;

public class TestLangTurtle
{
    @Test
    public void blankNodes1() {
        String s = "_:a <http://example/p> 'foo' . ";
        StringReader r = new StringReader(s);
        Model m = ModelFactory.createDefaultModel();
        RDFDataMgr.read(m, r, null, RDFLanguages.TURTLE);
        assertEquals(1, m.size());

        String x = m.listStatements().next().getSubject().getId().getLabelString();
        assertNotEquals(x, "a");

        // reset - reread - new bNode.
        r = new StringReader(s);
        RDFDataMgr.read(m, r, null, RDFLanguages.TURTLE);
        assertEquals(2, m.size());
    }

    @Test
    public void blankNodes2() {
        // Duplicate.
        String s = "_:a <http://example/p> 'foo' . _:a <http://example/p> 'foo' .";
        StringReader r = new StringReader(s);
        Model m = ModelFactory.createDefaultModel();
        RDFDataMgr.read(m, r, null, RDFLanguages.TURTLE);
        assertEquals(1, m.size());
    }

    @Test
    public void updatePrefixMapping1() {
        Model model = ModelFactory.createDefaultModel();
        StringReader reader = new StringReader("@prefix x: <http://example/x>.");
        RDFDataMgr.read(model, reader, null, RDFLanguages.TURTLE);
        assertEquals(1, model.getNsPrefixMap().size());
        assertEquals("http://example/x", model.getNsPrefixURI("x"));
    }

    @Test
    public void updatePrefixMapping2() {
        // Test that prefixes are resolved
        Model model = ModelFactory.createDefaultModel();
        StringReader reader = new StringReader("BASE <http://example/> PREFIX x: <abc>");
        RDFDataMgr.read(model, reader, null, RDFLanguages.TURTLE);
        assertEquals(1, model.getNsPrefixMap().size());
        assertEquals("http://example/abc", model.getNsPrefixURI("x"));
    }

    @Test
    public void optionalDotInPrefix() {
        Model model = ModelFactory.createDefaultModel();
        StringReader reader = new StringReader("@prefix x: <http://example/x>");
        RDFDataMgr.read(model, reader, null, RDFLanguages.TURTLE);
        assertEquals(1, model.getNsPrefixMap().size());
        assertEquals("http://example/x", model.getNsPrefixURI("x"));
    }

    @Test
    public void optionalDotInBase() {
        Model model = ModelFactory.createDefaultModel();
        StringReader reader = new StringReader("@base <http://example/> <x> <p> <o> .");
        RDFDataMgr.read(model, reader, null, RDFLanguages.TURTLE);
        assertEquals(1, model.size());
        Resource r = model.createResource("http://example/x");
        Property p = model.createProperty("http://example/p");
        assertTrue(model.contains(r, p));
    }

    private static ErrorHandler errorhandler = null;
    @BeforeAll
    public static void beforeClass() {
        errorhandler = getDefaultErrorHandler();
        setDefaultErrorHandler(errorHandlerNoLogging);
    }

    @AfterAll
    public static void afterClass() {
        setDefaultErrorHandler(errorhandler);
    }

    // Call parser directly.

    private static Graph parse(String...strings) {
        String string = String.join("\n", strings);
        return ParserTests.parser().fromString(string).lang(Lang.TTL).toGraph();
    }

    private static Triple parseOneTriple(String...strings) {
        Graph graph = parse(strings);
        assertEquals(1, graph.size());
        return graph.find(null, null, null).next();
    }

    @Test
    public void triple()                { parse("<s> <p> <o> ."); }

    private <T extends Throwable> T parseException(Class<T> exClass, String... strings) {
        return assertThrowsExactly(exClass, ()->parse(strings));
    }

    private <T extends Throwable> T parseException(Class<T> exClass, Executable action) {
        return assertThrows(exClass, action);
    }

    @Test
    public void errorJunk_1()           { parseException(ExFatal.class, "<p>"); }

    @Test
    public void errorJunk_2()           { parseException(ExFatal.class, "<r> <p>"); }

    @Test
    public void errorNoPrefixDef()      { parseException(ExFatal.class, "x:p <p> 'q' ."); }

    @Test
    public void errorNoPrefixDefDT()    { parseException(ExFatal.class, "<p> <p> 'q'^^x:foo ."); }

    @Test
    public void errorBadDatatype()      { parseException(ExFatal.class, "<p> <p> 'q'^^."); }

    @Test
    public void errorBadURI_1()         { parseException(ExError.class, "<http://example/a b> <http://example/p> 123 ."); }

    @Test
    // Passes tokenization but fails IRI parsing.
    public void errorBadURI_2()         { parseException(ExWarning.class, "<http://example/a%XAb> <http://example/p> 123 ."); }

    // Bad URIs
    @Test
    public void errorBadURI_3()         { parseException(ExError.class, "@prefix ex:  <bad iri> .  ex:s ex:p 123 "); }

    @Test
    public void errorBadURI_4()         { parseException(ExError.class, "<x> <p> 'number'^^<bad uri> "); }

    // Structural errors.
    @Test
    public void errorBadList_1()        { parseException(ExFatal.class, "<x> <p> ("); }

    @Test
    public void errorBadList_2()        { parseException(ExFatal.class, "<x> <p> ( <z>"); }

    @Test
    public void turtle_01() {
        Triple t = parseOneTriple("<s> <p> 123 . ");
        Triple t2 = SSE.parseTriple("(<http://base/s> <http://base/p> 123)");
        assertEquals(t2, t);
    }

    @Test
    public void turtle_02() {
        Triple t = parseOneTriple("@base <http://example/> . <s> <p> 123 . ");
        Triple t2 = SSE.parseTriple("(<http://example/s> <http://example/p> 123)");
        assertEquals(t2, t);
    }

    @Test
    public void turtle_03() {
        Triple t = parseOneTriple("@prefix ex: <http://example/x/> . ex:s ex:p 123 . ");
        Triple t2 = SSE.parseTriple("(<http://example/x/s> <http://example/x/p> 123)");
        assertEquals(t2, t);
    }

    // RDF 1.2 (some basic testing)

    private static final String PREFIXES = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX : <ex:/> ";

    @Test
    public void turtle_rdf12_01() {
        Graph graph = parse(PREFIXES, "<< :s :p 123 >> . ");
        assertEquals(1, graph.size());
    }

    @Test
    public void turtle_rdf12_02() {
        Graph graph = parse(PREFIXES, "<< :s :p 123 >> :q 'abc' ");
        assertEquals(2, graph.size());
    }

    @Test
    public void turtle_rdf12_03() {
        Graph graph = parse(PREFIXES, "[] rdf:reifies <<( :s :p :o )>>");
        assertEquals(1, graph.size());
    }

    @Test
    public void turtle_rdf12_04() {
        // Triple term as subject
        parseException(ExFatal.class, PREFIXES, "<<( :s :p :o )>> :q :z ");
    }

    @Test
    public void turtle_rdf12_05() {
        // Triple term as subject
        parseException(ExFatal.class, PREFIXES, ":a <<( :s :p :o )>> :b :c");
    }

    @Test
    public void turtle_rdf12_11() {
        parseOneTriple("VERSION \"1.2\" <x:s> <x:p> 123 . ");
    }

    @Test
    public void turtle_rdf12_12() {
        parseOneTriple("VERSION '1.2' <x:s> <x:p> 123 . ");
    }

    @Test
    public void turtle_rdf12_13() {
        parseOneTriple("@version '1.2' . <x:s> <x:p> 123 . ");
    }

    public void turtle_rdf12_14() {
        parseOneTriple("@version \"1.2\" . <x:s> <x:p> 123 . ");
    }

    @Test
    public void turtle_rdf12_bad_11() {
        parseException(ExFatal.class, ()->parseOneTriple("VERSION '1.2' . <x:s> <x:p> 123 . "));
    }

    @Test
    public void turtle_rdf12_bad_12() {
        parseException(ExFatal.class, ()->parseOneTriple("VERSION '''1.2''' <x:s> <x:p> 123 . "));
    }

    @Test
    public void turtle_rdf12_bad_13() {
        parseException(ExFatal.class, ()->parseOneTriple("@version \"\"\"1.2\"\"\" <x:s> <x:p> 123 . "));
    }

    // U+D800-U+DBFF is a high surrogate (first part of a pair)
    // U+DC00-U+DFFF is a low surrogate (second part of a pair)
    // so D800-DC00 is legal.

    @Test public void turtle_surrogate_1() {
        // escaped high, escaped low
        parseOneTriple("<x:s> <x:p> '\\ud800\\udc00' . ");
    }

    @Test public void turtle_surrogate_2() {
        // escaped high, raw low
        parseOneTriple("<x:s> <x:p> '\\ud800\udc00' . ");
    }

    // Compilation failure. (maven+openjdk - OK in Eclipse, and test correct)
//    @Test public void turtle_surrogate_3() {
//        // raw high, escaped low
//        parseOneTriple("<x:s> <x:p> '\ud800\\udc00' . ");
//    }

    @Test public void turtle_surrogate_4() {
        // raw high, raw low
        parseOneTriple("<x:s> <x:p> '\ud800\udc00' . ");
    }

    @Test
    public void turtle_bad_surrogate_1() {
        parseException(ExFatal.class, ()->parseOneTriple("<x:s> <x:p> '\\ud800' . "));
    }

    @Test
    public void turtle_bad_surrogate_2() {
        parseException(ExFatal.class, ()->parseOneTriple("<x:s> <x:p> '\\udfff' . "));
    }
    @Test
    public void turtle_bad_surrogate_3() {
        parseException(ExFatal.class, ()->parseOneTriple("<x:s> <x:p> '\\U0000d800' . "));
    }

    @Test
    public void turtle_bad_surrogate_4() {
        parseException(ExFatal.class, ()->parseOneTriple("<x:s> <x:p> '\\U0000dfff' . "));
    }

    @Test
    public void turtle_bad_surrogate_5() {
        // Wrong way round: low-high
        parseException(ExFatal.class, ()->parseOneTriple("<x:s> <x:p> '\\uc800\\ud800' . "));
    }

    // Compilation failure. Can't write \ud800
//    @Test
//    public void turtle_bad_surrogate_6() {
//        // raw low - escaped high
//        parseException(ExFatal.class, ()->parseOneTriple("<x:s> <x:p> '\ud800\\ud800' . ");
//    }

    @Test
    public void turtle_bad_surrogate_7() {
        // escaped low - raw high
        parseException(ExFatal.class, ()->parseOneTriple("<x:s> <x:p> '\\uc800\ud800' . "));
    }

    // No Formulae. Not trig.
    @Test
    public void turtle_50()     { parseException(ExFatal.class, "@prefix ex:  <http://example/> .  { ex:s ex:p 123 . } "); }

    @Test
    public void turtle_60()     { parseException(ExWarning.class, "@prefix xsd:  <http://www.w3.org/2001/XMLSchema#> . <x> <p> 'number'^^xsd:byte }"); }
}
