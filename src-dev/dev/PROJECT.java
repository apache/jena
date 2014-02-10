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
    // Backup and long-lived tasks.
    
    // Use ActionContainerItem for ActionDatasets
    // Use Action(Container)Item for ActionStats
    
    // Assembler file mgt
    
    // Tests for DataServiceDesc and configuration.
    // Other DataServiceDesc
    //  -- contains a DS and can't put it down (update/in-memory) (+file)
    //  -- TDB various
    
    // status -> two entries
    // Cleaner -> access point -> dataService -> endpoints 
    
    // --set affects the system DB! 
    
    // Backup task + polling
    //  Generalize to queries, updates, etc
    //   /dataset/operation/1234
    //   /dataset/query/1234
    
    // Create dataset - check for existing. 
    // Timeouts
    // Check variable names on DataAccessPoint,  DataService, Endpoint
    // Per HTTP operation counters
    // Per service timeouts. e.g. 
    // Per service context setting e.g. timeouts, tdb:unionDefaultGraph
    //   SPARQL_Query.setAnyTimeouts
    
    // Shiro - log failures?
    
    /* Use of:
<context-param>
  <param-name>org.apache.jena.fuseki.config</param-name>
  <param-value>{webapp}/WEB-INF/app.conf</param-value>
</context-param>
     */
    // TDB : insert a lock file?
    
    // async operations e.g. backup, stats
    
    // RDF patch
    //   /ds/patch service to apply a patch.
    // All TODO and XXX
    // Document (write/update) all protocol modes.
}

