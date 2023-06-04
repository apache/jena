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

package org.apache.jena.atlas.iterator;

import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertFalse ;
import static org.junit.Assert.assertTrue ;

import java.util.* ;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test ;

public class TestIter
{
    private List<String> data0 = new ArrayList<>() ;
    private List<String> data1 = Arrays.asList("a") ;
    private List<String> data2 = Arrays.asList("x","y","z") ;
    private List<String> data3 = Arrays.asList(null, "x", null, null, null, "y", "z", null);

    @Test
    public void append_1() {
        Iterator<String> iter = Iter.append(data1.iterator(), data0.iterator());
        test(iter, "a");
    }

    @Test
    public void append_2() {
        Iterator<String> iter = Iter.append(data0.iterator(), data1.iterator());
        test(iter, "a");
    }

    @Test
    public void append_3() {
        Iterator<String> iter = Iter.append(data1.iterator(), data2.iterator());
        test(iter, "a", "x", "y", "z");
    }

    private static List<String> mutableList(String...strings) {
        List<String> list = new ArrayList<>();
        for ( String s : strings )
            list.add(s);
        return list;
    }

    @Test
    public void append_4() {
        List<String> L = mutableList("a", "b", "c");
        List<String> R = mutableList("d", "e", "f");

        Iterator<String> LR = Iter.append(L.iterator(), R.iterator());

        while (LR.hasNext()) {
            String s = LR.next();
            if ( "c".equals(s) ) {
                LR.hasNext();  // test for JENA-60
                LR.remove();
            }
        }

        assertEquals(2, L.size());
        assertEquals(Arrays.asList("a", "b"), L);
        assertEquals(Arrays.asList("d", "e", "f"), R);
    }

    @Test
    public void append_5() {
        List<String> L = mutableList("a", "b", "c");
        List<String> R = mutableList("d", "e", "f");

        Iterator<String> LR = Iter.append(L.iterator(), R.iterator());

        while (LR.hasNext()) {
            String s = LR.next();

            if ( "d".equals(s) ) {
                LR.hasNext();  // test for JENA-60
                LR.remove();
            }
        }

        assertEquals(3, L.size());
        assertEquals(Arrays.asList("e", "f"), R);
    }

    @Test
    public void append_6() {
        List<String> L = mutableList("a", "b", "c");
        List<String> R = mutableList("d", "e", "f");

        Iterator<String> LR = Iter.append(L.iterator(), R.iterator());

        while (LR.hasNext()) {
            LR.next();
        }
        LR.remove();

        assertEquals(3, L.size());
        assertEquals(Arrays.asList("a", "b", "c"), L);
        assertEquals(Arrays.asList("d", "e"), R);
    }

    private static void test(Iterator<? > iter, Object...items) {
        for ( Object x : items ) {
            assertTrue(iter.hasNext());
            assertEquals(x, iter.next());
        }
        assertFalse(iter.hasNext());
    }

    private static void testWithForeachRemaining(Iterator<? > iter, Object...items) {
        Integer i[] = {0};
        iter.forEachRemaining(x -> {
            assertTrue(i[0] < items.length);
            assertEquals(items[i[0]], x);
            i[0]++;
        });
    }

    static Iter.Folder<String, String> f1 = (acc, arg)->acc + arg ;

    @Test
    public void fold_01() {
        String[] x = {"a", "b", "c"};
        String z = Iter.foldLeft(Arrays.asList(x).iterator(), "X", f1);
        assertEquals("Xabc", z);
    }

    @Test
    public void fold_02() {
        String[] x = {"a", "b", "c"};
        String z = Iter.foldRight(Arrays.asList(x).iterator(), "X", f1);
        assertEquals("Xcba", z);
    }

    @Test
    public void fold_03() {
        String[] x = {};
        String z = Iter.foldLeft(Arrays.asList(x).iterator(), "X", f1);
        assertEquals("X", z);
    }

