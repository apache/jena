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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.lib.Trie;
import org.apache.jena.ext.com.google.common.cache.Cache;
import org.apache.jena.ext.com.google.common.cache.CacheBuilder;
import org.apache.jena.sparql.graph.PrefixMappingBase;

/**
 * In-memory implementation of a {@link PrefixMap}.
 * <p>
 * This also provides fast URI to prefix name calculation suitable for output. For
 * output, calculating possible prefix names from a URI happens on every URI so this
 * operations needs to be efficient. Normally, a prefix map is "prefix to URI" and
 * the abbreviation is a reverse lookup, which is a scan of the value of the map.
 * This class keeps a reverse lookup map of URI to prefix which combined with a fast,
 * approximate for determining the split point exploiting the most common use cases,
 * provides efficient abbreviation.
 * <p>
 * Usage for abbreviation: call
 * {@linkplain PrefixMapFactory#createForOutput(PrefixMap)} which copies the argument
 * prefix map into an instance of this class, setting up the reverse lookup. This
 * copy is cheaper than repeated reverse lookups would be.
 */
public class PrefixMapStd extends PrefixMapBase {

    public static final int DFT_CACHE_SIZE = 1000;

    private ReadWriteLock rwl = new ReentrantReadWriteLock();

    private Map<String, String> prefixToIri;
    private Map<String, String> prefixToIriView;

    /** A trie for longest prefix lookups */
    private Trie<String> iriToPrefixTrie = new Trie<>();

    /** For exact matches of IRI strings the map is much faster than the trie */
    private Map<String, String> iriToPrefixMap = new HashMap<>();

    /** A cache for mapping iris to prefixes.
     * Wrapping with Optional is needed because the Guava Cache does not allow for null values */
    private Cache<String, Optional<String>> cache;

    /** A generation counter that is incremented on modifications and which is
     * used to invalidate the internal cache when needed.
     * If generation and cacheVersion differ then the next prefix lookup will invalidate the cache and
    /* set cacheVersion to generation */
    private int generation = 0;
    private int cacheVersion = 0;

    public PrefixMapStd() {
        this(DFT_CACHE_SIZE);
    }

    /** Copies the prefixes. Does not copy the cache. */
    public PrefixMapStd(PrefixMap prefixMap) {
        this(DFT_CACHE_SIZE);
        putAll(prefixMap);
    }

    public PrefixMapStd(long prefixLookupCacheSize) {
        this(new ConcurrentHashMap<>(), prefixLookupCacheSize);
    }

    /**
     * Create a PrefixMapStd instance using the the specified prefix-to-iri map implementation and cache size.
     * @param prefixToIri An empty map into which to store prefixes. Should not be changed externally.
     * @param prefixLookupCacheSize The cache size for prefix lookups.
     */
    public PrefixMapStd(Map<String, String> prefixToIri, long prefixLookupCacheSize) {
        super();
        Objects.requireNonNull(prefixToIri);
        if (!prefixToIri.isEmpty()) {
            // Best effort check; the caller may still perform concurrent modifications to the supplied map
            throw new IllegalArgumentException("PrefixToIri map must be initially empty");
        }
        this.prefixToIri = prefixToIri;
        this.prefixToIriView =  Collections.unmodifiableMap(prefixToIri);
        this.cache = CacheBuilder.newBuilder().maximumSize(prefixLookupCacheSize).build();
    }

    @Override
    public void add(String prefix, String iri) {
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(iri);
        String canonicalPrefix = PrefixLib.canonicalPrefix(prefix);
        execute(rwl.writeLock(), () -> {
            String oldIri = prefixToIri.get(canonicalPrefix);
            if (!Objects.equals(oldIri, iri)) {
                if (oldIri != null) {
                    iriToPrefixTrie.remove(oldIri);
                    iriToPrefixMap.remove(oldIri);
                }
                prefixToIri.put(canonicalPrefix, iri);
                iriToPrefixTrie.add(iri, canonicalPrefix);
                iriToPrefixMap.put(iri, canonicalPrefix);
                ++generation;
            }
        });
    }

    /** See notes on reverse mappings in {@link PrefixMappingBase}.
     * This is a complete implementation.
     * <p>
     * Test {@code AbstractTestPrefixMapping.testSecondPrefixDeletedUncoversPreviousMap}.
     */
    @Override
    public void delete(String prefix) {
        Objects.requireNonNull(prefix);
        String canonicalPrefix = PrefixLib.canonicalPrefix(prefix);
        execute(rwl.writeLock(), () -> {
            // Removal returns the previous value or null if there was none
            String iriForPrefix = prefixToIri.remove(canonicalPrefix);
            if (iriForPrefix != null) {
                String prefixForIri = iriToPrefixMap.get(iriForPrefix);
                if (canonicalPrefix.equals(prefixForIri)) {
                    iriToPrefixTrie.remove(iriForPrefix);
                    iriToPrefixMap.remove(prefixForIri);
                }
                ++generation;
            }
        });
    }

