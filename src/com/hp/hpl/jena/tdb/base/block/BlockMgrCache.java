/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import java.nio.ByteBuffer;
import java.util.Map.Entry;

import lib.ActionKeyValue;
import lib.CacheLRU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Caching block manager - this is an LRU cache */
public class BlockMgrCache extends BlockMgrWrapper
{
    private static Logger log = LoggerFactory.getLogger(BlockMgrCache.class) ;
    // Read cache
    CacheLRU<Integer, ByteBuffer> readCache = null ;

    // Delayed dirty writes.
    CacheLRU<Integer, ByteBuffer> writeCache = null ;
    
    public BlockMgrCache(int readSlots, int writeSlots, final BlockMgr blockMgr)
    {
        super(blockMgr) ;
        readCache = new CacheLRU<Integer, ByteBuffer>(readSlots) ;
        
        if ( writeSlots > 0 )
        {
            writeCache = new CacheLRU<Integer, ByteBuffer>(writeSlots) ;
            writeCache.setDropHandler(new ActionKeyValue<Integer, ByteBuffer>(){
                @Override
                public void apply(Integer id, ByteBuffer bb)
                { 
                    if ( log.isDebugEnabled() )
                        log.debug("Cache spill: write block") ;
                    blockMgr.put(id, bb) ; }
            }) ;
        }
    }
    
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
        ByteBuffer bb = readCache.get(id) ;
        if ( bb != null )
            return bb ;
        if ( writeCache != null )
        {
            // Maybe in the dirty blocks still.
            bb = writeCache.get(id) ;
            if ( bb != null )
                return bb ;
        }
        if ( silent )
            bb = blockMgr.getSilent(id) ;
        else
            bb = blockMgr.get(id) ;
        readCache.put(id, bb) ;
        return bb ;
    }

    @Override
    public void put(int id, ByteBuffer block)
    {
        if ( writeCache != null )
            writeCache.put(id, block) ;
        else
            blockMgr.put(id, block) ;

        //cache.remove(id) ;
        readCache.put(id, block) ;
    }
    
//    @Override
//    public void finishUpdate()
//    { 
//        sync() ; 
//    }
    
    @Override
    public void sync(boolean force)
    {
        if ( log.isDebugEnabled() )
        {
            if ( writeCache != null )
                log.debug("sync ("+writeCache.size()+" blocks)") ;
            else
                log.debug("sync") ;
        }
        //if ( force )
        flush() ;
    }
    
    @Override
    public void close()
    {
        if ( log.isDebugEnabled() )
            log.debug("close ("+writeCache.size()+" blocks)") ;
        flush() ;
        blockMgr.close() ;
    }

    private void flush()
    {
        if ( writeCache != null )
        {
            for ( Entry<Integer, ByteBuffer> e : writeCache.entrySet() )
                  blockMgr.put(e.getKey(), e.getValue()) ;
            writeCache.clear() ;   
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