/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tx;

import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

public class TestIteratorSlotted extends BaseTest
{
    static class IterStr extends IteratorSlotted<String>
    {
        private List<String> array ;
        private Iterator<String> iter ;

        IterStr(String...array)
        { 
            this.array = Arrays.asList(array) ;
            iter = this.array.iterator() ;
        }
        
        @Override
        protected String moveToNext()
        {
            return iter.next() ;
        }

        @Override
        protected boolean hasMore()
        {
            return iter.hasNext() ;
        }
        
    }
    
    @Test public void iter_01()
    {
        IterStr iter = new IterStr() ;
        assertFalse(iter.hasNext()) ;
    }
    
    @Test public void iter_02()
    {
        IterStr iter = new IterStr("A") ;
        assertTrue(iter.hasNext()) ;
        assertEquals("A", iter.peek()) ;
        assertEquals("A", iter.peek()) ;
        assertEquals("A", iter.next()) ;
        assertFalse(iter.hasNext()) ;
        assertNull(iter.peek()) ;
    }
    
    @Test public void iter_03()
    {
        IterStr iter = new IterStr("A", "B") ;
        assertTrue(iter.hasNext()) ;
        assertEquals("A", iter.peek()) ;
        assertEquals("A", iter.next()) ;
        assertEquals("B", iter.peek()) ;
        assertEquals("B", iter.next()) ;
        assertFalse(iter.hasNext()) ;
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