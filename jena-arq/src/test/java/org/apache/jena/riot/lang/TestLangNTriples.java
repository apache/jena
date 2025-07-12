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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.lib.CharSpace;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.ErrorHandlerTestLib.ExFatal;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.sparql.sse.SSE;

/** Test of syntax by a triples parser (does not include node validity checking) */

public class TestLangNTriples extends AbstractTestLangNTuples
{
    // Test streaming interface.

    @Override
    protected Lang getLang() {
        return Lang.NTRIPLES;
    }


    @Override
    protected LangRIOT createLangRIOT(Tokenizer tokenizer, StreamRDF sink, ParserProfile profile) {
        return IteratorParsers.createParserNTriples(tokenizer, sink, profile);
    }

    @Test
    public void nt_reader_twice() {
        String s = "_:a <http://example/p> 'foo' . ";
        StringReader r = new StringReader(s);
        Model m = ModelFactory.createDefaultModel();

        RDFDataMgr.read(m, r, null, RDFLanguages.NTRIPLES);
        assertEquals(1, m.size());

        String x = m.listStatements().next().getSubject().getId().getLabelString();
        assertNotEquals(x, "a");

        // reset - reread - new bNode.
        r = new StringReader(s);
        RDFDataMgr.read(m, r, null, RDFLanguages.NTRIPLES);
        assertEquals(2, m.size());
    }

    @Test
    public void nt_model_1() {
        String input = "<http://example/x> <http://example/p> \"abc-\\u00E9\". ";
        Model m1 = ParserTests.parser().fromString(input).lang(Lang.NTRIPLES).toModel();
        assertEquals(1, m1.size());
        Model m2 = ParserTests.parser().fromString(input).lang(Lang.NTRIPLES).toModel();
        assertTrue(m1.isIsomorphicWith(m2));

        Graph g1 = SSE.parseGraph("(graph (triple <http://example/x> <http://example/p> \"abc-é\"))");
        assertTrue(g1.isIsomorphicWith(m1.getGraph()));
    }

    @Test
    public void nt_only_no_quads() {
        parseException(ExFatal.class, "<http://example/x> <http://example/p> <http://example/s> <http://example/g> .");
    }

    @Test
    public void nt_only_no_base() {
        parseException(ExFatal.class, "BASE <http://example/>  <http://example/x> <http://example/p> <http://example/s> .");
    }

    @Test
    public void nt_only_5() {
        parseCount("<http://example/x> <http://example/p> \"é\" .");
    }

    @Test
    public void nt_ascii() {
        assertThrows(ExFatal.class, ()->parseCount(CharSpace.ASCII, "<scheme:x> <scheme:p> <scheme:é> ."));
    }
}
