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

package org.apache.jena.tdb.setup;

import org.apache.jena.tdb.TDBFactory ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.store.DatasetPrefixesTDB ;
import org.apache.jena.tdb.store.nodetable.NodeTable ;
import org.apache.jena.tdb.store.tupletable.TupleIndex ;
import org.apache.jena.tdb.sys.DatasetControl ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Building datastructures on top of the base file
 *  abstractions of indexes, block managers and object files.  
 */
public class Build
{
    private static boolean VERBOSE = true ;
    private static Logger log = LoggerFactory.getLogger(Build.class) ;
    private static StoreParams params = StoreParams.getDftStoreParams() ;
    
    public static TupleIndex openTupleIndex(Location loc, String indexName, String primary, String indexOrder) {
        return openTupleIndex(loc, indexName, primary, indexOrder, params) ;
    }
    
    public static TupleIndex openTupleIndex(Location location, String indexName, String primary, String indexOrder, StoreParams params) {
        return DatasetBuilderStd.stdBuilder().makeTupleIndex(location, indexName, primary, indexOrder, params) ;
    }
    
    public static TupleIndex openTupleIndex(Location location, String indexName, String primary, String indexOrder, int readCacheSize, int writeCacheSize, int dftKeyLength, int dftValueLength)
    {
        StoreParamsBuilder spb = StoreParams.builder() ;
        spb.blockReadCacheSize(readCacheSize) ;
        spb.blockWriteCacheSize(writeCacheSize) ;
        return openTupleIndex(location, indexName, primary, indexOrder, spb.build()) ;
    }
    
    public static DatasetPrefixesTDB makePrefixes(Location location, DatasetControl policy) {
        return DatasetBuilderStd.stdBuilder().makePrefixTable(location, policy, params) ;
    }

    public static NodeTable makeNodeTable(Location location) {
        return makeNodeTable(location, params) ;
    }

    public static NodeTable makeNodeTable(Location location, StoreParams params) {
        DatasetBuilderStd dbBuild = DatasetBuilderStd.stdBuilder() ;
        return dbBuild.makeNodeTable(location, params) ; 
    }
    
    public static NodeTable makeNodeTable(Location location, 
                                          String indexNode2Id, int node2NodeIdCacheSize,
                                          String indexId2Node, int nodeId2NodeCacheSize,
                                          int sizeNodeMissCacheSize) {
        StoreParamsBuilder spb = StoreParams.builder() ;
        spb.indexNode2Id(indexNode2Id).node2NodeIdCacheSize(node2NodeIdCacheSize) ;
        spb.indexId2Node(indexId2Node).nodeId2NodeCacheSize(nodeId2NodeCacheSize) ;
        DatasetBuilderStd dbBuild = DatasetBuilderStd.stdBuilder() ;
        return makeNodeTable(location, spb.build()) ; 
    }
    
    /** Choose the StoreParams.  This is the policy applied when creating or re-attaching to a database.
     * <p>
     * If the location has parameters in a {@code tdb.cfg} file, use them, as modified by any
     * application-supplied internal parameters.
     * <p>
     * Otherwise, if this is a new database, use the application provided
     * parameters or if there are no application provided 
     * parameters, use the system default parameters.
     * Write the parameters used to the location in {@code tdb.cfg}
     * <p>If this is an existing database and there are no location recorded parameters,
     * use system default parameters, modified by application parameters.   
     * <p>
     * Notes:
     * <ul>
     * <li><i>Modification</i> involves setting any of the parameters than can vary from run to run. 
     * These are the cache sizes and the file mode. 
     * <li><i>Block size</i>: it is critical that this set correctly. Silent corruption
     * of a database may occur if this is changed.  At the moment, it is not possible to provide
     * a complete check of block size.
     * <ul>  
     * <p>
     * Do not edit store parameters recorded at a location after the database has been created.
     * Only the dynamic parameters cna be safely changed. That is better done though the application
     * providing some parameters in the {@link TDBFactory} call.
     * <p>
     * This includes changing filenames,  indexing choices and block size. 
     * Otherwise, the database may be permanently and irrevocably corrupted.
     * You have been warned. 
     * 
     * @param location The place where the database is or will be.
     * @param isNew  Whether the database is being created or whether there is an existing database.
     * @param pApp   Application-provide store parameters.
     * @param pLoc   Store parameters found at the location.
     * @param pDft   System default store parameters.
     * @return       StoreParams
     * 
     * @see StoreParams
     * @see StoreParamsDynamic
     */
    static StoreParams decideStoreParams(Location location, boolean isNew, StoreParams pApp, StoreParams pLoc, StoreParams pDft) {
        StoreParams p = null ;
        if ( pLoc != null ) {
            // pLoc so use it, modify by pApp.
            // Covers new and reconnect cases.
            p = pLoc ;
            if ( pApp != null )
                p = StoreParamsBuilder.modify(pLoc, pApp) ;
            return p ;
        }
        // No pLoc.
        // Use pApp if available.  Write to location if new.
        if ( pApp != null ) {
            if ( isNew ) {
                if ( ! location.isMem() ) {
                    String filename = location.getPath(StoreParamsConst.TDB_CONFIG_FILE) ;
                    StoreParamsCodec.write(filename, pApp) ;
                }
                return pApp ;
            }
            // Not new : pLoc is implicitly pDft.
            return StoreParamsBuilder.modify(pDft, pApp) ;
        }
        // no pLoc, no pApp
        return pDft ;
    }
}
