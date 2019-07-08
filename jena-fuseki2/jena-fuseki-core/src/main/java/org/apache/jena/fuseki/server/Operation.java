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

import org.apache.jena.fuseki.servlets.OperationRegistry;

/**
 * Operations are symbol to look up in the {@link OperationRegistry#operationToHandler} map. The name
 * of an {@code Operation} is not related to the service name used to invoke the operation
 * which is determined by the {@link Endpoint}.
 */
public class Operation {

    /** Create/intern. */
    static private NameMgr<Operation> mgr = new NameMgr<>();
    static public Operation register(String name, String description) {
        return mgr.register(name, (x)->create(x, description));
    }

    /** Create; not registered */
    static private Operation create(String name, String description) {
        return new Operation(name, description);
    }

    public static final Operation Query          = register("Query", "SPARQL Query");
    public static final Operation Update         = register("Update", "SPARQL Update");
    public static final Operation Upload         = register("Upload", "File Upload");
    public static final Operation Patch          = register("Patch", "RDF Patch");
    public static final Operation GSP_R          = register("GSP_R", "Graph Store Protocol (Read)");
    public static final Operation GSP_RW         = register("GSP_RW", "Graph Store Protocol");

    private final String description;
    private final String name;

    private Operation(String name, String description) {
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
        Operation other = (Operation)obj;
        if ( name == null ) {
            if ( other.name != null )
                return false;
        } else if ( !name.equals(other.name) )
            return false;
        return true;
    }

    @Override
    public String toString() {
        return name;
    }
}

