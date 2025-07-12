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

package org.apache.jena.riot.lang.rdfxml.rrx;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.riot.ReaderRIOTFactory;

/**
 * Local basic testing to make sure the general parsing is OK in addition to running the W3C Test Suite.
 */
@ParameterizedClass
@MethodSource("provideArgs")
public class Test_RRX_Local_SAX extends AbstractTestRDFXML_RRX {

    private static ReaderRIOTFactory rdfxmlSAXfactory = ReaderRDFXML_SAX.factory;
    private static String implLabel = "SAX";

    private static Stream<RRX_TestFileArgs> provideArgs() {
        List<String> testfiles = RunTestRDFXML.localTestFiles();
        return RunTestRDFXML.makeTestSetup(implLabel, rdfxmlSAXfactory, testfiles).stream();
    }

    public Test_RRX_Local_SAX(RRX_TestFileArgs args) {
        super(args, implLabel);
    }
}
