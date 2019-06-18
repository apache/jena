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

package org.apache.jena.tdb.sys;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.tdb.StoreConnection ;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.transaction.DatasetGraphTransaction ;

/** Make datasets in various ways. */

public class TDBMaker
{
    // ---- Transactional dataset maker.
    // This hides details from the top level TDB Factory (e.g. for testing)

    /** Create a DatasetGraph that supports transactions */
    static DatasetGraphTransaction createDatasetGraphTransaction(String location) {
        return createDatasetGraphTransaction(Location.create(location)) ;
    }

    /** Create a Dataset that supports transactions */
    public static DatasetGraphTransaction createDatasetGraphTransaction(Location location) {
        return _create(location) ;
    }

    /**
     * Create a Dataset that supports transactions but runs in-memory (for
     * creating test cases)
     */
    public static DatasetGraphTransaction createDatasetGraphTransaction() {
        return createDatasetGraphTransaction(Location.mem()) ;
    }

    /* Caching here means that the same DatasetGraphTransaction is handed out by
     * TDBFactory for each location. It can then can check for multiple attempts to
     * start a transaction on the same thread. We don't support full nested
     * transactions; we only supported nested calls on the same transaction where the
     * inner transaction is compatible with the outer one. The inner one is not a
     * transaction context and the application will be using the outer one.
     * 
     * If CACHING is false, each TDBFactory call creates a new
     * DatasetGraphTransaction, and each acts independently, but safely for the
     * location.
     */
    
    private static final boolean CACHING = true;
    private static final Map<Location, DatasetGraphTransaction> cache = CACHING ? new ConcurrentHashMap<>() : null ;
    
    /**
     * Release a {@code Location}.
     * Do not use a {@code Dataset} at this location without
     * remaking it via {@link TDBFactory}.
     */
    public static void releaseLocation(Location location) {
        if ( CACHING && ! location.isMemUnique() )
            cache.remove(location);
        StoreConnection.release(location) ;
    }

    /** 
     * Reset the making and caching of datasets.
     * Applications should not use this operation.
     * All dataset must be rebuild with {@link TDBFactory}
     * after calling this operation.
     * @deprecated Use TDBInternal.reset()
     */
    @Deprecated
    public static void reset() {
        TDBInternal.reset();
    }
    
    public static void resetCache() {
        if ( CACHING )
            cache.clear();
    }
    
    // Make a DatasetGraphTransaction
    private static DatasetGraphTransaction _create(Location location) {
        if ( ! CACHING || location.isMemUnique() )
            return createDirect(location);
        return cache.computeIfAbsent(location, TDBMaker::createDirect);
    }

    /**
     * Create new {@link DatasetGraphTransaction}. A {@link DatasetGraphTransaction}
     * is analogous to a JDBC connection and it forms a context for the MR+SW
     * transaction behaviour. Normally, there is one {@link DatasetGraphTransaction}
     * per location. A directly created one allows independent transactions from the
     * same thread for specialized usage and testing.
     * 
     * @param location
     * @return DatasetGraphTransaction
     */
    public static DatasetGraphTransaction createDirect(Location location) {
        return new DatasetGraphTransaction(location) ;
    }

    
//    // ---- Raw non-transactional
//    /** 
//     * Create a non-transactional TDB dataset graph.
//     * <b>Use at your own risk.</b>
//     * <i>This function does not attempt to share databases at the same location.</i> 
//     * @deprecated Use {@link TDBFactory} or {@link StoreConnection}.
//     */
//    @Deprecated
//    public static DatasetGraphTDB createDatasetGraphTDB(Location loc, StoreParams params)
//    { return DatasetBuilderStd.create(loc, params) ; }
}
