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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.ErrorHandlerTestLib.ExError;
import org.apache.jena.riot.ErrorHandlerTestLib.ExFatal;
import org.apache.jena.riot.ErrorHandlerTestLib.ExWarning;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.sse.SSE;

/** Test the behaviour of the RIOT reader for TriG.  TriG includes checking of terms. */
public class TestLangTrig
{
    @Test public void trig_01()     { parse("{}"); }
    @Test public void trig_02()     { parse("{}."); }
    @Test public void trig_03()     { parse("<g> {}"); }

    @Test
    public void trig_04()     { parseException(ExFatal.class, "<g> = {}"); }

    @Test
    public void trig_05()     { parseException(ExFatal.class, "<g> = {} ."); }

    // Need to check we get resolved URIs.
    @Test public void trig_10() {
        DatasetGraph dsg = parse("{ <x> <p> <q> }");
        assertEquals(1, dsg.getDefaultGraph().size());
        Triple t = dsg.getDefaultGraph().find(null,null,null).next();
        Triple t2 = SSE.parseTriple("(<http://base/x> <http://base/p> <http://base/q>)");
        assertEquals(t2, t);
    }

    @Test public void trig_11() {
        DatasetGraph dsg = parse("""
                @prefix ex:  <http://example/> .
                { ex:s ex:p 123 }
                """);
        assertEquals(1, dsg.getDefaultGraph().size());
        Triple t = dsg.getDefaultGraph().find(null,null,null).next();
        Triple t2 = SSE.parseTriple("(<http://example/s> <http://example/p> 123)");
    }

    @Test
    public void trig_12() {
        parse("""
                PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#>
                { <x> <p> '1'^^xsd:byte }
              """);
    }

    // Also need to check that the RiotExpection is called in normal use.

    // Bad terms.
    @Test
    public void trig_bad_01() {
        parseException(ExError.class, """
                @prefix ex:  <bad iri> .
                { ex:s ex:p 123 }
              """);
    }

    @Test
    public void trig_bad_02() {
			parseException(ExError.class, """
                @prefix ex:  <http://example/> .
                { ex:s <http://example/broken p> 123 }
                """);
    }

    @Test
    public void trig_bad_03() {
        parseException(ExError.class, "{ <x> <p> 'number'^^<bad uri> }");

    }

    @Test
    public void trig_bad_04() {
        parseException(ExWarning.class, """
                @prefix xsd:  <http://www.w3.org/2001/XMLSchema#> .
                { <x> <p> 'number'^^xsd:byte }
                """);
    }

    private <T extends Throwable> T parseException(Class<T> exClass, String string) {
        return assertThrows(exClass, ()->parse(string));
    }

    private static DatasetGraph parse(String string) {
        return ParserTests.parser().fromString(string).lang(Lang.TRIG).toDatasetGraph();
    }

}
