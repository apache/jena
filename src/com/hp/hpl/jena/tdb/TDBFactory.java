/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils;
import com.hp.hpl.jena.tdb.assembler.VocabTDB;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;
import com.hp.hpl.jena.tdb.store.FactoryGraphTDB;
import com.hp.hpl.jena.tdb.store.GraphTDB;

/** Public factory for creating objects (graphs, datasest) associated with TDB */
public class TDBFactory
{
    /** Interface to maker of the actual implementations of TDB graphs and datasets */ 
    public interface ImplFactory 
    {
        /** Make a memory implementation of a TDB graph (memory graphs are for testing, not efficiency) */
        public GraphTDB createGraph() ;
        /** Make a TDB graph with persistent data at the location */
        public GraphTDB createGraph(Location loc) ;
        
        public DatasetGraphTDB createDatasetGraph() ;
        public DatasetGraphTDB createDatasetGraph(Location location) ;
    }

    // ---- Implementation factories 
    /** Implementation factory for creation of datasets - uncached */ 
    public final static ImplFactory uncachedFactory = new ConcreteImplFactory() ;
    
    /** Implementation factory for cached creation of datasets */ 
    public final static ImplFactory cachedFactory = new CachingImplFactory(uncachedFactory) ;

    // Caching by location.
    private static boolean CACHING = true ;

    /** The default implementation factory for TDB datasets. 
     *  Caching of daatsets for sharing purposes.  
     */

    public final static ImplFactory stdFactory = CACHING ? cachedFactory :
                                                           uncachedFactory ;

    /** In-memory datasets */
    public final static ImplFactory memFactory = new MemoryImplFactory() ;

    // ----
    
    // The one we are using.
    private static ImplFactory factory = null ;

    static { 
        TDB.init() ; 
        setImplFactory(stdFactory) ;
    }

    /** Clear any TDB dataset cache */
    public static void clearDatasetCache()
    {
        if ( factory instanceof CachingImplFactory )
            ((CachingImplFactory)factory).flush();
    }
    
    /** Release a dataset from  any caching */
    public static void releaseDataset(DatasetGraphTDB dataset)
    {
        if ( factory instanceof CachingImplFactory )
            ((CachingImplFactory)factory).release(dataset.getLocation()) ;
    }

    
    /** Read the file and assembler a model, of type TDB persistent graph */ 
    public static Model assembleModel(String assemblerFile)
    {
        return (Model)AssemblerUtils.build(assemblerFile, VocabTDB.tGraphTDB) ;
    }
    
    /** Read the file and assembler a graph, of type TDB persistent graph */ 
    public static Graph assembleGraph(String assemblerFile)
    {
        Model m = assembleModel(assemblerFile) ;
        Graph g = m.getGraph() ;
        return g ;
    }

    /** Read the file and assembler a dataset */ 
    public static Dataset assembleDataset(String assemblerFile)
    {
        return (Dataset)AssemblerUtils.build(assemblerFile, VocabTDB.tDatasetTDB) ;
    }
    
    /** Create a model, at the given location */
    public static Model createModel(Location loc)
    {
        return ModelFactory.createModelForGraph(createGraph(loc)) ;
    }

    /** Create a model, at the given location */
    public static Model createModel(String dir)
    {
        return ModelFactory.createModelForGraph(createGraph(dir)) ;
    }

    /** Create a TDB model backed by an in-memory block manager. For testing. */  
    public static Model createModel()
    { return ModelFactory.createModelForGraph(createGraph()) ; }

    
    /** Create a TDB model for named model */  
    public static Model createNamedModel(String name, String location)
    { return createDataset(location).getNamedModel(name) ; }
    
    /** Create a TDB model for named model */  
    public static Model createNamedModel(String name, Location location)
    { return createDataset(location).getNamedModel(name) ; }

    // Meaningless unless there is only one in-memeory dataset */
//    /** Create a TDB model for named model for an in-memory */  
//    public static Model createNamedModel(String name)
//    { return createDataset().getNamedModel(name) ; }
    
    /** Create or connect to a TDB-backed dataset */ 
    public static Dataset createDataset(String dir)
    { return createDataset(new Location(dir)) ; }

    /** Create or connect to a TDB-backed dataset */ 
    public static Dataset createDataset(Location location)
    { return new DatasetImpl(_createDatasetGraph(location)) ; }

    /** Create or connect to a TDB dataset backed by an in-memory block manager. For testing.*/ 
    public static Dataset createDataset()
    { return new DatasetImpl(_createDatasetGraph()) ; }

    /** Create a graph, at the given location */
    public static Graph createGraph(Location loc)       { return _createGraph(loc) ; }

    /** Create a graph, at the given location */
    public static Graph createGraph(String dir)
    {
        Location loc = new Location(dir) ;
        return createGraph(loc) ;
    }
    
    /** Create a TDB graph backed by an in-memory block manager. For testing. */  
    public static Graph createGraph()   { return _createGraph() ; }

    /** Create a TDB graph for named graph */  
    public static Graph createNamedGraph(String name, String location)
    { return createDatasetGraph(location).getGraph(Node.createURI(name)) ; }
    
