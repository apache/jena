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
import java.util.function.Predicate;

import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.SPARQL_GSP_R;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

public class Filtered_SPARQL_GSP_R extends SPARQL_GSP_R {
    
    private final Function<HttpAction, String> requestUser;

    public Filtered_SPARQL_GSP_R(Function<HttpAction, String> determineUser) {
        this.requestUser = determineUser;
    }

    // Where? SPARQL_GSP_R < SPARQL_GSP < ActionREST < ActionService
    @Override
    protected DatasetGraph actOn(HttpAction action) {
        DatasetGraph dsg = action.getDataset();
        if ( dsg == null )
            return dsg;//super.actOn(action);
        if ( ! DataAccessCtl.isAccessControlled(dsg) )
            // Not access controlled.
            return dsg;//super.actOn(action);
        SecurityPolicy sCxt = DataAccessLib.getSecurityPolicy(action, dsg, requestUser);
        dsg = DatasetGraphAccessControl.unwrap(dsg);
        Predicate<Quad> filter = sCxt.predicateQuad();
        dsg = new DatasetGraphFiltered(dsg, filter, sCxt.visibleGraphs());
        return dsg;
    }
}
