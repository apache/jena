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

import java.util.HashMap;
import java.util.Map;

/** 
 * Names (symbols) for operations.
 * An {@code OperationName} is not related to the service name used to invoke the operation.
 * That is determined by the {@link Endpoint}. 
 */
public class OperationName {
    
    // Create intern'ed symbols. 
    static private Map<String, OperationName> registered = new HashMap<>();
    
    /**
     * Create an intern'ed {@code OperationName}. That is, if the object has already been
     * created, return the original. There is only ever one object for a given name.
     * (It is an extensible enum without subclassing).
     */
    static public OperationName register(String name) {
        return registered.computeIfAbsent(name, (n)->new OperationName(n));
    }
    
    public static OperationName Query    = register("SPARQL Query");
    public static OperationName Update   = register("SPARQL Update");
    public static OperationName Upload   = register("File Upload");
    public static OperationName GSP_RW   = register("Graph Store Protocol");
    public static OperationName GSP_R    = register("Graph Store Protocol (Read)");
    public static OperationName Quads_RW = register("HTTP Quads");
    public static OperationName Quads_R  = register("HTTP Quads (Read)");
    
    private final String name ;
    private OperationName(String name) { this.name = name ; }
    
    public String getName() { return name ; }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
    
    // Could be this == obj
    // because we intern'ed the object
    
    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        OperationName other = (OperationName)obj;
        if ( name == null ) {
            if ( other.name != null )
                return false;
        } else if ( !name.equals(other.name) )
            return false;
        return true;
    }
    
}

