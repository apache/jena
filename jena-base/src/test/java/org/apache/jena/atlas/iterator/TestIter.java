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
import java.util.function.Predicate;

import org.junit.Test ;

public class TestIter
{
    List<String> data0 = new ArrayList<>() ;
    List<String> data1 = Arrays.asList("a") ;
    List<String> data2 = Arrays.asList("x","y","z") ;
    List<String> data3 = Arrays.asList(null, "x", null, null, null, "y", "z", null);
 
    @Test
    public void append_1()
    {
        Iterator<String> iter = Iter.append(data1.iterator(), data0.iterator()) ;
        test(iter, "a") ;
    }
        
        
    @Test
    public void append_2()
    {
        Iterator<String> iter = Iter.append(data0.iterator(), data1.iterator()) ;
        test(iter, "a") ;
    }
        
    @Test
    public void append_3()
    {
        Iterator<String> iter = Iter.append(data1.iterator(), data2.iterator()) ;
        test(iter, "a", "x", "y", "z") ;
    }

    @Test
    public void append_4()
    {
        List<String> L = new ArrayList<>(3);
        L.add("a");
        L.add("b");
        L.add("c");
        List<String> R = new ArrayList<>(3);
        R.add("d");
        R.add("e");
        R.add("f");
        
        
        Iterator<String> LR = Iter.append(L.iterator(), R.iterator()) ;
        
        while (LR.hasNext())
        {
            String s = LR.next();
            
            if ("c".equals(s))
            {
                LR.hasNext();  // test for JENA-60
                LR.remove();
            }
        }
        
        assertEquals("ab", Iter.asString(L, ""));
        assertEquals("def", Iter.asString(R, ""));
    }
    
    @Test
    public void append_5()
    {
        List<String> L = new ArrayList<>(3);
        L.add("a");
        L.add("b");
        L.add("c");
        List<String> R = new ArrayList<>(3);
        R.add("d");
        R.add("e");
        R.add("f");
        
        
        Iterator<String> LR = Iter.append(L.iterator(), R.iterator()) ;
        
        while (LR.hasNext())
        {
            String s = LR.next();
            
            if ("d".equals(s))
            {
                LR.hasNext();  // test for JENA-60
                LR.remove();
            }
        }
        
        assertEquals("abc", Iter.asString(L, ""));
        assertEquals("ef", Iter.asString(R, ""));
    }
    
    @Test
    public void append_6()
    {
        List<String> L = new ArrayList<>(3);
        L.add("a");
        L.add("b");
        L.add("c");
        List<String> R = new ArrayList<>(3);
        R.add("d");
        R.add("e");
        R.add("f");
        
        
        Iterator<String> LR = Iter.append(L.iterator(), R.iterator()) ;
        
        while (LR.hasNext())
        {
            LR.next() ;
        }
        LR.remove() ;
        
        assertEquals("abc", Iter.asString(L, "")) ;
        assertEquals("de", Iter.asString(R, "")) ;
    }
    
    @Test
    public void asString_1() 
    {
        String x = Iter.asString(data0, "") ;
        assertEquals("", x) ;
    }

    @Test
    public void asString_2() 
    {
        String x = Iter.asString(data1, "") ;
        assertEquals("a", x) ;
    }

    @Test
    public void asString_3() 
    {
        String x = Iter.asString(data1, "/") ;
        assertEquals("a", x) ;
    }

    @Test
    public void asString_4() 
    {
        String x = Iter.asString(data2, "/") ;
        assertEquals("x/y/z", x) ;
    }
    
    private void test(Iterator<?> iter, Object... items)
    {
        for ( Object x : items )
        {
            assertTrue(iter.hasNext()) ;
            assertEquals(x, iter.next()) ;
        }
        assertFalse(iter.hasNext()) ;
    }
    
    static Iter.Folder<String, String> f1 = new Iter.Folder<String, String>() {
                                                 @Override
                                                public String eval(String acc, String arg)
                                                 {
                                                     return acc + arg ;
                                                 }
                                             } ;
    
    @Test
    public void fold_01() 
    {
        String[] x = { "a", "b", "c" } ;
        String z = Iter.foldLeft(Arrays.asList(x), f1, "X") ;
        assertEquals("Xabc", z) ;
    }
    
    @Test
    public void fold_02() 
    {
        String[] x = { "a", "b", "c" } ;
        String z = Iter.foldRight(Arrays.asList(x), f1, "X") ;
        assertEquals("Xcba", z) ;
    }
    
    @Test
    public void fold_03() 
    {
        String[] x = {  } ;
        String z = Iter.foldLeft(Arrays.asList(x), f1, "X") ;
        assertEquals("X", z) ;
    }
    
    @Test
    public void fold_04() 
    {
        String[] x = { } ;
        String z = Iter.foldRight(Arrays.asList(x), f1, "X") ;
        assertEquals("X", z) ;
    }

    
    @Test
    public void map_01()
    {
        Iterator<String> it = Iter.map(data2.iterator(), item -> item + item);
        test(it, "xx", "yy", "zz");
    }
	
    Predicate<String> filter = item -> item.length() == 1;
   
    @Test
    public void first_01()
    {
        Iter<String> iter = Iter.nullIter() ;
        assertEquals(null, Iter.first(iter, filter)) ;
    }

    @SuppressWarnings("deprecation")
    @Test
    public void first_02()
    {
        List<String> data = Arrays.asList( "11", "A", "B", "C") ;
        assertEquals("A", Iter.first(data, filter)) ;
    }

