/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.idx2;

import lib.Tuple;
import org.junit.Test;
import test.BaseTest;

public class TestColumnMap extends BaseTest
{
    @Test public void remap1() 
    {
        ColumnMap x = new ColumnMap("SPO->POS", 2,0,1) ;   // S->2 etc
        
        assertEquals(2, x.mapOrder(0)) ;   
        assertEquals(0, x.mapOrder(1)) ;
        assertEquals(1, x.mapOrder(2)) ;
    }
    
    @Test public void remap2() 
    {
        ColumnMap x = new ColumnMap("SPO->POS", 2,0,1) ;
        assertEquals(1, x.unmapOrder(0)) ;   // The index 1 comes from position 0.
        assertEquals(2, x.unmapOrder(1)) ;
        assertEquals(0, x.unmapOrder(2)) ;
    }

    @Test public void remap3() 
    {
        ColumnMap x = new ColumnMap("POS", 2,0,1) ;
        Tuple<String> tuple = new Tuple<String>("S", "P", "O") ;
        Tuple<String> mapped = x.map(tuple) ;
        Tuple<String> expected = new Tuple<String>("P", "O", "S") ;
        assertEquals(expected, mapped) ;
    }
    
    @Test public void remap4() 
    {
        ColumnMap x = new ColumnMap("POS", 2,0,1) ;
        Tuple<String> tuple = new Tuple<String>("S", "P", "O") ;
        Tuple<String> tuple2 = x.map(tuple) ;
        tuple2 = x.unmap(tuple2) ;
        assertEquals(tuple, tuple2) ;
    }
    
    @Test public void compile1()
    {
        int[] x = ColumnMap.compileMapping("SPO", "POS") ;
        // SPO -> POS so col 0 goes to col 2, col 1 goes to col 0 and col 2 goes to col 1
        int[] expected = { 2,0,1 } ;
        assertArrayEquals(expected, x) ;
    }

    @Test public void compile2()
    {
        int[] x = ColumnMap.compileMapping("SPOG", "GOPS") ;
        int[] expected = { 3, 2, 1, 0 } ;
        assertArrayEquals(expected, x) ;
    }

    @Test public void map1()
    {
        ColumnMap cmap = new ColumnMap("GSPO", "OSPG") ;
        Tuple<String> tuple = new Tuple<String>("G", "S", "P", "O") ;
        Tuple<String> mapped = cmap.map(tuple) ;
        Tuple<String> expected = new Tuple<String>("O", "S", "P", "G") ;
        assertEquals(expected, mapped) ;
        Tuple<String> unmapped = cmap.unmap(mapped) ;
        assertEquals(new Tuple<String>("G", "S", "P", "O"), unmapped) ;
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