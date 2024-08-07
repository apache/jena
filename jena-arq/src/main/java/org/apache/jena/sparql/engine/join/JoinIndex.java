/**
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
package org.apache.jena.sparql.engine.join;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A join index contains information about the indexing of a set of rows w.r.t. a join key.
 * <p />
 * Consider a primary index for the join key [?x, ?y, ?z]:
 * <p />
 * All rows that bind all three variables will be placed into the {@link #mainTable}.
 *
 * If there is any data that only binds a sub set of the variables, such as [?x, ?z] or [?y],
 * then that data will be placed into respective skew tables.
 */
public class JoinIndex
    implements Iterable<Binding>
{
    private static final Logger logger = LoggerFactory.getLogger(JoinIndex.class);

    private JoinKey superJoinKey;

    private BitSet mainJoinKeyBitSet;
    private HashProbeTable mainTable;

    /** Skew tables hold rows whose variables are a strict sub set of those of this table. */
    private Map<BitSet, HashProbeTable> skewTables;

    public JoinIndex(JoinKey superJoinKey, JoinKey mainJoinKey, BitSet mainJoinKeyBitSet) {
        this.superJoinKey = Objects.requireNonNull(superJoinKey);
        this.mainJoinKeyBitSet = mainJoinKeyBitSet;
        this.mainTable = new HashProbeTable(mainJoinKey);
    }

    public BitSet getMainJoinKeyBitSet() {
        return mainJoinKeyBitSet;
    }

    public HashProbeTable getMainTable() {
        return mainTable;
    }

    public Set<BitSet> getSkewKeys() {
        return skewTables == null ? Collections.emptySet() : skewTables.keySet();
    }

    /** Return a secondary table for the given key. Returns null if there is none. */
    public HashProbeTable getSkewTable(BitSet key) {
        return skewTables == null ? null : skewTables.get(key);
    }

    public Map<BitSet, HashProbeTable> getSkewTables() {
        return skewTables == null ? Collections.emptyMap() : skewTables;
    }

    /** Return a secondary table for the given key. Never returns null.*/
    public HashProbeTable getOrCreateSkewTable(BitSet tableKey) {
        if (skewTables == null) {
            // LinkedHashMap for determinism: Always traverse skew tables in their creation order
            skewTables = new LinkedHashMap<>();
        }
        HashProbeTable result = skewTables.computeIfAbsent(tableKey, key -> {
            List<Var> vars = BitSetMapper.toList(superJoinKey, key);
            return new HashProbeTable(JoinKey.create(vars));
        });
        return result;
    }

    public void put(Binding row) {
        BitSet rawRowKey = BitSetMapper.toBitSet(superJoinKey, row);

        // The next two lines are: effectiveRowKey = bitwiseAnd(primaryKey, rawRowKey)
        BitSet effectiveRowKey = (BitSet)mainJoinKeyBitSet.clone();
        effectiveRowKey.and(rawRowKey);
        boolean isSameKey = effectiveRowKey.equals(mainJoinKeyBitSet);

        // If there are no joining variables then append the data to the no-key bucket of the main table
        if (effectiveRowKey.isEmpty()) {
            mainTable.getNoKey$().add(row);
        } else if (isSameKey) {
            // Hash the row; will never end up in the no-key bucket because that case is handled first
            mainTable.put(row);
        } else {
            HashProbeTable skewTable = getOrCreateSkewTable(effectiveRowKey);
            skewTable.put(row);
        }
    }

    public Iterator<Binding> getCandidates(Binding row) {
        if (logger.isTraceEnabled()) {
            BitSet joinKeyBitSet = BitSetMapper.toBitSet(superJoinKey, row);
            logger.trace("Lookup with " + BitSetMapper.toList(superJoinKey, joinKeyBitSet));
        }

        Iterator<Binding> it = getMainTable().getCandidates(row); // Includes no-key bucket
        // it = print(it, "it", logger::trace, logger.isTraceEnabled());

        if (skewTables != null) {
            for (Entry<BitSet, HashProbeTable> entry : skewTables.entrySet()) {
                // BitSet skewKey = entry.getKey();
                HashProbeTable skewTable = entry.getValue();

                Iterator<Binding> subIt = skewTable.getCandidates(row, false); // Excludes no-key bucket which should be empty anyway.
                subIt = print(subIt, "subIt", logger::trace, logger.isTraceEnabled());

                it = Iter.concat(it, subIt);
            }
        }
        it = print(it, "Lookup result for " + row, logger::trace, logger.isTraceEnabled());

        return it;
    }

    @Override
    public Iterator<Binding> iterator() {
        return getCandidates(BindingFactory.empty());
    }

    /** Helper function to conditionally print out the content of an iterator. Returns another iterator over the seen data. */
    private static <T> Iterator<T> print(Iterator<T> it, String label, Consumer<String> logger, boolean enabled) {
        if (enabled) {
            List<T> list = new ArrayList<>();
            it.forEachRemaining(list::add);
            if (label != null) {
                logger.accept(label + ": " + list.size() + " items");
            }
            for (T item : list) {
                logger.accept("- " + item);
            }
            return list.iterator();
        } else {
            return it;
        }
    }

    public void clear() {
        mainTable.clear();
        if (skewTables != null) {
            skewTables.clear();
        }
    }
}