    @SuppressWarnings("deprecation")
    @Test
    public void first_03()
    {
        List<String> data = Arrays.asList( "11", "AA", "BB", "CC") ;
        assertEquals(null, Iter.first(data, filter)) ;
    }
 
    @Test
    public void first_04()
    {
        Iter<String> iter = Iter.nullIter() ;
        assertEquals(-1, Iter.firstIndex(iter, filter)) ;
    }

    @SuppressWarnings("deprecation")
    @Test
    public void first_05()
    {
        List<String> data = Arrays.asList( "11", "A", "B", "C") ;
        assertEquals(1, Iter.firstIndex(data, filter)) ;
    }

    @SuppressWarnings("deprecation")
    @Test
    public void first_06()
    {
        List<String> data = Arrays.asList( "11", "AA", "BB", "CC") ;
        assertEquals(-1, Iter.firstIndex(data, filter)) ;
    }

    @Test
    public void last_01()
    {
        Iter<String> iter = Iter.nullIter() ;
        assertEquals(null, Iter.last(iter, filter)) ;
    }

    @SuppressWarnings("deprecation")
    @Test
    public void last_02()
    {
        List<String> data = Arrays.asList( "11", "A", "B", "C") ;
        assertEquals("C", Iter.last(data, filter)) ;
    }

    @SuppressWarnings("deprecation")
    @Test
    public void last_03()
    {
        List<String> data = Arrays.asList( "11", "AA", "BB", "CC") ;
        assertEquals(null, Iter.last(data, filter)) ;
    }
 
    @Test
    public void last_04()
    {
        Iter<String> iter = Iter.nullIter() ;
        assertEquals(-1, Iter.lastIndex(iter, filter)) ;
    }

    @SuppressWarnings("deprecation")
    @Test
    public void last_05()
    {
        List<String> data = Arrays.asList( "11", "A", "B", "C") ;
        assertEquals(3, Iter.lastIndex(data, filter)) ;
    }

    @SuppressWarnings("deprecation")
    @Test
    public void last_06()
    {
        List<String> data = Arrays.asList( "11", "AA", "BB", "CC") ;
        assertEquals(-1, Iter.lastIndex(data, filter)) ;
    }
    
    @Test
    public void take_01()
    {
        List<String> data = Arrays.asList( "1", "A", "B", "CC") ;
        List<String> data2 = Iter.take(data.iterator(), 2) ;
        assertEquals(2, data2.size()) ;
        assertEquals("1", data2.get(0)) ;
        assertEquals("A", data2.get(1)) ;
    }
    
    @Test
    public void take_02()
    {
        List<String> data = Arrays.asList( "1", "A", "B", "CC") ;
        List<String> data2 = Iter.take(data.iterator(), 0) ;
        assertEquals(0, data2.size()) ;
    }

    @Test
    public void take_03()
    {
        List<String> data = Arrays.asList( "1", "A", "B", "CC") ;
        List<String> data2 = Iter.take(data.iterator(), 10) ;
        assertEquals(4, data2.size()) ;
        assertEquals("1", data2.get(0)) ;
        assertEquals("A", data2.get(1)) ;
        assertEquals("B", data2.get(2)) ;
        assertEquals("CC", data2.get(3)) ;
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
    public void filter_01()
    {
        test(Iter.removeNulls(data3.iterator()), "x", "y", "z");
    }
    
    @Test
    public void filter_02()
    {
        Iterator<String> it = Iter.filter(data3.iterator(), item -> "x".equals(item) || "z".equals(item));
        
        test(it, "x", "z");
    }
    
    @Test
    public void filter_03()
    {
        Iterator<String> it = Iter.filter(data3.iterator(), item -> null == item || "x".equals(item));
        
        test(it, null, "x", null, null, null, null);
    }
    
    @Test public void distinct_01() 
    {
        List<String> x = Arrays.asList("a", "b", "a") ;
        Iterator<String> iter = Iter.distinct(x.iterator()) ;
        test(iter, "a", "b") ;
    }
    
    @Test public void distinct_02() 
    {
        List<String> x = Arrays.asList("a", "b", "a") ;
        Iterator<String> iter = Iter.distinctAdjacent(x.iterator()) ;
        test(iter, "a", "b", "a") ;
    }
    
    @Test public void distinct_03() 
    {
        List<String> x = Arrays.asList("a", "a", "b", "b", "b", "a", "a") ;
        Iterator<String> iter = Iter.distinct(x.iterator()) ;
        test(iter, "a", "b") ;
    }
    
    @Test public void distinct_04() 
    {
        List<String> x = Arrays.asList("a", "a", "b", "b", "b", "a", "a") ;
        Iterator<String> iter = Iter.distinctAdjacent(x.iterator()) ;
        test(iter, "a", "b", "a") ;
    }
    
    private static class AlwaysAcceptFilterStack extends FilterStack<Object> {
        public AlwaysAcceptFilterStack(Predicate<Object> f) {
            super(f);
        }

        @Override
        public boolean acceptAdditional(Object o) {
            return true;
        }
    }

    @Test
    public void testFilterStack_01() {
        Predicate<Object> filter = x -> true;
        FilterStack<Object> filterStack = new AlwaysAcceptFilterStack(filter);
        assertTrue(filterStack.test(new Object()));
    }
    
    @Test
    public void testFilterStack_02() {
        Predicate<Object> filter = x -> false;
        FilterStack<Object> filterStack = new AlwaysAcceptFilterStack(filter);
        assertFalse(filterStack.test(new Object()));
    }

}
