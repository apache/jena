/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.* ;
import org.junit.Test;

public class TestAlg
{
    List<String> data0 = new ArrayList<String>() ;
    List<String> data1 = new ArrayList<String>() ;
    {
        data1.add("a") ;
    }
    
    List<String> data2 = new ArrayList<String>() ;
    {
        data2.add("x") ;
        data2.add("y") ;
        data2.add("z") ;
    }

    @Test
    public void append_1()
    {
        Iterator<String> iter = Stream.append(data1, data0).iterator() ;
        test(iter, "a") ;
    }
        
        
    @Test
    public void append_2()
    {
        Iterator<String> iter = Stream.append(data0, data1).iterator() ;
        test(iter, "a") ;
    }
        
    @Test
    public void append_3()
    {
        Iterator<String> iter = Stream.append(data1, data2).iterator() ;
        test(iter, "a", "x", "y", "z") ;
    }

    
    @Test
    public void asString_1() 
    {
        String x = Stream.asString(data0, "") ;
        assertEquals("", x) ;
    }

    @Test
    public void asString_2() 
    {
        String x = Stream.asString(data1, "") ;
        assertEquals("a", x) ;
    }

    @Test
    public void asString_3() 
    {
        String x = Stream.asString(data1, "/") ;
        assertEquals("a", x) ;
    }

    @Test
    public void asString_4() 
    {
        String x = Stream.asString(data2, "/") ;
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
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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