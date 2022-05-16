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

package org.apache.jena.sparql.service.enhancer.impl.util;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.ext.com.google.common.collect.AbstractIterator;
import org.apache.jena.ext.com.google.common.collect.Table.Cell;
import org.apache.jena.ext.com.google.common.collect.Tables;

public class IteratorUtils {

    /**
     * For every item in lhs return an iterator over the corresponding sub-range of items in rhs.
     * As soon as rhs is consumed, then for every remaining key in lhs the iterator will be null.
     *
     * Keys in both iterators must appear in the same order - although rhs may omit some or all keys.
     * There MUST NOT exist an item in rhs which has a key that does not correspond to an item in lhs!
     */
    public static <K, X, Y> AbstractIterator<Cell<K, X, Iterator<Y>>> partialLeftMergeJoin(
            Iterator<X> lhs,
            Iterator<Y> rhs,
            Function<X, K> xToK,
            Function<Y, K> yToK) {

        // View rhs as an iterator over (key, value) pairs w.r.t. vToK
        Iterator<Entry<K, X>> lhsKvIt = Iter.map(lhs, x -> new SimpleEntry<>(xToK.apply(x), x));

        Iterator<Entry<K, Y>> rawRhsKvIt = Iter.map(rhs, v -> new SimpleEntry<>(yToK.apply(v), v));
        PeekIteratorLazy<Entry<K, Y>> rhsKvIt = PeekIteratorLazy.create(rawRhsKvIt);

        // TODO We should add sanity checks if rhs contain keys not in lhs
        AbstractIterator<Cell<K, X, Iterator<Y>>> result = new AbstractIterator<>() {
            @Override
            protected Cell<K, X, Iterator<Y>> computeNext() {
                // TODO If there is a prior rhs iterator then consume it

                Cell<K, X, Iterator<Y>> r;

                if (lhsKvIt.hasNext()) {
                    Entry<K, X> lhsE = lhsKvIt.next();
                    K lhsK = lhsE.getKey();
                    X x = lhsE.getValue();

                    // For every lhs key create a sub iterator over consecutive items in rhs having key lhsK
                    Iterator<Y> rhsSubIt = null;
                    if (rhsKvIt.hasNext()) {
                        Entry<K, Y> e = rhsKvIt.peek();
                        K rhsK = e.getKey();

                        if (Objects.equals(lhsK, rhsK)) {
                            rhsSubIt = new AbstractIterator<>() {
                                @Override
                                protected Y computeNext() {
                                    Y rhsR;
                                    if (rhsKvIt.hasNext()) {
                                        Entry<K, Y> subE = rhsKvIt.peek();
                                        K subK = subE.getKey();
                                        if (Objects.equals(lhsK, subK)) {
                                            rhsR = subE.getValue();
                                            rhsKvIt.next();
                                        } else {
                                            rhsR = endOfData();
                                        }
                                    } else {
                                        rhsR = endOfData();
                                    }
                                    return rhsR;
                                }
                            };
                        } else {
                            rhsSubIt = Collections.emptyIterator();
                        }
                    } else {
                        // Return null to indicate that there will be no more value for rhs
                    }

                    r = Tables.immutableCell(lhsK, x, rhsSubIt);
                } else {
                    r = endOfData();
                }

                return r;
            }
        };
        return result;
    }


    public static void mainBasic() {
        List<Integer> lhs = Arrays.asList(1, 4, 5, 8, 9, 11, 12);
        List<Entry<Integer, String>> rhs = new ArrayList<>();
        rhs.add(new SimpleEntry<>(4, "fourA"));
        rhs.add(new SimpleEntry<>(4, "fourB"));
        rhs.add(new SimpleEntry<>(8, "eightA"));
        rhs.add(new SimpleEntry<>(8, "eightB"));
        rhs.add(new SimpleEntry<>(9, "nineA"));

        Iterator<Cell<Object, Integer, Iterator<Entry<Integer, String>>>> it = partialLeftMergeJoin(lhs.iterator(), rhs.iterator(), x -> x, Entry::getKey);

        while (it.hasNext()) {
            Cell<Object, Integer, Iterator<Entry<Integer, String>>> cell = it.next();
            System.out.println(cell.getColumnKey() + ":");

            Iterator<Entry<Integer, String>> subIt = cell.getValue();
            if (subIt == null) {
                System.out.println("  No more items");
            } else {
                subIt.forEachRemaining(x -> System.out.println("  " + x));
            }
        }
/*
 * expected:
1:
4:
  4=fourA
  4=fourB
5:
8:
  8=eightA
  8=eightB
9:
  9=nineA
11:
  No more items
12:
  No more items
*/
    }


    // mainWithEndMarker
    public static void main(String[] args) {
        List<Integer> lhs = Arrays.asList(1, 4, 5, 8, 9, 11, 12, 666);
        List<Entry<Integer, String>> rhs = new ArrayList<>();
        rhs.add(new SimpleEntry<>(4, "fourA"));
        rhs.add(new SimpleEntry<>(4, "fourB"));
        rhs.add(new SimpleEntry<>(8, "eightA"));
        rhs.add(new SimpleEntry<>(8, "eightB"));
        rhs.add(new SimpleEntry<>(9, "nineA"));
        rhs.add(new SimpleEntry<>(666, "endMarker"));

        AbstractIterator<Cell<Object, Integer, Iterator<Entry<Integer, String>>>> it = partialLeftMergeJoin(lhs.iterator(), rhs.iterator(), x -> x, Entry::getKey);

        while (it.hasNext()) {
            Cell<Object, Integer, Iterator<Entry<Integer, String>>> cell = it.next();
            Integer key = cell.getColumnKey();

            System.out.println(key + ":");

            Iterator<Entry<Integer, String>> subIt = cell.getValue();
            if (subIt == null) {
                System.out.println("  No more items");
            } else {
                subIt.forEachRemaining(x -> System.out.println("  " + x));

                if (Objects.equals(key, 666)) {
                    System.out.println("End marker reached");
                }
            }
        }
/*
 * expected:
...
666:
  666=endMarker
End marker reached
*/
    }
}
