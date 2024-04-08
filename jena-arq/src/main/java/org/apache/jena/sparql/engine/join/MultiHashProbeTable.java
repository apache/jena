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
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

/** Probe table that dynamically creates HashProbeTables on-demand based on the lookup requests */
class MultiHashProbeTable {
    // Enable printing of debug output
    private static boolean    isDebugEnabled = false;

    /**
     * The joinKey specifies the largest set of variables for which to create HashProbeTable indexes.
     * If it is null then there is no restriction.
     */
    private final JoinKey     rootJoinKey;

    /** The materialized rows as a list - not indexed. */
    private List<Binding>     rows                   = new ArrayList<>();

    /** Tables for hash lookups. Will be created dynamically during {@link #getCandidates(Binding)}. */
    private final Map<BitSet, JoinIndex> joinIndexes = new HashMap<>();

    /** The set of seen variables across all rows */
    private Set<Var>          seenVarSet             = new HashSet<>();

    /* The following two fields are initialized on the first call to getCandidates  */

    private boolean           isFinalized            = false;

    /** Instead of using Set<Var> we create bit sets w.r.t. seenVarSet to represent subsets of variables */
    private JoinKey           seenVarsJoinKey;

    public MultiHashProbeTable(JoinKey joinKey) {
        this.rootJoinKey = joinKey;
    }

    public void put(Binding row) {
        if (isFinalized) {
            throw new IllegalStateException("Cannot add more bindings after a lookup was performed.");
        }
        updateSeenVars(row);
        rows.add(row);
    }

    /** Update seen vars with the row's relevant variables w.r.t. an optional rootJoinKey. */
    private void updateSeenVars(Binding row) {
        if (rootJoinKey == null) {
            for (Iterator<Var> it = row.vars(); it.hasNext(); ) {
                Var var = it.next();
                seenVarSet.add(var);
            }
        } else {
            // Iterate over the smallest set of variables
            if (rootJoinKey.length() < row.size()) { // join key has fewer vars than the row
                for (Iterator<Var> it = rootJoinKey.iterator(); it.hasNext(); ) {
                    Var var = it.next();
                    if (row.contains(var)) {
                        seenVarSet.add(var);
                    }
                }
            } else { // row has fewer vars than the join key
                for (Iterator<Var> it = row.vars(); it.hasNext(); ) {
                    Var var = it.next();
                    if (rootJoinKey.contains(var)) {
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
        if (isDebugEnabled) {
            System.err.println("Lookup with " + BitSetMapper.toList(seenVarsJoinKey, joinKeyBitSet));
        }

        Iterator<Binding> it;
        if (joinKeyBitSet.isEmpty()) {
            // Case for no joining variables: all bindings unconditionally become candidates
            it = rows.iterator();
        } else {
            JoinIndex primaryIndex = getOrCreateIndex(joinKeyBitSet);
            it = primaryIndex.getTable().getCandidates(row);
            it = print(it, "it", isDebugEnabled);

            for (BitSet secondaryKey : primaryIndex.getSecondaryKeys()) {
                HashProbeTable secondaryTable = primaryIndex.getSecondaryTable(secondaryKey);

                // Sanity check: secondaryTable must not be null
                if (secondaryTable == null) {
                    List<Var> secondaryVars = BitSetMapper.toList(seenVarsJoinKey, secondaryKey);
                    throw new ARQInternalErrorException("No table found although it was declared to exist for variables: " + secondaryVars);
                }

                Iterator<Binding> subIt = secondaryTable.getCandidates(row, false);
                subIt = print(subIt, "subIt", isDebugEnabled);

                it = Iter.concat(it, subIt);
            }
        }
        return it;
    }

    /**
     * Calling this method indicates that all bindings have been collected and are ready for indexing.
     */
    private void doFinalize() {
        Var[] seenVars = seenVarSet.toArray(new Var[0]);
        Arrays.sort(seenVars, (a, b) -> a.getName().compareTo(b.getName()));
        seenVarsJoinKey = JoinKey.createUnsafe(seenVars);
        isFinalized = true;
    }

    private JoinIndex getOrCreateIndex(BitSet joinKeyBitSet) {
        JoinIndex result = joinIndexes.get(joinKeyBitSet);
        if (result == null) {
            List<Var> vars = BitSetMapper.toList(seenVarsJoinKey, joinKeyBitSet);
            JoinKey subJoinKey = JoinKey.create(vars);
            result = new JoinIndex(seenVarsJoinKey, subJoinKey);
            joinIndexes.put(joinKeyBitSet, result);

            HashProbeTable primaryTable = result.getTable();
            for (Binding row : rows) {
                BitSet effectiveRowKey = null;
                BitSet rawRowKey = BitSetMapper.toBitSet(seenVarsJoinKey, row);

                // The next two lines are: effectiveRowKey = bitwiseAnd(primaryKey, rawRowKey)
                effectiveRowKey = (BitSet)joinKeyBitSet.clone();
                effectiveRowKey.and(rawRowKey);
                boolean isSameKey = effectiveRowKey.equals(joinKeyBitSet);

                if (effectiveRowKey.isEmpty()) {
                    primaryTable.getNoKey$().add(row);
                } else if (isSameKey) {
                    // Hash the row (will never end up in the no key bucket)
                    primaryTable.put(row);
                } else {
                    HashProbeTable secondaryTable = result.getOrCreateSecondaryTable(effectiveRowKey);
                    secondaryTable.put(row);
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return joinIndexes.toString();
    }

    public Iterator<Binding> values() {
        return rows.iterator();
    }

    public void clear() {
        joinIndexes.clear();
        rows.clear();
        seenVarSet.clear();
        seenVarsJoinKey = null;
        isFinalized = false;
    }

    private static <T> Iterator<T> print(Iterator<T> it, String label, boolean enabled) {
        if (enabled) {
            List<T> list = new ArrayList<>();
            it.forEachRemaining(list::add);
            System.err.println(label + ":");
            for (T item : list) {
                System.err.println("- " + item);
            }
            it = list.iterator();
        }
        return it;
    }
}

