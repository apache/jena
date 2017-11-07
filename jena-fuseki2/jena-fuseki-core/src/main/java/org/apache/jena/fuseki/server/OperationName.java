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

/** 
 * Names (symbols) for operations.
 * An {@code OperationName} is not related to the service name used to invoke the operation.
 * That is determined by the {@link Endpoint}. 
 */
public class OperationName {
    
    // Create intern'ed symbols. 
    static private NameMgr<OperationName> mgr = new NameMgr<>(); 
    static public OperationName register(String name, String description) { return mgr.register(name, (x)->new OperationName(x, description)); }
    
    public static final OperationName Query    = register("Query", "SPARQL Query");
    public static final OperationName Update   = register("Update", "SPARQL Update");
    public static final OperationName Upload   = register("Upload", "File Upload");
    public static final OperationName GSP_RW   = register("GSP_RW", "Graph Store Protocol");
    public static final OperationName GSP_R    = register("GSP_R", "Graph Store Protocol (Read)");
    public static final OperationName Quads_RW = register("Quads_RW", "HTTP Quads");
    public static final OperationName Quads_R  = register("Quads_R", "HTTP Quads (Read)");
    
    private final String description ;
    private final String name ;

    private OperationName(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
    
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

