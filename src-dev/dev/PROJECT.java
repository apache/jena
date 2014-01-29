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
    // Access point stats? HTTP stats?
    // Create dataset - check for existing. 
    // Timeouts
    // Active/offline - names of data services? 
    // Operation to have several endpoints?
    //  Need Operation and "OperationInstance" = endpoint.
    //   OperationName == Operation Type?
    // Check variable nameds on DataAccessPoint,  DataService, Operation
    // Per HTTP operation counters
    // Counters interface -> 
    //   Add to other things? e.g. DatseService
    // HTTP stats
    // Per service timeouts. e.g. 
    // Per service context setting e.g. timeouts, tdb:unionDefaultGraph
    //   SPARQL_Query.setAnyTimeouts
    
    // Testing - all in-mmeory
    //   Don't save assemblers 
    
    // Shiro - log failures
    
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