    @Override
    public Pair<String, String> abbrev(String iriStr) {
        Objects.requireNonNull(iriStr);
        return calculate(rwl.readLock(), () -> {
            Pair<String, String> r = null;

            String prefix = performPrefixLookup(iriStr);
            String iriForPrefix = prefix != null ? prefixToIri.get(prefix) : null;

            // Post process a found solution
            if (prefix != null && iriForPrefix != null) {
                String localName = iriStr.substring(iriForPrefix.length());
                if (PrefixLib.isSafeLocalPart(localName)) {
                    r = Pair.create(prefix, localName);
                }
            }
            return r;
        });
    }

    @Override
    public String abbreviate(String iriStr) {
        Objects.requireNonNull(iriStr);
        String result = null;
        // Locking is only needed in abbrev
        Pair<String, String> prefixAndLocalName = abbrev(iriStr);
        if (prefixAndLocalName != null) {
            String prefix = prefixAndLocalName.getLeft();
            String ln = prefixAndLocalName.getRight();
            // Safe for RDF/XML as well
            if (strSafeFor(ln, ':')) {
                result = prefix + ":" + ln;
            }
        }
        return result;
    }

    @Override
    public String get(String prefix) {
        Objects.requireNonNull(prefix);
        return calculate(rwl.readLock(), () -> {
            String canonicalPrefix = PrefixLib.canonicalPrefix(prefix);
            return prefixToIri.get(canonicalPrefix);
        });
    }

    /** Returns an unmodifiable and non-synchronized(!) view of the mappings */
    @Override
    public Map<String, String> getMapping() {
        return prefixToIriView;
    }

    @Override
    public Map<String, String> getMappingCopy() {
        Map<String, String> result = calculate(rwl.readLock(), () -> Map.copyOf(prefixToIri));
        return result;
    }

    @Override
    public void clear() {
        execute(rwl.writeLock(), () -> {
            if (!prefixToIri.isEmpty()) {
                prefixToIri.clear();
                iriToPrefixTrie.clear();
                iriToPrefixMap.clear();
                cache.invalidateAll();
                ++generation;
            }
        });
    }

    @Override
    public boolean isEmpty() {
        return calculate(rwl.readLock(), () -> prefixToIri.isEmpty());
    }

    @Override
    public int size() {
        return calculate(rwl.readLock(), () -> prefixToIri.size());
    }

    @Override
    public boolean containsPrefix(String prefix) {
        Objects.requireNonNull(prefix);
        return calculate(rwl.readLock(), () -> {
            String canonicalPrefix = PrefixLib.canonicalPrefix(prefix);
            return prefixToIri.containsKey(canonicalPrefix);
        });
    }

    /**
     * Takes a guess for the namespace URI string to use in abbreviation.
     * Finds the part of the IRI string before the last '#' or '/'.
     *
     * @param iriString String string
     * @return String or null
     */
    protected static String getPossibleKey(String iriString) {
        int n = iriString.length();
        int i;
        for (i = n - 1; i >= 0; --i) {
            char c = iriString.charAt(i);
            if (c == '#' || c == '/') {
                // We could add ':' here, it is used as a separator in URNs.
                // But it is a multiple use character and always present in the scheme name.
                // This is a fast-track guess so don't try guessing based on ':'.
                break;
            }
        }
        String result = i >= 0 ? iriString.substring(0, i + 1) : null;
        return result;
    }

    private String performPrefixLookup(String iriStr) {
        String prefix = null;
        String iriForPrefix = getPossibleKey(iriStr);
        // Try fast track first - if it produces a hit then
        // no overhead writing to the cache is needed
        // The drawback is that we do not necessarily get the longest prefix
        if (iriForPrefix != null) {
            prefix = iriToPrefixMap.get(iriForPrefix);
        }

        // If no solution yet then search for longest prefix
        if (prefix == null) {
            prefix = cachedPrefixLookup(iriStr).orElse(null);
        }
        return prefix;
    }

    private Optional<String> cachedPrefixLookup(String iri) {
        if (cacheVersion != generation) {
            cache.invalidateAll();
            cacheVersion = generation;
        }

        Optional<String> prefix;
        try {
            prefix = cache.get(iri, () -> Optional.ofNullable(uncachedPrefixLookup(iri)));
        } catch (ExecutionException e) {
            throw new RuntimeException("Unexpected failure during cache lookup", e);
        }
        return prefix;
    }

    private String uncachedPrefixLookup(String iriStr) {
        String prefix = iriToPrefixTrie.longestMatch(iriStr);
        return prefix;
    }

    private static void execute(Lock lock, Runnable runnable) {
        lock.lock();
        try {
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

    private static <T> T calculate(Lock lock, Supplier<T> supplier) {
        T result;
        lock.lock();
        try {
            result = supplier.get();
        } finally {
            lock.unlock();
        }
        return result;
    }
}
