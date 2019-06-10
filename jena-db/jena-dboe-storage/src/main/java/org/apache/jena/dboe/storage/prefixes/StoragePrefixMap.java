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
import java.util.stream.Stream;

/**
 * Storage-oriented abstraction for one set of prefix mappings.
 * The API features, such as expanding prefix names. are provided elsewhere.
 * This interface is just storage of the pairs (prefix, uri string)
 */
public interface StoragePrefixMap extends Iterable<PrefixEntry> {

    /** Put a (prefix, uri) pair into the mapping.
     * This replaces any previous mapping for the prefix.
     * @param prefix    Prefix string (without colon).
     * @param uriStr    URI as a string.
     */
    public void put(String prefix, String uriStr);

    /** Get the URI string associated with a prefix, or return null if there is no association.
     *
     * @param prefix
     * @return String
     */
    public String get(String prefix);

    /**
     * Remove the mapping for a prefix.
     * @param prefix The prefix of the mapping to be removed.
     */
    public void remove(String prefix);

    /** Return whether the mapping contains an entry for the given prefix. */
    public boolean containsPrefix(String prefix);

    /** Clear the prefix mapping storage. */
    public void clear();

    /** Return whether there are any prefix mappings or not. */
    public boolean isEmpty();

    /** Return the number of prefix mappings. */
    public int size();

    /** Iterator over all prefix entries. */
    @Override
    public Iterator<PrefixEntry> iterator();

//    @Override
//    public default Iterator<PrefixEntry> iterator() {
//        return stream().iterator();
//    }

    /** Stream of over all prefix entries. */
    public Stream<PrefixEntry> stream();

    /** Stream all prefixes. */
    public default Stream<String> prefixes() {
        return stream().map(PrefixEntry::getPrefix);
    }
}

