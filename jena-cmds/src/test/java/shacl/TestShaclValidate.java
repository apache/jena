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

package shacl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.shacl.ValidationReport;

public class TestShaclValidate {
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void conforming(boolean textOutput) {
        validate(true, textOutput);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void nonConforming(boolean textOutput) {
        validate(false, textOutput);
    }

    private void validate(boolean conforms, boolean textOutput) {
        String input = "testing/cmd/" +
            (conforms ? "shacl-conforming.ttl" : "shacl-nonconforming.ttl");

        String[] args = textOutput
            ? new String[] { "--text", input }
            : new String[] { input };
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try ( PrintStream out = new PrintStream(output, true, StandardCharsets.UTF_8) ) {
            System.setOut(out);
            // Embedded callers must receive the status without exiting the JVM.
            int status = new shacl_validate(args).mainRun(false, false);
            assertEquals(conforms ? 0 : 1, status);
        } finally {
            System.setOut(originalOut);
        }

        String report = output.toString(StandardCharsets.UTF_8);
        if ( textOutput ) {
            assertTrue(report.contains(conforms ? "Conforms" : "minCount"));
        } else {
            ValidationReport validationReport = ValidationReport.fromGraph(
                RDFParser.fromString(report, Lang.TTL).toGraph());
            assertEquals(conforms, validationReport.conforms());
        }
    }
}
