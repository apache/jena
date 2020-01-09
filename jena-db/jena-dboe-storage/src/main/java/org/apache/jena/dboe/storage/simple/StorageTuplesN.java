/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.apache.jena.dboe.storage.simple;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.ListUtils;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Match;

/** Tuple of Nodes */
public class StorageTuplesN {

    private Set<Tuple<Node>> tuples = new HashSet<>();

    public final int N;

    public StorageTuplesN(int N) {
        this.N = N;
    }

    public void add(Tuple<Node> terms) {
        check(terms);
        tuples.add(terms);
    }

    public void delete(Tuple<Node> terms) {
        check(terms);
        tuples.remove(terms);
    }

    public void removeAll(Tuple<Node> pattern) {
        List<Tuple<Node>> acc = ListUtils.toList(find(pattern));
        acc.stream().forEach(this::delete);
    }

    public Stream<Tuple<Node>> find(Tuple<Node> pattern) {
        check(pattern);
        return tuples.stream().filter((t)-> match(t, pattern));
    }

    public boolean contain(Tuple<Node> terms) {
        check(terms);
        return tuples.contains(terms);
    }

    private boolean match(Tuple<Node> terms, Tuple<Node> pattern) {
        for (int i = 0; i < terms.len(); i++ ) {
            if ( ! Match.match(terms.get(i), pattern.get(i) ) ) {
                return false;
            }
        }
        return true;
    }

    private static boolean match(Node node, Node pattern) {
        // This is the only use of M
        return Match.match(node, pattern);
        //return pattern == null || pattern == Node.ANY || pattern.equals(node);
    }

    private void check(Tuple<Node> terms) {
        if ( terms.len() != N )
            throw new IllegalArgumentException("Length "+terms.len()+" : expected "+N);
    }

}
