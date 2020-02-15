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

import java.util.Map;
import java.util.function.BiConsumer ;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.iri.IRI;
import org.apache.jena.shared.PrefixMapping ;

/**
 * Interface for lightweight prefix maps, this is similar to
 * {@link PrefixMapping} from Jena Core but it omits any reverse lookup
 * functionality.
 * <p>
 * The contract also does not require an implementation to do any validation
 * unlike the Jena Core {@link PrefixMapping} which requires validation of
 * prefixes.
 * </p>
 */
public interface PrefixMap {

    /**
     * Return the underlying mapping, this is generally unsafe to modify and
     * implementations may opt to return an unmodifiable view of the mapping if
     * they wish.
     *
     * @see #getMappingCopy()
     *
     * @return Underlying mapping
     */
    public abstract Map<String, String> getMapping();

    /**
     * Return a fresh copy of the underlying mapping, should be safe to modify
     * unlike the mapping returned from {@link #getMapping()}
     *
     * @return Copy of the mapping
     */
    public abstract Map<String, String> getMappingCopy();

    /**
     * Apply a {@link BiConsumer}{@code <String, String>} to each entry in the Prefixmap.
     */
    public abstract void forEach(BiConsumer<String, String> action) ;

    /**
     * Add a prefix, overwrites any existing association
     *
     * @param prefix Prefix
     * @param iriString Namespace IRI
     */
    public abstract void add(String prefix, String iriString);

    /**
     * Add a prefix, overwrites any existing association
     *
     * @param prefix Prefix
     * @param iri Namespace IRI
     * @deprecated Use {@link #add(String, String)}
     */
    @Deprecated
    public default void add(String prefix, IRI iri) {
        add(prefix, iri.toString());
    }

    /**
     * Add a prefix, overwrites any existing association
     *
     * @param pmap Prefix Map
     */
    public abstract void putAll(PrefixMap pmap);

    /**
     * Add a prefix, overwrites any existing association
     *
     * @param pmap Prefix Mapping
     */
    public abstract void putAll(PrefixMapping pmap);

    /**
     * Add a prefix, overwrites any existing association
     *
     * @param mapping A Map of prefix name to IRI string
     */
    public abstract void putAll(Map<String, String> mapping) ;

    /**
     * Delete a prefix
     *
     * @param prefix Prefix to delete
     */
    public abstract void delete(String prefix);

    /**
     * Clear all prefixes.
     */
    public abstract void clear();

    /**
     * Gets whether the map contains a given prefix
     *
     * @param prefix
     *            Prefix
     * @return True if the prefix is contained in the map, false otherwise
     */
    public abstract boolean containsPrefix(String prefix);

    /** @deprecated Use {@link #containsPrefix(String)} */
    @Deprecated
    public default boolean contains(String prefix) {
        return containsPrefix(prefix);
    }

    /**
     * Abbreviate an IRI or return null
     *
     * @param uriStr URI to abbreviate
     * @return URI in prefixed name form if possible, null otherwise
     */
    public abstract String abbreviate(String uriStr);

    /**
     * Abbreviate an IRI and return a pair of prefix and local parts, or null.
     *
     * @param uriStr URI string to abbreviate
     * @return Pair of prefix and local name
     * @see #abbreviate
     */
    public abstract Pair<String, String> abbrev(String uriStr);

    /**
     * Expand a prefix named, return null if it can't be expanded
     *
     * @param prefixedName Prefixed Name
     * @return Expanded URI if possible, null otherwise
     */
    public abstract String expand(String prefixedName);

    /**
     * Expand a prefix, return null if it can't be expanded
     *
     * @param prefix Prefix
     * @param localName Local name
     * @return Expanded URI if possible, null otherwise
     */
    public abstract String expand(String prefix, String localName);

    /**
     * Return whether the prefix map is empty or not.
     */
    public boolean isEmpty();

    /**
     * Return the number of entries in the prefix map.
     */
    public int size();
}