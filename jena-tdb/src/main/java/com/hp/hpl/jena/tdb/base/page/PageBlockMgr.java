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

package com.hp.hpl.jena.tdb.base.page;

import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.block.BlockConverter ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockType ;

/** Engine that wraps from blocks to typed pages. */

public class PageBlockMgr<T extends Page>
{
    protected final BlockMgr blockMgr ;
    protected BlockConverter<T> pageFactory ;

    protected PageBlockMgr(BlockConverter<T> pageFactory, BlockMgr blockMgr)
    { 
        this.pageFactory = pageFactory ;
        this.blockMgr = blockMgr ;
    }
   
    // Sometimes, the subclass must pass null to the constructor then call this. 
    protected void setConverter(BlockConverter<T> pageFactory) { this.pageFactory = pageFactory ; }
    
    public BlockMgr getBlockMgr() { return blockMgr ; } 
    
//    /** Allocate an uninitialized slot.  Fill with a .put later */ 
//    public int allocateId()           { return blockMgr.allocateId() ; }
    
    /** Allocate a new thing */
    public T create(BlockType bType)
    {
        Block block = blockMgr.allocate(-1) ;
        block.setModified(true) ;
        T page = pageFactory.createFromBlock(block, bType) ;
        return page ;
    }
    
    public T getWrite(int id)
    { 
        Block block = blockMgr.getWrite(id) ;
        block.setModified(true) ;
        T page = pageFactory.fromBlock(block) ;
        return page ;
    }
    
    public T getRead(int id)
    { 
        Block block = blockMgr.getRead(id) ;
        T page = pageFactory.fromBlock(block) ;
        return page ;
    }

    public void put(T page)
    {
        write(page) ;
        release(page) ;
    }
    
    public void write(T page)
    {
        // Catch updates to non-transactioned datasetgraph.  Check in BlockMgrJournal instead.
//        if ( ! page.getBackingBlock().isModified() )
//            warn("Page for block "+page.getBackingBlock().getId()+" not modified") ;
        
        Block blk = pageFactory.toBlock(page) ;
        blockMgr.write(blk) ;
    }

    public void release(Page page)
    { 
        Block block = page.getBackingBlock() ;
        blockMgr.release(block) ;
    }
    
    private void warn(String string)
    {
        Log.warn(this, string) ;
    }
    
    public void free(Page page)
    {
        Block block = page.getBackingBlock() ;
        blockMgr.free(block) ;
    }
    
    public void promote(Page page)
    { 
        // Replace, reset Block in page.
        Block block = page.getBackingBlock() ;
        Block block2 = blockMgr.promote(block) ;
        if ( block2 != block )
        {        
            block2.setModified(true) ;
            // Change - reset Block in page.
            page.reset(block2) ;
        }
    }
    
    public boolean valid(int id)        { return blockMgr.valid(id) ; }
    
    public void dump()
    { 
        for ( int idx = 0 ; valid(idx) ; idx++ )
        {
            T page = getRead(idx) ;
            System.out.println(page) ;
            release(page) ;
        }
    }
    
    /** Signal the start of an update operation */
    public void startUpdate()       { blockMgr.beginUpdate() ; }
    
    /** Signal the completion of an update operation */
    public void finishUpdate()      { blockMgr.endUpdate() ; }

    /** Signal the start of an update operation */
    public void startRead()         { blockMgr.beginRead() ; }
    
    /** Signal the completeion of an update operation */
    public void finishRead()        { blockMgr.endRead() ; }
}
