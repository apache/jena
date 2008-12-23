/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;
import test.BaseTest;

public class TestIteratorArray extends BaseTest
{
    @Test public void arrayIterator_1()
    {
        String[] a = { } ;
        Iterator<String> iter = IteratorArray.create(a) ;
        assertFalse(iter.hasNext()) ;
        assertFalse(iter.hasNext()) ;
    }

    @Test public void arrayIterator_2()
    {
        String[] a = { "a" } ;
        Iterator<String> iter = IteratorArray.create(a) ;
        assertTrue(iter.hasNext()) ;
        assertEquals("a", iter.next()) ;
        assertFalse(iter.hasNext()) ;
        assertFalse(iter.hasNext()) ;
    }

    
    @Test public void arrayIterator_3()
    {
        String[] a = { "a", "b", "c"} ;
        Iterator<String> iter = IteratorArray.create(a) ;
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
        String[] a = { "a" } ;
        Iterator<String> iter = IteratorArray.create(a) ;
        assertEquals("a", iter.next()) ;
        try { iter.next() ; fail("Expected NoSuchElementException") ; }
        catch (NoSuchElementException ex) {}
    }
    
    @Test public void arrayIterator_5()
    {
        String[] a = { "a", "b", "c" } ;
        Iterator<String> iter = IteratorArray.create(a,0,1) ;
        assertEquals("a", iter.next()) ;
        assertFalse(iter.hasNext()) ;
    }
    
    @Test public void arrayIterator_6()
    {
        String[] a = { "a", "b", "c", "d" } ;
        Iterator<String> iter = IteratorArray.create(a,1,3) ;
        assertEquals("b", iter.next()) ;
        assertEquals("c", iter.next()) ;
        assertFalse(iter.hasNext()) ;
    }
    
    @Test public void arrayIterator_7()
    {
        String[] a = { "a", "b", "c", "d" } ;
        IteratorArray<String> iter = IteratorArray.create(a,1,3) ;
        assertEquals("b", iter.current()) ;
        assertEquals("b", iter.current()) ;
        assertEquals("b", iter.next()) ;
        assertEquals("c", iter.current()) ;
        assertEquals("c", iter.next()) ;
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