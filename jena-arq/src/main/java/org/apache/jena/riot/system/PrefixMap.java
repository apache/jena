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

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.iri.IRI;

import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * Interface for lightweight prefix maps, this is similar to
 * {@link PrefixMapping} from Jena Core but it omits any reverse lookup
 * functionality.
 * <p>
 * The contract also does not require an implementation to do any validation
 * unlike the Jena Core {@link PrefixMapping} which require validation of
 * prefixes.
 * </p>
 * <h3>Implementations</h3>
 * <p>
 * For input dominated workloads where you are primarily calling
 * {@link #expand(String)} or {@link #expand(String, String)} it is best to use
 * the default implementation - {@link PrefixMapStd}. For output dominated
 * workloads where you are primarily calling {@link #abbrev(String)} or
 * {@link #abbreviate(String)} it is better to use the {@link FastAbbreviatingPrefixMap}
 * implementation.  See the javadoc for those classes for more explanation
 * of their differences.
 * </p>
 * 
 */
public interface PrefixMap {

    /**
     * Return the underlying mapping, this is generally unsafe to modify and
     * implementations may opt to return an unmodifiable view of the mapping if
     * they wish
     * 
     * @return Underlying mapping
     * */
    public abstract Map<String, IRI> getMapping();

    /**
     * Return a fresh copy of the underlying mapping, should be safe to modify
     * unlike the mapping returned from {@link #getMapping()}
     * 
     * @return Copy of the mapping
     */
    public abstract Map<String, IRI> getMappingCopy();

    /**
     * Gets a fresh copy of the mapping with the IRIs translated into their
     * strings
     * 
     * @return Copy of the mapping
     */
    public abstract Map<String, String> getMappingCopyStr();

    /**
     * Add a prefix, overwrites any existing association
     * 
     * @param prefix
     *            Prefix
     * @param iriString
     *            Namespace IRI
     */
    public abstract void add(String prefix, String iriString);

    /**
     * Add a prefix, overwrites any existing association
     * 
     * @param prefix
     *            Prefix
     * @param iri
     *            Namespace IRI
     */
    public abstract void add(String prefix, IRI iri);

    /**
     * Add a prefix, overwrites any existing association
     * 
     * @param pmap
     *            Prefix Map
     */
    public abstract void putAll(PrefixMap pmap);

    /**
     * Add a prefix, overwrites any existing association
     * 
     * @param pmap
     *            Prefix Mapping
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
     * @param prefix
     *            Prefix to delete
     */
    public abstract void delete(String prefix);

    /**
     * Gets whether the map contains a given prefix
     * 
     * @param prefix
     *            Prefix
     * @return True if the prefix is contained in the map, false otherwise
     */
    public abstract boolean contains(String prefix);

    /**
     * Abbreviate an IRI or return null
     * 
     * @param uriStr
     *            URI to abbreviate
     * @return URI in prefixed name form if possible, null otherwise
     */
    public abstract String abbreviate(String uriStr);

    /**
     * Abbreviate an IRI and return a pair of prefix and local parts, or null.
     * 
     * @param uriStr
     *            URI string to abbreviate
     * @return Pair of prefix and local name
     * @see #abbreviate
     */
    public abstract Pair<String, String> abbrev(String uriStr);

    /**
     * Expand a prefix named, return null if it can't be expanded
     * 
     * @param prefixedName
     *            Prefixed Name
     * @return Expanded URI if possible, null otherwise
     */
    public abstract String expand(String prefixedName);

    /**
     * Expand a prefix, return null if it can't be expanded
     * 
     * @param prefix
     *            Prefix
     * @param localName
     *            Local name
     * @return Expanded URI if possible, null otherwise
     */
    public abstract String expand(String prefix, String localName);
    
    /**
     * return whether the 
     * @return boolean
     */

    public boolean isEmpty();
    
    /**
     * Return the number of entries in the prefix map.
     * @return Size of the prefix mapping
     */

    public int size();
}