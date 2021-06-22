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

package org.apache.jena.shex.eval;

import static org.apache.jena.atlas.lib.CollectionUtils.oneElt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.shex.expressions.TripleExprCardinality;
import org.apache.jena.shex.sys.ValidationContext;

public class ShapeEvalCardinality {

    /*package*/ static boolean matchesCardinality(ValidationContext vCxt, Set<Triple> matchables, Node node, TripleExprCardinality tripleExprCard, Set<Node> extras) {
        int min = tripleExprCard.min();
        int max = tripleExprCard.max();
        if ( min == 1 && max == 1 )
            // Direct
            return ShapeEval.matches(vCxt, matchables, node, tripleExprCard.target(), extras);

        if ( min == 0 )
            return true;

        // Partition
        List<List<Set<Triple>>> partitions = cardinalityPartition(matchables, min, max);

        if ( ShapeEval.DEBUG_cardinalityOf  ) {
            System.out.println("Cardinality: "+tripleExprCard);
            if ( partitions.isEmpty() ) {
                System.out.println("--");
                System.out.println("<empty>");
            }
            System.out.println("----");
        }

        for ( List<Set<Triple>> partition : partitions ) {
            boolean OK = true;

            if ( ShapeEval.DEBUG_cardinalityOf )
                System.out.println(partition);

            // One partition
            for ( Set<Triple> part : partition ) {
                boolean b = ShapeEval.matches(vCxt, part, node, tripleExprCard.target(), extras);
                if ( ShapeEval.DEBUG_cardinalityOf )
                    System.out.println("  "+b);
                if ( !b ) {
                    OK = false;
                    break;
                }
            }
            if ( OK )
                return true;
        }
        return false;
    }

    // A partition is a list of set of X
    // All partitions are a list of a list of set of X
    private static <X> List<List<Set<X>>> cardinalityPartition(Set<X> collection, int min, int max) {
        if ( max == 1 )
            return List.of(List.of(collection));
        List<List<Set<X>>> results = new ArrayList<>();

        int maxK = (max < 0 ) ? collection.size() : max;

        for ( int k = min; k <= maxK ; k++ ) {
            List<List<Set<X>>> allSizeK = partition(collection, k);
            if ( allSizeK != null )
                results.addAll(allSizeK);
        }
        return results;
    }

    // Consider using Algorithm H : Knuth using integers for the triples ()i.e. copyeds to a list)
    // so that we can use "a1 < a2"

    /* The ways to partition into exactly k boxes.<br/> The algorithm (induction):
     *
     * In a partition involving "A" we have:
     *
     * 1/ either A is a singleton, and the rest is a permutation of the set without A
     * in k-1 slots
     *
     * 2/ or A is one of the permutations, and if removed, the rest are a permutation
     *    of the set without A in k slots.
     *
     * Do the remainder cases and build this case.
     */
    private static <X> List<List<Set<X>>> partition(Set<X> collection, int k) {
        if ( k <= 0 )
            return null;
        if ( collection.size() < k )
            return null;
        // Choose an element.
        if ( collection.size() == k ) {
            // Only one way. Explode.
            // Explode matches.
            List<Set<X>> result1 = new ArrayList<>();
            collection.forEach(item->{
                Set<X> xs = new HashSet<>();
                xs.add(item);
                result1.add(xs);
            });
            return List.of(result1);
        }

        X item = oneElt(collection);
        Set<X> processing = new HashSet<>(collection);
        processing.remove(item);

        List<List<Set<X>>> results = new ArrayList<>();


        // Case 1: singleton item.
        // For all permutations of the rest in (k-1) slots,
        //    add a singleton set to each list of partitions.
        List<List<Set<X>>> x2 = partition(processing, k-1);
        if ( x2 != null ) {
            for ( List<Set<X>> partition : x2 ) {
                List<Set<X>> partition2 = new ArrayList<>(partition);
                partition2.add(Set.of(item));
                results.add(partition2);
            }
        }

        // Case 2: item in each partition not as a singleton.
        // For all permutations of the rest
        //    add the removed element to one of the partition sets.
        List<List<Set<X>>> x = partition(processing, k);

        for ( int i = 0 ; i < x.size() ; i++ ) {
            List<Set<X>> subPartition = x.get(i);

            for ( int j = 0 ; j < subPartition.size() ; j++ ) {
                Set<X> box = new HashSet<>(subPartition.get(j));
                List<Set<X>> withItem = new ArrayList<>(subPartition);
                box.add(item);
                withItem.set(j, box);
                results.add(withItem);
            }
        }

        return results;
    }
}