    /** Create a TDB graph for named graph */  
    public static Graph createNamedGraph(String name, Location location)
    { return createDatasetGraph(location).getGraph(Node.createURI(name)) ; }

    // Meaningless unless there is only one in-memeory dataset */
//    /** Create a TDB model for named model for an in-memory */  
//    public static Graph createNamedGraph(String name)
//    { return createDataset().getNamedModel(name) ; }
    
    /** Create or connect to a TDB-backed dataset (graph-level) */
    public static DatasetGraphTDB createDatasetGraph(String directory)
    { return _createDatasetGraph(new Location(directory)) ; }
    
    /** Create or connect to a TDB-backed dataset (graph-level) */
    public static DatasetGraphTDB createDatasetGraph(Location location)
    { return _createDatasetGraph(location) ; }

    /** Create or connect to a TDB-backed dataset (graph-level) */
    public static DatasetGraphTDB createDatasetGraph()
    { return _createDatasetGraph() ; }

    // ---- Point at which actual graphs are made.
    // Cache?
    
    private static Graph _createGraph()
    { return factory.createGraph() ; }

    private static Graph _createGraph(Location loc)
    {
        // The code to choose the optimizer is in GraphTDBFactory.chooseOptimizer
        return factory.createGraph(loc) ;
    }

    private static DatasetGraphTDB _createDatasetGraph()
    { return factory.createDatasetGraph() ; }

    private static DatasetGraphTDB _createDatasetGraph(Location loc)
    { return factory.createDatasetGraph(loc) ; }
    
    // -------- Dataset and graph implementation factories. 
    
    /** Set the implementation factory.  Not normally needed - only systems that wish
     * to create unusually combinations of indexes and node tables need to use this call.
     * A detailed knowledge of how TDB works, and internal assumptions, is needed to
     * create full functional TDB graphs or datasets.  Beware.   
     */
    public static void setImplFactory(ImplFactory f) { factory = f ; }
    
    /** Get the current implementation factory. */
    public static ImplFactory getImplFactory() { return factory ; }
    
    // ---- Concrete

    /** An ImplFactory that creates datasets in the usual way for TDB */
    public final static class ConcreteImplFactory implements ImplFactory
    {
        @Override
        public GraphTDB createGraph()
        { return FactoryGraphTDB.createGraphMem() ; }
    
        @Override
        public GraphTDB createGraph(Location location)      
        { 
            if ( location.isMem() )
                return createGraph() ;
            return FactoryGraphTDB.createGraph(location) ; }
    
        @Override
        public DatasetGraphTDB createDatasetGraph(Location location)
        { 
            if ( location.isMem() )
                return createDatasetGraph() ;
            return FactoryGraphTDB.createDatasetGraph(location) ;
        }
    
        @Override
        public DatasetGraphTDB createDatasetGraph()
        { return FactoryGraphTDB.createDatasetGraphMem() ; }
    }

    // ---- Caching
    
    /** An ImplFactory that creates datasets in the usual way for TDB and caches them
     * baed on the location.  Asking for a dataset at a location will 
     * return the same (cached) one. 
     */
    public final static class CachingImplFactory implements ImplFactory
    {
        private static Logger log = LoggerFactory.getLogger(CachingImplFactory.class) ;
        private ImplFactory factory1 ;
        private Map<String, DatasetGraphTDB> cache = new HashMap<String, DatasetGraphTDB>() ;
    
        public CachingImplFactory(ImplFactory factory)
        { this.factory1 = factory ; }
        
        // Uncached currently
        @Override
        public GraphTDB createGraph()                  { return factory1.createGraph() ; }
        
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
    
        @Override
        public GraphTDB createGraph(Location loc)
        {
            // Note - this equates a graph with the default graph of a dataset.  
            return createDatasetGraph(loc).getDefaultGraphTDB() ;
        }
    
        public void flush() { cache.clear() ; }
        public void release(Location location)
        {
            if ( location == null /*|| location.isMem()*/ )
                return ;
            
            String absPath = location.getDirectoryPath() ;
            
            if ( ! cache.containsKey(absPath) )
                if ( ! absPath.equals(Location.pathnameMem) )
                    // Don't worry if a dataset in-memory is cached or not.
                    log.warn("Not a cached location: "+absPath) ;
            log.debug("Remove from dataset cache: "+absPath) ;
            cache.remove(absPath) ;
        }
    }

    // ---- In-memory
    
    /** ImplFactory for many in-memeory datasets. Mainly for testing. */ 
    public static class MemoryImplFactory implements ImplFactory
    {
        @Override
        public GraphTDB createGraph()
        { return FactoryGraphTDB.createGraphMem() ; }
    
        @Override
        public GraphTDB createGraph(Location loc)      
        { return FactoryGraphTDB.createGraphMem() ; }
    
        @Override
        public DatasetGraphTDB createDatasetGraph(Location location)
        { return FactoryGraphTDB.createDatasetGraphMem() ; }
    
        @Override
        public DatasetGraphTDB createDatasetGraph()
        { return FactoryGraphTDB.createDatasetGraphMem() ; }
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