    @Test
    public void fold_04() {
        String[] x = {};
        String z = Iter.foldRight(Arrays.asList(x).iterator(), "X", f1);
        assertEquals("X", z);
    }

    @Test
    public void operate_01() {
        var elements = new ArrayList<>(Arrays.asList("x", "y", "z"));
        Iterator<String> it = Iter.operate(data2.iterator(), item -> elements.remove(item));
        test(it, elements.toArray());
        assertEquals(0, elements.size());
    }

    @Test
    public void operate_02() {
        var elements = new ArrayList<>(Arrays.asList("x", "y", "z"));
        Iterator<String> it = Iter.operate(data2.iterator(), item -> elements.remove(item));
        testWithForeachRemaining(it, elements.toArray());
        assertEquals(0, elements.size());
    }

    @Test
    public void limit_01() {
        Iterator<String> it = Iter.limit(data2.iterator(), 0);
        assertFalse(it.hasNext());
    }

    @Test
    public void limit_02() {
        Iterator<String> it = Iter.limit(data2.iterator(), 1);
        test(it, "x");
    }

    @Test
    public void limit_03() {
        Iterator<String> it = Iter.limit(data2.iterator(), 2);
        test(it, "x", "y");
    }

    @Test
    public void limit_04() {
        Iterator<String> it = Iter.limit(data2.iterator(), 3);
        test(it, "x", "y", "z");
    }

    @Test
    public void limit_05() {
        Iterator<String> it = Iter.limit(data2.iterator(), 4);
        test(it, "x", "y", "z");
    }

    @Test
    public void map_01() {
        Iterator<String> it = Iter.map(data2.iterator(), item -> item + item);
        test(it, "xx", "yy", "zz");
    }

    @Test
    public void map_02() {
        Iterator<String> it = Iter.map(data2.iterator(), item -> item + item);
        testWithForeachRemaining(it, "xx", "yy", "zz");
    }

    @Test
    public void flatmap_01() {
        Iterator<String> it = Iter.flatMap(data2.iterator(), item -> Arrays.asList(item+item, item).iterator());
        test(it, "xx", "x", "yy", "y", "zz", "z");
    }

    @Test
    public void flatmap_02() {
        List<Integer> data = Arrays.asList(1,2,3);
        Iterator<Integer> it = Iter.flatMap(data.iterator(), x -> {
            if ( x == 2 ) return Iter.nullIterator();
            return Arrays.asList(x*x).iterator();
        });
        test(it, 1, 9);
    }
    @Test
    public void flatmap_03() {
        List<Integer> data = Arrays.asList(1,2,3);
        Function<Integer, Iterator<String>> mapper = x -> {
                switch(x) {
                    case 1: return Iter.nullIterator();
                    case 2: return Arrays.asList("two").iterator();
                    case 3: return Iter.nullIterator();
                    default: throw new IllegalArgumentException();
                }
            };

        Iter<String> it = Iter.iter(data.iterator()).flatMap(mapper);
        test(it, "two");
    }
    @Test
    public void flatmap_04() {
        Iterator<String> it = Iter.flatMap(data2.iterator(), item -> Arrays.asList(item+item, item).iterator());
        testWithForeachRemaining(it, "xx", "x", "yy", "y", "zz", "z");
    }

    @Test
    public void flatmap_05() {
        List<Integer> data = Arrays.asList(1,2,3);
        Iterator<Integer> it = Iter.flatMap(data.iterator(), x -> {
            if ( x == 2 ) return Iter.nullIterator();
            return Arrays.asList(x*x).iterator();
        });
        testWithForeachRemaining(it, 1, 9);
    }

