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


/** Add synchronized to a BlockMgr.  This is the same as BlockMgrWrapper but with 'synchronized' added */

public class BlockMgrSync implements BlockMgr
{
    protected final BlockMgr blockMgr ;

    public BlockMgrSync(BlockMgr blockMgr)
    {
        this.blockMgr = blockMgr ;
    }

    public BlockMgr getWrapped() { return blockMgr ; }
    
    @Override
    synchronized
    public Block allocate(int blockSize)
    {
        return blockMgr.allocate(blockSize) ;
    }

    @Override
    synchronized
    public Block getRead(long id)
    {
        return blockMgr.getRead(id) ;
    }
    
    @Override
    synchronized
    public Block getReadIterator(long id)
    {
        return blockMgr.getReadIterator(id) ;
    }

    @Override
    synchronized
    public Block getWrite(long id)
    {
        return blockMgr.getWrite(id) ;
    }

    @Override
    synchronized
    public Block promote(Block block)
    {
        return blockMgr.promote(block) ;
    }
    
    @Override
    synchronized
    public void release(Block block)
    {
        blockMgr.release(block) ;
    }

    @Override
    synchronized
    public void write(Block block)
    {
        blockMgr.write(block) ;
    }
    
    @Override
    synchronized
    public void overwrite(Block block)
    {
        blockMgr.overwrite(block) ;
    }

    @Override
    synchronized
    public void free(Block block)
    {
        blockMgr.free(block) ;
    }

    @Override
    synchronized
    public void sync()
    {
        blockMgr.sync() ;
    }
    

    @Override
    synchronized
    public void syncForce()
    {
        blockMgr.syncForce() ;
    }

    
    @Override
    synchronized
    public void close()
    { blockMgr.close() ; }

    @Override
    synchronized
    public boolean isEmpty()
    {
        return blockMgr.isEmpty() ;
    }

    @Override
    synchronized
    public void beginIterator(Iterator<?> iter)
    {
        blockMgr.beginIterator(iter) ;
    }

    @Override
    synchronized
    public void endIterator(Iterator<?> iter)
    {
        blockMgr.endIterator(iter) ;
    }
    
    @Override
    synchronized
    public void beginRead()
    {
        blockMgr.beginRead() ;
    }

    @Override
    synchronized
    public void endRead()
    {
        blockMgr.endRead() ;
    }

    @Override
    synchronized
    public void beginUpdate()
    {
        blockMgr.beginUpdate() ;
    }

    @Override
    synchronized
    public void endUpdate()
    {
        blockMgr.endUpdate() ;
    }

    @Override
    synchronized
    public boolean valid(int id)
    {
        return blockMgr.valid(id) ;
    }

    @Override
    synchronized
    public boolean isClosed()
    {
        return blockMgr.isClosed() ;
    }

    @Override
    synchronized
    public String getLabel()
    {
        return blockMgr.getLabel() ;
    }
    
    @Override
    public String toString()
    {
        return "Sync:"+blockMgr.toString() ;
    }

}
