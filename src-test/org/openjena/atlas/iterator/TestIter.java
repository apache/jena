/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.iterator;

import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertFalse ;
import static org.junit.Assert.assertTrue ;

import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.junit.Test ;

public class TestIter
{
    List<String> data0 = new ArrayList<String>() ;
    List<String> data1 = Arrays.asList("a") ;
    List<String> data2 = Arrays.asList("x","y","z") ;
    List<String> data3 = Arrays.asList(null, "x", null, null, null, "y", "z", null);
 
    @Test
    public void append_1()
    {
        Iterator<String> iter = Iter.append(data1, data0) ;
        test(iter, "a") ;
    }
        
        
    @Test
    public void append_2()
    {
        Iterator<String> iter = Iter.append(data0, data1) ;
        test(iter, "a") ;
    }
        
    @Test
    public void append_3()
    {
        Iterator<String> iter = Iter.append(data1, data2) ;
        test(iter, "a", "x", "y", "z") ;
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
    
    Filter<String> filter = new Filter<String>() {
        public boolean accept(String item)
        {
            return item.length() == 1 ;
        }} ;
   
    @Test
    public void first_01()
    {
        Iter<String> iter = Iter.nullIter() ;
        assertEquals(null, Iter.first(iter, filter)) ;
    }

    @Test
    public void first_02()
    {
        List<String> data = Arrays.asList( "11", "A", "B", "C") ;
        assertEquals("A", Iter.first(data, filter)) ;
    }

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

    @Test
    public void first_05()
    {
        List<String> data = Arrays.asList( "11", "A", "B", "C") ;
        assertEquals(1, Iter.firstIndex(data, filter)) ;
    }

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

    @Test
    public void last_02()
    {
        List<String> data = Arrays.asList( "11", "A", "B", "C") ;
        assertEquals("C", Iter.last(data, filter)) ;
    }

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

    @Test
    public void last_05()
    {
        List<String> data = Arrays.asList( "11", "A", "B", "C") ;
        assertEquals(3, Iter.lastIndex(data, filter)) ;
    }

    @Test
    public void last_06()
    {
        List<String> data = Arrays.asList( "11", "AA", "BB", "CC") ;
        assertEquals(-1, Iter.firstIndex(data, filter)) ;
    }
    
    @Test
    public void filter_01()
    {
        test(Iter.removeNulls(data3), "x", "y", "z");
    }
    
    @Test
    public void filter_02()
    {
        Iterator<String> it = Iter.filter(data3, new Filter<String>()
        {
            public boolean accept(String item)
            {
                return "x".equals(item) || "z".equals(item) ;
            }
        });
        
        test(it, "x", "z");
    }
    
    @Test
    public void filter_03()
    {
        Iterator<String> it = Iter.filter(data3, new Filter<String>()
        {
            public boolean accept(String item)
            {
                return (null == item) || "x".equals(item) ;
            }
        });
        
        test(it, null, "x", null, null, null, null);
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */