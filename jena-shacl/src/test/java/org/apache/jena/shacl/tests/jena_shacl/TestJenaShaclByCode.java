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

package org.apache.jena.shacl.tests.jena_shacl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintStream;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.sparql.util.IsoMatcher;

public class TestJenaShaclByCode {
    static final String DIR = "src/test/files/local/other/";
    @Test public void sparql_vars_001() {
        execTest("sparql-vars-001",
                 DIR+"sparql-vars-001-data.ttl",
                 DIR+"sparql-vars-001-shape.ttl",
                 DIR+"sparql-vars-001-results.ttl");

    }

    private void execTest(String label, String datafile, String shapesfile, String expectedResults) {
        Graph data = load(datafile) ;
        Graph shapes = load(shapesfile) ;
        Graph expected = load(expectedResults);

        Graph actual = ShaclValidator.get().validate(shapes, data).getGraph();

        boolean checkResults = IsoMatcher.isomorphic(expected, actual);
        if ( ! checkResults ) {
            PrintStream out = System.out;
            out.println("==== Failure: "+label);
            if ( false ) {
                // Very verbose : enable for development/debugging!
                out.println("== Data:");
                write(out, data);
                out.println("== Shapes:");
                write(out, shapes);
            }
            out.println("== Expected:");
            write(out, expected);
            out.println("== Actual:");
            write(out, actual);
        }
        assertTrue(checkResults);
    }

    private Graph load(String file) {
        return RDFParser.source(file).toGraph();
    }

    private void write(PrintStream out, Graph graph) {
        RDFWriter.source(graph).lang(Lang.TTL).output(out);
    }

}
