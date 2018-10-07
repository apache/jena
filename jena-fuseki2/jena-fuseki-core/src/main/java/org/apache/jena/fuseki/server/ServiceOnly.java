/*
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

import org.apache.jena.fuseki.DEF;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphSink;

/** Configuration for a "no dataset" service */
public class ServiceOnly {
    
    public static DataService dataService() {
        return serviceOnlyDataService; 
    }
    
    public static DataAccessPoint dataAccessPoint() {
        return null;
    }

    private static final DataService serviceOnlyDataService;
    static {
        DatasetGraph dsg = new DatasetGraphSink();
        serviceOnlyDataService = new DataService(dsg);
        serviceOnlyDataService.addEndpoint(Operation.Query, DEF.ServiceQuery);
        serviceOnlyDataService.addEndpoint(Operation.Query, DEF.ServiceQueryAlt);
    }
    private static final DataAccessPoint serviceOnlyDataAccessPoint = new DataAccessPoint("", serviceOnlyDataService);
}
