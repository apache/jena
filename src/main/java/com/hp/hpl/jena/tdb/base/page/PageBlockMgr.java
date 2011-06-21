/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.page;

import org.openjena.atlas.logging.Log ;

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
        if ( ! page.getBackingBlock().isModified() )
            warn("Page for block "+page.getBackingBlock().getId()+" not modified") ;
        
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

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * 
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