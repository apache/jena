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

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTransaction ;

public class SystemState {
    // XXX  Make part of the mgt webapp. 
    private static String SystemDatabaseLocation = "system" ;
    // Testing may reset this.
    public static Location location = new Location(SystemDatabaseLocation)  ;
    
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
    
    
    public /* for testing */ static void init$() {
        if ( initialized )
            return ;
        initialized = true ;
        if ( ! location.isMem() )
            FileOps.ensureDir(location.getDirectoryPath()) ;
        dataset = TDBFactory.createDataset(location) ;
        dsg     = (DatasetGraphTransaction)(dataset.asDatasetGraph()) ;
    }

    
    public static String PREFIXES = StrUtils.strjoinNL
        ("BASE <http://example/base#>",
         "PREFIX fu:      <http://jena.apache.org/fuseki#>",
         "PREFIX fuseki:  <http://jena.apache.org/fuseki#>",
         "PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
         "PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>",
         "PREFIX tdb:     <http://jena.hpl.hp.com/2008/tdb#>",
         "PREFIX sdb:     <http://jena.hpl.hp.com/20087/sdb#>",
         "PREFIX list:    <http://jena.hpl.hp.com/ARQ/list#>",
         "PREFIX xsd:     <http://www.w3.org/2001/XMLSchema#>",
         "PREFIX apf:     <http://jena.hpl.hp.com/ARQ/property#>",
         "PREFIX afn:     <http://jena.hpl.hp.com/ARQ/function#>",
         "") ;
}

