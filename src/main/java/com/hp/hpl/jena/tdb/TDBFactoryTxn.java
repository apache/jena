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

package com.hp.hpl.jena.tdb;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.tdb.base.file.Location ;

/** Public factory for creating objects datasets backed by TDB storage which support transactions */
public class TDBFactoryTxn
{
    // Assembler versions
    
    /** Create a Dataset that supports transactions */  
    public static Dataset createDataset(Location location)
    {
        return DatasetFactory.create(createDatasetGraph(location)) ;
    }

    /** Create a Dataset that supports transactions */  
    public static Dataset createDataset(String location)
    {
        return DatasetFactory.create(createDatasetGraph(location)) ;
    }
    
    /** Create a Dataset that supports transactions but runs in-memory (for creating test cases)*/  
    public static Dataset createDataset()
    {
        return createDataset(Location.mem()) ;
    }
    

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

}

