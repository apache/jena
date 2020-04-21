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

package org.apache.jena.sparql.engine;

import static org.junit.Assert.assertEquals;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.query.*;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphZero;
import org.junit.Test;

public class TestJsonEval {

    @Test public void json_eval_01() {
        test("JSON { 'x' : ?x } WHERE { VALUES ?x { 'X' } }",
             "[ { 'x' : 'X' } ]");
    }

    @Test public void json_eval_02() {
        test("JSON { 'x' : ?x } WHERE { VALUES ?x { 'X' 'Y' } }",
             "[ { 'x' : 'X' } , {'x' : 'Y' }]");
    }

    @Test public void json_eval_03() {
        test("JSON { 's' : 'FOO' } WHERE { }",
             "[ { 's' : 'FOO' } ]");
    }

    @Test public void json_eval_04() {
        test("JSON { 'F' : 'string' } WHERE { }",
             "[ { 'F' : 'string' } ]");
    }

    @Test public void json_eval_05() {
        test("JSON { 'x' : 123 } WHERE { }",
             "[ { 'x' : 123 }]");
    }

    @Test public void json_eval_06() {
        test("JSON { 'x' : 123.5 } WHERE { }",
             "[ { 'x' : 123.5 }]");
    }

    @Test public void json_eval_07() {
        test("JSON { 'x' : -10 } WHERE { }",
             "[ { 'x' : -10 }]");
    }

    private void test(String queryString, String jsonExpected) {
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
        DatasetGraph dsg = new DatasetGraphZero();
        Dataset ds = DatasetFactory.wrap(dsg);
        try ( QueryExecution qExec = QueryExecutionFactory.create(query, ds) ) {
            JsonValue jvGot = qExec.execJson() ;
            JsonValue jvExpected = JSON.parseAny(jsonExpected) ;
            assertEquals(jvExpected, jvGot);
        }
    }
}
