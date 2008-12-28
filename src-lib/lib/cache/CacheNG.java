/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package lib.cache;


import java.util.Iterator;

import lib.ActionKeyValue;
import lib.Cache;
import lib.CacheLRU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.util.ALog;

/** A cache.  But we already use that name (and this is a replacement for that).
 *  This class provides a cache of objects that can be retrieved for shared (read)
 *  use or for exclusive (write) use.  It assumes the application will not make
 *  conflicting demands (e.g. exclusive of a currently multiply shared object).
 *  
 *   This class does not control the making of objects.
 * 
 * @author Andy Seaborne
 */

// Rename later.  This does not assume that read-only objects are returned (may change)?  
public class CacheNG<Key, T> implements Cache<Key, T>
{
    // XXX Stats and loging/reporting
    private static Logger log = LoggerFactory.getLogger(CacheNG.class) ;
    private int statsDumpTick = -1 ;
    private final boolean logging = false  ;
    
    // SoftReference<T>? and then app has a hard reference when using.
    // Probably overkill.
    private int max ;
    private int min ;
    CacheLRU.CacheImpl<Key, PoolEntry> objects ;
    
    // Overall statistics 
    private long cacheEntries ;
    private long cacheHits ;
    private long cacheMisses ; 
    private long cacheEjects ;
    private long cacheTimePoint = 0;  
    
    public CacheNG(int num)            { this(0, num) ; }
    private CacheNG(int min, int max)
    { 
        this.min = min ;
        this.max = max ;
        cacheEntries = 0 ;
        cacheHits = 0 ;
        cacheMisses = 0 ;
        cacheEjects = 0 ;
        
        objects = new CacheLRU.CacheImpl<Key, PoolEntry>(max) ;
        objects.setDropHandler(new ActionKeyValue<Key, PoolEntry>(){
            @Override
            public void apply(Key key, PoolEntry entry)
            {
                cacheEjects++ ;
                if ( logging && log.isDebugEnabled() )
                    log.debug("Eject: "+str(key)+ "Hits: "+entry.cacheHit) ;
            }}) ;
    }

    synchronized
    public boolean contains(Key key)
    {
        boolean b = objects.containsKey(key) ;
        if ( logging )
            log.info("Miss/chk : "+str(key)) ;
        return b ;
    }

    public T getObject(Key key) { return getObject(key, false) ; }

    
    //@Override
    synchronized
    public T getObject(Key key, boolean exclusive)
    { 
        PoolEntry entry = objects.get(key) ;
        if ( entry == null )
        {
            cacheMisses++ ;
            if ( logging )
                log.info("Miss/Get : "+str(key)) ;
            stats() ;
            return null ;
        }
        
        entry.cacheHit++ ;
        
        if ( exclusive )
        {
            if ( entry.refCount > 0 )
            {
                log.error("Attempt to get exclusive access when the object is already being shared: "+key) ;
                throw new CacheException("Failed to get exclusive access") ;
            }
            entry.refCount = -1 ;
            cacheHits++ ;
            if ( logging )
                log.info("Miss/Hit(ex): "+str(entry)) ;
            stats() ;
            return entry.thing ; 
        }
        else
        {
            if ( entry.refCount < 0 )
            {
                log.error("Attempt to get shared access when the object is already exclusively allocated: "+key) ;
                throw new CacheException("Failed to get shared access") ;
            }
            entry.refCount++ ;
            cacheHits++ ;
            if ( logging )
                log.info("Miss/Hit    : "+str(entry)) ;
            stats() ;
            return entry.thing ;
        }
    }
    synchronized
    public void putObject(Key key, T thing)
    {
        PoolEntry entry = objects.get(key) ;
        if ( entry != null )
        {
            if ( entry.thing.equals(thing) )
                log.error("Putting the same object into the cache: "+key) ;
            else
                if ( logging )
                    log.info("Replace    : "+str(entry)) ;
            return ;
        }
        cacheEntries++ ;
        if ( logging )
            log.info("Miss/Put    : "+str(key)) ;
        objects.put(key, new PoolEntry(key, thing)) ;
        stats() ;
    }

    // A simple cache just has get/put.
    // No promotion.
    
