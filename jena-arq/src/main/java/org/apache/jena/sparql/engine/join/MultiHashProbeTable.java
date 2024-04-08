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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Probe table that dynamically creates HashProbeTables on-demand based on the lookup requests */
class MultiHashProbeTable {
    private static final Logger logger = LoggerFactory.getLogger(MultiHashProbeTable.class);

    // Enable printing of debug output
    private static boolean    isDebugOutputEnabled = false;

    /**
     * The joinKey specifies the largest set of variables for which to create HashProbeTable indexes.
     * If it is null then there is no restriction.
     */
    private final JoinKey     maxJoinKey;

    /** The initial index where all rows are put into. Further indexes may be created as needed during lookups. */
    private JoinIndex initialIndex;

    /** Tables for hash lookups. Will be created dynamically during {@link #getCandidates(Binding)}. */
    private final Map<BitSet, JoinIndex> indexes = new HashMap<>();

    /** The set of seen variables across all rows */
    private Set<Var>          seenVarSet             = new LinkedHashSet<>();

    /* The following two fields are initialized on the first call to getCandidates  */

    private boolean           isFinalized            = false;

    /** Instead of using Set<Var> we create bit sets w.r.t. seenVarSet to represent subsets of variables */
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

        this.initialIndex = new JoinIndex(initialJoinKey, initialJoinKey, initialJoinKeyBitset);
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
            for (Iterator<Var> it = row.vars(); it.hasNext(); ) {
                Var var = it.next();
                seenVarSet.add(var);
            }
        } else {
            // Iterate over the smallest set of variables
            if (maxJoinKey.length() < row.size()) { // join key has fewer vars than the row
                for (Iterator<Var> it = maxJoinKey.iterator(); it.hasNext(); ) {
                    Var var = it.next();
                    if (row.contains(var)) {
                        seenVarSet.add(var);
                    }
                }
            } else { // row has fewer vars than the join key
                for (Iterator<Var> it = row.vars(); it.hasNext(); ) {
                    Var var = it.next();
                    if (maxJoinKey.contains(var)) {
                        seenVarSet.add(var);
                    }
                }
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

    /**
     * Calling this method indicates that all bindings have been collected and are ready for indexing.
     */
    // XXX Instead of doFinalize we could introduce a MultiHashProbeTableBuilder that returns an immutable HashProbeTable.
    private void doFinalize() {
        Var[] seenVars = seenVarSet.toArray(new Var[0]);
        // Arrays.sort(seenVars, (a, b) -> a.getName().compareTo(b.getName()));
        seenVarsJoinKey = JoinKey.createUnsafe(seenVars);
        indexes.put(initialIndex.getMainJoinKeyBitSet(), initialIndex);
        isFinalized = true;
    }

    private JoinIndex getOrCreateJoinIndex(BitSet joinKeyBitSet) {
        JoinIndex result = indexes.computeIfAbsent(joinKeyBitSet, this::createJoinIndex);
        return result;
    }

    private JoinIndex createJoinIndex(BitSet joinKeyBitSet) {
        List<Var> vars = BitSetMapper.toList(seenVarsJoinKey, joinKeyBitSet);
        JoinKey subJoinKey = JoinKey.create(vars);
        JoinIndex result = new JoinIndex(seenVarsJoinKey, subJoinKey, joinKeyBitSet);

        if (logger.isTraceEnabled()) {
            logger.trace("Creating join index with variables " + subJoinKey + " and bits " + joinKeyBitSet);
        }

        for (Binding row : initialIndex) {
            result.put(row);
        }

        return result;
    }

    @Override
    public String toString() {
        return indexes.toString();
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

    /** Helper function to conditionally print out the content of an iterator. Returns another iterator over the seen data. */
//    private static <T> Iterator<T> print(Iterator<T> it, String label, Consumer<String> logger, boolean enabled) {
//        if (enabled) {
//            List<T> list = new ArrayList<>();
//            it.forEachRemaining(list::add);
//            if (label != null) {
//                logger.accept(label + ":");
//            }
//            for (T item : list) {
//                logger.accept("- " + item);
//            }
//            return list.iterator();
//        } else {
//            return it;
//        }
//    }
}

