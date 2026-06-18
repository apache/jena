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

package org.apache.jena.core_ttl.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * Check the test Turtle reader is wired in.
 */
public class TestTurtleReader {

    @Test public void test_read() {
        Model m = ModelFactory.createDefaultModel();
        m.read("file:testing/Turtle/simple.ttl", "TTL");
        assertFalse(m.isEmpty());
    }

    @Test public void test_i18n() {
        Model m = ModelFactory.createDefaultModel();
        m.read("file:testing/Turtle/i18n.ttl", "TTL");
        assertFalse(m.isEmpty());
    }

    @SuppressWarnings("deprecation")
    public@Test  void testReaderNames() {
        Model m = ModelFactory.createDefaultModel();
        assertNotNull(m.getReader("TURTLE"));
        assertNotNull(m.getReader("Turtle"));
        assertNotNull(m.getReader("TTL"));
        assertNotNull(m.getReader("N3"));
    }
}
