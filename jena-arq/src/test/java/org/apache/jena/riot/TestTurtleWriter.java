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

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/** Tests for Turtle (and Trig) */
@RunWith(Parameterized.class)
public class TestTurtleWriter {
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        List<Object[]> x = new ArrayList<>() ;
        x.add(new Object[]{"Turtle", RDFFormat.TURTLE});
        x.add(new Object[]{"Turtle/Pretty", RDFFormat.TURTLE_PRETTY});
        x.add(new Object[]{"Turtle/Blocks", RDFFormat.TURTLE_BLOCKS});
        x.add(new Object[]{"Turtle/Flat", RDFFormat.TURTLE_FLAT});
        x.add(new Object[]{"Trig", RDFFormat.TRIG});
        x.add(new Object[]{"Trig/Pretty", RDFFormat.TRIG_PRETTY});
        x.add(new Object[]{"Trig/Blocks", RDFFormat.TRIG_BLOCKS});
        x.add(new Object[]{"Trig/Flat", RDFFormat.TRIG_FLAT});
        return x ; 
    }
    
    private static String DIR = "testing/RIOT/Writer/";

    private static String BASE = "http://BASE/";

    private final RDFFormat format;

    private final String filename;
    

    public TestTurtleWriter(String name, RDFFormat format) {
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

    // Stream writer (BLOCKS and FLAT) don't print a base URI unless explicitly given one in the data.
    // THis test is in TestTurtleWriterPretty
    //@Test public void writer_parse_base_2()
}
