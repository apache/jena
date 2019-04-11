/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.apache.jena.dboe.storage.prefixes;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** {@link PrefixMapI} implemented using {@link StoragePrefixMap} */
public class PrefixMapBase implements PrefixMapI {

    // Keep a cache of the map? Invalidate on update.
    private final StoragePrefixMap prefixes;
    protected StoragePrefixMap spm() { return prefixes; }
    
    public PrefixMapBase(StoragePrefixMap storage) {
        this.prefixes = storage;
    }

    @Override
    public Map<String, String> getMapping() {
        return getMappingCopy();
    }

    @Override
    public Map<String, String> getMappingCopy() {
        return spm().stream().collect(Collectors.toMap((e) -> e.getPrefix(), (e) -> e.getUri()));
    }

    @Override
    public StoragePrefixMap getPrefixMapStorage() {
        return prefixes;
    }

    @Override
    public void add(String prefix, String iriString) {
        prefix = PrefixLib.canonicalPrefix(prefix);
        spm().put(prefix, iriString);
    }

    @Override
    public void putAll(PrefixMapI pmap) {
        Map<String, String> map = pmap.getMapping();
        for ( Entry<String, String> e : map.entrySet() )
            add(e.getKey(), e.getValue());
    }

    @Override
    public void delete(String prefix) {
        prefix = PrefixLib.canonicalPrefix(prefix);
        spm().remove(prefix);
    }

    @Override
    public Stream<PrefixEntry> stream() {
        return spm().stream();
    }

    @Override
    public void forEach(BiConsumer<String, String> action) {}

    @Override
    public void clear() {
        spm().clear();
    }

    @Override
    public boolean isEmpty() {
        return spm().isEmpty();
    }

    @Override
    public int size() {
        return spm().size();
    }

    @Override
    public String get(String prefix) {
        prefix = PrefixLib.canonicalPrefix(prefix);
        return spm().get(prefix);
    }

    @Override
    public boolean containPrefix(String prefix) {
        prefix = PrefixLib.canonicalPrefix(prefix);
        return spm().containsPrefix(prefix);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        boolean first = true;

        for ( PrefixEntry e : prefixes ) {
            if ( first )
                first = false;
            else
                sb.append(" ,");
            sb.append(e.getPrefix());
            sb.append(":=");
            sb.append(e.getUri());
        }
        sb.append(" }");
        return sb.toString();
    }
}
