/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.sys;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;
import com.hp.hpl.jena.tdb.store.FactoryGraphTDB;

/** The workhorse for TDB Factory - hides the internal operations from
 * the public interface (and javadoc) of TDBFactory for clarity.  
 */
public class TDBMaker
{
    

    /** An ImplFactory that creates datasets in the usual way for TDB */
    public final static class ConcreteImplFactory implements DatasetGraphMakerTDB
    {
        //@Override
        public DatasetGraphTDB createDatasetGraph(Location location)
        { 
            if ( location.isMem() )
                return createDatasetGraph() ;
            return FactoryGraphTDB.createDatasetGraph(location) ;
        }
    
        //@Override
        public DatasetGraphTDB createDatasetGraph()
        { return FactoryGraphTDB.createDatasetGraphMem() ; }

        public void releaseDatasetGraph(DatasetGraphTDB dataset)
        {}
    }

    /** An ImplFactory that creates datasets in the usual way for TDB and caches them
     * baed on the location.  Asking for a dataset at a location will 
     * return the same (cached) one. 
     */
    public final static class CachingImplFactory implements DatasetGraphMakerTDB
    {
        private static Logger log = LoggerFactory.getLogger(CachingImplFactory.class) ;
        private DatasetGraphMakerTDB factory1 ;
        private Map<String, DatasetGraphTDB> cache = new HashMap<String, DatasetGraphTDB>() ;
    
        public CachingImplFactory(DatasetGraphMakerTDB factory)
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
    
        public void flush() { cache.clear() ; }

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

    /** ImplFactory for many in-memory datasets. Mainly for testing. */ 
    public final static class MemoryImplFactory implements DatasetGraphMakerTDB
    {
        //@Override
        public DatasetGraphTDB createDatasetGraph(Location location)
        { return FactoryGraphTDB.createDatasetGraphMem() ; }
    
        //@Override
        public DatasetGraphTDB createDatasetGraph()
        { return FactoryGraphTDB.createDatasetGraphMem() ; }

        public void releaseDatasetGraph(DatasetGraphTDB dataset)
        {}
    }

    // ---- Implementation factories 
    /** Implementation factory for creation of datasets - uncached */ 
    public final static DatasetGraphMakerTDB uncachedFactory = new TDBMaker.ConcreteImplFactory() ;
    
    /** Implementation factory for cached creation of datasets */ 
    public final static DatasetGraphMakerTDB cachedFactory = new TDBMaker.CachingImplFactory(uncachedFactory) ;

    // Caching by location.
    private static boolean CACHING = true ;

    /** The default implementation factory for TDB datasets. 
     *  Caching of daatsets for sharing purposes.  
     */

    public final static DatasetGraphMakerTDB stdFactory = CACHING ? cachedFactory :
                                                           uncachedFactory ;

    /** In-memory datasets */
    public final static DatasetGraphMakerTDB memFactory = new TDBMaker.MemoryImplFactory() ;

    // ----
    
    // The one we are using.
    private static DatasetGraphMakerTDB factory = stdFactory ;

    /** Clear any TDB dataset cache */
    public static void clearDatasetCache()
    {
        if ( factory instanceof TDBMaker.CachingImplFactory )
            ((TDBMaker.CachingImplFactory)factory).flush();
    }
    
    /** Release a dataset from any caching */
    public static void releaseDataset(DatasetGraphTDB dataset)
    { factory.releaseDatasetGraph(dataset) ; }
    
    public static Graph _createGraph()
    { return factory.createDatasetGraph().getDefaultGraph() ; }

    public static Graph _createGraph(Location loc)
    {
        // The code to choose the optimizer is in GraphTDBFactory.chooseOptimizer
        return factory.createDatasetGraph(loc).getDefaultGraph() ;
    }

    public static DatasetGraphTDB _createDatasetGraph()
    { return factory.createDatasetGraph() ; }

    public static DatasetGraphTDB _createDatasetGraph(Location loc)
    { return factory.createDatasetGraph(loc) ; }

    /** Set the implementation factory.  Not normally needed - only systems that wish
     * to create unusually combinations of indexes and node tables need to use this call.
     * A detailed knowledge of how TDB works, and internal assumptions, is needed to
     * create full functional TDB graphs or datasets.  Beware.   
     */
    public static void setImplFactory(DatasetGraphMakerTDB f) { factory = f ; }

    /** Get the current implementation factory. */
    public static DatasetGraphMakerTDB getImplFactory() { return factory ; }

}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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