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

import java.util.function.Function;

import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.sparql.core.DatasetGraph;

/** Package-only operations */
class DataAccessLib {
    
    /** Determine the {@link SecurityPolicy} for this request */  
    static SecurityPolicy getSecurityPolicy(HttpAction action, DatasetGraph dataset, Function<HttpAction, String> requestUser) {
        SecurityRegistry registry = getSecurityRegistry(action, dataset);
        if ( registry == null )
            ServletOps.errorOccurred("Internal Server Error");

        SecurityPolicy sCxt = null;
        String user = requestUser.apply(action);
        sCxt = registry.get(user);
        if ( sCxt == null )
            sCxt = noSecurityPolicy();
        return sCxt;
    }
    
    /** Get the {@link SecurityRegistry} for an action/query/dataset */
    static SecurityRegistry getSecurityRegistry(HttpAction action, DatasetGraph dsg) {
        if ( dsg instanceof DatasetGraphAccessControl )
            return ((DatasetGraphAccessControl)dsg).getRegistry();
        return dsg.getContext().get(DataAccessCtl.symSecurityRegistry);
    }

    static SecurityPolicy noSecurityPolicy() {
        ServletOps.errorForbidden();
        // Should not get here.
        throw new InternalError();
    }
    
    static DatasetGraph decideDataset(HttpAction action, Function<HttpAction, String> requestUser) {
        DatasetGraph dsg = action.getDataset();
        if ( dsg == null )
            return dsg;//super.actOn(action);
        if ( ! DataAccessCtl.isAccessControlled(dsg) )
            // Not access controlled.
            return dsg;//super.actOn(action);
        SecurityPolicy sCxt = DataAccessLib.getSecurityPolicy(action, dsg, requestUser);
        dsg = DatasetGraphAccessControl.removeWrapper(dsg);
        dsg = DataAccessCtl.filteredDataset(dsg, sCxt);
        return dsg;
    }
}