    static class CacheException extends RuntimeException
    { CacheException(String msg) { super(msg) ; } }
    
    /** Turn a single-shared object into an exclusively locked object */ 
    synchronized
    public void promote(Key key)
    {
        PoolEntry entry = objects.get(key) ;
        if ( entry == null )
        {
            log.error("Attempt to promote object noit in the pool: "+key) ;
            throw new CacheException("Failed to promote") ;
        }
        if ( entry.refCount < 0 )
        {
            log.error("Attempt to promote object that is already exclusively allocated: "+key) ;
            throw new CacheException("Failed to promote") ;
        }
        else if ( entry.refCount == 0 )
        {
            log.error("Attempt to promote object that is not allocated: "+key) ;
            throw new CacheException("Failed to promote") ;
        }
        else if ( entry.refCount > 1 )
        {
            log.error("Attempt to promote object that is multiply shared: "+key) ;
            throw new CacheException("Failed to promote") ;
        }

        // OK!  Lock it.
        entry.refCount = -1 ;
        if ( logging )
            log.info("Promote    : "+str(entry)) ;
        stats() ;
    }
    
    //@Override
    synchronized
    public void returnObject(Key key)
    {
        PoolEntry entry = objects.get(key) ;
        if ( entry == null )
        {
            log.warn("Object returned that is not allocated: "+key) ;
            return ;
        }        
        
        if ( logging )
            log.info("Return    : "+key) ;
        
        if ( entry.refCount < 0 )
        {
            entry.refCount = 0 ;
            return ;
        }
        
        entry.refCount -- ;
//        if ( entry.refCount == 0 )
//        { }
        stats() ;
    }
    
    @Override
    synchronized
    public void removeObject(Key key)
    {
        PoolEntry entry = objects.get(key) ;
        if ( entry == null )
            return ;
        
        if ( entry.refCount != 0 )
            ; // Problems
        objects.remove(key) ;
        if ( logging )
            log.info("Remove   : "+key) ;
        cacheEntries-- ;
        stats() ;
    }
    
    
    @Override
    public Iterator<Key> keys()
    {
        return objects.keySet().iterator() ;
    }
    synchronized
    public void clear()
    {
        if ( logging )
            log.info("Clear") ;
        objects.clear() ;
    }
    @Override
    public boolean isEmpty()
    {
        return objects.isEmpty() ;
    }
    
    @Override
    public void setDropHandler(ActionKeyValue<Key, T> dropHandler)
    {}
    
    @Override
    public long size()
    {
        return objects.size() ;
    }
    private void stats()
    {
        long total = cacheMisses + cacheHits ;
        if ( total != 0 && total%statsDumpTick == 0 )
        {
            String x = String.format("Size=%d, Hits=%d, Misses=%d, Ejects=%d", size(), cacheHits, cacheMisses, cacheEjects) ;
            log.info(x) ;
        }
    }
    
    
    private void warn(String message, Object... args)
    {
        String x = String.format(message, args) ;
        ALog.warn(this, x) ;
    }
    
    private String str(Key key) { return key.toString() ; } 
    
    private String str(PoolEntry entry)
    { return entry.key.toString()+" ["+entry.refCount+"]"  ; }
    
//    static class Handler<Key, T> implements ActionKeyValue<Key, PoolEntry>
//    {
//
//        @Override
//        public void apply(Key key, PoolEntry entry)
//        {
//            if ( entry.refCount != 0 )
//                ;
//            entry.refCount = 0 ;
//        }
//        
//    }

    // Hmm - bet there is an existing class to do all this. 
    class PoolEntry
    {
        // Ref count = -1 ==> exclusive lock.
        int refCount = 0 ;
        int cacheHit = 0 ;
        long cachePoint = 0 ;
//        enum Status { FREE, ALLOCATED, INVALID } ;
//        Status status = Status.INVALID ;
        T thing ;
        private Object key ;
        PoolEntry(Key key, T thing)
        { 
            this.key = key ; 
            this.thing = thing ;
            this.cachePoint = (++cacheTimePoint) ;
        }
        
        @Override public String toString() 
        {
            return String.format("[H:%d@%d] %s ", cacheHit, cachePoint, thing) ;
        }
    }
    
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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