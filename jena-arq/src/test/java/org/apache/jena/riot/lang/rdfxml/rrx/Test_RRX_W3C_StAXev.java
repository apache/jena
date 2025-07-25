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
import org.apache.jena.riot.lang.rdfxml.rrx_stax_ev.ReaderRDFXML_StAX_EV;

/**
 * Run over all files found by a deep walk of the directory tree.
 */

@ParameterizedClass
@MethodSource("provideArgs")
public class Test_RRX_W3C_StAXev extends AbstractTestRDFXML_RRX {

    private static ReaderRIOTFactory rdfxmlStAXevFactory = ReaderRDFXML_StAX_EV.factory;
    private static String implLabel = "StAX(ev)";

    private static Stream<RRX_TestFileArgs> provideArgs() {
        List<String> testfiles = RunTestRDFXML.w3cTestFiles();
        return RunTestRDFXML.makeTestSetup(implLabel, rdfxmlStAXevFactory, testfiles).stream();
    }

    public Test_RRX_W3C_StAXev(RRX_TestFileArgs args) {
        super(args, implLabel);
    }
}

