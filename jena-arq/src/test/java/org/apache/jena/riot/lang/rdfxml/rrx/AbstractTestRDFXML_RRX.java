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

import org.apache.jena.riot.ReaderRIOTFactory;
import org.junit.Test;

// Eventually a  manifests?

/**
 * Local basic testing to make sure the general parsing is OK in addition to running the W3C Test Suite.
 *
 * These test assume that ARP is correct!
 */

public abstract class AbstractTestRDFXML_RRX {

    private final String testLabel;
    private final String implName;
    private final ReaderRIOTFactory factory;
    private final String filename;

    public AbstractTestRDFXML_RRX(String testLabel, ReaderRIOTFactory factory, String implName, String filename) {
        this.testLabel = testLabel;
        this.implName = implName;
        this.factory = factory;
        this.filename = filename;
    }

    @Test public void test() {
        RunTestRDFXML.runTest(testLabel, factory, implName, filename);
    }
}
