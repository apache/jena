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

package org.apache.jena.rdf.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;

import org.apache.jena.rdf.model.impl.NTripleReader;
import org.apache.jena.shared.JenaException;
import org.slf4j.LoggerFactory;

@ParameterizedClass(name = "{0}")
@MethodSource("org.apache.jena.rdf.model.helpers.ModelCreators#creators")
public class TestReaders extends AbstractModelTestBase {

    public TestReaders() {}

    /**
     * Test to ensure that the reader is set.
     */
    @Test
    public void testGetNTripleReader() {
        final RDFReaderI reader = new NTripleReader();
        assertNotNull(reader);
    }

    @Test
    public void testReadLocalNTriple() {
        model.read(getInputStream("TestReaders.nt"), "", "N-TRIPLE");
        assertEquals(5, model.size(), "Wrong size model");
        final StmtIterator iter = model.listStatements(null, null, "foo\"\\\n\r\tbar");
        assertTrue(iter.hasNext(), "No next statement found");
    }

    @Test
    public void testReadLocalRDF() {
        model.read(getInputStream("TestReaders.rdf"), "http://example.org/");
    }

    @Test
    public void testReadRemoteNTriple() {
        try {
            model.read("https://www.w3.org/2000/10/rdf-tests/rdfcore/" + "rdf-containers-syntax-vs-schema/test001.nt", "N-TRIPLE");
        } catch (final JenaException jx) {
            if ( (jx.getCause() instanceof NoRouteToHostException) || (jx.getCause() instanceof UnknownHostException)
                 || (jx.getCause() instanceof ConnectException) || (jx.getCause() instanceof IOException) ) {
                noPublicInternet();
            } else {
                throw jx;
            }
        }
    }

    @Test
    public void testReadRemoteRDF() {
        try {
            model.read("https://www.w3.org/2000/10/rdf-tests/rdfcore/" + "rdf-containers-syntax-vs-schema/test001.rdf");
        } catch (final JenaException jx) {
            if ( (jx.getCause() instanceof NoRouteToHostException) || (jx.getCause() instanceof UnknownHostException)
                 || (jx.getCause() instanceof ConnectException) || (jx.getCause() instanceof IOException) ) {
                noPublicInternet();
            } else {
                throw jx;
            }
        }
    }

    private void noPublicInternet() {
        LoggerFactory.getLogger(this.getClass()).warn("Cannot access public internet - part of test not executed");
    }
}
