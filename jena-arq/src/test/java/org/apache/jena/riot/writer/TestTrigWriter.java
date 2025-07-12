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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.*;
import org.apache.jena.sparql.graph.GraphFactory;

@ParameterizedClass
@MethodSource("provideArgs")
public class TestTrigWriter {

    private static Stream<Arguments> provideArgs() {
        List<Arguments> x = List.of
                (Arguments.of("Turtle", RDFFormat.TURTLE),
                 Arguments.of("Turtle/Pretty", RDFFormat.TURTLE_PRETTY),
                 Arguments.of("Turtle/Blocks", RDFFormat.TURTLE_BLOCKS),
                 Arguments.of("Turtle/Flat", RDFFormat.TURTLE_FLAT),
                 Arguments.of("Turtle/Long", RDFFormat.TURTLE_LONG),
                 Arguments.of("Trig", RDFFormat.TRIG),
                 Arguments.of("Trig/Pretty", RDFFormat.TRIG_PRETTY),
                 Arguments.of("Trig/Blocks", RDFFormat.TRIG_BLOCKS),
                 Arguments.of("Trig/Flat", RDFFormat.TRIG_FLAT),
                 Arguments.of("Trig/Long", RDFFormat.TRIG_LONG)
                        );
        return x.stream();
    }

    private static String DIR = "testing/RIOT/Writer/";
    private static String BASE = "http://BASE/";

    private final RDFFormat format;
    private final String filename;

    public TestTrigWriter(String name, RDFFormat format) {
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

    // .base() for Turtle.
    @Test public void writer_parse_base_1() {
        // This has a relative URI
        // Not an ideal URI but legal (host is upper case). Allowed.
        Graph g = data(filename, BASE);

        String written =
            RDFWriter.create()
                .base(BASE)
                .source(g)
                .set(RIOT.symTurtleDirectiveStyle, "sparql")
                .format(format)
                .base(BASE)
                .asString();

        // Test BASE used.
        assertTrue(written.contains("<>"));
        assertTrue(written.contains("BASE"));
    }
}
