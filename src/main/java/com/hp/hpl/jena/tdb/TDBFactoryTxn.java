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
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils ;
import com.hp.hpl.jena.tdb.assembler.VocabTDB ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;

/** factory for creating objects datasets backed by TDB storage which support transactions */
public class TDBFactoryTxn
{
    // This is TDBFactory with a different maker.??
    
    // Assembler versions
    
    /** Create a Dataset that supports transactions */  
    public static Dataset XcreateDataset(Location location)
    {
        return DatasetFactory.create(XcreateDatasetGraph(location)) ;
    }

    /** Create a Dataset that supports transactions */  
    public static Dataset XcreateDataset(String location)
    {
        return DatasetFactory.create(XcreateDatasetGraph(location)) ;
    }
    
    /** Create a Dataset that supports transactions but runs in-memory (for creating test cases)*/  
    public static Dataset XcreateDataset()
    {
        return XcreateDataset(Location.mem()) ;
    }
    
    /** Read the assembler file and create a dataset with transctional capabilities.
     * Assumes the file contains exactly one definition of a TDB dataset.  
     */ 
    public static Dataset XassembleDataset(String assemblerFile)
    {
        Dataset ds = (Dataset)AssemblerUtils.build(assemblerFile, VocabTDB.tDatasetTDB) ;
        DatasetGraphTDB dsg = (DatasetGraphTDB)(ds.asDatasetGraph()) ;
        return DatasetFactory.create(_create(dsg)) ;
    }
    
    /** Return the location of a dataset if it is backed by TDB, else null */ 
    public static Location location(Dataset dataset)
    {
        DatasetGraph dsg = dataset.asDatasetGraph() ;
        return location(dsg) ;
    }
        
    /** Return the location of a DatasetGraph if it is backed by TDB, else null */ 
    public static Location location(DatasetGraph datasetGraph)
    {
        if ( datasetGraph instanceof DatasetGraphTDB )
            return ((DatasetGraphTDB)datasetGraph).getLocation() ;
        if ( datasetGraph instanceof DatasetGraphTransaction )
            return ((DatasetGraphTransaction)datasetGraph).getLocation() ;
        return null ;
    }

    /** Create a DatasetGraph that supports transactions */  
    public static DatasetGraphTransaction XcreateDatasetGraph(String location)
    {
        return XcreateDatasetGraph(new Location(location)) ;
    }
    
    /** Create a Dataset that supports transactions */  
    public static DatasetGraphTransaction XcreateDatasetGraph(Location location)
    {
        return _create(location) ;
    }
    
    /** Create a Dataset that supports transactions but runs in-memory (for creating test cases)*/  
    public static DatasetGraphTransaction XcreateDatasetGraph()
    {
        return XcreateDatasetGraph(Location.mem()) ;
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
}

