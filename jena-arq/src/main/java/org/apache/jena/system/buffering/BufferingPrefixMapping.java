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

package org.apache.jena.system.buffering;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.graph.PrefixMappingBase;
import org.apache.jena.sparql.graph.PrefixMappingMem;

/**
 * A {@link PrefixMapping} that buffers changes until {@link #flush()} is called.
 */
public class BufferingPrefixMapping extends PrefixMappingBase implements BufferingCtl {

    private static final boolean CHECK = false;

    private final PrefixMapping added = new PrefixMappingMem();
    private final Set<String> deleted = new HashSet<>();
    // The underlying PrefixMapping
    private final PrefixMapping other;

    public BufferingPrefixMapping(PrefixMapping other) {
        this.other = other;
    }

    public PrefixMapping base() { return other; }

    @Override
    public void flush() {
        deleted.forEach(prefix->other.removeNsPrefix(prefix));
        other.setNsPrefixes(added);
        reset();
    }

    @Override
    public void reset() {
        deleted.clear();
        added.clearNsPrefixMap();
    }

    public PrefixMapping getAdded() {
        return added;
    }

    public Set<String> getDeleted() {
        return deleted;
    }

    @Override
    protected void add(String prefix, String uri) {
        if ( CHECK ) {
            String u = other.getNsPrefixURI(prefix);
            if ( uri.equals(u) )
                return;
        }
        added.setNsPrefix(prefix, uri);
    }

    @Override
    protected String prefixToUri(String prefix) {
        if ( deleted.contains(prefix) )
            return null;
        String uri = added.getNsPrefixURI(prefix);
        if ( uri != null )
            return uri;
        return other.getNsPrefixURI(prefix);
    }

    @Override
    protected String uriToPrefix(String uri) {
        String prefix1 = added.getNsURIPrefix(uri);
        if ( prefix1 != null )
            return prefix1;
        String prefix2 = other.getNsURIPrefix(uri);
        if ( prefix2 == null )
            return null;
        if ( deleted.contains(prefix2) )
            return null;
        return prefix2;
    }

    @Override
    protected void remove(String prefix) {
        deleted.add(prefix);
        added.removeNsPrefix(prefix);
    }

    @Override
    public int numPrefixes() {
        return added.numPrefixes() - deleted.size() + other.numPrefixes();
    }

    @Override
    public boolean hasNoMappings() {
        return numPrefixes() == 0 ;
    }

    @Override
    protected void clear() {
        apply((prefix, iri) -> remove(prefix));
    }

    @Override
    protected boolean isEmpty() {
        return size() == 0;
    }

    @Override
    protected int size() {
        return numPrefixes();
    }

    @Override
    protected Map<String, String> asMap() {
        return asMapCopy();
    }

    @Override
    protected Map<String, String> asMapCopy() {
        Map<String, String> map = other.getNsPrefixMap();
        deleted.forEach(prefix->map.remove(prefix));
        map.putAll(added.getNsPrefixMap());
        return map;
    }

    @Override
    protected void apply(BiConsumer<String, String> action) {
        asMap().forEach(action);
    }
}
