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

/** Names for all counters */ 
public enum CounterName {
    // There are generic names - apply to all services and datasets
    // and specific ones.

    
    // Total request received
    Requests("requests"),
    // .. of which some and "good" and some are "bad".
    // #"good" + #"bad" roughly equals #"requests"
    // except that the total is incremented at the start, and the outcome at the end.
    // There may also be short term consistency issues.
    RequestsGood("requests.good"),
    RequestsBad("requests.bad") ,
    
    // SPARQL Protocol - query and update - together with upload.  
    
    // Query - standard and ... 
    QueryTimeouts("query.timeouts") ,
    QueryExecErrors("query.execerrors") ,
    
    // Update - standard and ...
    UpdateExecErrors("update.execerrors"),
    
    // Upload ... standard counters
    
    // Graph Store Protocol.

    // For each HTTP method
    GSPget("gsp.get.requests") ,
    GSPgetGood("gsp.get.requests.good") ,
    GSPgetBad("gsp.get.requests.bad") ,

    GSPpost("gsp.post.requests") ,
    GSPpostGood("gsp.post.requests.good") ,
    GSPpostBad("gsp.post.requests.bad") ,

    GSPdelete("gsp.delete.requests") ,
    GSPdeleteGood("gsp.delete.requests.good") ,
    GSPdeleteBad("gsp.delete.requests.bad") ,

    GSPput("gsp.put.requests") ,
    GSPputGood("gsp.put.requests.good") ,
    GSPputBad("gsp.put.requests.bad") ,

    GSPhead("gsp.head.requests") ,
    GSPheadGood("gsp.head.requests.good") ,
    GSPheadBad("gsp.head.requests.bad") ,

    GSPpatch("gsp.patch.requests") ,
    GSPpatchGood("gsp.patch.requests.good") ,
    GSPpatchBad("gsp.patch.requests.bad") ,

    GSPoptions("gsp.options.requests") ,
    GSPoptionsGood("gsp.options.requests.good") ,
    GSPoptionsBad("gsp.options.requests.bad") ,
    
    ;
    
    private String name ;
    private CounterName(String name) { this.name = name ; }
    
}
