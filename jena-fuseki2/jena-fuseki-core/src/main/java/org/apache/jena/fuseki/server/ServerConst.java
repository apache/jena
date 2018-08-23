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

/** Various constants used in the API functions and JSON responses. */ 
public class ServerConst {
    // Location under /$/
    public static final String  opDump          = "dump" ;  
    public static final String  opPing          = "ping" ;
    public static final String  opStats         = "stats" ;
    
//    // JSON constants
    public static final String datasets         = "datasets" ;
    public static final String operation        = "operation" ;
    public static final String description      = "description" ;
    public static final String endpoints        = "endpoints" ;

    public static final String dsName           = "ds.name" ;
    public static final String dsState          = "ds.state" ;
    public static final String dsService        = "ds.services" ;
    public static final String srvType          = "srv.type" ;
    public static final String srvDescription   = "srv.description" ;
    public static final String srvEndpoints     = "srv.endpoints" ;

}
