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

package org.apache.jena.riot.system;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer ;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.shared.PrefixMapping ;

/**
 * Abstract base implementation of a {@link PrefixMap} which provides
 * some implementations of API methods.
 */
public abstract class PrefixMapBase implements PrefixMap {

    protected PrefixMapBase() {}

    protected boolean strSafeFor(String str, char ch) {
        return str.indexOf(ch) == -1;
    }

    @Override
    public Map<String, String> getMappingCopy() {
        return new HashMap<>(this.getMapping());
    }

    @Override
    public void forEach(BiConsumer<String, String> action) {
        getMapping().forEach(action);
    }

    // e.g. Iterable.
    private Iterator<PrefixEntry> iterator() {
        return Iter.iter(getMapping().entrySet()).map(e->PrefixEntry.create(e.getKey(), e.getValue()));
    }

    @Override
    public Stream<PrefixEntry> stream() {
        return getMapping().entrySet().stream().map(e->PrefixEntry.create(e.getKey(), e.getValue()));
    }

    @Override
    public void putAll(PrefixMap pmap) {
        pmap.getMapping().forEach(this::add);
    }

    @Override
    public void putAll(PrefixMapping pmap) {
        putAll(pmap.getNsPrefixMap()) ;
    }

    @Override
    public void putAll(Map<String, String> mapping) {
        mapping.forEach(this::add);
    }

    @Override
    public String abbreviate(String uriStr) {
        Objects.requireNonNull(uriStr);
        // Includes safe ":"
        // return PrefixLib.abbreviate(this, uriStr);
        Pair<String, String> p = abbrev(uriStr);
        if (p == null)
            return null;
        return p.getLeft() + ":" + p.getRight();
    }

    @Override
    public String expand(String prefix, String localName)  {
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(localName);
        return PrefixLib.expand(this, prefix, localName);
    }

    @Override
    public Pair<String, String> abbrev(String uriStr) {
        return PrefixLib.abbrev(this, uriStr);
    }

    /**
     * Abbreviate an IRI and return a pair of prefix and local parts; return null otherwise.
     *
     * @param uriStr               URI string to abbreviate
     * @param turtleSafeLocalPart  Only return legal Turtle local names.
     */
    protected Pair<String, String> abbrev(Map<String, String> prefixesMap, String uriStr, boolean turtleSafeLocalPart) {
        return PrefixLib.abbrev(prefixesMap, uriStr, turtleSafeLocalPart);
    }

    @Override
    public String expand(String prefixedName) {
        Objects.requireNonNull(prefixedName);
        return PrefixLib.expand(this, prefixedName);
    }

    @Override
    public String toString() {
        return Prefixes.toString(this);
    }
}
