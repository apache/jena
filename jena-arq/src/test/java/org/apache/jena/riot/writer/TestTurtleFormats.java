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

package org.apache.jena.riot.writer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.*;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;

/** Tests for Turtle and Trig formats */
@ParameterizedClass
@MethodSource("provideArgs")
public class TestTurtleFormats {

    public static Stream<Arguments> provideArgs() {
        List<Arguments> x = new ArrayList<>();
        x.add(Arguments.of("Turtle/Pretty", RDFFormat.TURTLE_PRETTY));
        x.add(Arguments.of("Turtle/Long", RDFFormat.TURTLE_LONG));
        x.add(Arguments.of("Trig/Pretty", RDFFormat.TRIG_PRETTY));
        x.add(Arguments.of("Trig/Long", RDFFormat.TRIG_LONG));
        return x.stream();
    }

    private static String DIR = "testing/RIOT/Writer/";

    private static String BASE = "http://BASE/";

    private final RDFFormat format;

    private final String filename;

    public TestTurtleFormats(String name, RDFFormat format) {
        this.format = format;
        if ( format.getLang().equals(Lang.TRIG) )
            this.filename = DIR+"rdfwriter-02.trig";
        else
            this.filename = DIR+"rdfwriter-01.ttl";
    }

    // read file, with external base URI
    private static Graph data(String fn, String baseURI) {
        Graph g1 = GraphFactory.createDefaultGraph();
        RDFParser.create()
            .base(BASE)
            .source(fn)
            .parse(g1);
        return g1;
    }

    // Stream writer (BLOCKS and FLAT) don't print a base URI unless explicitly given one in the data.

    @Test public void writer_parse_base_2() {
        RDFFormatVariant fmtVariant = format.getVariant();
        boolean isPretty = ( fmtVariant == RDFFormat.PRETTY || fmtVariant == RDFFormat.LONG );

        assumeTrue(isPretty);

        Graph g = data(filename, BASE);

        String written =
            RDFWriter.create()
                .base(BASE)
                .source(g)
                .set(RIOT.symTurtleDirectiveStyle, "sparql")
                .set(RIOT.symTurtleOmitBase, true)
                .format(format)
                .base(BASE)
                .asString();
        {
            // Same base URI => same graph
            Graph g1 = GraphFactory.createDefaultGraph();
            RDFParser.create()
                .base(BASE)
                .fromString(written)
                .lang(Lang.TTL)
                .parse(g1);
            assertTrue(g.isIsomorphicWith(g1));
        }
        {
            // Different base URI => different graph
            Graph g2 = GraphFactory.createDefaultGraph();
            String BASE2 = "http://BASE2/";
            RDFParser.create()
                .base(BASE2)
                .fromString(written)
                .lang(Lang.TTL)
                .parse(g2);
            assertFalse(g.isIsomorphicWith(g2));
        }
    }

    @Test public void writer_bad_data_iri_1() {
        // SSE allows aweful IRIs! Good for test data ...
        Graph g = SSE.parseGraph("(graph (:s :p <[[>))");
        String x = RDFWriter.source(g).base("http://example/").format(format).asString();
    }
}
