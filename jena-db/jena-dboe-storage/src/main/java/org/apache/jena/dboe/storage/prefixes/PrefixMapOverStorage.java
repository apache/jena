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

import static org.apache.jena.riot.system.PrefixLib.canonicalPrefix;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapBase;
import org.apache.jena.riot.system.Prefixes;

/** {@link PrefixMap} implemented using {@link StoragePrefixMap} */
public class PrefixMapOverStorage extends PrefixMapBase {

    private final StoragePrefixMap prefixes;
    protected StoragePrefixMap spm() { return prefixes; }

    public PrefixMapOverStorage(StoragePrefixMap storage) {
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
    public void add(String prefix, String iriString) {
        prefix = canonicalPrefix(prefix);
        spm().put(prefix, iriString);
    }

    @Override
    public void delete(String prefix) {
        prefix = canonicalPrefix(prefix);
        spm().remove(prefix);
    }

//    @Override
//    public Stream<PrefixEntry> stream() {
//        return spm().stream();
//    }

    @Override
    public void forEach(BiConsumer<String, String> action) {
        spm().forEach(entry->action.accept(entry.getPrefix(), entry.getUri()));
    }

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
        prefix = canonicalPrefix(prefix);
        return spm().get(prefix);
    }

    @Override
    public boolean containsPrefix(String prefix) {
        prefix = canonicalPrefix(prefix);
        return spm().containsPrefix(prefix);
    }

    @Override
    public String toString() {
        return Prefixes.toString(this);
    }
}
