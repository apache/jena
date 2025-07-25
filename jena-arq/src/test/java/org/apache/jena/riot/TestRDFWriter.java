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

package org.apache.jena.riot;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.Writer;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.sparql.sse.SSE;

public class TestRDFWriter {
    private static Graph graph = SSE.parseGraph("(graph (:s :p :o))");

    @Test public void rdfwriter_create() {
        RDFWriter.source(graph).build();
    }

    @Test public void rdfwriter_string_output_1() {
        String s =
            RDFWriter.create()
                .source(graph)
                .lang(Lang.NT)
                .asString();
        assertTrue(s.contains("example/s"));
    }

    @Test public void rdfwriter_string_output_2() {
        String s =
            RDFWriter.create()
                .source(graph)
                .lang(Lang.NT)
                .toString();
        assertTrue(s.contains("example/s"));
    }


    @Test
    public void rdfwriter_no_source() {
        assertThrows(RiotException.class,()->
            // No source
            RDFWriter.create().build()
        );
    }

    @Test
    public void rdfwriter_no_syntax() {
        assertThrows(RiotException.class,()->
            RDFWriter.create()
                // No syntax
                .source(graph)
                .asString()
            );
    }

    @Test public void rdfwriter_output_1() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        RDFWriter.create()
            .source(graph)
            .lang(Lang.NT)
            .output(bout);
        String s = StrUtils.fromUTF8bytes(bout.toByteArray());
        assertTrue(s.contains("example/s"));
    }

    @SuppressWarnings("deprecation")
    @Test public void rdfwriter_output_2() {
        Writer w = new CharArrayWriter();
        RDFWriter.create()
            .source(graph)
            .lang(Lang.NT)
            .build()
            .output(w);
        String s = w.toString();
        assertTrue(s.contains("example/s"));
    }
}
