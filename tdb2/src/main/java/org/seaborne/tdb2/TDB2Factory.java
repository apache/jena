/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.tdb2;

import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.tdb2.store.DatasetGraphTDB;
import org.seaborne.tdb2.sys.StoreConnection ;

/** Public factory for connecting to and creating datasets backed by TDB2 storage */
public class TDB2Factory
{
    private TDB2Factory() {} 

    /** @deprecated Use {@link #connectDatasetGraph(Location)} */
    @Deprecated
    public static DatasetGraph createDatasetGraph(Location location) {
        return connectDatasetGraph(location);
    }
    
    /** Create or connect to a TDB2-backed dataset */
    public static DatasetGraph connectDatasetGraph(Location location) {
        StoreConnection sConn = StoreConnection.connectCreate(location) ;
        return sConn.getDatasetGraph() ; 
    }

    /** @deprecated Use {@link #connectDataset(Location)} */
    @Deprecated
    public static Dataset createDataset(Location location) {
        return connectDataset(location);
    }
    
    /** Create or connect to a TDB2-backed dataset */
    public static Dataset connectDataset(Location location) {
        DatasetGraph dsg = connectDatasetGraph(location) ;
        return DatasetFactory.wrap(dsg) ;
    }

    /** @deprecated Use {@link #connectDatasetGraph(String)} */
    @Deprecated
    public static DatasetGraph createDatasetGraph(String location) {
        return connectDatasetGraph(location);
    }
    
    /** Create or connect to a TDB2-backed dataset */
    public static DatasetGraph connectDatasetGraph(String location) {
        return connectDatasetGraph(Location.create(location)) ;
    }

    /** @deprecated Use {@link #connectDataset(String)} */
    @Deprecated
    public static Dataset createDataset(String location) {
        return connectDataset(location);
    }
    
    /** Create or connect to a TDB2-backed dataset */
    public static Dataset connectDataset(String location) {
        return connectDataset(Location.create(location)) ;    }

    /** Create an in-memory TDB2-backed dataset (for testing) */
    public static DatasetGraph createDatasetGraph() {
        return connectDatasetGraph(Location.mem()) ;
    }

    /** Create an in-memory TDB2-backed dataset (for testing) */
    public static Dataset createDataset() {
        return connectDataset(Location.mem()) ;
    }

//    /** Read the file and assembler a dataset */
//    public static Dataset assembleDataset(String assemblerFile) {
//        return (Dataset)AssemblerUtils.build(assemblerFile, VocabTDB.tDatasetTDB) ;
//    }
//    
//    /** Release from the JVM. All caching is lost. */
//    public static void release(Dataset dataset) {
//        _release(location(dataset)) ;
//    }
//    
//    /** Release from the JVM.  All caching is lost. */
//    public static void release(DatasetGraph dataset) {
//        _release(location(dataset)) ;
//    }

    /** Return the location of a dataset if it is backed by TDB, else null */ 
    public static boolean isBackedByTDB(Dataset dataset) {
        DatasetGraph dsg = dataset.asDatasetGraph() ;
        return isBackedByTDB(dsg) ;
    }
    
    /** Return the location of a dataset if it is backed by TDB, else null */ 
    public static boolean isBackedByTDB(DatasetGraph datasetGraph) {
        if ( datasetGraph instanceof DatasetGraphTDB )
            // A transaction or the base storage.
            return true ;
        return false ;
    }

    /** Return the location of a dataset if it is backed by TDB, else null */
    public static Location location(Dataset dataset) {
        DatasetGraph dsg = dataset.asDatasetGraph() ;
        return location(dsg) ;
    }

    /** Return the location of a DatasetGraph if it is backed by TDB, else null */
    public static Location location(DatasetGraph datasetGraph) {
        if ( datasetGraph instanceof DatasetGraphTDB )
            return ((DatasetGraphTDB)datasetGraph).getLocation() ;
        return null ;
    }

//    /** Set the {@link StoreParams} for specific Location.
//     *  This call must only be called before a dataset from Location
//     *  is created. This operation should be used with care; bad choices
//     *  of {@link StoreParams} can reduce performance.
//     *  
//     *  <a href="http://jena.apache.org/documentation/tdb/store-paramters.html"
//     *  >See documentation</a>.
//     *  
//     *  @param location  The persistent storage location
//     *  @param params  StoreParams to use
//     *  @throws IllegalStateException If the dataset has already been setup.
//     */
//    public static void setup(Location location, StoreParams params) {
//        StoreConnection sConn = StoreConnection.getExisting(location) ;
//        if ( sConn != null )
//            throw new IllegalStateException("Location is already active") ;
//        StoreConnection.make(location, params) ;
//    }
    
}
