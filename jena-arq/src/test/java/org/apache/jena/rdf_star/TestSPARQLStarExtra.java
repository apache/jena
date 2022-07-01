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

package org.apache.jena.rdf_star;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.resultset.ResultsReader;
import org.junit.Test;

/**
 * Odds and ends for SPARQL*.
 */
public class TestSPARQLStarExtra {

    private static String FILES = "testing/ARQ/RDF-star/Other/";

    // RDF4J format JSON results.
    // It uses "s", "p" and "o" for the RDF term results.
    @Test public void parse_alt_1() {
        String x = FILES+"alternate-results-1.srj";
        ResultSet rs = ResultsReader.create().read(x);
        ResultSetFormatter.consume(rs);
    }

    // Believed Stardog format JSON results.
    // It uses "s", "p" and "o" for the RDF term results.
    // It uses "statement" for "triple".
    @Test public void parse_alt_2() {
        String x = FILES+"alternate-results-2.srj";
        ResultSet rs = ResultsReader.create().read(x);
        ResultSetFormatter.consume(rs);
    }
}
