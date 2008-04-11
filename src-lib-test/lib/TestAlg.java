/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package lib;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import lib.Alg;

import org.junit.Test;
import test.BaseTest;

public class TestAlg extends BaseTest
{
    // Linear search is really there to test binary search.
    @Test public void linear1() 
    {
        int[] data = {1, 2, 3} ; 
        IntBuffer b = make(data) ;
        int idx = 0 ; 
        
        idx = Alg.linearSearch(b, 1) ;
        assertEquals(0, idx) ;
        
        idx = Alg.linearSearch(b, 2) ;
        assertEquals(1, idx) ;

        idx = Alg.linearSearch(b, 3) ;
        assertEquals(2, idx) ;
        
    }

    @Test public void linear2() 
    {
        int[] data = {2, 4, 6} ; 
        IntBuffer b = make(data) ;
        int idx = 0 ; 
        
        idx = Alg.linearSearch(b, 1) ;
        assertEquals(-1, idx) ;
        
        idx = Alg.linearSearch(b, 3) ;
        assertEquals(-2, idx) ;

        idx = Alg.linearSearch(b, 5) ;
        assertEquals(-3, idx) ;
        
        idx = Alg.linearSearch(b, 7) ;
        assertEquals(-4, idx) ;
    }

    @Test public void linear3() 
    {
        int[] data = {} ; 
        IntBuffer b = make(data) ;
        int idx = Alg.linearSearch(b, 1) ;
        assertEquals(-1, idx) ;
    }
    
    @Test public void linear4() 
    {
        int[] data = {9} ; 
        IntBuffer b = make(data) ;
        int idx ;
        idx = Alg.linearSearch(b, 1) ;
        assertEquals(-1, idx) ;
        idx = Alg.linearSearch(b, 9) ;
        assertEquals(0, idx) ;
        idx = Alg.linearSearch(b, 100) ;
        assertEquals(-2, idx) ;
    }
    
    @Test public void linear5()
    {
        int[] data = {2, 4, 6, 8, 10} ; 
        IntBuffer b = make(data) ;
        int idx = 0 ;
        idx = Alg.linearSearch(b, 1, 4, 6) ;
        assertEquals(2, idx) ;
        
        idx = Alg.linearSearch(b, 1, 4, 5) ;
        assertEquals(-3, idx) ;
        
        idx = Alg.linearSearch(b, 1, 4, 2) ;
        assertEquals(-2, idx) ;
        
        idx = Alg.linearSearch(b, 1, 4, 10) ;
        assertEquals(-5, idx) ;
    }
    
    @Test public void linear6()
    {
        int[] data = {2, 4, 6, 8, 10} ; 
        IntBuffer b = make(data) ;
        int idx = 0 ;
        idx = Alg.linearSearch(b, 3, 3, 6) ;
        assertEquals(-4, idx) ;
        idx = Alg.linearSearch(b, 3, 3, 5) ;
        assertEquals(-4, idx) ;
        idx = Alg.linearSearch(b, 3, 3, 50) ;
        assertEquals(-4, idx) ;
    }
    
    @Test public void linear7()
    {
        int[] data = {2, 4, 4, 8, 8} ; 
        IntBuffer b = make(data) ;
        int idx = 0 ;
        idx = Alg.linearSearch(b, 4) ;
        assertEquals(1, idx) ;
        idx = Alg.linearSearch(b, 8) ;
        assertEquals(3, idx) ;
    }
    
    @Test public void binary1() 
    {
        int[] data = {1, 2, 3} ; 
        IntBuffer b = make(data) ;
        search(b, 1) ; 
        search(b, 2) ;
        search(b, 3) ;
    }
    
    @Test public void binary2() 
    {
        int[] data = {2, 4, 6} ; 
        IntBuffer b = make(data) ;
        search(b, 1) ;
        search(b, 3) ;
        search(b, 5) ;
        search(b, 7) ;
    }
    
    @Test public void binary3() 
    {
        int[] data = {} ; 
        IntBuffer b = make(data) ;
        search(b, 1) ;
    }
    
    @Test public void binary4() 
    {
        int[] data = {9} ; 
        IntBuffer b = make(data) ;
        search(b, 1) ;
        search(b, 9) ;
        search(b, 100) ;
    }
    
    @Test public void binary5()
    {
        int[] data = {2, 4, 6, 8, 10} ; 
        IntBuffer b = make(data) ;
        search(b, 6, 1, 4) ;
        search(b, 5, 1, 4) ;
        search(b, 2, 1, 4) ;
        search(b, 10, 1, 4) ;
    }

    @Test public void binary6()
    {
        int[] data = {2, 4, 6, 8, 10} ; 
        IntBuffer b = make(data) ;
        search(b, 6, 3, 3) ;
        search(b, 5, 3, 3) ;
        search(b, 50, 3, 3) ;
    }

    // Binary serach does not state which index is returned for sequenences of same value
    @Test public void binary7()
    {
        int[] data = {2, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 8, 8} ; 
        IntBuffer b = make(data) ;
        int idx = 0 ;
        idx = Alg.binarySearch(b, 4) ;
        assertEquals(4, data[idx]) ;
        idx = Alg.linearSearch(b, 8) ;
        assertEquals(8, data[idx]) ;
        
        search(b, 3) ;
        search(b, 5) ;
        search(b, 9) ;
    }
    
    private static IntBuffer make(int[] data) 
    {
        //IntBuffer x = IntBuffer.allocate(data.length) ;
        ByteBuffer z = ByteBuffer.allocate(4*data.length) ;
        z.order(ByteOrder.BIG_ENDIAN) ;
        IntBuffer x = z.asIntBuffer() ;
        
        for ( int i = 0 ; i < data.length ; i++ )
            x.put(i,data[i]) ;
        return x ;
    }

    private static void search(IntBuffer b, int k)
    {
        int idx1 = Alg.linearSearch(b, k) ;
        int idx2 = Alg.binarySearch(b, k) ;
        assertEquals(idx1, idx2) ;
    }
    
    private static void search(IntBuffer b, int k, int low, int high)
    {
        int idx1 = Alg.linearSearch(b, low, high, k) ;
        int idx2 = Alg.binarySearch(b, low, high, k) ;
        assertEquals(idx1, idx2) ;
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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