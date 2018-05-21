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

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.Writer;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.riot.RiotException;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

public class TestRDFWriter {
    private static Graph graph = SSE.parseGraph("(graph (:s :p :o))");
    
    @Test public void rdfwriter_1() {
        RDFWriter.create().source(graph).build();
    }
    
    @Test(expected=RiotException.class)
    public void rdfwriter_2() {
        RDFWriter.create().build();
    }

    @Test public void rdfwriter_3() {
        String s = 
            RDFWriter.create()
                .source(graph)
                .lang(Lang.NT)
                .asString();
        assertTrue(s.contains("example/s"));
    }

    @Test(expected=RiotException.class)
    public void rdfwriter_4() {
        String s = 
            RDFWriter.create()
                // No syntax
                .source(graph)
                .asString();
    }
    
    @Test public void rdfwriter_5() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        RDFWriter.create()
            .source(graph)
            .lang(Lang.NT)
            .output(bout);
        String s = StrUtils.fromUTF8bytes(bout.toByteArray());
        assertTrue(s.contains("example/s"));
    }
    
    @SuppressWarnings("deprecation")
    @Test public void rdfwriter_6() {
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
