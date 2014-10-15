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

import java.util.ArrayDeque ;
import java.util.Deque ;

/** Recycle blocks - but only in-session.
 *   At the end of JVM run, the blocks are made "permanent" as no one finds them again on restart.
 */
final
public class BlockMgrFreeChain extends BlockMgrWrapper
{
    // Could keep Pair<Integer, ByteBuffer>
    //List<Block> freeBlocks = new ArrayList<Block>() ;
    private final Deque<Block> freeBlocks = new ArrayDeque<>();
    
    public BlockMgrFreeChain(BlockMgr blockMgr)
    {
        super(blockMgr) ;
    }

    @Override
    public Block allocate(int blockSize)
    {
        if ( ! freeBlocks.isEmpty() )
        {
            Block block = freeBlocks.removeFirst() ;
            block.getByteBuffer().position(0) ;
            return block ;
        }
        return super.allocate(blockSize) ;
    }

    @Override
    public void free(Block block)
    {
        freeBlocks.add(block) ;
    }

    @Override
    public boolean valid(int id)
    {
        for ( Block blk : freeBlocks ) 
        {
            if ( blk.getId() == id )
                return true ; 
        }
        return super.valid(id) ;
    }
    
    private boolean isFree(int id)
    {
        return freeBlocks.contains(id) ; 
    }

    @Override
    public void sync()
    {
        // Flush free blocks?
        super.sync() ;
    }
    
    @Override
    public String toString() { return "Free:"+super.toString() ; }
}
