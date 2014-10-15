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

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.setup.DatasetBuilderStd ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTransaction ;

/** Make datasets in various ways. */

public class TDBMaker
{
    // ---- Transactional
    // This hides details from the top level TDB Factory 
    
    /** Create a DatasetGraph that supports transactions */  
    public static DatasetGraphTransaction createDatasetGraphTransaction(String location)
    {
        return createDatasetGraphTransaction(new Location(location)) ;
    }
    
    /** Create a Dataset that supports transactions */  
    public static DatasetGraphTransaction createDatasetGraphTransaction(Location location)
    {
        return _create(location) ;
    }
    
    /** Create a Dataset that supports transactions but runs in-memory (for creating test cases)*/  
    public static DatasetGraphTransaction createDatasetGraphTransaction()
    {
        return createDatasetGraphTransaction(Location.mem()) ;
    }

    private static DatasetGraphTransaction _create(Location location)
    {
        // No need to cache StoreConnection does all that.
        return new DatasetGraphTransaction(location) ;
    }
    
    public static void releaseLocation(Location location)
    {
        StoreConnection.release(location) ;
    }

    public static void reset()
    {
        StoreConnection.reset() ;
    }
    
    // ---- Base storage.
    
    /* The one we are using */
    private static DatasetGraphMakerTDB builder = new BuilderStd() ;

    public static DatasetGraphTDB createDatasetGraphTDB(Location loc)
    { return builder.createDatasetGraph(loc) ; }

    // -- Different ways of doing it.
    
    /** Interface to maker of the actual implementations of TDB datasets */ 
    private interface DatasetGraphMakerTDB 
    {
        /** Create a TDB-backed dataset at a given location */
        public DatasetGraphTDB createDatasetGraph(Location location) ;
    }

    /** Make directly the base DatasetGraphTDB */
    private static class BuilderStd implements DatasetGraphMakerTDB
    {
        @Override
        public DatasetGraphTDB createDatasetGraph(Location location)
        {
            return DatasetBuilderStd.create(location) ;
        }
    }
    
    /** Make by creating the normal, transactional one and finding the base */ 
    private static class BuilderBase implements DatasetGraphMakerTDB
    {
        @Override
        public DatasetGraphTDB createDatasetGraph(Location location)
        {
            DatasetGraph dsg = TDBFactory.createDatasetGraph(location) ;
            return TDBInternal.getBaseDatasetGraphTDB(dsg) ;
        }
    }
    
    /** The StoreConnection-cached base DatasetGraphTDB.*/ 
    private static class BuilderStoreConnectionBase implements DatasetGraphMakerTDB
    {
        @Override
        public DatasetGraphTDB createDatasetGraph(Location location)
        {
            return StoreConnection.make(location).getBaseDataset() ;
        }
    }
    
}
