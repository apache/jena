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

package org.apache.jena.sparql.graph;

import java.util.Map ;

import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.PrefixMapping ;

public class PrefixMappingReadOnly implements PrefixMapping {

    private final PrefixMapping other;

    public PrefixMappingReadOnly(PrefixMapping other) {
        this.other = other;
    }

    private JenaException exception() {
        return new JenaException("Read-only prefix mapping");
    }

    // Throw an exception for all updates.

    @Override
    public PrefixMapping setNsPrefix(String prefix, String uri) {
        throw exception();
    }

    @Override
    public PrefixMapping removeNsPrefix(String prefix) {
        throw exception();
    }

    @Override
    public PrefixMapping clearNsPrefixMap() {
        throw exception();
    }

    @Override
    public PrefixMapping setNsPrefixes(PrefixMapping other) {
        throw exception();
    }

    @Override
    public PrefixMapping setNsPrefixes(Map<String, String> map) {
        throw exception();
    }

    @Override
    public PrefixMapping withDefaultMappings(PrefixMapping map) {
        throw exception();
    }

    // Pass to the underlying prefix mapping for read operations.

    @Override
    public String getNsPrefixURI(String prefix) {
        return other.getNsPrefixURI(prefix);
    }

    @Override
    public String getNsURIPrefix(String uri) {
        return other.getNsURIPrefix(uri);
    }

    @Override
    public Map<String, String> getNsPrefixMap() {
        return other.getNsPrefixMap();
    }

    @Override
    public String expandPrefix(String prefixed) {
        return other.expandPrefix(prefixed);
    }

    @Override
    public String shortForm(String uri) {
        return other.shortForm(uri);
    }

    @Override
    public String qnameFor(String uri) {
        return other.qnameFor(uri);
    }

    @Override
    public PrefixMapping lock() {
        return this;
    }

    @Override
    public int numPrefixes() {
        return other.numPrefixes();
    }

    @Override
    public boolean samePrefixMappingAs(PrefixMapping prefixMapping) {
        return other.samePrefixMappingAs(prefixMapping);
    }
}
