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

package org.apache.jena.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;

public class TestJenaTitanium {

    @Test
    public final void readContext_1() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        RDFParser.source("testing/RIOT/jsonld11/doc-1.jsonld11").parse(dsg);
        assertFalse(dsg.isEmpty());
        // "@vocab" : "http://example.org/vocab" -- not a prefix - does not end in "/" "#" or ":"
        assertEquals(0, dsg.prefixes().size());
    }

    @Test
    public final void readContextArrayPrefixes_1() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        RDFParser.source("testing/RIOT/jsonld11/doc-2.jsonld11").parse(dsg);
        assertTrue(dsg.prefixes().containsPrefix("foaf"));
        assertEquals(1, dsg.prefixes().size());
    }

    @Test
    public final void readContextArrayPrefixes_2() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        RDFParser.source("testing/RIOT/jsonld11/doc-3.jsonld11").parse(dsg);
        assertTrue(dsg.prefixes().containsPrefix("foaf"));
        assertEquals(3, dsg.prefixes().size());
        assertTrue(dsg.prefixes().containsPrefix("foaf"));
        assertTrue(dsg.prefixes().containsPrefix("foo"));
        assertTrue(dsg.prefixes().containsPrefix(""));
        assertFalse(dsg.prefixes().containsPrefix("bar"));
    }
}
