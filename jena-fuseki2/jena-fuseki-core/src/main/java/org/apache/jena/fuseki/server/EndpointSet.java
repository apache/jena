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

import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.fuseki.servlets.HttpAction;

/** Collection of endpoints for a dispatch point.
 * A dispatch point is a URL name, without queyr string or content-type.
 * {@linkplain Dispatcher#chooseOperation(HttpAction)} looks at request and decides which
 * {@linkplain Operation} is being requested.
 * 
 * {@linkplain Dispatcher#chooseEndpoint} looks at request,
 * and a {@linkplain DataService}
 * and decides the endpoint.
 * 
 * There can be only be one endpoint for each operation in a {@code EndpointSet}.   
 */
public class EndpointSet {
    private final String name;
    // Fast path for a set of one endpoint.
    private Endpoint single;
    private Map<Operation, Endpoint> endpoints = new ConcurrentHashMap<>();

    public EndpointSet(String name) {
        super();
        this.name = name;
        this.single = null;
    }
    
    public void put(Endpoint endpoint) {
        if ( name != null ) {
            if ( ! endpoint.getName().equals(name) )
                Log.warn(EndpointSet.class, "Different endpoint name: set = '"+name+"' : endpoint = '"+endpoint.getName()+"'");    
        } else {
            if ( ! endpoint.isUnnamed() )
                Log.warn(EndpointSet.class, "Different endpoint name: set = '' : endpoint = '"+endpoint.getName()+"'");
        }
        Operation operation = endpoint.getOperation();
        if ( endpoints.containsKey(operation) ) {
            Log.warn(EndpointSet.class, "Redefining endpoint for "+operation); 
        }
        Endpoint endpointPrev = endpoints.put(operation, endpoint);
        resetSingle();
    }
    
    public void remove(Endpoint endpoint) {
        endpoints.remove(endpoint.getOperation());
        resetSingle();
    }
    
    public Endpoint get(Operation operation) {
        return endpoints.get(operation);
    }

    public boolean contains(Operation operation) {
        return endpoints.containsKey(operation);
    }
    
    public boolean isEmpty() {
        return endpoints.isEmpty();
    }

    public int size() {
        return endpoints.size();
    }

    public void forEach(BiConsumer<Operation, Endpoint> action) { endpoints.forEach(action); }
    
    private void resetSingle() {
        if ( endpoints.size() == 1 )
            single = endpoints.values().iterator().next();
        else {
            // Set to null IFF not null. Debug point. 
            if ( single != null)
                single = null;
        }
    }

    public Collection<Endpoint> endpoints() { return endpoints.values(); }

    public Collection<Operation> operations() { return endpoints.keySet(); }
    
    /** Get the Endpoint for a singleton EndpointSet */
    public Endpoint getExactlyOne() {
        return single;
    }
    
    @Override
    public String toString() {
        String x = (name==null)?"":name;
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        operations().forEach(op->sj.add(op.toString()));
        return x+sj.toString(); 
    }
}
