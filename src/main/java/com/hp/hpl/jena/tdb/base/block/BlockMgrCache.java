/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import java.util.Iterator ;

import org.openjena.atlas.lib.ActionKeyValue ;
import org.openjena.atlas.lib.Cache ;
import org.openjena.atlas.lib.CacheFactory ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Caching block manager - this is an LRU cache */
public class BlockMgrCache extends BlockMgrSync
{
    // Actually, this is two cache one on the read blocks and one on the write blocks.
    // The overridden public operations are sync'ed.
    // As sync is on "this", it also covers all the other operations via BlockMgrSync
    
    private static Logger log = LoggerFactory.getLogger(BlockMgrCache.class) ;
    // Read cache : always present.
    private final Cache<Integer, Block> readCache ;

    // Delayed dirty writes.  May be present, may not.
    private final Cache<Integer, Block> writeCache ;
    
    public static boolean globalLogging = false ;           // Also enable the logging level. 
    private boolean logging = false ;                       // Also enable the logging level. 
    private String indexName ; 
    // ---- stats
    long cacheReadHits = 0 ;
    long cacheMisses = 0 ;
    long cacheWriteHits = 0 ;
    
    public BlockMgrCache(String indexName, int readSlots, int writeSlots, final BlockMgr blockMgr)
    {
        super(blockMgr) ;
        this.indexName = String.format("%-12s", indexName) ;
        
        // Caches are related so we can't use a Getter for cache management.
        readCache = CacheFactory.createCache(readSlots) ;
        if ( writeSlots <= 0 )
            writeCache = null ;
        else
        {
            writeCache = CacheFactory.createCache(writeSlots) ;
            writeCache.setDropHandler(new ActionKeyValue<Integer, Block>(){
                @Override
                public void apply(Integer id, Block block)
                { 
                    // We're inside a synchronized operation at this point.
                    log("Cache spill: write block: %d", id) ;
                    if (block == null)
                    {
                        log.warn("Write cache: " + id + " dropping an entry that isn't there") ;
                        return ;
                    }
                    // Force the block to be writtern
                    // by sending it to the wrapped BlockMgr
                    BlockMgrCache.super.put(block) ;
                }
            }) ;
        }
    }
    
    // Pool?
//    @Override
//    public ByteBuffer allocateBuffer(int id)
//    {
//        super.allocateBuffer(id) ;
//    }
    
    @Override
    synchronized
    public Block getRead(int _id)
    {
        Integer id = _id ;
        // A Block may be in the read cache or the write cache.
        // It can be just in the write cache because the read cache is finite.
        Block blk = readCache.get(id) ;
        if ( blk != null )
        {
            cacheReadHits++ ;
            log("Hit(r->r) : %d", id) ;
            return blk ;
        }
        
        // A requested block may be in the other cache.
        // Writable blocks are readable.
        // readable blocks are not writeable (see below).
        if ( writeCache != null )
            // Might still be in the dirty blocks.
            // Leave in write cache
            blk = writeCache.get(id) ;
        if ( blk != null )
        {
            cacheWriteHits++ ;
            log("Hit(r->w) : %d",id) ;
            return blk ;
        }
        
        cacheMisses++ ;
        log("Miss/r: %d", id) ;
        blk = super.getRead(id) ;
        readCache.put(id, blk) ;
        return blk ;
    }

    @Override
    synchronized
    public Block getWrite(int _id)
    {
        Integer id = _id ;
        Block blk = null ;
        if ( writeCache != null )
            blk = writeCache.get(id) ;
        if ( blk != null )
        {
            cacheWriteHits++ ;
            log("Hit(w->w) : %d", id) ;
            return blk ;
        }
        
        // blk is null.
        // A requested block may be in the other cache. Promote it.
        
        if ( readCache.containsKey(id) )
        {
            cacheReadHits++ ;
            log("Hit(w->r) : %d", id) ;
            blk = promote(blk) ;
            return blk ;
        }
        
        // Did not find.
        cacheMisses++ ;
        log("Miss/w: %d", id) ;
        if ( writeCache != null )
            writeCache.put(id, blk) ;
        return blk ;
    }
    

