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
import java.util.stream.Collectors;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.io.Printable;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class indexes a set of rows w.r.t. a join key, referred to as the <i>main join key</i>.
 * <p />
 * Consider the main join key [?x, ?y, ?z]:
 * <p />
 * All rows that bind all three variables will be placed into the main table.
 *
 * Any rows that only bind a sub set of the variables, such as [?x, ?z] or [?y],
 * are placed into respective skew tables.
 *
 * JoinIndex instances are dynamically created by {@link MultiHashProbeTable} based on the
 * variables of the bindings used in lookup requests for matching rows.
 *
 * Internally, the lists of variables are represented as bit sets, see also {@link BitSetMapper}.
 */
class JoinIndex
    implements Iterable<Binding>, Printable
{
    private static final Logger logger = LoggerFactory.getLogger(JoinIndex.class);

    private JoinKey superJoinKey;

    private BitSet mainJoinKeyBitSet;
    private HashProbeTable mainTable;

    /** Skew tables hold rows whose variables are a strict sub set of those of this table. */
    private Map<BitSet, HashProbeTable> skewTables;

    /**
     * Constructor of JoinIndex.
     *
     * @param superJoinKey      The join key to which the bit set representation of all involved variable lists refer to.
     * @param mainJoinKeyBitSet The main join key as a bit set w.r.t. to the super join key.
     * @param mainJoinKey       Optionally, as a minor optimization, the main join key can be provided directly.
     *                            It must hold that {@code mainJoinKey = JoinKey.create(BitSetMapper.toList(superJoinKey, mainJoinKeyBitSet));}
     */
    public JoinIndex(JoinKey superJoinKey, BitSet mainJoinKeyBitSet, JoinKey mainJoinKey) {
        this.superJoinKey = Objects.requireNonNull(superJoinKey);
        this.mainJoinKeyBitSet = mainJoinKeyBitSet;
        if (mainJoinKey == null) {
            mainJoinKey = JoinKey.create(BitSetMapper.toList(superJoinKey, mainJoinKeyBitSet));
        }
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

    /** Returns the skew table for the given key. Returns null if there is none. */
    public HashProbeTable getSkewTable(BitSet key) {
        return skewTables == null ? null : skewTables.get(key);
    }

    public Map<BitSet, HashProbeTable> getSkewTables() {
        return skewTables == null ? Collections.emptyMap() : skewTables;
    }

    public Map<JoinKey, HashProbeTable> getSkewTablesByJoinKey() {
        return getSkewTables().entrySet().stream().collect(Collectors.toMap(
            e -> JoinKey.create(BitSetMapper.toList(mainTable.getJoinKey(), e.getKey())),
            Entry::getValue));
    }

    /** Returns the skew table for the given key. Creates the table if needed. Never returns null. */
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

        // The next two lines are: effectiveRowKey = bitwiseAnd(mainKey, rawRowKey)
        BitSet effectiveRowKey = (BitSet)mainJoinKeyBitSet.clone();
        effectiveRowKey.and(rawRowKey);
        boolean isSameKey = effectiveRowKey.equals(mainJoinKeyBitSet);

        // If there are no joining variables then append the data to the no-key bucket of the main table
        if (effectiveRowKey.isEmpty()) {
            mainTable.putNoKey(row);
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

        // Append data from skew tables (if they exist)
        if (skewTables != null) {
            for (Entry<BitSet, HashProbeTable> entry : skewTables.entrySet()) {
                // BitSet skewKey = entry.getKey();
                HashProbeTable skewTable = entry.getValue();

                Iterator<Binding> subIt = skewTable.getCandidates(row, false); // Excludes no-key bucket which should be empty anyway.
                if (logger.isTraceEnabled()) {
                    subIt = printIteratorItems(subIt, "sub-iterator", logger::trace);
                }

                it = Iter.concat(it, subIt);
            }
        }

        if (logger.isTraceEnabled()) {
            it = printIteratorItems(it, "Lookup result for " + row, logger::trace);
        }

        return it;
    }

    @Override
    public Iterator<Binding> iterator() {
        return getCandidates(BindingFactory.empty());
    }

    public void clear() {
        mainTable.clear();
        if (skewTables != null) {
            skewTables.clear();
        }
    }

    @Override
    public String toString() {
        return Printable.toString(this);
    }

    @Override
    public void output(IndentedWriter out) {
        out.ensureStartOfLine();
        out.println("JoinIndex " + mainTable.getJoinKey());
        out.incIndent();
        out.println("Main table: " + mainTable);
        Map<BitSet, HashProbeTable> skewTables = getSkewTables();
        if (skewTables.isEmpty()) {
            out.println("Skew tables: none");
        } else {
            out.println("Skew tables");
            skewTables.values().forEach(table -> {
                out.incIndent();
                out.println("|- " + table);
                out.decIndent();
            });
        }
        out.decIndent();
    }

    /** Helper function to conditionally print out the content of an iterator. Returns another iterator over the seen data. */
    private static <T> Iterator<T> printIteratorItems(Iterator<T> it, String label, Consumer<String> logger) {
        List<T> list = new ArrayList<>();
        it.forEachRemaining(list::add);
        if (label != null) {
            logger.accept(label + ": " + list.size() + " items");
        }
        for (T item : list) {
            logger.accept("- " + item);
        }
        return list.iterator();
    }
}
