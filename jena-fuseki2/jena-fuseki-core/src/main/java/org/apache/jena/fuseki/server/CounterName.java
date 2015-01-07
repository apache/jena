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
    // There are generic names - apply to all services and datasets - and
    // also specific ones that relate only to a particular kind of service.
    
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
    QueryIOErrors("query.ioerrors") ,
    
    // Update - standard and ...
    UpdateExecErrors("update.execerrors"),
    
    // Upload ... standard counters
    
    // Graph Store Protocol. uses HTTP codes.

    // For each HTTP method

    HTTPget("http.get.requests") ,
    HTTPgetGood("http.get.requests.good") ,
    HTTPGetBad("http.get.requests.bad") ,

    HTTPpost("http.post.requests") ,
    HTTPpostGood("http.post.requests.good") ,
    HTTPpostBad("http.post.requests.bad") ,

    HTTPdelete("http.delete.requests") ,
    HTTPdeleteGood("http.delete.requests.good") ,
    HTTPdeleteBad("http.delete.requests.bad") ,

    HTTPput("http.put.requests") ,
    HTTPputGood("http.put.requests.good") ,
    HTTPputBad("http.put.requests.bad") ,

    HTTPhead("http.head.requests") ,
    HTTPheadGood("http.head.requests.good") ,
    HTTPheadBad("http.head.requests.bad") ,

    HTTPpatch("http.patch.requests") ,
    HTTPpatchGood("http.patch.requests.good") ,
    HTTPpatchBad("http.patch.requests.bad") ,

    HTTPoptions("http.options.requests") ,
    HTTPoptionsGood("http.options.requests.good") ,
    HTTPoptionsBad("http.options.requests.bad") ,
    
    ;
    
    public final String name ;
    private CounterName(String name) { this.name = name ; }
    
}
