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

import org.apache.jena.atlas.lib.ActionKeyValue ;
import org.apache.jena.atlas.lib.Cache ;
import org.apache.jena.atlas.lib.CacheFactory ;
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
    private final Cache<Long, Block> readCache ;

    // Delayed dirty writes.  May be present, may not.
    private final Cache<Long, Block> writeCache ;
    
    public static boolean globalLogging = false ;           // Also enable the logging level. 
    private boolean logging = false ;                       // Also enable the logging level. 
    // ---- stats
    long cacheReadHits = 0 ;
    long cacheMisses = 0 ;
    long cacheWriteHits = 0 ;
    
    static BlockMgr create(int readSlots, int writeSlots, final BlockMgr blockMgr)
    {
        if ( readSlots < 0 && writeSlots < 0 )
            return blockMgr ;
        return new BlockMgrCache(readSlots, writeSlots, blockMgr) ;
    }
    
    private BlockMgrCache(int readSlots, int writeSlots, final BlockMgr blockMgr)
    {
        super(blockMgr) ;
        // Caches are related so we can't use a Getter for cache management.
        if ( readSlots < -1 )
            readCache = CacheFactory.createNullCache() ;
        else
            readCache = CacheFactory.createCache(readSlots) ;
        if ( writeSlots <= 0 )
            writeCache = null ;
        else
        {
            writeCache = CacheFactory.createCache(writeSlots) ;
            writeCache.setDropHandler(new ActionKeyValue<Long, Block>(){
                @Override
                public void apply(Long id, Block block)
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
                    BlockMgrCache.super.write(block) ;
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
    public Block getRead(long id)
    {
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
    public Block getReadIterator(long id)
    {
        // And don't pass down "iterator" calls.
        return getRead(id) ; 
    }


    @Override
    synchronized
    public Block getWrite(long _id)
    {
        Long id = _id;
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
            blk = readCache.get(id) ;
            cacheReadHits++ ;
            log("Hit(w->r) : %d", id) ;
            blk = promote(blk) ;
            return blk ;
        }
        
        // Did not find.
        cacheMisses++ ;
        log("Miss/w: %d", id) ;
        // Pass operation to wrapper.
        blk = super.getWrite(id);
        if ( writeCache != null )
            writeCache.put(id, blk) ;
        return blk ;
    }
    

    @Override
    synchronized
    public Block promote(Block block)
    {
        Long id = block.getId() ;
        readCache.remove(id) ;
        Block block2 = super.promote(block) ;
        if ( writeCache != null )
            writeCache.put(id, block2) ;
        return block ;
    }
    
    @Override
    synchronized
    public void write(Block block)
    {
        writeCache(block) ;
        super.write(block) ;
    }
    
    @Override
    synchronized
    public void overwrite(Block block)
    {
        Long id = block.getId() ;
        // It can be a read block (by the transaction), now being written for real (enacting a transaction).
        super.overwrite(block) ;
        // Keep read cache up-to-date. 
        // Must at least expel the read block (which is not the overwrite block).
        readCache.put(id, block) ;
    }
    
    private void writeCache(Block block)
    {
        Long id = block.getId() ;
        log("WriteCache : %d", id) ;
        // Should not be in the read cache due to a getWrite earlier.
        if ( readCache.containsKey(id) )
            log.warn("write: Block in the read cache") ;
        if ( writeCache != null )
        {
            writeCache.put(id, block) ;
            return ;
        }
    }
    
    @Override
    synchronized
    public void free(Block block)
    {
        Long id = block.getId() ;
        log("Free  : %d", id) ;
        if ( readCache.containsKey(id) )
        {
            log.warn("Freeing block from read cache") ;
            readCache.remove(id) ;
        }
        if ( writeCache != null )
            writeCache.remove(id) ;
        super.free(block) ;
    }

    @Override
    synchronized
    public void sync()
    {
        _sync(false) ;
    }
    
    @Override
    synchronized
    public void syncForce()
    {
        _sync(true) ;
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
    
    @Override
    public String toString()
    {
        return "Cache:"+super.blockMgr.toString() ; 
    }
    

    private void log(String fmt, Object... args)
    { 
        if ( ! logging && ! globalLogging ) return ;
        String msg = String.format(fmt, args) ;
        if ( getLabel() != null )
             msg = getLabel()+" : "+msg ;
        log.debug(msg) ;
    }
    
    private void _sync(boolean force)
    {
        if ( true )
        {
            String x = "" ;
            if ( getLabel() != null )
                x = getLabel()+" : ";
            log("%sH=%d, M=%d, W=%d", x, cacheReadHits, cacheMisses, cacheWriteHits) ;
        }
        
        if ( writeCache != null )
            log("sync (%d blocks)", writeCache.size()) ;
        else
            log("sync") ;
        boolean somethingWritten = syncFlush() ;

        if ( force )
        {
            log("syncForce underlying BlockMgr") ;
            super.syncForce() ;
        }
        else if ( somethingWritten )
        {
            log("sync underlying BlockMgr") ;
            super.sync() ;
        }
        else
            log("Empty sync") ;
    }

    private boolean syncFlush()
    {
        if ( writeCache == null ) return false ;

        boolean didSync = false ;

        log("Flush (write cache)") ;

        long N = writeCache.size() ;
        Long[] ids = new Long[(int)N] ;

        // Single writer (sync is a write operation MRSW)
        // Iterating is safe.

        Iterator<Long> iter = writeCache.keys() ;
        if ( iter.hasNext() )
            didSync = true ;

        // Need to get all then delete else concurrent modification exception. 
        for ( int i = 0 ; iter.hasNext() ; i++ )
            ids[i] = iter.next() ;

        for ( int i = 0 ; i < N ; i++ )
        {
            Long id = ids[i] ;
            expelEntry(id) ;
        }
        if ( didSync )
            super.sync() ;
        return didSync ;
    }
    
    // Write out when flushed.
    // Do not call from drop handler.
    private void expelEntry(Long id)
    {
        Block block = writeCache.get(id) ;
        if ( block == null )
        {
            log.warn("Write cache: "+id+" expelling entry that isn't there") ;
            return ;
        }
        log("Expel (write cache): %d", id) ;
        // This pushes the block to the BlockMgr being cached.
        super.write(block) ;
        writeCache.remove(id) ;

        // Move it into the readCache because it's often read after writing
        // and the read cache is often larger.
        readCache.put(id, block) ;
    }


}
