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
    // All TODO and XXX
    // Graph upload in ActionDatasets == Upload.

    // txn and nonTxn server set up for tests.
    
    // Enables, disable R/RW on SPARQL_UberServlet(renamed) and quads operations.
    // Need a switch on whether quadding is support and whether it's RW or not.
    
    // Remove all direct naming (put in separate servlet if ever needed)  
    //   last place : SPARQL_UberServlet
    
    // Document (write/update) all protocol modes.
    // TESTS
    
    // JENA-201 - WAR Fuseki.
    //   WEB.xml
    //   ContextPath in uber dispatch.
    //   FusekiServletContextListener
    
    // SPARQLServer.start kicks FusekiServletContextListener which could then do the main initialization.
    
    // The whole X_Config thing
    // Check compression enabled for UberServlet
    
    // In-memory system state.
}

