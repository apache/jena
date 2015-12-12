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

import org.apache.jena.tdb.StoreConnection ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.setup.DatasetBuilderStd ;
import org.apache.jena.tdb.setup.StoreParams ;
import org.apache.jena.tdb.store.DatasetGraphTDB ;
import org.apache.jena.tdb.transaction.DatasetGraphTransaction ;

/** Make datasets in various ways. */

public class TDBMaker
{
    // ---- Transactional
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

    private static DatasetGraphTransaction _create(Location location) {
        // No need to cache StoreConnection does all that.
        return new DatasetGraphTransaction(location) ;
    }

    public static void releaseLocation(Location location) {
        StoreConnection.release(location) ;
    }

    public static void reset() {
        StoreConnection.reset() ;
    }
    
    // ---- Raw non-transactional
    /** Create a non-transactional TDB dataset graph.
     * Bypasses StoreConnection caching. 
     * <b>Use at your own risk.</b> 
     */
    public static DatasetGraphTDB createDatasetGraphTDB(Location loc, StoreParams params)
    { return DatasetBuilderStd.create(loc, params) ; }
}
