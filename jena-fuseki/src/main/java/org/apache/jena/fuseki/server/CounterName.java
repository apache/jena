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
    // Per datasset
    DatasetRequests("requests"),
    DatasetRequestsGood("requests.good"),
    DatasetRequestsBad("requests.bad") ,
    
    // Service specific
    QueryRequests("query.requests") ,  
    QueryRequestsGood("query.requests.good") ,
    QueryRequestsBad("query.requests.bad") ,
    QueryTimeouts("query.timeouts") ,
    QueryExecErrors("query.execerrors") ,
    
    UpdateRequests("update.requests") ,
    UpdateRequestsGood("update.requests.good") ,
    UpdateRequestsBad("update.requests.bsd") ,
    UpdateExecErrors("update.execerrors"),
    
    GSPrequests("gsp.requests") ,
    GSPrequestsGood("gsp.requests.good") ,
    GSPrequestsBad("gsp.requests.bad") ,

    GSPget("gsp.requests.get") ,
    GSPgetGood("gsp.requests.get.good") ,
    GSPgetBad("gsp.requests.get.bad") ,

    GSPpost("gsp.requests.post") ,
    GSPpostGood("gsp.requests.post.good") ,
    GSPpostBad("gsp.requests.post.bad") ,
    
    GSPdelete("gsp.requests.delete") ,
    GSPdeleteGood("gsp.requests.delete.good") ,
    GSPdeleteBad("gsp.requests.delete.bad") ,
    
    GSPput("gsp.requests.put") ,
    GSPputGood("gsp.requests.put.good") ,
    GSPputBad("gsp.requests.put.bad") ,
    
    GSPhead("gsp.requests.head") ,
    GSPheadGood("gsp.requests.head.good") ,
    GSPheadBad("gsp.requests.head.bad") ,
    
    GSPpatch("gsp.requests.patch") ,
    GSPpatchGood("gsp.requests.patch.good") ,
    GSPpatchBad("gsp.requests.patch.bad") ,
    
    UploadRequests("upload.requests") ,
    UploadRequestsGood("upload.requests.good") ,
    UploadRequestsBad("upload.requests.bad") ,
    
    ;
    
    private String name ;
    private CounterName(String name) { this.name = name ; }
    
}
