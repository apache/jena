/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
