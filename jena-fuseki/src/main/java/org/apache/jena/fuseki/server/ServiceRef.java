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

import java.util.ArrayList ;
import java.util.List ;

/** Configuration of an individual service */
public class ServiceRef implements ServiceMXBean, Counters
{
    public final String name ;
    
    // Service-level counters.
    private final CounterSet counters           = new CounterSet() ;
    @Override
    public  CounterSet getCounters() { return counters ; }

    /** Endpoints (as absolute path URLs) */
    public List<String> endpoints               = new ArrayList<String>() ;
    
    // Attach counters to services or datasets 
    // Can we have a counter of the same name on different services?
    // Cost : number of maps.
    // +ve: Multiple services with the same name counter
    
    public ServiceRef(String serviceName) {
        this.name = serviceName ;
    }
    
    public boolean isActive() { return endpoints.isEmpty() ; }

    @Override
    public String getName()     { return name ; }

    @Override public long getRequests() { 
        return counters.value(CounterName.Requests) ;
    }
    @Override
    public long getRequestsGood() {
        return counters.value(CounterName.RequestsGood) ;
    }
    @Override
    public long getRequestsBad() {
        return counters.value(CounterName.RequestsBad) ;
    }
}

