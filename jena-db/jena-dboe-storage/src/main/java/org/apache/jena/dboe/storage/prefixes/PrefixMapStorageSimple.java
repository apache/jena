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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

public class PrefixMapStorageSimple implements StoragePrefixMap {
    private Map<String, String> map = new HashMap<>();

    public PrefixMapStorageSimple() { }

    @Override
    public void put(String prefix, String uriStr) {
        map.put(prefix, uriStr);
    }

    @Override
    public String get(String prefix) {
        return map.get(prefix);
    }

    @Override
    public void remove(String prefix) {
        map.remove(prefix);
    }

    @Override
    public boolean containsPrefix(String prefix) {
        return map.containsKey(prefix);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Iterator<PrefixEntry> iterator() {
        return stream().iterator();
    }

    @Override
    public Stream<PrefixEntry> stream() {
        return map.entrySet().stream().map(e->PrefixEntry.create(e.getKey(), e.getValue()));
    }
}
