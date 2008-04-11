/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.btree;

import java.nio.ByteBuffer;

import com.hp.hpl.jena.tdb.base.BaseConfig;
import com.hp.hpl.jena.tdb.btree.PtrBuffer;


import org.junit.BeforeClass;
import org.junit.Test;
import test.BaseTest;

public class TestPtrBuffer extends BaseTest
{
    @BeforeClass static public void before()
    {
        BaseConfig.NullOut = true ;    
    }
    
    // Testing the test framework!
    @Test public void ptrbuffer01()
    {
        PtrBuffer pb = make(4) ;
        contains(pb, 2, 4, 6, 8) ;
    }
    
    // No BinarySearch test
    
    // Shift at LHS
    @Test public void ptrbuffer03()
    {
        PtrBuffer pb = make(4,5) ;
        contains(pb, 2, 4, 6, 8) ;
        
        pb.shiftUp(0) ;
        pb.set(0, 99) ;
        contains(pb, 99, 2, 4, 6, 8) ;
        
        pb.shiftDown(0) ;
        contains(pb, 2, 4, 6, 8) ;
    }    
    
    @Test public void ptrbuffer04()
    {
        PtrBuffer pb = make(4,5) ;
        contains(pb, 2, 4, 6, 8) ;
        pb.shiftDown(0) ;
        
        contains(pb, 4, 6, 8) ;
        pb.shiftUp(0) ;

        pb.set(0,1) ;
        contains(pb, 1, 4, 6, 8) ;
    }    
    

    // Shift at middle
    @Test public void ptrbuffer05()
    {
        PtrBuffer pb = make(4,5) ;
        contains(pb, 2, 4, 6, 8) ;
        pb.shiftUp(2) ;
        pb.set(2, 0) ;
        contains(pb, 2, 4, 0, 6, 8) ;
        pb.shiftDown(2) ;
        contains(pb, 2, 4, 6, 8) ;
    }    

    @Test public void ptrbuffer06()
    {
        PtrBuffer pb = make(4,5) ;
        contains(pb, 2, 4, 6, 8) ;
        pb.shiftDown(2) ;
        contains(pb, 2, 4, 8) ;
        pb.shiftUp(2) ;
        assertTrue(pb.isClear(2)) ;
        contains(pb, 2, 4, -1, 8) ;
    }    

    // Shift RHS - out of bounds
    @Test public void ptrbuffer07()
    {
        PtrBuffer pb = make(4,5) ;
        contains(pb, 2, 4, 6, 8) ;
        pb.shiftUp(3) ;
        pb.set(3, 1) ;
        contains(pb, 2, 4, 6, 1, 8) ;
        pb.shiftDown(3) ;
        contains(pb, 2, 4, 6, 8) ;
    }    

    @Test public void ptrbuffer08()
    {
        PtrBuffer pb = make(4,5) ;
        contains(pb, 2, 4, 6, 8) ;
        pb.shiftDown(3) ;
        contains(pb, 2, 4, 6) ;
        pb.shiftUp(2) ;
        contains(pb, 2, 4, -1, 6) ;
    }    

    // Errors - IllegalArgumentException
    @Test(expected=IllegalArgumentException.class) 
    public void ptrbuffer09()
    {
        PtrBuffer pb = make(4,5) ;
        contains(pb, 2, 4, 6, 8) ;
        pb.shiftDown(4) ;
    }  
    
    @Test(expected=IllegalArgumentException.class) 
    public void ptrbuffer10()
    {
        PtrBuffer pb = make(4,5) ;
        contains(pb, 2, 4, 6, 8) ;
        pb.shiftUp(4) ;
    }  

    @Test(expected=IllegalArgumentException.class) 
    public void ptrbuffer11()
    {
        PtrBuffer pb = make(5,5) ;
        contains(pb, 2, 4, 6, 8, 10) ;
        pb.add(12) ;
    }  
    
    // Copy, duplicate, clear
    @Test public void ptrbuffer12()
    {
        PtrBuffer pb = make(5,5) ;
        contains(pb, 2, 4, 6, 8, 10) ;
        PtrBuffer pb2 = pb.duplicate() ;
        pb2.set(1, 99) ;
        contains(pb, 2, 4, 6, 8, 10) ;
        contains(pb2, 2, 99, 6, 8, 10) ;
    }
    
    @Test public void ptrbuffer13()
    {
        PtrBuffer pb = make(5,5) ;
        contains(pb, 2, 4, 6, 8, 10) ;
        pb.clear(1, 3) ;
        contains(pb, 2, -1, -1, -1, 10) ;
    }
    
    @Test public void ptrbuffer14()
    {
        PtrBuffer pb = make(5,5) ;
        contains(pb, 2, 4, 6, 8, 10) ;
        
        PtrBuffer pb2 = make(5,5) ;
        contains(pb2, 2, 4, 6, 8, 10) ;
        
        pb.copy(0, pb2, 1, 4) ;
        contains(pb2, 2, 2, 4, 6, 8) ;
    }

    // Remove tests

    @Test public void ptrbuffer15()
    {
        PtrBuffer pb = make(5,5) ;
        contains(pb, 2, 4, 6, 8, 10) ;
        pb.removeTop() ;
        contains(pb, 2, 4, 6, 8) ;
        pb.remove(1) ;
        contains(pb, 2, 6, 8) ;
        pb.remove(2) ;
        contains(pb, 2, 6) ;
        pb.remove(0) ;
        contains(pb, 6) ;
        pb.remove(0) ;
        contains(pb) ;
    }
    
    // ---- Support
    private static void contains(PtrBuffer pb, int... vals)
    {
        assertEquals("Length mismatch: ", vals.length, pb.size()) ;
        for ( int i = 0 ; i < vals.length ; i++ )
            if ( vals[i] == -1 )
                assertTrue(pb.isClear(i))  ;
            else
                assertEquals("Value mismatch: ", vals[i], pb.get(i)) ;
    }

    
    // 2,4,6,8 who do we ...
     
    private static PtrBuffer make(int n) { return make(n,n) ; }
    private static PtrBuffer make(int n, int len)
    { 
        ByteBuffer bb = ByteBuffer.allocate(4*len) ;
        PtrBuffer pb = new PtrBuffer(bb, 0) ;
        for ( int i = 0 ; i < n ; i++ )
            pb.add(2+2*i) ;
        return pb ;
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