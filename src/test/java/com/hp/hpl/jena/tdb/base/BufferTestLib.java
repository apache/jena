/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base;

import java.nio.ByteBuffer ;

import com.hp.hpl.jena.tdb.base.block.Block ;

public class BufferTestLib
{
    public static boolean sameValue(Block block1, Block block2)
    {
        if ( block1.getId() != block2.getId()) return false ;
        ByteBuffer bb1 = block1.getByteBuffer() ; 
        ByteBuffer bb2 = block2.getByteBuffer() ;
        
        if ( bb1.capacity() != bb2.capacity() ) return false ;
        
        for ( int i = 0 ; i < bb1.capacity() ; i++ )
            if ( bb1.get(i) != bb2.get(i) ) return false ;
        return true ;
    }
    
    public static boolean sameValue(ByteBuffer bb1, ByteBuffer bb2)
    {
        if ( bb1.capacity() != bb2.capacity() ) return false ;
        
        int posn1 = bb1.position();
        int limit1 = bb1.limit();
        int posn2 = bb2.position();
        int limit2 = bb2.limit();
        
        bb1.clear() ;
        bb2.clear() ;
        
        try {
            for ( int i = 0 ; i < bb1.capacity() ; i++ )
                if ( bb1.get(i) != bb2.get(i) ) return false ;
            return true ;
        } finally {
            bb1.position(posn1) ;
            bb1.limit(limit1) ;
            bb2.position(posn2) ;
            bb2.limit(limit2) ;
        }
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