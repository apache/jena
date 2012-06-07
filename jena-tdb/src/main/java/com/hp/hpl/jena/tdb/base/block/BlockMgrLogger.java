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

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;


public class BlockMgrLogger implements BlockMgr //extends BlockMgrWrapper
{
    private final BlockMgr blockMgr ;
    protected final Logger log ;
    protected final boolean logAllOperations ;
    private final String label ;
    
    public BlockMgrLogger(BlockMgr blockMgr, boolean logAllOperations )
    {
        this(null, blockMgr.getLabel(), blockMgr, logAllOperations) ;
    }
    
    public BlockMgrLogger(String label, BlockMgr blockMgr, boolean logAllOperations )
    {
        this(null, label, blockMgr, logAllOperations) ;
    }

    public BlockMgrLogger(Logger log, String label, BlockMgr blockMgr, boolean logAllOperations )
    {
        this.blockMgr = blockMgr ;
        if ( log == null )
            log = LoggerFactory.getLogger(BlockMgr.class) ;
        this.log = log ;
        this.logAllOperations = logAllOperations ;
        this.label = label ;
    }

    @Override
    public String getLabel() { return label ; }
    
    @Override
    public Block allocate(int blockSize)
    {
        Block x = blockMgr.allocate(blockSize) ;
        info("Allocate("+x.getId()+")") ;
        return x ;
    }

    @Override
    public boolean isEmpty()
    {
        info("isEmpty") ;
        return blockMgr.isEmpty() ;
    }

    @Override
    public Block getRead(long id)
    {
        info("getRead("+id+")") ;
        return blockMgr.getRead(id) ;
    }

    @Override
    public Block getReadIterator(long id)
    {
        info("getReadIterator("+id+")") ;
        return blockMgr.getReadIterator(id) ;
    }
    
    @Override
    public Block getWrite(long id)
    {
        info("getWrite("+id+")") ;
        return blockMgr.getWrite(id) ;
    }

    @Override
    public Block promote(Block block)
    {
        info("promote("+block.getId()+")") ;
        return blockMgr.promote(block) ;
    }

    @Override
    public void release(Block block)
    {
        info("release("+block.getId()+")") ;
        blockMgr.release(block) ;
    }

    @Override
    public void write(Block block)
    {
        info("write("+block.getId()+")") ;
        blockMgr.write(block) ;
    }

    @Override
    public void overwrite(Block block)
    {
        info("overwrite("+block.getId()+")") ;
        blockMgr.overwrite(block) ;
    }

    @Override
    public void free(Block block)
    {
        info("freeBlock("+block.getId()+")") ;
        blockMgr.free(block) ;
    }

    @Override
    public boolean valid(int id)
    {
        info("valid("+id+")") ;
        return blockMgr.valid(id) ;
    }

    @Override
    public void close()
    {
        info("close") ;
        blockMgr.close() ;
    }

    @Override
    public boolean isClosed()
    {
        info("isClosed") ;
        return blockMgr.isClosed() ;
    }

    @Override
    public void sync()
    {
        info("sync") ;
        blockMgr.sync() ;
    }

    @Override
    public void syncForce()
    {
        info("syncForce") ;
        blockMgr.syncForce() ;
    }

    @Override
    public void beginIterator(Iterator<?> iter)
    {
        info("> start iterator") ;
        blockMgr.beginIterator(iter) ;
    }
    
    @Override
    public void endIterator(Iterator<?> iter)
    {
        info("< end iterator") ;
        blockMgr.endIterator(iter) ;
    }
    
    @Override
    public void beginRead()
    {
        info("> start read") ;
        blockMgr.beginRead() ;
    }

    @Override
    public void endRead()
    {
        info("< finish read") ;
        blockMgr.endRead() ;
    }

    @Override
    public void beginUpdate()
    {
        info("> start update") ;
        blockMgr.beginUpdate() ;
    }

    @Override
    public void endUpdate()
    {
        info("< finish update") ;
        blockMgr.endUpdate() ;
    }

    private void info(String string)
    {
        if ( label != null )
            string = label+": "+string ;
        log.info(string) ; 
    }
}
