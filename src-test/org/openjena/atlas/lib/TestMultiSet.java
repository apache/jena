/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

import java.util.Arrays ;
import java.util.Collections ;
import java.util.List ;

import org.junit.Test ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.MultiSet ;


public class TestMultiSet extends BaseTest
{
    @Test public void multiSet_01()
    {
        MultiSet<String> x = new MultiSet<String>() ;
        assertTrue(x.isEmpty()) ;
        assertEquals(0, x.count("A")) ;
    }

    @Test public void multiSet_02()
    {
        MultiSet<String> x = new MultiSet<String>() ;
        x.add("A") ;
        assertFalse(x.isEmpty()) ;
        assertEquals(1, x.count("A") ) ;
        x.add("A") ;
        assertEquals(2, x.count("A") ) ;
    }

    @Test public void multiSet_03()
    {
        MultiSet<String> x = new MultiSet<String>() ;
        x.add("A") ;
        x.add("A") ;
        x.remove("A") ;
        assertEquals(1, x.count("A") ) ;
        assertTrue(x.contains("A")) ;
        x.remove("A") ;
        assertEquals(0, x.count("A") ) ;
        assertFalse(x.contains("A")) ;
    }

    @Test public void multiSet_04()
    {
        String[] data = { } ;
        iterTest(data) ;
    }


    @Test public void multiSet_05()
    {
        String[] data = { "A" } ;
        iterTest(data) ;
    }

    @Test public void multiSet_06()
    {
        String[] data = { "A", "B", "C" } ;
        iterTest(data) ;
    }


    @Test public void multiSet_07()
    {
        String[] data = { "A", "B", "C", "A" } ;
        iterTest(data) ;
    }

    
    private static void iterTest(String[] data)
    {
        List<String> expected = Arrays.asList(data) ;
        MultiSet<String> x = new MultiSet<String>() ;
        for ( String str : data )
            x.add(str) ;
        List<String> actual = Iter.toList(x.iterator()) ;
        Collections.sort(expected) ;
        Collections.sort(actual) ;
        assertEquals(expected, actual) ;
    }
    
}

/*
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