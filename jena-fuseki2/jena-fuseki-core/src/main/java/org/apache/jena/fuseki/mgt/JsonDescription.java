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

import java.util.List ;

import org.apache.jena.atlas.json.JsonBuilder ;
import org.apache.jena.fuseki.server.DataAccessPoint ;
import org.apache.jena.fuseki.server.DataAccessPointRegistry ;
import org.apache.jena.fuseki.server.Endpoint ;
import org.apache.jena.fuseki.server.Operation ;

/** Create a description of a service */
public class JsonDescription {
    
    public static void arrayDatasets(JsonBuilder builder, DataAccessPointRegistry registry) {
        builder.startArray() ;
        for ( String ds : registry.keys() ) {
            DataAccessPoint access = registry.get(ds) ;
            JsonDescription.describe(builder, access) ;
        }
        builder.finishArray() ;
    }
    
    public static void describe(JsonBuilder builder, DataAccessPoint access) {
        builder.startObject() ;
        builder.key(MgtConst.dsName).value(access.getName()) ;
        
        builder.key(MgtConst.dsState).value(access.getDataService().isAcceptingRequests()) ;
        
        builder.key(MgtConst.dsService) ;
        builder.startArray() ;
        
        for ( Operation operation : access.getDataService().getOperations() ) {
            List<Endpoint> endpoints = access.getDataService().getEndpoints(operation) ;
            describe(builder, operation, endpoints) ;
        }
        builder.finishArray() ;
        builder.finishObject() ;
    }
    
    private static void describe(JsonBuilder builder, Operation operation, List<Endpoint> endpoints) {
        builder.startObject() ;
        
        builder.key(MgtConst.srvType).value(operation.getName()) ;
        builder.key(MgtConst.srvDescription).value(operation.getDescription()) ;
        builder.key(MgtConst.srvEndpoints) ;
        builder.startArray() ;
        for ( Endpoint endpoint : endpoints )
            builder.value(endpoint.getEndpoint()) ;
        builder.finishArray() ;

        builder.finishObject() ;
    }
}

