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

package org.apache.jena.sparql.syntax;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

public class TestQueryProjectVars {

    @Test public void testProjectVars_01() { testCalculatedProjectVars("SELECT * { SELECT * { ?s :p :o } }", "s"); }
    @Test public void testProjectVars_02() { testCalculatedProjectVars("SELECT * { :x :y :z  { SELECT ?s { ?s :p :o } } }", "s"); }

    @Test public void testProjectVars_03() { testCalculatedProjectVars("SELECT * { :x :y ?z  { SELECT ?s { ?s :p :o } } }", "s", "z"); }
    @Test public void testProjectVars_04() { testCalculatedProjectVars("SELECT * { :x :y ?z  { SELECT * { ?s :p :o } } }", "s", "z"); }
    @Test public void testProjectVars_05() { testCalculatedProjectVars("SELECT ?s { :x :y ?z  { SELECT ?s { ?s :p :o } } }", "s"); }
    @Test public void testProjectVars_06() { testCalculatedProjectVars("SELECT ?z { :x :y ?z  { SELECT ?s { ?s :p :o } } }", "z"); }

    @Test public void testProjectVars_07() { testCalculatedProjectVars("SELECT ?z { :x :y ?z  { SELECT ?s { ?s :p :o } GROUP BY ?s } }", "z"); }
    @Test public void testProjectVars_08() { testCalculatedProjectVars("SELECT ?s { :x :y ?z  { SELECT ?s { ?s :p :o } GROUP BY ?s } }", "s"); }
    @Test public void testProjectVars_09() { testCalculatedProjectVars("SELECT ?z { :x :y ?z  { SELECT * { ?s :p :o } GROUP BY ?s } }", "z"); }

    // syntax-group-01.arq
    @Test public void testProjectVars_20() { testCalculatedProjectVars("SELECT * { ?x :p ?p } GROUP BY ?p", "p"); }

    private static String PREFIXES = "PREFIX : <http://example/>\n";
    private static void testCalculatedProjectVars(String queryString, String... expected) {
        Query query = QueryFactory.create(PREFIXES+queryString);

        List<String> actualVars = query.getResultVars();
        List<String> expectedResultVars = List.of(expected);

        assertTrue( CollectionUtils.isEqualCollection(actualVars, expectedResultVars),
                    ()->String.format("Different: query=%s, expected=%s actual=%s\n", queryString, expectedResultVars, actualVars));
    }
}
