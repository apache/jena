/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import java.nio.ByteBuffer;
import java.util.Iterator;

import lib.ActionKeyValue;
import lib.Cache;
import lib.CacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Caching block manager - this is an LRU cache */
public class BlockMgrCache extends BlockMgrWrapper
{
    private static Logger log = LoggerFactory.getLogger(BlockMgrCache.class) ;
    // Read cache
    Cache<Integer, ByteBuffer> readCache = null ;

    // Delayed dirty writes.
    Cache<Integer, ByteBuffer> writeCache = null ;
    
    private boolean logging = log.isDebugEnabled() ;        // Avoid the string assembly overhea.
    private String indexName ; 
    // ---- stats
    long cacheHits = 0 ;
    long cacheMisses = 0 ;
    long cacheWriteHits = 0 ;
    
    public BlockMgrCache(String indexName, int readSlots, int writeSlots, final BlockMgr blockMgr)
    {
        super(blockMgr) ;
        this.indexName = String.format("%-12s", indexName) ;
        //logging = log.isInfoEnabled() && indexName.startsWith("SPO") ;
        
        readCache = CacheFactory.createCache(readSlots) ;
        
        if ( writeSlots > 0 )
        {
            writeCache = CacheFactory.createCache(writeSlots) ;
            writeCache.setDropHandler(new ActionKeyValue<Integer, ByteBuffer>(){
                @Override
                public void apply(Integer id, ByteBuffer bb)
                { 
                    log("Cache spill: write block: %d", id) ;
                    expelEntry(id) ;
                }
            }) ;
        }
    }
    
    private void expelEntry(Integer id)
    {
        ByteBuffer bb = writeCache.getObject(id) ;
        if ( bb == null )
            return ;
        log("Drop (write cache): %d", id) ;
        blockMgr.put(id, bb) ;
        writeCache.removeObject(id) ;
    }

    // Pool?
//    @Override
//    public ByteBuffer allocateBuffer(int id)
//    {
//        super.allocateBuffer(id) ;
//    }
    
    @Override
    public ByteBuffer get(int id)
    {
        return fetchEntry(id, false) ;
    }

    @Override
    public ByteBuffer getSilent(int id)
    {
        return fetchEntry(id, true) ;    
    }
    
    // A ByteBuffer may be in the read cache or the write cache - 
    // it can be just in the write cache because the read cache is finite.

    private ByteBuffer fetchEntry(int id, boolean silent)
    {
        ByteBuffer bb = readCache.getObject(id) ;
        if ( bb != null )
        {
            cacheHits++ ;
            //log("Hit   : %d", id) ;
            return bb ;
        }
        if ( writeCache != null )
        {
            // Maybe in the dirty blocks still.
            bb = writeCache.getObject(id) ;
            if ( bb != null )
            {
                cacheWriteHits++ ;
                log("Hit(w) : %d",id) ;
                return bb ;
            }
        }
        
        cacheMisses++ ;
        log("Miss  : %d", id) ;
        
        if ( silent )
            bb = blockMgr.getSilent(id) ;
        else
            bb = blockMgr.get(id) ;
        readCache.putObject(id, bb) ;
        return bb ;
    }

    @Override
    public void put(int id, ByteBuffer block)
    {
        log("Put   : %d", id) ;
        
        if ( writeCache != null )
            writeCache.putObject(id, block) ;
        else
            blockMgr.put(id, block) ;

        //cache.remove(id) ;
        readCache.putObject(id, block) ;
    }
    
//    @Override
//    public void finishUpdate()
//    { 
//        sync() ; 
//    }
    
    @Override
    public void sync(boolean force)
    {
        if ( true )
        {
            String x = "" ;
            if ( indexName != null )
                x = indexName+" : ";
            log("%sH=%d, M=%d, W=%d", x, cacheHits, cacheMisses, cacheWriteHits) ;
        }
        
        if ( writeCache != null )
            log("sync (%d blocks)", writeCache.size()) ;
        else
            log("sync") ;
        syncFlush(force) ;
    }
    
    private void log(String fmt, Object... args)
    { 
        if ( ! logging ) return ;
        String msg = String.format(fmt, args) ;
        if ( indexName != null )
             msg = indexName+" : "+msg ;
        log.debug(msg) ;
    }
    
    @Override
    public void close()
    {
        log("close ("+writeCache.size()+" blocks)") ;
        syncFlush(true) ;
        blockMgr.close() ;
    }

    private void syncFlush(boolean all)
    {
        if ( writeCache != null )
        {
            log("Flush (write cache)") ;
            
            long N = writeCache.size() ;
            Integer[] ids = new Integer[(int)N] ;

            // Choose ... and it's in order.
            Iterator<Integer> iter = writeCache.keys() ;
            for ( int i = 0 ; iter.hasNext() ; i++ )
                ids[i] = iter.next() ;
            
            // Flush entries.
            long limit = 3*N/4 ;
            if ( all ) limit = N ;
            
            for ( int i = 0 ; i < (int)limit ; i++ )
            {
                Integer id = ids[i] ;
                expelEntry(id) ;
            }
        }
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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