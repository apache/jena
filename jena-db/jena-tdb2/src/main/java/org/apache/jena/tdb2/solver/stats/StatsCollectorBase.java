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

package org.apache.jena.tdb2.solver.stats;


import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.graph.Node ;

/** Statistics collector, general purpose */
abstract class StatsCollectorBase<T> {
    private long            count      = 0;
    private Map<T, Integer> predicates = new HashMap<>(10000);
    private Map<T, Integer> types      = new HashMap<>(10000);
    private T               typeTrigger;

    protected StatsCollectorBase(T typeTrigger) {
        this.typeTrigger = typeTrigger;
    }

    public void record(T g, T s, T p, T o) {
        count++;
        predicates.put(p, predicates.getOrDefault(p, 0) + 1);
        if ( typeTrigger != null && typeTrigger.equals(p) )
            types.put(o, types.getOrDefault(o, 0) + 1);
    }

    protected abstract Map<Node, Integer> convert(Map<T, Integer> map);

    public StatsResults results() {
        return new StatsResults(convert(predicates), convert(types), count);
    }
}
