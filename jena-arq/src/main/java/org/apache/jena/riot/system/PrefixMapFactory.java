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

import java.util.Map ;

import org.apache.jena.shared.PrefixMapping ;

/**
 * Factory which provides prefix maps
 *
 */
public class PrefixMapFactory {

    /**
     * Creates a new prefix map.
     * <p>
     * Will use whatever the version of ARQ you are using considers the default
     * implementation, this may change from release to release.
     * </p>
     *
     * @return Prefix Map
     */
    public static PrefixMap create() {
        return new PrefixMapStd();
    }

    /**
     * Creates a new prefix map which starts with a copy of an existing prefix
     * map.
     * <p>
     * Will use whatever the version of ARQ you are using considers the default
     * implementation, this may change from release to release.
     * </p>
     *
     * @param pmap
     *            Prefix Map to copy
     *
     * @return Prefix Map
     */
    public static PrefixMap create(PrefixMap pmap) {
        return new PrefixMapStd(pmap);
    }

    /**
     * Creates a new prefix map which starts
     * with a copy of an existing map.
     * <p>
     * Will use whatever the version of ARQ you are using considers the default
     * implementation, this may change from release to release.
     * </p>
     *
     * @param pmap
     *            PrefixMapping to copy
     *
     * @return Prefix Map
     */
    public static PrefixMap create(PrefixMapping pmap) {
        PrefixMap created = create();
        created.putAll(pmap);
        return created;
    }

    /**
     * Creates a new prefix map,initialized from a Map of prefix to IRI string.
     * <p>
     * Will use whatever the version of ARQ you are using considers the default
     * implementation, this may change from release to release.
     * </p>
     *
     * @param pmap Mapping from prefix to IRI string
     * @return Prefix Map
     */
    public static PrefixMap create(Map<String, String> pmap) {
        PrefixMap created = create();
        created.putAll(pmap);
        return created;
    }

    /**
     * Creates a new prefix map which is intended for use in input.
     *
     * @return Prefix map
     * @deprecated Use {@link #create()}
     */
    @Deprecated
    public static PrefixMap createForInput() {
        return create();
    }

    /**
     * Creates a new prefix map which is intended for use in input which starts
     * with a copy of an existing map
     * <p>
     * Will use whatever the version of ARQ you are using considers the best
     * implementation for input, this may change from release to release.
     * </p>
     *
     * @param pmap
     *            Prefix Map to copy
     * @return Prefix Map
     * @deprecated Use {@link #create(PrefixMap)}
     */
    @Deprecated
    public static PrefixMap createForInput(PrefixMap pmap) {
        return create(pmap);
    }

    /**
     * Creates a new prefix map which is intended for use in iput which starts
     * with a copy of an existing map
     * <p>
     * Will use whatever the version of ARQ you are using considers the best
     * implementation for input, this may change from release to release.
     * </p>
     *
     * @param pmap
     *            Prefix Map to copy
     *
     * @return Prefix Map
     * @deprecated Use {@link #create(PrefixMapping)}
     */
    @Deprecated
    public static PrefixMap createForInput(PrefixMapping pmap) {
        return create(pmap);
    }

    /**
     * Creates a new prefix map, initialized from a Map of prefix to IRI string.
     *
     * @param pmap Mapping from prefix to IRI string
     * @return Prefix Map
     * @deprecated Use {@link #create(Map)}
     */
    @Deprecated
    public static PrefixMap createForInput(Map<String, String> pmap) {
        return create(pmap);
    }

    /**
     * Creates a new prefix map which is intended for use in output
     * <p>
     * Will use whatever the version of ARQ you are using considers the best
     * implementation for output, this may change from release to release.
     * </p>
     *
     * @return Prefix Map
     */
    public static PrefixMap createForOutput() {
        return new PrefixMapStd();
    }

    /**
     * Creates a new prefix map which is intended for use in output which starts
     * with a copy of an existing map
     * <p>
     * Will use whatever the version of ARQ you are using considers the best
     * implementation for output, this may change from release to release.
     * </p>
     *
     * @param pmap
     *            Prefix Map to copy
     *
     * @return Prefix Map
     */
    public static PrefixMap createForOutput(PrefixMap pmap) {
        return new PrefixMapStd(pmap);
    }

    /**
     * Creates a new prefix map which is intended for use in output which starts
     * with a copy of an existing map.
     * <p>
     * Will use whatever the version of ARQ you are using considers the best
     * implementation for output, this may change from release to release.
     * </p>
     *
     * @param pmap
     *            Prefix Map to copy
     *
     * @return Prefix Map
     */
    public static PrefixMap createForOutput(PrefixMapping pmap) {
        PrefixMap created = createForOutput();
        created.putAll(pmap);
        return created;
    }

    /**
     * Creates a new prefix map, initialized from a Map of prefix to IRI string.
     *
     * @param pmap Mapping from prefix to IRI string
     * @return Prefix Map
     */
    public static PrefixMap createForOutput(Map<String, String> pmap) {
        PrefixMap created = createForOutput();
        created.putAll(pmap);
        return created;
    }

    /** Return an immutable view of the prefix map.
     * Throws {@link UnsupportedOperationException} on
     * attempts to update it.  Reflects changes made to the underlying map.
     * @param pmap  PrefixMap
     * @return Prefix Map
     */
    public static PrefixMap unmodifiablePrefixMap(PrefixMap pmap)
    {
        return new PrefixMapUnmodifiable(pmap) ;
    }

    /** Return an always-empty and immutable prefix map
     * @return Prefix Map
     */
    public static PrefixMap emptyPrefixMap()
    {
        return PrefixMapZero.empty ;
    }

}
