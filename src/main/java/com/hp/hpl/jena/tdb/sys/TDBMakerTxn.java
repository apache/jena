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

import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTransaction ;

/** factory for creating objects datasets backed by TDB storage which support transactions */
public class TDBMakerTxn
{
    /** Create a DatasetGraph that supports transactions */  
    public static DatasetGraphTransaction createDatasetGraph(String location)
    {
        return createDatasetGraph(new Location(location)) ;
    }
    
    /** Create a Dataset that supports transactions */  
    public static DatasetGraphTransaction createDatasetGraph(Location location)
    {
        return _create(location) ;
    }
    
    /** Create a Dataset that supports transactions but runs in-memory (for creating test cases)*/  
    public static DatasetGraphTransaction createDatasetGraph()
    {
        return createDatasetGraph(Location.mem()) ;
    }

    private static DatasetGraphTransaction _create(Location location)
    {
        // No need to cache StoreConnection does all that.
        return new DatasetGraphTransaction(location) ;
    }
    
    private static DatasetGraphTransaction _create(DatasetGraphTDB dsg)
    {
        // No need to cache StoreConnection does all that.
        return new DatasetGraphTransaction(dsg) ;
    }

    public static void releaseLocation(Location location)
    {
        StoreConnection.release(location) ;
    }

    public static void reset()
    {
        StoreConnection.reset() ;
    }
}

