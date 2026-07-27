/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.resultset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.resultset.ResultSetLang;

public class TestResultsFormat {

    @Test public void entry_srj()       { test("srj", ResultsFormat.JSON, ResultSetLang.RS_JSON, RDFFormat.JSONLD, RDFFormat.JSONLD); }
    @Test public void entry_srx()       { test("srx", ResultsFormat.XML, ResultSetLang.RS_XML, RDFFormat.RDFXML, null); }

    @Test public void entry_ttl1()      { test("ttl", ResultsFormat.TTL, null, RDFFormat.TURTLE, null); }
    @Test public void entry_ttl2()      { test("turtle", ResultsFormat.TTL, null, RDFFormat.TURTLE, null); }

    @Test public void entry_nt1()       { test("nt", ResultsFormat.NT, null, RDFFormat.NTRIPLES, null); }
    @Test public void entry_nt2()       { test("n-triples", ResultsFormat.NT, null, RDFFormat.NTRIPLES, null); }

    @Test public void entry_trig1()     { test("trig", ResultsFormat.TRIG, null, RDFFormat.TRIG, RDFFormat.TRIG); }

    @Test public void entry_nq1()       { test("NQ", ResultsFormat.NQ, null, RDFFormat.NQUADS, RDFFormat.NQUADS); }
    @Test public void entry_nq2()       { test("n-quads", ResultsFormat.NQ, null, RDFFormat.NQUADS, RDFFormat.NQUADS); }

    @Test public void entry_text1()     { test("text", ResultsFormat.TEXT, ResultSetLang.RS_Text, RDFFormat.TURTLE, RDFFormat.TRIG); }
    @Test public void entry_text2()     { test("txt", ResultsFormat.TEXT, ResultSetLang.RS_Text, RDFFormat.TURTLE, RDFFormat.TRIG); }
    @Test public void entry_text3()     { test("TXT", ResultsFormat.TEXT, ResultSetLang.RS_Text, RDFFormat.TURTLE, RDFFormat.TRIG); }

    @Test public void entry_xml()       { test("xml", ResultsFormat.XML, ResultSetLang.RS_XML, RDFFormat.RDFXML, null); }
    @Test public void entry_rdfxml()    { test("rdfxml", ResultsFormat.RDFXML, null, RDFFormat.RDFXML, null); }

    @Test public void entry_json()      { test("json", ResultsFormat.JSON, ResultSetLang.RS_JSON, RDFFormat.JSONLD, RDFFormat.JSONLD); }
    @Test public void entry_jsonld()    { test("jsonld", ResultsFormat.JSONLD, null, RDFFormat.JSONLD, RDFFormat.JSONLD); }

    @Test public void entry_bad() { testBad("SHACL"); }

    private void test(String string, ResultsFormat expected, Lang rsLang, RDFFormat triplesFormat, RDFFormat quadsFormat) {
        ResultsFormat rFmt = ResultsFormat.lookup(string);
        assertEquals(expected, rFmt);
        assertEquals(rsLang, rFmt.resultSetLang());
        assertEquals(triplesFormat, rFmt.triplesFormat());
        assertEquals(quadsFormat, rFmt.quadsFormat());
    }

    private void testBad(String string) {
        ResultsFormat rFmt = ResultsFormat.lookup(string);
        assertNull(rFmt);
    }
}