    @Test
    public void flatmap_06() {
        List<Integer> data = Arrays.asList(1,2,3);
        Function<Integer, Iterator<String>> mapper = x -> {
            switch(x) {
                case 1: return Iter.nullIterator();
                case 2: return Arrays.asList("two").iterator();
                case 3: return Iter.nullIterator();
                default: throw new IllegalArgumentException();
            }
        };

        Iter<String> it = Iter.iter(data.iterator()).flatMap(mapper);
        testWithForeachRemaining(it, "two");
    }

    private Predicate<String> filter = item -> item.length() == 1;

    @Test
    public void first_01() {
        Iter<String> iter = Iter.nullIter();
        assertEquals(null, Iter.first(iter, filter));
    }

    @Test
    public void first_04() {
        Iter<String> iter = Iter.nullIter();
        assertEquals(-1, Iter.firstIndex(iter, filter));
    }

    @Test
    public void last_01() {
        Iter<String> iter = Iter.nullIter();
        assertEquals(null, Iter.last(iter, filter));
    }

    @Test
    public void last_05() {
        List<String> data = Arrays.asList("11", "A", "B", "C");
        assertEquals(3, Iter.lastIndex(data, filter));
    }

    @Test
    public void last_06() {
        List<String> data = Arrays.asList("11", "AA", "BB", "CC");
        assertEquals(-1, Iter.lastIndex(data, filter));
    }

    private static Iterator<? > iterator(int n) {
        return IntStream.range(1, n + 1).iterator();
    }

    @Test
    public void iteratorStep_1() {
        Iterator<? > iter = iterator(0);
        int x = Iter.step(iter, 0);
        assertEquals(0, x);
    }

    @Test
    public void iteratorStep_2() {
        Iterator<? > iter = iterator(0);
        int x = Iter.step(iter, 1);
        assertEquals(0, x);
    }

    @Test
    public void iteratorStep_3() {
        Iterator<? > iter = iterator(5);
        int x = Iter.step(iter, 1);
        assertEquals(1, x);
    }

    @Test
    public void iteratorStep_4() {
        Iterator<? > iter = iterator(5);
        int x = Iter.step(iter, 4);
        assertEquals(4, x);
    }

    @Test
    public void iteratorStep_5() {
        Iterator<? > iter = iterator(5);
        int x = Iter.step(iter, 5);
        assertEquals(5, x);
    }

    @Test
    public void iteratorStep_6() {
        Iterator<? > iter = iterator(5);
        int x = Iter.step(iter, 6);
        assertEquals(5, x);
    }

    @SafeVarargs
    private static <X> Iterator<X> data(X ... items) {
        List<X> a = new ArrayList<>(items.length);
        for (X x : items )
            a.add(x);
        return a.iterator();
    }

    @Test public void anyMatch1() {
        boolean b = Iter.anyMatch(data("2"), x->x.equals("2"));
        assertTrue(b);
    }

    @Test public void anyMatch2() {
        boolean b = Iter.anyMatch(data("2","3"), x->x.equals("2"));
        assertTrue(b);
    }

    @Test public void anyMatch3() {
        boolean b = Iter.anyMatch(data("2","3"), x->x.equals("1"));
        assertFalse(b);
    }

    @Test public void allMatch1() {
        boolean b = Iter.allMatch(data("2", "2"), x->x.equals("2"));
        assertTrue(b);
    }

    @Test public void allMatch2() {
        boolean b = Iter.allMatch(data("1", "2"), x->x.equals("2"));
        assertFalse(b);
    }


    @Test public void noneMatch1() {
        boolean b = Iter.noneMatch(data("1", "2", "3"), x->x.equals("A"));
        assertTrue(b);
    }

    @Test public void noneMatch2() {
        boolean b = Iter.noneMatch(data("A", "2", "3"), x->x.equals("A"));
        assertFalse(b);
    }

    @Test public void findFirst1() {
        Optional<String> r = Iter.findFirst(data("A", "2", "3"), x->x.equals("A"));
        assertTrue(r.isPresent());
        assertEquals("A", r.get());
    }

