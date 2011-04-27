/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.sys;



import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;

/** The workhorse for TDB Factory - hides the internal operations from
 * the public interface (and javadoc) of TDBFactory for clarity.  
 */
public class TDBMaker
{
    // ---- Implementation factories 
    /** Implementation factory for creation of datasets - uncached */ 
    //public final static DatasetGraphMakerTDB uncachedFactory = new TDBMakerFactoryGraph() ;
    public final static DatasetGraphMakerTDB uncachedFactory = new DatasetGraphSetup() ;
    
    /** Implementation factory for cached creation of datasets */ 
    public final static DatasetGraphMakerTDB cachedFactory = new CachingTDBMaker(uncachedFactory) ;

    // Caching by location.
    private final static boolean CACHING = true ;

    /** The default implementation factory for TDB datasets. 
     *  Caching of daatsets for sharing purposes.  
     */

    public final static DatasetGraphMakerTDB stdFactory = CACHING ? cachedFactory :
                                                           uncachedFactory ;

    /** In-memory datasets */
    //public final static DatasetGraphMakerTDB memFactory = new TDBMakerFactoryGraphMem() ;
    public final static DatasetGraphMakerTDB memFactory = new DatasetGraphSetupMem() ;

    // ----
    
    // The one we are using.
    private static DatasetGraphMakerTDB factory = stdFactory ;

    /** Clear any TDB dataset cache */
    public static void clearDatasetCache()
    {
        if ( factory instanceof CachingTDBMaker )
            ((CachingTDBMaker)factory).flush();
    }
    
    /** Sync all cached datasets */
    public synchronized static void syncDatasetCache()
    {
        if ( factory instanceof CachingTDBMaker )
            ((CachingTDBMaker)factory).sync();
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