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

package org.apache.jena.fuseki.server;

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.tdb.StoreConnection ;
import org.apache.jena.tdb.TDB ;
import org.apache.jena.tdb.TDBFactory ;
import org.apache.jena.tdb.base.block.FileMode ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.setup.StoreParams ;
import org.apache.jena.tdb.transaction.DatasetGraphTransaction ;

public class SystemState {
    private static String SystemDatabaseLocation ;
    // Testing may reset this.
    public static Location location ; 
    
    private  static Dataset                 dataset   = null ;
    private  static DatasetGraphTransaction dsg       = null ;
    
    public static Dataset getDataset() {
        init() ;
        return dataset ;
    }
    
    public static DatasetGraphTransaction getDatasetGraph() {
        init() ;
        return dsg ;
    }
    
    private static boolean initialized = false ; 
    private static void init() {
        init$() ;
    }
    
    /** Small footprint database.  The system database records the server state.
     * It should not be performance critical, mainly being used for system admin
     * functions.
     * <p>Direct mode so that it is not competing for OS file cache space.
     * <p>Small caches - 
     */
    private static final StoreParams systemDatabaseParams = StoreParams.builder()
        .fileMode(FileMode.direct)
        .blockSize(1024)
        .blockReadCacheSize(50)
        .blockWriteCacheSize(20)
        .node2NodeIdCacheSize(500)
        .nodeId2NodeCacheSize(500)
        .nodeMissCacheSize(100)
        .build() ;
    
    public /* for testing */ static void init$() {
        if ( initialized )
            return ;
        initialized = true ;
        
        if ( location == null )
            location = Location.create(FusekiSystem.dirSystemDatabase.toString()) ;
        
        if ( ! location.isMem() )
            FileOps.ensureDir(location.getDirectoryPath()) ;
        
        // Force it into the store connection as a low footprint
        if ( StoreConnection.getExisting(location) != null )
            Fuseki.serverLog.warn("System database already in the StoreConnection cache") ;
        StoreConnection.make(location, systemDatabaseParams) ;
        
        dataset = TDBFactory.createDataset(location) ;
        dsg     = (DatasetGraphTransaction)(dataset.asDatasetGraph()) ;
        dsg.getContext().set(TDB.symUnionDefaultGraph, false) ;
    }
    
    public static String PREFIXES = StrUtils.strjoinNL
        ("BASE            <http://example/base#>",
         "PREFIX ja:      <http://jena.hpl.hp.com/2005/11/Assembler#>",
         "PREFIX fu:      <http://jena.apache.org/fuseki#>",
         "PREFIX fuseki:  <http://jena.apache.org/fuseki#>",
         "PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
         "PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>",
         "PREFIX tdb:     <http://jena.hpl.hp.com/2008/tdb#>",
         "PREFIX sdb:     <http://jena.hpl.hp.com/2007/sdb#>",
         "PREFIX list:    <http://jena.apache.org/ARQ/list#>",
         "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>",
         "PREFIX apf:     <http://jena.apache.org/ARQ/property#>",
         "PREFIX afn:     <http://jena.apache.org/ARQ/function#>",
         "") ;
}

