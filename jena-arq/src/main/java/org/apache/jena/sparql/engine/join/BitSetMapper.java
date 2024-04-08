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
import java.util.Iterator;
import java.util.List;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

/**
 * Methods for converting collections to and from a bit set representation
 * w.r.t. a list of reference items.
 * <p>
 * For example, if the list of reference items is {@code [ ?x ?y ?z ]}
 * and the given items is the set {@code { ?y ?z }} then
 * the resulting bit set has the value 011.
 */
public class BitSetMapper {

    /**
     * Create a bit set from the binding's key set.
     *
     * @implNote
     * This method relies on {@link List#indexOf(Object)}.
     * The class {@link ImmutableUniqueList} provides an index for these lookups.
     */
    public static BitSet toBitSet(List<Var> referenceList, Binding binding) {
        int n = referenceList.size();
        BitSet result = new BitSet(n);
        if (n < binding.size()) {
            for (int i = 0; i < n; ++i) {
                Var var = referenceList.get(i);
                if (binding.contains(var)) {
                    result.set(i);
                }
            }
        } else { // Iterate over the fewer row variables
            for (Iterator<Var> it = binding.vars(); it.hasNext();) {
                Var var = it.next();
                int idx = referenceList.indexOf(var);
                if (idx != -1) {
                    result.set(idx);
                }
            }
        }
        return result;
    }

    /** Map the positions of all set bits to items in the list. */
    public static <T> List<T> toList(List<T> referenceList, BitSet key) {
        List<T> result = new ArrayList<>(referenceList.size());
        for (int i = key.nextSetBit(0); i >= 0; i = key.nextSetBit(i + 1)) {
            T item = referenceList.get(i);
            result.add(item);
        }
        return result;
    }
}
