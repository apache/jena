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

import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.io.Printable;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Probe table that can store a set of bindings and dynamically re-indexes them
 * in additional {@link JoinIndex} instances based on the lookup requests.
 * The initial set of bindings is stored in an initial JoinIndex from which further indexes are derived.
 * <p>
 * A unique list of seen variables is maintained which captures the set of variables mentioned across all stored bindings.
 * <p>
 * A JoinIndex instance is created for each list of variables obtained from the intersection of
 * the seen variable list and the set of variables that appear in a lookup request.
 * <p>
 * Sets of variables are represented as bit sets based on the seen variable list. So if the variables [?x ?y ?z] have been seen,
 * then a lookup with a binding mentioning [?x ?z] will be represented with the bit set 101.
 * <p>
 * Each JoinIndex holds all bindings that were added to the MultiHashProbeTable.
 * A JoinIndex is partitioned into a single main table and zero or more skew tables.
 * Every binding that bind all variables of the intersection with a lookup request is placed into the main table.
 * Every binding that binds fewer variables is placed into a respective skew table.
 */
class MultiHashProbeTable
    implements Printable
{
    private static final Logger logger = LoggerFactory.getLogger(MultiHashProbeTable.class);

    // Enable printing of debug output
    private static boolean    isDebugOutputEnabled = false;

    /**
     * The joinKey specifies the largest set of variables for which to create HashProbeTable indexes.
     * If it is null then there is no restriction.
     */
    private final JoinKey     maxJoinKey;

    /** The initial index where all rows are put into. Further indexes may be created as needed during lookups. */
    private final JoinIndex   initialIndex;

    /** Tables for hash lookups. Will be created dynamically during {@link #getCandidates(Binding)}. */
    private final Map<BitSet, JoinIndex> indexes = new HashMap<>();

    /** The set of seen variables across all rows */
    private final Set<Var>    seenVarSet             = new LinkedHashSet<>();

    /* The following two fields are initialized on the first call to getCandidates  */

    private boolean           isFinalized            = false;

    /** Instead of using Set&gt;Var&lt; we create bit sets w.r.t. seenVarSet to represent subsets of variables */
    private JoinKey           seenVarsJoinKey;

    public MultiHashProbeTable(JoinKey maxJoinKey, JoinKey initialJoinKey) {
        this.maxJoinKey = maxJoinKey;

        if (maxJoinKey != null && initialJoinKey != null && !maxJoinKey.containsAll(initialJoinKey)) {
            throw new IllegalArgumentException("Variables of the initial join key must be a sub set of the root one.");
        }

        if (initialJoinKey == null) {
            initialJoinKey = JoinKey.empty();
        }

        // If an initial join key is given then its variables are added to seen vars
        // so that those correspond to the first bits of the bit keys
        seenVarSet.addAll(initialJoinKey);

        int nbits = initialJoinKey.size();
        BitSet initialJoinKeyBitset = new BitSet(nbits);
        initialJoinKeyBitset.flip(0, nbits);

        if (logger.isTraceEnabled()) {
            logger.trace("Initial join index configured with variables " + initialJoinKey + " and bits " + initialJoinKeyBitset);
        }

        this.initialIndex = new JoinIndex(initialJoinKey, initialJoinKeyBitset, initialJoinKey);
    }

    Map<JoinKey, JoinIndex> getIndexesByJoinKeys() {
        return indexes.entrySet().stream()
                .collect(Collectors.toMap(e -> toJoinKey(e.getKey()), Entry::getValue));
    }

    Map<BitSet, JoinIndex> getIndexes() {
        return indexes;
    }

    JoinKey toJoinKey(BitSet bitSet) {
        JoinKey vars = JoinKey.create(BitSetMapper.toList(seenVarsJoinKey, bitSet));
        return vars;
    }

    public void put(Binding row) {
        if (isFinalized) {
            throw new IllegalStateException("Cannot add more bindings after a lookup was performed.");
        }
        updateSeenVars(row);
        initialIndex.put(row);
    }

    /** Update seen vars with the row's relevant variables w.r.t. an optional rootJoinKey. */
    private void updateSeenVars(Binding row) {
        if (maxJoinKey == null) {
            row.vars().forEachRemaining(seenVarSet::add);
        } else {
            // Iterate over the smallest set of variables
            if (maxJoinKey.length() < row.size()) { // join key has fewer vars than the row
                maxJoinKey.forEach(v -> {
                    if (row.contains(v)) {
                        seenVarSet.add(v);
                    }
                });
            } else { // row has fewer vars than the join key
                row.vars().forEachRemaining(v -> {
                    if (maxJoinKey.contains(v)) {
                        seenVarSet.add(v);
                    }
                });
            }
        }
    }

    public Iterator<Binding> getCandidates(Binding row) {
        if (!isFinalized) {
            doFinalize();
        }
        BitSet joinKeyBitSet = BitSetMapper.toBitSet(seenVarsJoinKey, row);
        if (isDebugOutputEnabled) {
            if (logger.isDebugEnabled()) {
                logger.debug("Lookup with " + BitSetMapper.toList(seenVarsJoinKey, joinKeyBitSet));
            }
        }

        Iterator<Binding> it;
        if (joinKeyBitSet.isEmpty()) {
            // Case for no joining variables: all bindings unconditionally become candidates
            it = initialIndex.iterator();
        } else {
            JoinIndex primaryIndex = getOrCreateJoinIndex(joinKeyBitSet);
            it = primaryIndex.getCandidates(row);
        }
        return it;
    }

    /** Calling this method indicates that all bindings have been collected
     *  and are ready for indexing. No further updates are allowed unless
     *  {@link #clear()} is called.
     *
     *  This method is package private so that it can be called from tests.
     */
    void doFinalize() {
        // Note: We need to stick with the variable order provided in the initial index -> don't sort!
        // Arrays.sort(seenVars, (a, b) -> a.getName().compareTo(b.getName()));
        seenVarsJoinKey = JoinKey.create(seenVarSet);
        indexes.put(initialIndex.getMainJoinKeyBitSet(), initialIndex);
        isFinalized = true;
    }

    private JoinIndex getOrCreateJoinIndex(BitSet joinKeyBitSet) {
        JoinIndex result = indexes.computeIfAbsent(joinKeyBitSet, this::createJoinIndex);
        return result;
    }

    private JoinIndex createJoinIndex(BitSet joinKeyBitSet) {
        JoinKey joinKey = JoinKey.create(BitSetMapper.toList(seenVarsJoinKey, joinKeyBitSet));
        JoinIndex result = new JoinIndex(seenVarsJoinKey, joinKeyBitSet, joinKey);

        if (logger.isTraceEnabled()) {
            logger.trace("Creating join index with variables " + joinKey + " and bits " + joinKeyBitSet);
        }

        for (Binding row : initialIndex) {
            result.put(row);
        }

        return result;
    }

    @Override
    public String toString() {
        return Printable.toString(this);
    }

    @Override
    public void output(IndentedWriter out) {
        out.ensureStartOfLine();
        out.println("MultiHashProbeTable");
        out.incIndent();
        getIndexesByJoinKeys().forEach((joinKey, index) -> {
            index.output(out);
        });
        out.decIndent();
    }

    public Iterator<Binding> values() {
        return initialIndex.iterator();
    }

    public void clear() {
        indexes.clear();
        initialIndex.clear();
        seenVarSet.clear();
        seenVarsJoinKey = null;
        isFinalized = false;
    }
}
