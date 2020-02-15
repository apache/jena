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

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.shared.PrefixMapping;

/**
 * Interface for lightweight prefix maps as an implementation and storage view. This
 * is similar to {@link PrefixMapping} from Jena Core but it omits any reverse lookup
 * functionality (URI to prefix) and does not impose XML-specific rules (e.g. local
 * names must start with a letter).
 * <p>
 * The contract does not require an implementation to do any validation unlike the
 * Jena Core {@link PrefixMapping} which requires validation of prefixes.
 * </p>
 * 
 * @implNote The package {@code org.apache.jena.dboe.storage.prefixes} in module
 *     {@code jena-dboe-storage} provides implementations that work with
 *     {@code StoragePrefixes} which is dataset provision of prefixes on per-named
 *     graph basis.
 */
public interface PrefixMapI extends Iterable<PrefixEntry>, PrefixMap
{
    /** Returns the underlying mapping.
     *  Do not modify this map, call interface operations.
     */
    @Override
    public Map<String, String> getMapping();

    /** Returns a copy of the underlying mapping */
    @Override
    public default Map<String, String> getMappingCopy() {
        return this.stream().collect(Collectors.toMap((e) -> e.getPrefix(), (e) -> e.getUri()));
    }

    /* Return the underlying PrefixMapStorage - optional operation, may return null */
    public StoragePrefixMap getPrefixMapStorage();

    /** Add a prefix, overwrites any existing association */
    @Override
    public void add(String prefix, String iriString);

    /** Merge one {@code PrefixMap} into another. */
    @Override
    public default void putAll(PrefixMap pmap) {
        pmap.forEach(this::add);
    }
    
    @Override
    public default void putAll(PrefixMapping pmap) {
        pmap.getNsPrefixMap().forEach(this::add);
    }

    /** Merge all the entries from the map into this {@link PrefixMapI}. */
    @Override
    public default void putAll(Map<String, String> map) {
        map.forEach(this::add);
    }

    /** Delete a prefix */
    @Override
    public void delete(String prefix);

    /** Clear the prefix mappings. */
    @Override
    public void clear();

    /** Get the mapping for a prefix (or return null). */
    public String get(String prefix);

    @Override
    public boolean containsPrefix(String prefix);

    /** Abbreviate an IRI or return null */
    @Override
    public default String abbreviate(String uriStr) {
        return PrefixLib.abbreviate(this, uriStr);
    }

    /** Abbreviate an IRI or return null */
    @Override
    public default Pair<String, String> abbrev(String uriStr) {
        return PrefixLib.abbrev(this, uriStr);
    }

    /** Expand a prefixed name, return null if it can't be expanded */
    @Override
    public default String expand(String prefixedName) {
        return PrefixLib.expand(this, prefixedName);
    }

    /** Expand a prefix, return null if it can't be expanded */
    @Override
    public default String expand(String prefix, String localName)  {
        return PrefixLib.expand(this, prefix, localName);
    }

    /** Iterator over all prefix entries. */
    @Override
    public default Iterator<PrefixEntry> iterator() {
        return stream().iterator();
    }

    /** Apply a {@link BiConsumer}{@code <String, String>} to each entry in the PrefixMap. */
    @Override
    public abstract void forEach(BiConsumer<String, String> action);

    /** Stream of over all prefix entries. */
    public Stream<PrefixEntry> stream();

    /** Stream all prefixes. */
    public default Stream<String> prefixes() {
        return stream().map(PrefixEntry::getPrefix);
    }

    /** Return whether there are any prefix mappings or not. */
    @Override
    public boolean isEmpty();

    /** Return the number of mappings. */
    @Override
    public int size();
}
