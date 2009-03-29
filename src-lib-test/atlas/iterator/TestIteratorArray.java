/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package atlas.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import atlas.iterator.IteratorArray;
import atlas.test.BaseTest;

import org.junit.Test;

public class TestIteratorArray extends BaseTest
{
    IteratorArray<String> create(String ... a)
    {
        return IteratorArray.create(a) ;
    }
    
    IteratorArray<String> create(int start, int finish, String ... a)
    {
        return IteratorArray.create(a, start, finish) ;
    }
    
    @Test public void arrayIterator_1()
    {
        Iterator<String> iter = create() ;
        assertFalse(iter.hasNext()) ;
        assertFalse(iter.hasNext()) ;
    }

    @Test public void arrayIterator_2()
    {
        Iterator<String> iter = create("a") ;
        assertTrue(iter.hasNext()) ;
        assertEquals("a", iter.next()) ;
        assertFalse(iter.hasNext()) ;
        assertFalse(iter.hasNext()) ;
    }

    
    @Test public void arrayIterator_3()
    {
        Iterator<String> iter = create("a", "b", "c") ;
        assertTrue(iter.hasNext()) ;
        assertEquals("a", iter.next()) ;
        assertTrue(iter.hasNext()) ;
        assertEquals("b", iter.next()) ;
        assertTrue(iter.hasNext()) ;
        assertEquals("c", iter.next()) ;
        assertFalse(iter.hasNext()) ;
        assertFalse(iter.hasNext()) ;
    }
    
    @Test public void arrayIterator_4()
    {
        Iterator<String> iter = create("a") ;
        assertEquals("a", iter.next()) ;
        try { iter.next() ; fail("Expected NoSuchElementException") ; }
        catch (NoSuchElementException ex) {}
    }
    
    @Test public void arrayIterator_5()
    {
        Iterator<String> iter = create(0,1, "a", "b", "c") ;
        assertEquals("a", iter.next()) ;
        assertFalse(iter.hasNext()) ;
    }
    
    @Test public void arrayIterator_6()
    {
        Iterator<String> iter = create(1, 3, "a", "b", "c", "d") ;
        assertEquals("b", iter.next()) ;
        assertEquals("c", iter.next()) ;
        assertFalse(iter.hasNext()) ;
    }
    
    @Test public void arrayIterator_7()
    {
        IteratorArray<String> iter = create(1, 3, "a", "b", "c", "d") ;
        assertEquals("b", iter.current()) ;
        assertEquals("b", iter.current()) ;
        assertEquals("b", iter.next()) ;
        assertEquals("c", iter.current()) ;
        assertEquals("c", iter.next()) ;
        assertFalse(iter.hasNext()) ;
    }

}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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