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

import static org.junit.Assert.assertEquals;

import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.riot.ErrorHandlerTestLib.ExFatal ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.junit.Test ;

/** Test of syntax by a quads parser (does not include node validitiy checking) */

public class TestLangNQuads extends AbstractTestLangNTuples
{
    @Override
    protected Lang getLang() {
        return Lang.NQUADS ;
    }

    @Test
    public void quad_1() {
        parseCount("<x> <p> <s> <g> .");
    }

    @Test(expected = ExFatal.class)
    public void quad_2() {
        parseCount("<x> <p> <s> <g>"); // No trailing DOT
    }

    @Test(expected = ExFatal.class)
    public void nq_only_1_no_tuple5() {
        parseCount("<x> <p> <s> <g> <c> .");
    }

    @Test(expected = ExFatal.class)
    public void nq_only_2_no_base() {
        parseCount("@base <http://example/> . <x> <p> <s> .");
    }

    @Test
    public void dataset_1() {
        // This must parse to <g>
        DatasetGraph dsg = ParserTests.parser().fromString("<x> <p> <s> <g> .").lang(Lang.NQUADS).toDatasetGraph();
        assertEquals(1, dsg.size());
        assertEquals(1, dsg.getGraph(NodeFactory.createURI("g")).size());
        assertEquals(0, dsg.getDefaultGraph().size());
    }
}
