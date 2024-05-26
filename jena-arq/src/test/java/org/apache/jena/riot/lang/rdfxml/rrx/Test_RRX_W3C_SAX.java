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

import org.apache.jena.riot.ReaderRIOTFactory;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Run over all files found by a deep walk of the directory tree.
 * Test for an error if ARP generates an error.
 * These test assume that ARP is correct!
 */

@RunWith(Parameterized.class)
public class Test_RRX_W3C_SAX extends AbstractTestRDFXML_RRX {

    private static ReaderRIOTFactory rdfxmlSAXfactory = ReaderRDFXML_SAX.factory;
    private static String implLabel = "SAX";

    @Parameters(name = "{index}: {0} {1}")
    public static Iterable<Object[]> data() {
        List<String> testfiles = RunTestRDFXML.w3cTestFiles();
        return RunTestRDFXML.makeTestSetup(testfiles, implLabel);
    }

    public Test_RRX_W3C_SAX(String label, String filename) {
        super(label, rdfxmlSAXfactory, implLabel, filename);
    }
}
