/**
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
    @Override
    public DatasetGraphTDB createDatasetGraph()    { return factory1.createDatasetGraph() ; }

    @Override
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
    @Override
    public void releaseLocation(Location location)
    {
        // The dataset may already be closed.
        
        if ( location == null /*|| location.isMem()*/ )
            return ;
        
        String absPath = location.getDirectoryPath() ;
        DatasetGraphTDB dsg = cache.get(absPath) ;
        if ( dsg == null )
            return ;
        log.debug("Remove from dataset cache: "+absPath) ;
        cache.remove(absPath) ;
    }

    @Override
    public void releaseDatasetGraph(DatasetGraphTDB dataset)
    {
        Location location = dataset.getLocation() ; 
        releaseLocation(location) ;
    } 
}
