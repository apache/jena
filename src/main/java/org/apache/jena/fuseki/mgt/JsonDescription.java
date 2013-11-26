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
import org.apache.jena.fuseki.server.DatasetRef ;
import org.apache.jena.fuseki.server.DatasetRegistry ;
import org.apache.jena.fuseki.server.ServiceRef ;

/** Create a description of a service */
public class JsonDescription {
    
    static final String dsName = "ds.name" ;
    static final String dsService = "ds.services" ;
    
    static final String srvType = "srv.type" ;
    static final String srvEndpoints = "srv.endpoints" ;
    
    public static void arrayDatasets(JsonBuilder builder, DatasetRegistry registry) {
        builder.startArray() ;
        for ( String ds : registry.keys() ) {
            DatasetRef desc = DatasetRegistry.get().get(ds) ;
            JsonDescription.describe(builder, desc) ;
        }
        builder.finishArray() ;
    }
    
    public static void describe(JsonBuilder builder, ServiceRef serviceref) {
        builder.startObject() ;
        
        builder.key(srvType).value(serviceref.name) ;

        builder.key(srvEndpoints) ;
        builder.startArray() ;
        for ( String ep : serviceref.endpoints )
            builder.value(ep) ;
        builder.finishArray() ;
        
        builder.finishObject() ;
    }
    
    public static void describe(JsonBuilder builder, DatasetRef ds) {
        
        builder.startObject() ;
        
        builder.key(dsName).value(ds.getName()) ;
        
        builder.key(dsService) ;
        builder.startArray() ;
        for ( ServiceRef sRef : ds.getServiceRefs() )
            describe(builder, sRef) ;
        builder.finishArray() ;
        builder.finishObject() ;
    }
}

