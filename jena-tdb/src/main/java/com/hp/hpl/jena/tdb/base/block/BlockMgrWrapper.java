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

import java.util.Iterator ;


public class BlockMgrWrapper implements BlockMgr
{
    protected BlockMgr blockMgr ;

    public BlockMgrWrapper(BlockMgr blockMgr)
    {
        setBlockMgr(blockMgr) ;
    }

    /** Set another BlockMgr as the target of the wrapper - return the old one */ 
    protected final BlockMgr setBlockMgr(BlockMgr blockMgr)
    {
        BlockMgr old = this.blockMgr ;
        this.blockMgr = blockMgr ;
        return old ;
    }
    
    public BlockMgr getWrapped() { return blockMgr ; }
    
    @Override
    public Block allocate(int blockSize)
    {
        return blockMgr.allocate(blockSize) ;
    }

    @Override
    public Block getRead(long id)
    {
        return blockMgr.getRead(id) ;
    }

    @Override
    public Block getReadIterator(long id)
    {
        return blockMgr.getReadIterator(id) ;
    }


    @Override
    public Block getWrite(long id)
    {
        return blockMgr.getWrite(id) ;
    }

    @Override
    public Block promote(Block block)
    {
        return blockMgr.promote(block) ;
    }

    @Override
    public void release(Block block)
    {
        blockMgr.release(block) ;
    }

    @Override
    public void write(Block block)
    {
        blockMgr.write(block) ;
    }

    @Override
    public void overwrite(Block block)
    {
        blockMgr.overwrite(block) ;
    }

    @Override
    public void free(Block block)
    {
        blockMgr.free(block) ;
    }

    @Override
    public boolean isEmpty()
    {
        return blockMgr.isEmpty() ;
    }

    @Override
    public void sync()
    {
        blockMgr.sync() ;
    }


    @Override
    public void syncForce()
    {
        blockMgr.syncForce() ;
    }

    
    @Override
    public boolean valid(int id)
    {
        return blockMgr.valid(id) ;
    }

    @Override
    public boolean isClosed()
    {
        return blockMgr.isClosed() ;
    }

    @Override
    public void close()
    { blockMgr.close() ; }


    @Override
    public void beginIterator(Iterator<?> iter)
    {
        blockMgr.beginIterator(iter) ;
    }

    @Override
    public void endIterator(Iterator<?> iter)
    {
        blockMgr.endIterator(iter) ;
    }
    
    @Override
    public void beginRead()
    {
        blockMgr.beginRead() ;
    }

    @Override
    public void endRead()
    {
        blockMgr.endRead() ;
    }

    @Override
    public void beginUpdate()
    {
        blockMgr.beginUpdate() ;
    }

    @Override
    public void endUpdate()
    {
        blockMgr.endUpdate() ;
    }

    @Override
    public String getLabel()
    {
        return blockMgr.getLabel() ;
    }
    
    @Override
    public String toString() { return blockMgr.toString() ; } 
}
