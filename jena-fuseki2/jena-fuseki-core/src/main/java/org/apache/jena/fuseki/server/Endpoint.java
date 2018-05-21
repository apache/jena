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

import org.apache.jena.atlas.lib.InternalErrorException ;

public class Endpoint implements Counters {
    
    public final Operation operation ;
    public final String endpointName ;
    // Endpoint-level counters.
    private final CounterSet counters           = new CounterSet() ;

    public Endpoint(Operation operation, String endpointName) {
        this.operation = operation ;
        if ( operation == null )
            throw new InternalErrorException("operation is null") ;
        this.endpointName = endpointName ;
        // Standard counters - there may be others
        counters.add(CounterName.Requests) ;
        counters.add(CounterName.RequestsGood) ;
        counters.add(CounterName.RequestsBad) ;
    }

    @Override
    public  CounterSet getCounters()    { return counters ; }

    //@Override
    public Operation getOperation()     { return operation ; }
    
    //@Override
    public boolean isType(Operation operation) { 
        return operation.equals(operation) ;
    }

    public String getEndpoint()         { return endpointName ; }
    
    //@Override 
    public long getRequests() { 
        return counters.value(CounterName.Requests) ;
    }
    //@Override
    public long getRequestsGood() {
        return counters.value(CounterName.RequestsGood) ;
    }
    //@Override
    public long getRequestsBad() {
        return counters.value(CounterName.RequestsBad) ;
    }

}

