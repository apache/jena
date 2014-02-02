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

import org.apache.jena.atlas.json.JsonBuilder ;
import org.apache.jena.fuseki.server.DataAccessPoint ;
import org.apache.jena.fuseki.server.DataAccessPointRegistry ;
import org.apache.jena.fuseki.server.Operation ;

/** Create a description of a service */
public class JsonDescription {
    
    static final String dsName = "ds.name" ;
    static final String dsService = "ds.services" ;
    
    static final String srvType = "srv.type" ;
    static final String srvEndpoints = "srv.endpoints" ;
    
    public static void arrayDatasets(JsonBuilder builder, DataAccessPointRegistry registry) {
        builder.startArray() ;
        for ( String ds : registry.keys() ) {
            DataAccessPoint access = DataAccessPointRegistry.get().get(ds) ;
            JsonDescription.describe(builder, access) ;
        }
        builder.finishArray() ;
    }
    
    public static void describe(JsonBuilder builder, DataAccessPoint access) {
        builder.startObject() ;
        builder.key(dsName).value(access.getName()) ;
        
        builder.key(dsService) ;
        builder.startArray() ;
        for ( Operation sRef : access.getDataService().getOperations() )
            describe(builder, sRef) ;
        builder.finishArray() ;
        builder.finishObject() ;
    }
    
    
    public static void describe(JsonBuilder builder, Operation operation) {
        builder.startObject() ;
        
        builder.key(srvType).value(operation.getName().name) ;

        //Group same operation type? 
        builder.key(srvEndpoints) ;
        builder.startArray() ;
        builder.value(operation.endpointName) ;
        builder.finishArray() ;
        
        builder.finishObject() ;
    }
    
}

