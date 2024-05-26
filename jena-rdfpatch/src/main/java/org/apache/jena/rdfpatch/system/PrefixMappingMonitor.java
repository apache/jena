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

package org.apache.jena.rdfpatch.system;

import java.util.Map;

import org.apache.jena.shared.PrefixMapping;

/**
 * An implementation of {@link PrefixMapping} that is a wrapper for another PrefixMapping
 * and also decomposes all changes to set/remove operations.
 */
public class PrefixMappingMonitor implements PrefixMapping {

    private final PrefixMapping pmap;

    public PrefixMappingMonitor(PrefixMapping pmap) { this.pmap = pmap ; }

    protected PrefixMapping get() { return pmap ; }

    // The two change operations
    protected void set(String prefix, String uri) { }

//    protected String get(String prefix) { }

    protected void remove(String prefix) { }

    @Override
    public PrefixMapping setNsPrefix(String prefix, String uri) {
        set(prefix, uri);
        get().setNsPrefix(prefix, uri);
        return this;
    }

    @Override
    public PrefixMapping removeNsPrefix(String prefix) {
        remove(prefix);
        get().removeNsPrefix(prefix);
        return this;
    }

    @Override
    public PrefixMapping clearNsPrefixMap() {
        get().getNsPrefixMap().forEach((prefix,uri)->remove(prefix));
        get().clearNsPrefixMap();
        return this;
    }

    @Override
    public PrefixMapping setNsPrefixes(PrefixMapping other) {
        other.getNsPrefixMap().forEach((p,u) -> set(p,u));
        get().setNsPrefixes(other);
        return this;
    }

    @Override
    public PrefixMapping setNsPrefixes(Map<String, String> map) {
        map.forEach((p,u) -> set(p,u));
        get().setNsPrefixes(map);
        return this;
    }

    @Override
    public PrefixMapping withDefaultMappings(PrefixMapping map) {
        // fake
        map.getNsPrefixMap().forEach((p,u) -> set(p,u));
        get().withDefaultMappings(map);
        return this;
    }

    @Override
    public String getNsPrefixURI(String prefix) {
        return get().getNsPrefixURI(prefix);
    }

    @Override
    public String getNsURIPrefix(String uri) {
        return get().getNsURIPrefix(uri);
    }

    @Override
    public Map<String, String> getNsPrefixMap() {
        return get().getNsPrefixMap();
    }

    @Override
    public String expandPrefix(String prefixed) {
        return get().expandPrefix(prefixed);
    }

    @Override
    public String shortForm(String uri) {
        return get().shortForm(uri);
    }

    @Override
    public String qnameFor(String uri) {
        return get().qnameFor(uri);
    }

    @Override
    public PrefixMapping lock() {
        get().lock();
        return this;
    }

    @Override
    public boolean hasNoMappings() {
        return get().hasNoMappings();
    }

    @Override
    public int numPrefixes() {
        return get().numPrefixes();
    }

    @Override
    public boolean samePrefixMappingAs(PrefixMapping other) {
        return get().samePrefixMappingAs(other);
    }
}