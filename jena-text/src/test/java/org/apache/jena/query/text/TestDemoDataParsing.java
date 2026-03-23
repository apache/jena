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

package org.apache.jena.query.text;

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.junit.Test;

/**
 * Validates that the demo Turtle data files parse correctly.
 * Catches syntax errors (e.g. invalid prefixed names) before deployment.
 */
public class TestDemoDataParsing {

    private static final Path DEMO_DATA_DIR = findDemoDataDir();

    private static Path findDemoDataDir() {
        // Walk up from the working directory to find the repo root
        Path dir = Paths.get("").toAbsolutePath();
        while (dir != null) {
            Path candidate = dir.resolve("demo/test/data");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
        }
        return null;
    }

    @Test
    public void testMiningTtlParses() {
        assertTurtleParses("mining.ttl");
    }

    @Test
    public void testGeneratedTtlParses() {
        assertTurtleParses("generated.ttl");
    }

    @Test
    public void testConfigTtlParses() {
        if (DEMO_DATA_DIR == null) return;
        Path file = DEMO_DATA_DIR.getParent().resolve("config.ttl");
        if (!Files.exists(file)) return;
        try {
            Model model = ModelFactory.createDefaultModel();
            RDFDataMgr.read(model, file.toString());
            assertTrue("Expected triples in config.ttl", model.size() > 0);
        } catch (RiotException e) {
            throw new AssertionError("Failed to parse config.ttl: " + e.getMessage(), e);
        }
    }

    private void assertTurtleParses(String filename) {
        if (DEMO_DATA_DIR == null) {
            // Skip if demo data dir not found (e.g. running from a different working directory)
            return;
        }
        Path file = DEMO_DATA_DIR.resolve(filename);
        if (!Files.exists(file)) {
            return;
        }
        try {
            Model model = ModelFactory.createDefaultModel();
            RDFDataMgr.read(model, file.toString());
            assertTrue("Expected triples in " + filename, model.size() > 0);
        } catch (RiotException e) {
            throw new AssertionError("Failed to parse " + filename + ": " + e.getMessage(), e);
        }
    }
}
