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

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.ErrorHandlerTestLib.ExFatal;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.sparql.core.DatasetGraph;

/** Test of syntax by a quads parser (does not include node validitiy checking) */

public class TestLangNQuads extends AbstractTestLangNTuples
{
    @Override
    protected Lang getLang() {
        return Lang.NQUADS;
    }

    @Override
    protected LangRIOT createLangRIOT(Tokenizer tokenizer, StreamRDF sink, ParserProfile profile) {
        return IteratorParsers.createParserNQuads(tokenizer, sink, profile);
    }


    @Test
    public void quad_1() {
        parseCount("<http://example/x> <http://example/p> <http://example/s> <http://example/g> .");
    }

    @Test
    public void quad_2() {
        parseException(ExFatal.class, "<http://example/x> <http://example/p> <http://example/s> <http://example/g>"); // No trailing DOT
    }

    @Test
    public void nq_only_1_no_tuples() {
        parseException(ExFatal.class, "<http://example/x> <http://example/p> <http://example/s> <http://example/g> <http://example/c> .");
    }

    @Test
    public void nq_only_2_no_base() {
        parseException(ExFatal.class, "@base <http://example/> . <http://example/x> <http://example/p> <http://example/s> .");
    }

    @Test
    public void dataset_1() {
        // This must parse to <http://example/g>
        DatasetGraph dsg = ParserTests.parser().fromString("<http://example/x> <http://example/p> <http://example/s> <http://example/g> .").lang(Lang.NQUADS).toDatasetGraph();
        assertEquals(1, dsg.size());
        assertEquals(1, dsg.getGraph(NodeFactory.createURI("http://example/g")).size());
        assertEquals(0, dsg.getDefaultGraph().size());
    }
}
