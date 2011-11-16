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
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils ;
import com.hp.hpl.jena.tdb.assembler.VocabTDB ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;

/** Public factory for creating objects datasets backed by TDB storage which support transactions */
public class TDBFactoryTxn
{
    // This is TDBFactory with a different maker.??
    
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
    
    /** Read the assembler file and create a dataset with transctional capabilities.
     * Assumes the file contains exactly one definition of a TDB dataset.  
     */ 
    public static Dataset assembleDataset(String assemblerFile)
    {
        // A bit of a kludge for now but it does mean we can reuse the same 
        // (original) assembler definitions.  
        // Downside is opening files, then reopening them.
        // Eventually, we will combine old and new worlds.
        // Maybe:
        // 1 - StoreConnection that takes over a DatasetGraphTDB
        // 2 - reuse the assembler machinery to directly create a StoreConenction.
        // 3 - Flip: Make as a transactional and downgrade to old style. 
        Dataset ds = (Dataset)AssemblerUtils.build(assemblerFile, VocabTDB.tDatasetTDB) ;
        DatasetGraphTDB dsg = (DatasetGraphTDB)(ds.asDatasetGraph()) ;
        Location location = dsg.getLocation() ;
        return createDataset(location) ;
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

