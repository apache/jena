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
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.Pair;

/** Implementation-view of prefix mappings.
 *
 * @apiNotes
 * <p>See {@link StoragePrefixMap} for the storage implementation view.
 * <p>See {@link PrefixMapBase} for an implementation over {@link StoragePrefixMap}
 */

public interface PrefixMapI extends Iterable<PrefixEntry> // replaces PrefixMap
{
    /** returns the underlying mapping - do not modify */
    public Map<String, String> getMapping();

    /** returns a copy of the underlying mapping */
    public Map<String, String> getMappingCopy();

    /* Return the underlying PrefixMapStorage - optional operation, may return null */
    public StoragePrefixMap getPrefixMapStorage();

    /** Add a prefix, overwrites any existing association */
    public void add(String prefix, String iriString);

    /** Add a prefix, overwrites any existing association */
    public void putAll(PrefixMapI pmap);

    /** Delete a prefix */
    public void delete(String prefix);

    /** Clear the prefix mappings. */
    public void clear();

    /** Get the mapping for a prefix (or return null). */
    public String get(String prefix);

    public boolean containPrefix(String prefix);

    /** Abbreviate an IRI or return null */
    public default String abbreviate(String uriStr) {
        return PrefixLib.abbreviate(this, uriStr);
    }    

    /** Abbreviate an IRI or return null */
    public default Pair<String, String> abbrev(String uriStr) {
        return PrefixLib.abbrev(this, uriStr);
    }

    /** Expand a prefix named, return null if it can't be expanded */
    public default String expand(String prefixedName) {
        return PrefixLib.expand(this, prefixedName);
    }

    /** Expand a prefix, return null if it can't be expanded */
    public default String expand(String prefix, String localName)  {
        return PrefixLib.expand(this, prefix, localName);
    }

    /** Iterator over all prefix entries. */
    @Override
    public default Iterator<PrefixEntry> iterator() {
        return stream().iterator();
    }

    /** Apply a {@link BiConsumer}{@code <String, String>} to each entry in the PrefixMap. */
    public abstract void forEach(BiConsumer<String, String> action) ;
    
    /** Stream of over all prefix entries. */
    public Stream<PrefixEntry> stream();

    /** Stream all prefixes. */
    public default Stream<String> prefixes() {
        return stream().map(PrefixEntry::getPrefix);
    }

    /** Return whether there are any prefix mappings or not. */
    public boolean isEmpty();

    /** Return the number of mappings. */
    public int size();
}