    @Test public void findFirst2() {
        Optional<String> r = Iter.findFirst(data("A", "2", "3"), x->x.equals("Z"));
        assertFalse(r.isPresent());
    }

    @Test public void findAny1() {
        Optional<String> r = Iter.findAny(data("A", "2", "3"), x->x.equals("A"));
        assertTrue(r.isPresent());
        assertEquals("A", r.get());
    }

    @Test public void reduce1() {
        Optional<String> r = Iter.reduce(data("A", "2", "3"), String::concat);
        assertEquals(Optional.of("A23"), r);
    }

    @Test public void reduce2() {
        Optional<String> r = Iter.reduce(data("A"), String::concat);
        assertEquals(Optional.of("A"), r);
    }

    @Test public void reduce3() {
        Optional<String> r = Iter.reduce(data(), String::concat);
        assertFalse(r.isPresent());
    }

    @Test public void min1() {
        Optional<String> x = Iter.min(data(), String::compareTo);
        assertFalse(x.isPresent());
    }

    @Test public void min2() {
        Optional<String> x = Iter.min(data("2"), String::compareTo);
        assertTrue(x.isPresent());
        assertEquals("2", x.get());
    }

    @Test public void min3() {
        Optional<String> x = Iter.min(data("1", "2", "3"), String::compareTo);
        assertTrue(x.isPresent());
        assertEquals("1", x.get());
    }

    @Test public void min4() {
        Optional<String> x = Iter.min(data("3", "1", "2"), String::compareTo);
        assertTrue(x.isPresent());
        assertEquals("1", x.get());
    }

    @Test public void max1() {
        Optional<String> x = Iter.max(data(), String::compareTo);
        assertFalse(x.isPresent());
    }

    @Test public void max2() {
        Optional<String> x = Iter.max(data("2"), String::compareTo);
        assertTrue(x.isPresent());
        assertEquals("2", x.get());
    }

    @Test public void max3() {
        Optional<String> x = Iter.max(data("1", "2", "3"), String::compareTo);
        assertTrue(x.isPresent());
        assertEquals("3", x.get());
    }

    @Test public void max4() {
        Optional<String> x = Iter.max(data("3", "1", "2"), String::compareTo);
        assertTrue(x.isPresent());
        assertEquals("3", x.get());
    }

    @Test public void collect3() {
        List<String> x = Iter.collect(data("A", "B", "C"), Collectors.toList());
        assertEquals(3, x.size());
        assertEquals(Arrays.asList("A", "B", "C"), x);
    }

    @Test public void collect1() {
        List<String> x = Iter.collect(data("A", "B", "C"), ArrayList::new, ArrayList::add);
        assertEquals(Arrays.asList("A", "B", "C"), x);
    }

    @Test
    public void take_01() {
        List<String> data = Arrays.asList("1", "A", "B", "CC");
        List<String> data2 = Iter.take(data.iterator(), 2);
        assertEquals(2, data2.size());
        assertEquals("1", data2.get(0));
        assertEquals("A", data2.get(1));
    }

    @Test
    public void forEach_1() {
        List<String> data = Arrays.asList("1", "A", "B", "CC");
        AtomicInteger counter = new AtomicInteger(0);
        Iter.forEach(data.iterator(), x->counter.incrementAndGet());
        assertEquals(4, counter.get());
    }

    @Test
    public void forEach_2() {
        List<String> data = Collections.emptyList();
        AtomicInteger counter = new AtomicInteger(0);
        Iter.forEach(data.iterator(), x->counter.incrementAndGet());
        assertEquals(0, counter.get());
    }

    @Test
    public void take_02() {
        List<String> data = Arrays.asList("1", "A", "B", "CC");
        List<String> data2 = Iter.take(data.iterator(), 0);
        assertEquals(0, data2.size());
    }

