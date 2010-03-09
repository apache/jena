package com.hp.hpl.jena.tdb.sys;

import java.util.HashMap ;
import java.util.Map ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

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
            dsg.sync(true) ;
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