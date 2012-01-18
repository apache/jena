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

package com.hp.hpl.jena.tdb.sys;

import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;

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

//    /** In-memory datasets */
//    public final static DatasetGraphMakerTDB memFactory = new DatasetGraphSetupMem() ;

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
    
    /** Release a location from any caching */
    public static void releaseLocation(Location location)
    { factory.releaseLocation(location) ; }
    
//    public static Graph _createGraph()
//    { return factory.createDatasetGraph().getDefaultGraph() ; }
//
//    public static Graph _createGraph(Location loc)
//    {
//        // The code to choose the optimizer is in GraphTDBFactory.chooseOptimizer
//        return factory.createDatasetGraph(loc).getDefaultGraph() ;
//    }

    public static DatasetGraphTDB _createDatasetGraph()
    { return factory.createDatasetGraph() ; }

    public static DatasetGraphTDB _createDatasetGraph(Location loc)
    { return factory.createDatasetGraph(loc) ; }
    
    public static DatasetGraphTDB _createDatasetGraph(String loc)
    { return _createDatasetGraph(new Location(loc)) ; }

    /** Set the implementation factory.  Not normally needed - only systems that wish
     * to create unusually combinations of indexes and node tables need to use this call.
     * A detailed knowledge of how TDB works, and internal assumptions, is needed to
     * create full functional TDB graphs or datasets.  Beware.   
     */
    public static void setImplFactory(DatasetGraphMakerTDB f) { factory = f ; }

    /** Get the current implementation factory. */
    public static DatasetGraphMakerTDB getImplFactory() { return factory ; }

}