    @Override
    synchronized
    public Block promote(Block block)
    {
        Integer id = block.getId() ;
        readCache.remove(id) ;
        Block block2 = super.promote(block) ;
        writeCache.put(id, block2) ;
        return block ;
    }
    
    @Override
    synchronized
    public void put(Block block)
    {
        Integer id = block.getId() ;
        log("Put   : %d", id) ;
        // Should not be in the read cache due to a getWrite earlier.
        if ( readCache.containsKey(id) )
            log.error("put: Block in the read cache") ;
        if ( writeCache != null )
        {
            writeCache.put(id, block) ;
            return ;
        }
        super.put(block) ;
    }
    
//    @Override
//    synchronized
//    public void releaseRead(Block block)
//    {
//        blockMgr.releaseRead(block) ;
//    }
//
//    @Override
//    synchronized
//    public void releaseWrite(Block block)
//    {
//        blockMgr.releaseWrite(block) ;
//    }
    
    @Override
    synchronized
    public void freeBlock(Block block)
    {
        Integer id = block.getId() ;
        log("Free  : %d", id) ;
        if ( readCache.containsKey(id) )
        {
            log.warn("Freeing block from read cache") ;
            readCache.remove(id) ;
        }
        if ( writeCache != null )
            writeCache.remove(id) ;
        super.freeBlock(block) ;
    }

    @Override
    synchronized
    public void sync()
    {
        if ( true )
        {
            String x = "" ;
            if ( indexName != null )
                x = indexName+" : ";
            log("%sH=%d, M=%d, W=%d", x, cacheReadHits, cacheMisses, cacheWriteHits) ;
        }
        
        if ( writeCache != null )
            log("sync (%d blocks)", writeCache.size()) ;
        else
            log("sync") ;
        boolean somethingWritten = syncFlush() ;
        // Sync the wrapped object
        if ( somethingWritten ) 
            log("sync underlying BlockMgr") ;
        else
            log("Empty sync") ;
    }
    
    @Override
    synchronized
    public void close()
    {
        if ( writeCache != null )
            log("close ("+writeCache.size()+" blocks)") ;
        syncFlush() ;
        super.close() ;
    }

    private void log(String fmt, Object... args)
    { 
        if ( ! logging && ! globalLogging ) return ;
        String msg = String.format(fmt, args) ;
        if ( indexName != null )
             msg = indexName+" : "+msg ;
        log.debug(msg) ;
    }
    
    private boolean syncFlush()
    {
        if ( writeCache == null ) return false ;

        boolean didSync = false ;

        log("Flush (write cache)") ;

        long N = writeCache.size() ;
        Integer[] ids = new Integer[(int)N] ;

        // Single writer (sync is a write operation MRSW)
        // Iterating is safe.

        Iterator<Integer> iter = writeCache.keys() ;
        if ( iter.hasNext() )
            didSync = true ;

        // Need to get all then delete else concurrent modification exception. 
        for ( int i = 0 ; iter.hasNext() ; i++ )
            ids[i] = iter.next() ;

        for ( int i = 0 ; i < N ; i++ )
        {
            Integer id = ids[i] ;
            expelEntry(id) ;
        }
        return didSync ;
    }
    
    // Write out when flushed.
    // Do not call from drop handler.
    private void expelEntry(Integer id)
    {
        Block block = writeCache.get(id) ;
        if ( block == null )
        {
            log.warn("Write cache: "+id+" expelling entry that isn't there") ;
            return ;
        }
        log("Expel (write cache): %d", id) ;
        // This pushes the block to the BlockMgr being cached.
        super.put(block) ;
        writeCache.remove(id) ;

        // Move it into the readCache because it's often read after writing
        // and the read cache is often larger.
        readCache.put(id, block) ;
    }


}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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