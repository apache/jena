/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.iterator;

import java.util.ArrayList ;
import java.util.List ;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

public class TestIteratorPushback extends BaseTest
{

    static List<String> data = new ArrayList<String>() ;
    static {
        data.add("a") ;
        data.add("b") ;
        data.add("c") ;
    }

    @Test(expected=IllegalArgumentException.class)
    public void pushback01() { new PushbackIterator<String>(null) ; }
    
    @Test public void pushback02()
    { 
        PushbackIterator<String> iter = new PushbackIterator<String>(data.iterator()) ;
        assertEquals("a", iter.next()) ;
        assertEquals("b", iter.next()) ;
        assertEquals("c", iter.next()) ;
        assertFalse(iter.hasNext()) ;
    }
    
    @Test public void pushback03()
    { 
        PushbackIterator<String> iter = new PushbackIterator<String>(data.iterator()) ;
        iter.pushback("x") ;
        assertEquals("x", iter.next()) ;
        assertEquals("a", iter.next()) ;
        assertEquals(2, Iter.count(iter)) ;
    }
    
    @Test public void pushback04()
    { 
        PushbackIterator<String> iter = new PushbackIterator<String>(data.iterator()) ;
        assertEquals("a", iter.next()) ;
        iter.pushback("x") ;
        assertEquals("x", iter.next()) ;
        assertEquals("b", iter.next()) ;
        assertEquals(1, Iter.count(iter)) ;
    }
    
    @Test public void pushback05()
    { 
        PushbackIterator<String> iter = new PushbackIterator<String>(data.iterator()) ;
        assertEquals("a", iter.next()) ;
        iter.pushback("x") ;
        iter.pushback("y") ;
        assertEquals("y", iter.next()) ;
        assertEquals("x", iter.next()) ;
        assertEquals("b", iter.next()) ;
        assertEquals(1, Iter.count(iter)) ;
    }
    
    @Test public void pushback06()
    { 
        PushbackIterator<String> iter = new PushbackIterator<String>(data.iterator()) ;
        assertEquals(3, Iter.count(iter)) ;
        iter.pushback("x") ;
        iter.pushback("y") ;
        assertEquals("y", iter.next()) ;
        assertEquals("x", iter.next()) ;
        assertFalse(iter.hasNext()) ;
    }

}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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