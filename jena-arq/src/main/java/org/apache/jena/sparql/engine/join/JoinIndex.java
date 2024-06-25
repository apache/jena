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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

/**
 * A primary index contains information about the indexing of a set of rows w.r.t. a join key.
 * <p />
 * Consider a primary index for the join key [?x, ?y, ?z].
 * <p />
 * All rows that bind all three variables will be placed into {@link #table}.
 *
 * If there is any data that only binds a sub set of the variables, such as [?x, ?z] or [?y]
 * then that data will be referenced via secondary keys.
 *
 * The secondary keys therefore specifically point to <i>all</i> other indexes that may contain relevant data.
 * <p />
 * During primary index creation, bindings may be encountered that map to a secondary key.
 * and thus a secondary table is created on this index.
 */
public class JoinIndex {
    private JoinKey superJoinKey;
    private HashProbeTable table;

    /**
     * Local partial indexes for rows whose variables are a strict sub set of those of this table.
     * Secondary tables are only created if no primary table for the key already exists.
     */
    private Map<BitSet, HashProbeTable> secondaryTables;

    public JoinIndex(JoinKey superJoinKey, JoinKey subJoinKey) {
        this.superJoinKey = Objects.requireNonNull(superJoinKey);
        table = new HashProbeTable(subJoinKey);
    }

    public HashProbeTable getTable() {
        return table;
    }

    public Set<BitSet> getSecondaryKeys() {
        return secondaryTables == null ? Collections.emptySet() : secondaryTables.keySet();
    }

    /** Return a secondary table for the given key. Returns null if there is none. */
    public HashProbeTable getSecondaryTable(BitSet key) {
        return secondaryTables == null ? null : secondaryTables.get(key);
    }

    /** Return a secondary table for the given key. Never returns null.*/
    public HashProbeTable getOrCreateSecondaryTable(BitSet tableKey) {
        if (secondaryTables == null) {
            secondaryTables = new HashMap<>();
        }
        HashProbeTable result = secondaryTables.computeIfAbsent(tableKey, key -> {
            List<Var> vars = BitSetMapper.toList(superJoinKey, key);
            return new HashProbeTable(JoinKey.create(vars));
        });
        return result;
    }
}
