/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.lib;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.RefLong ;


public class TestRefLong extends BaseTest
{
    @Test public void ref_01()
    {
        RefLong ref1 = new RefLong() ;
        assertEquals(0, ref1.value()) ;
        RefLong ref2 = new RefLong() ;
        assertNotSame(ref1, ref2) ;
    }
    
    @Test public void ref_02()
    {
        RefLong ref = new RefLong() ;
        assertEquals(0, ref.value()) ;
        ref.inc() ;
        assertEquals(1, ref.value()) ;
        ref.dec() ;
        assertEquals(0, ref.value()) ;
    }
    
    @Test public void ref_03()
    {
        RefLong ref = new RefLong(99) ;
        assertEquals(99, ref.value()) ;
        long x = ref.incAndGet() ;
        assertEquals(100, x) ;
        assertEquals(100, ref.value()) ;
        x = ref.getAndInc() ;
        assertEquals(100, x) ;
        assertEquals(101, ref.value()) ;
    }
    
    @Test public void ref_04()
    {
        RefLong ref = new RefLong(99) ;
        assertEquals(99, ref.value()) ;
        long x = ref.decAndGet() ;
        assertEquals(98, x) ;
        assertEquals(98, ref.value()) ;
        x = ref.getAndDec() ;
        assertEquals(98, x) ;
        assertEquals(97, ref.value()) ;
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