    @Test
    public void take_03() {
        List<String> data = Arrays.asList("1", "A", "B", "CC");
        List<String> data2 = Iter.take(data.iterator(), 10);
        assertEquals(4, data2.size());
        assertEquals("1", data2.get(0));
        assertEquals("A", data2.get(1));
        assertEquals("B", data2.get(2));
        assertEquals("CC", data2.get(3));
    }

    @Test
    public void take_04() {
        List<String> data = Arrays.asList("a", "b", "b", "c", "c", "d");
        Iterator<String> iter = Iter.takeWhile(data.iterator(), item -> !item.equals("c"));
        List<String> x = Iter.toList(iter);
        List<String> expected = Arrays.asList("a", "b", "b");
        assertEquals(expected, x);
    }

    @Test
    public void take_05() {
        List<String> data = Arrays.asList("a", "b", "b", "c", "c", "d");
        Iterator<String> iter = Iter.takeUntil(data.iterator(), item -> item.equals("c"));
        List<String> x = Iter.toList(iter);
        List<String> expected = Arrays.asList("a", "b", "b");
        assertEquals(expected, x);
    }

    @Test
    public void take_06() {
        List<String> data = Arrays.asList("a", "b", "b", "c", "c", "d");
        Iterator<String> iter = Iter.takeWhile(data.iterator(), item -> true);
        List<String> x = Iter.toList(iter);
        assertEquals(data, x);
    }

    @Test
    public void take_07() {
        List<String> data = Arrays.asList("a", "b", "b", "c", "c", "d");
        Iterator<String> iter = Iter.takeWhile(data.iterator(), item -> false);
        assertFalse(iter.hasNext());
    }

    @Test
    public void take_08() {
        List<String> data = Collections.emptyList();
        Iterator<String> iter = Iter.takeWhile(data.iterator(), item -> false);
        assertFalse(iter.hasNext());
    }

    @Test
    public void take_09() {
        List<String> data = Collections.emptyList();
        Iterator<String> iter = Iter.takeWhile(data.iterator(), item -> true);
        assertFalse(iter.hasNext());
    }

    @Test
    public void filter_01() {
        test(Iter.removeNulls(data3.iterator()), "x", "y", "z");
    }

    @Test
    public void filter_02() {
        Iterator<String> it = Iter.filter(data3.iterator(), item -> "x".equals(item) || "z".equals(item));
        test(it, "x", "z");
    }

    @Test
    public void filter_03() {
        Iterator<String> it = Iter.filter(data3.iterator(), item -> null == item || "x".equals(item));
        test(it, null, "x", null, null, null, null);
    }

    @Test
    public void filter_04() {
        Iterator<String> it = Iter.filter(data3.iterator(), item -> "x".equals(item) || "z".equals(item));
        testWithForeachRemaining(it, "x", "z");
    }

    @Test
    public void filter_05() {
        Iterator<String> it = Iter.filter(data3.iterator(), item -> null == item || "x".equals(item));
        testWithForeachRemaining(it, null, "x", null, null, null, null);
    }

    @Test
    public void distinct_01() {
        List<String> x = Arrays.asList("a", "b", "a");
        Iterator<String> iter = Iter.distinct(x.iterator());
        test(iter, "a", "b");
    }

    @Test
    public void distinct_02() {
        List<String> x = Arrays.asList("a", "b", "a");
        Iterator<String> iter = Iter.distinctAdjacent(x.iterator());
        test(iter, "a", "b", "a");
    }

    @Test
    public void distinct_03() {
        List<String> x = Arrays.asList("a", "a", "b", "b", "b", "a", "a");
        Iterator<String> iter = Iter.distinct(x.iterator());
        test(iter, "a", "b");
    }

    @Test
    public void distinct_04() {
        List<String> x = Arrays.asList("a", "a", "b", "b", "b", "a", "a");
        Iterator<String> iter = Iter.distinctAdjacent(x.iterator());
        test(iter, "a", "b", "a");
    }
}
