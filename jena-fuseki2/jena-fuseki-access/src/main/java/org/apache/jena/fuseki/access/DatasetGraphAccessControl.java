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

package org.apache.jena.fuseki.access;

import java.util.Objects;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;

/** DatasetGraph layer that carries a SecurityRegistry. */ 
class DatasetGraphAccessControl extends DatasetGraphWrapper {
    
    private SecurityRegistry registry = null; 

    public DatasetGraphAccessControl(DatasetGraph dsg, SecurityRegistry registry) {
        super(Objects.requireNonNull(dsg));
        this.registry = Objects.requireNonNull(registry); 
    }
    
    public SecurityRegistry getRegistry() {
        return registry;
    }

    /**
     * Return the underlying {@code DatasetGraph}. If the argument is not a
     * {@code DatasetGraphAccessControl}, return the argument.
     */
    public static DatasetGraph removeWrapper(DatasetGraph dsg) {
        if ( ! ( dsg instanceof DatasetGraphAccessControl ) )
            return dsg;
        return ((DatasetGraphAccessControl)dsg).getWrapped();
    }
    
    /**
     * Return the underlying {@code DatasetGraph}. If the argument is not a
     * {@code DatasetGraphAccessControl}, return null.
     */
    public static DatasetGraph unwrapOrNull(DatasetGraph dsg) {
        if ( ! ( dsg instanceof DatasetGraphAccessControl ) )
            return null;
        return ((DatasetGraphAccessControl)dsg).getWrapped();
    }
}
