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

package org.apache.jena.fuseki.mgt;

/** Various constants used in the admin functions */ 
public class MgtConst {
    public static final String  opDump          = "dump" ;  
    public static final String  opPing          = "ping" ;
    
    public static final String  opStats         = "stats" ;  
    public static final String  opDatasets      = "datasets" ;
    public static final String  opListBackups   = "backups-list" ;
    public static final String  opServer        = "server" ;
    
    // JSON constants
    public static final String datasets         = "datasets" ;
    public static final String uptime           = "uptime" ;
    public static final String startDT          = "startDateTime" ;
    public static final String server           = "server" ;
    public static final String port             = "port" ;
    public static final String hostname         = "hostname" ;
    public static final String admin            = "admin" ;
    public static final String version          = "version" ;
    public static final String built            = "built" ;
    public static final String services         = "services" ;
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

