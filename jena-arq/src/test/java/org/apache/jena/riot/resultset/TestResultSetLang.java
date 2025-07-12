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

package org.apache.jena.riot.resultset;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.apache.jena.riot.Lang;
import org.apache.jena.sys.JenaSystem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TestResultSetLang {

    // Testing assumes JenaSystem.init() has run.
    // Call here for a standalone test suite.
    static { JenaSystem.init(); }

    @DisplayName("ResultSetLang registration")
    @ParameterizedTest(name="{index} - {1}")
    @MethodSource("provideLangs")
    void registrationResultSetLang(Lang rsLang, String label) {
        boolean b = ResultSetLang.isRegistered(rsLang);
        assertTrue(b, label+" : not registered");
    }

    private static Stream<Arguments> provideLangs() {
        return Stream.of
                (Arguments.of(ResultSetLang.RS_JSON, "SPARQL JSON results"),
                 Arguments.of(ResultSetLang.RS_XML, "SPARQL XML results"),
                 Arguments.of(ResultSetLang.RS_CSV, "SPARQL CSV results"),
                 Arguments.of(ResultSetLang.RS_TSV, "SPARQL TSV results"),
                 Arguments.of(ResultSetLang.RS_Thrift, "SPARQL RDF-Thrift results"),
                 Arguments.of(ResultSetLang.RS_Protobuf, "SPARQL RDF-Protobuf results"),
                 // Not a readable format -- ResultSetLang.RS_Text
                 Arguments.of(ResultSetLang.RS_None, "SPARQL None results"));
    }
}
