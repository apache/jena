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

package org.apache.jena.fuseki.access;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.sparql.util.Symbol;

/**
 * A graph filter for TDB1 and TDB2 (by generic type).
 * <p>
 * This filter takes a collection of graph names and returns true from
 * {@link #test(Tuple)} if the tuple graph slot is in the collection of graph names or
 * matchDefaultGraph is true. It can be used as an "allow" filter; it can be negated to
 * become a "deny" filter.
 * 
 * @see GraphFilterTDB1#graphFilter
 * @see GraphFilterTDB2#graphFilter
 */
public abstract class GraphFilter<X> implements Predicate<Tuple<X>> {
    private final Set<X> graphs;
    private final boolean matchDefaultGraph;
//    // This makes the GraphFilter stateful.
//    private X slot = null;
    
    protected GraphFilter(Collection<X> matches, boolean matchDefaultGraph) {
        this.graphs = new HashSet<X>(matches);
        this.matchDefaultGraph = matchDefaultGraph;
    }
    
    public abstract Symbol getContextKey();
    
    @Override
    public boolean test(Tuple<X> t) {
        if ( t.len() == 3 ) {
            // Default graph.
            return matchDefaultGraph; 
        }
        X g = t.get(0);
        boolean b = perGraphTest(g);
        return b;
    }

    // The per graph test.
    private boolean perGraphTest(X g) {
        return graphs.contains(g);
//        if ( g == slot ) {
//            System.err.println("Slot hit");
//            return true;
//        }
//        boolean b = matches.contains(g);
//        if ( b )
//            slot = g ;
//        return b;
    }
}