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

package dev;

public class PROJECT {
    // Backup
    
    // Create dataset - check for existing. 
    
    
//    Add DS1
//    Add DS2
//    Add symbolic link
//    Offline DS2
//
//    ==>
//      check offlining works.
//      how do we manage assembler descriptions?
//        System database is only status + link to assembler?
    
    // Naming 
    //  fuseki:Service is the endpoints = dataset
    // DataService : 
    
    // Shiro - log failures
    
    // DatasetRef -> DataService 
    // DataService has "target"
    
    // DSG_Switchable does not work : need to access underlying DB by name.
    //  ==>
    //    [] a fuseki:Service, fuseki:Switchable ;
    //        fuseki:name1 "" ;
    //        fuseki:name2 "" ;
    //        .
    
    // Assembler for switchable?
    //   Still need to be able to contact each subDSG for update?
    //   OR assume just active/offline?
    
    // DatasetGraphSwitchable(dsg1, dsg2) + assembler.
    //   NB The need to query the right DSG 
    
    // Stats need to chase down links?
    
    // Chase down DatasetRef.getDataset and split into "for action" (follows link) and not
    // Where do the stats go?
    // If we want 
    
    /* Use of:
<context-param>
  <param-name>org.apache.jena.fuseki.config</param-name>
  <param-value>{webapp}/WEB-INF/app.conf</param-value>
</context-param>
     */
    // TDB : insert a lock file?
    
    // Test for servlet context
    
    // async operations e.g. backup, stats
    
    // RDF patch
    //   /ds/patch service to apply a patch.
    // "This DSG is that DSG + wrapper"
    
    // TDB and server configuration parameters.
    //   per database -> assembler.
    
    // WebContent tidying
    //   DEFs
    
    // Webapp-ization
    //   Shiro - security.
    //   Need server wide configuration - ARQ context setting. 
    //   ContextPath
    //   Fuseki.configure/defaultConfiguration
    
    // All TODO and XXX
    // Configuration and startup.
    // Change to using a real webapp.
    
    // txn and nonTxn server set up for tests.
    
    // Enables, disable R/RW on SPARQL_UberServlet(renamed) and quads operations.
    // Need a switch on whether quadding is support and whether it's RW or not.
    
    // Document (write/update) all protocol modes.
    
    // Check compression enabled for UberServlet
}

