/*
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.sys;

import java.util.HashMap ;
import java.util.Map ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;

/** An ImplFactory that creates datasets in the usual way for TDB and caches them
 * based on the location.  Asking for a dataset at a location will 
 * return the same (cached) one. 
 */
public final class CachingTDBMaker implements DatasetGraphMakerTDB
{
    private static Logger log = LoggerFactory.getLogger(CachingTDBMaker.class) ;
    private DatasetGraphMakerTDB factory1 ;
    private Map<String, DatasetGraphTDB> cache = new HashMap<String, DatasetGraphTDB>() ;

    public CachingTDBMaker(DatasetGraphMakerTDB factory)
    { this.factory1 = factory ; }
    
    // Uncached currently
    //@Override
    public DatasetGraphTDB createDatasetGraph()    { return factory1.createDatasetGraph() ; }

    //@Override
    public DatasetGraphTDB createDatasetGraph(Location location)
    {
        //if ( location.isMem() )
        // The named in-memory location.  This is cacheable.
        
        String absPath = location.getDirectoryPath() ;
        DatasetGraphTDB dg = cache.get(absPath) ;
        if ( dg == null )
        {
            dg = factory1.createDatasetGraph(location) ;
            log.debug("Add to dataset cache: "+absPath) ;
            cache.put(absPath, dg) ;
        }
        else
            log.debug("Reuse from dataset cache: "+absPath) ;
        return dg ;
    }

    public void flush() { sync() ; cache.clear() ; }
    
    public void sync()
    { 
        for ( DatasetGraphTDB dsg : cache.values() )
        {
            dsg.getLock().enterCriticalSection(Lock.WRITE) ;
            try {
                dsg.sync() ;
            } finally { dsg.getLock().leaveCriticalSection() ; }
        }
    }
    public void releaseDatasetGraph(DatasetGraphTDB dataset)
    {
        Location location = dataset.getLocation() ; 
        if ( location == null /*|| location.isMem()*/ )
            return ;
        
        String absPath = location.getDirectoryPath() ;
        
        if ( ! cache.containsKey(absPath) )
            if ( ! absPath.equals(Names.memName) )
                // Don't worry if a dataset in-memory is cached or not.
                log.warn("Not a cached location: "+absPath) ;
        log.debug("Remove from dataset cache: "+absPath) ;
        cache.remove(absPath) ;
    } 
}

/*
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
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
