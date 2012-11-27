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

package com.hp.hpl.jena.tdb.base.block;


import java.nio.ByteBuffer ;

import static org.apache.jena.atlas.lib.ByteBufferLib.fill ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;

public abstract class AbstractTestBlockMgr extends BaseTest
{
    static final public int BlkSize = 256 ;
    
    protected BlockMgr blockMgr = null ;
    
    @Before public void before()
    { 
       blockMgr = make() ;
       blockMgr.beginUpdate() ;
    }
    @After  public void after()
    {
        if (blockMgr != null)
        {
            blockMgr.endUpdate() ;
            blockMgr.close() ;
        }
    }
    
    @Test public void file01()
    {
        Block block = blockMgr.allocate(BlkSize) ;
        ByteBuffer bb = block.getByteBuffer() ;
        fill(bb, (byte)1) ;
        blockMgr.write(block) ;
        blockMgr.release(block) ;
    }
    
    @Test public void file02()
    {
        Block block = blockMgr.allocate(BlkSize) ;
        ByteBuffer bb = block.getByteBuffer() ;
        fill(bb, (byte)1) ;
        long id = block.getId() ;
        blockMgr.write(block) ;
        blockMgr.release(block) ;
        
        Block block2 = blockMgr.getRead(id) ;
        ByteBuffer bb2 = block2.getByteBuffer() ;
        assertEquals(bb2.capacity(), BlkSize) ;
        assertEquals(bb2.get(0), (byte)1) ;
        assertEquals(bb2.get(BlkSize-1), (byte)1) ;
        blockMgr.release(block2) ;
    }
    
    @Test public void file03()
    {
        Block block = blockMgr.allocate(BlkSize) ;
        ByteBuffer bb = block.getByteBuffer() ;
        fill(bb, (byte)2) ;
        long id = block.getId() ;
        blockMgr.write(block) ;
        blockMgr.release(block) ;

        Block block2 = blockMgr.getRead(id) ;
        ByteBuffer bb2 = block2.getByteBuffer() ;
        assertEquals(bb2.capacity(), BlkSize) ;
        assertEquals(bb2.get(0), (byte)2) ;
        assertEquals(bb2.get(BlkSize-1), (byte)2) ;
        blockMgr.release(block2) ;
    }

    @Test public void multiAccess01()
    {
        Block block1 = blockMgr.allocate(BlkSize) ;
        Block block2 = blockMgr.allocate(BlkSize) ;
        long id1 = block1.getId() ;
        long id2 = block2.getId() ;
        
        ByteBuffer bb1 = block1.getByteBuffer() ;
        ByteBuffer bb2 = block2.getByteBuffer() ;
        
        fill(bb1, (byte)1) ;
        fill(bb2, (byte)2) ;
        
        blockMgr.write(block1) ;
        blockMgr.write(block2) ;
        blockMgr.release(block1) ;
        blockMgr.release(block2) ;
        
        Block block3 = blockMgr.getRead(id1) ;
        Block block4 = blockMgr.getRead(id2) ;
        
        ByteBuffer bb_1 = block3.getByteBuffer() ;
        ByteBuffer bb_2 = block4.getByteBuffer() ;

        contains(bb_1, (byte)1) ;
        contains(bb_2, (byte)2) ;
        
        blockMgr.release(block3) ;
        blockMgr.release(block4) ;
    }
    
    
    protected abstract BlockMgr make() ; 
    
    protected static void contains(ByteBuffer bb, byte fillValue)
    {
        for ( int i = 0; i < bb.limit(); i++ )
            assertEquals("Index: "+i, bb.get(i), fillValue ) ;
    }
}
