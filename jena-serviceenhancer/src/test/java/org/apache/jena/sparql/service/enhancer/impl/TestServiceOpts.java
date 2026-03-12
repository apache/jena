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

package org.apache.jena.sparql.service.enhancer.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpTable;

/** Test cases for (un)parsing key-value pairs from IRI schemes */
public class TestServiceOpts {
    @Test
    public void testParsing_01() {
        assertEquals(
                List.of("foo::bar", ":", "baz"),
                ServiceOpts.parseEntriesRaw("foo::bar:baz"));
    }

    @Test
    public void testParsing_02() {
        assertEquals(
                List.of("::::foo::bar", ":", "baz::", ":"),
                ServiceOpts.parseEntriesRaw("::::foo::bar:baz:::"));
    }

    @Test
    public void testParsing_03() {
        assertEquals(
                List.of("::", ":", "foo::bar", ":", "baz::", ":"),
                ServiceOpts.parseEntriesRaw(":::foo::bar:baz:::"));
    }

    @Test
    public void testRoundTrip_01() {
        String input = "::::foo::bar:baz:::";
        List<Entry<String, String>> opts = ServiceOpts.parseEntries(input);
        String unparsedStr = ServiceOpts.unparseEntries(opts);
        assertEquals(input, unparsedStr);
    }

    @Test
    public void testServiceOpts_01() {
        Node node = NodeFactory.createURI("cache:foo:bar:");
        OpService op = new OpService(node, OpTable.unit(), false);
        ServiceOpts opts = ServiceOptsSE.getEffectiveService(op);
        assertEquals("foo:bar:", opts.getTargetService().getService().getURI());
        assertEquals(List.of(new SimpleEntry<>("cache", null)), opts.getOptions());
    }
}
