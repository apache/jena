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

package org.apache.jena.riot.lang.rdfxml;

import static org.apache.jena.arq.junit.Scripts.withAltParserFactory;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import org.apache.jena.arq.TestConsts;
import org.apache.jena.riot.Lang;

/**
 * Manifest driven tests for ARP1.
 */
public class Scripts_ARP1_RDFXML {

    @TestFactory
    @DisplayName("ARP1 (legacy)")
    public Stream<DynamicNode> testFactoryARP1() {
        return withAltParserFactory(Lang.RDFXML, ReaderRDFXML_ARP1.factory, TestConsts.RDF11_TESTS_DIR+"rdf-xml/manifest.ttl");
    }
}
