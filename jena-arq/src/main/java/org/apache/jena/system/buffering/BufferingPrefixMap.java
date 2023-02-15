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

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.riot.system.PrefixEntry;
import org.apache.jena.riot.system.PrefixLib;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapBase;

/** A {@link PrefixMap} that buffers changes until {@link #flush()} is called. */
public class BufferingPrefixMap extends PrefixMapBase {

    private Map<String, String> addedMappings = new HashMap<>();
    private Set<String> deletedMappings = new HashSet<>();
    private final PrefixMap base;

    public BufferingPrefixMap(PrefixMap prefixes) {
        this.base = prefixes;
    }

    @Override
    public Map<String, String> getMapping() {
        return getMappingCopy();
    }

    @Override
    public Map<String, String> getMappingCopy() {
        Map<String, String> map = new HashMap<>();
        map.putAll(base.getMapping());
        map.putAll(addedMappings);
        deletedMappings.forEach(map::remove);
        return map;
    }

    @Override
    public void forEach(BiConsumer<String, String> action) {
        base.stream().filter(entry->!deletedMappings.contains(entry.getPrefix())).forEach(e->action.accept(e.getPrefix(), e.getUri()));
        addedMappings.forEach((p,u)->action.accept(p, u));
    }

    @Override
    public Stream<PrefixEntry> stream() {
        Stream<PrefixEntry> stream1 = base.stream().filter(entry->!deletedMappings.contains(entry.getPrefix()));
        Stream<PrefixEntry> stream2 = addedMappings.entrySet().stream().map(e->PrefixEntry.create(e.getKey(), e.getValue()));
        return Streams.concat(stream1, stream2);
    }

    @Override
    public String get(String prefix) {
        prefix = PrefixLib.canonicalPrefix(prefix);
        if ( addedMappings.containsKey(prefix) ) {
            return addedMappings.get(prefix);
        }
        if ( deletedMappings.contains(prefix) )
            return null;
        return base.get(prefix);
    }

    @Override
    public void add(String prefix, String iriString) {
        prefix = PrefixLib.canonicalPrefix(prefix);
        if ( base.containsPrefix(prefix) ) {
            String x = get(prefix);
            if ( Objects.equals(x, iriString) ) {
                // No change.
                return ;
            }
        }
        addedMappings.put(prefix, iriString);
        deletedMappings.remove(prefix);
    }

    @Override
    public void delete(String prefix) {
        prefix = PrefixLib.canonicalPrefix(prefix);
        addedMappings.remove(prefix);
        if ( base.containsPrefix(prefix) )
            deletedMappings.add(prefix);
        else
            deletedMappings.remove(prefix);
    }

    @Override
    public void clear() {
        addedMappings.clear();
        deletedMappings = new HashSet<>(base.getMapping().keySet());
    }

    @Override
    public boolean containsPrefix(String prefix) {
        return stream().anyMatch(e->Objects.equals(e.getPrefix(), prefix));
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public int size() {
        // XXX Wrong for added same prefix, different iri.
        return (int)stream().count();
    }

    public PrefixMap base() { return base; }

    public void flush() {
        addedMappings.forEach(base::add);
        deletedMappings.forEach(base::delete);
        addedMappings.clear();
        deletedMappings.clear();

    }

    public String state() {
        StringBuilder sb = new StringBuilder();
        sb.append("Prefixes").append("\n");
        sb.append("  Added:   "+addedMappings).append("\n");
        sb.append("  Deleted: "+deletedMappings).append("\n");
        return sb.toString();
    }
}

