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

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.SPARQL_QueryDataset;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.DatasetGraph;

/** A Query {@link ActionService} that inserts a security filter on each query. */
final
public class Filtered_SPARQL_QueryDataset extends SPARQL_QueryDataset {
    private final Function<HttpAction, String> requestUser;

    public Filtered_SPARQL_QueryDataset(Function<HttpAction, String> requestUser) {
        this.requestUser = requestUser; 
    }

    @Override
    protected Collection<String> customParams() {
        // The additional ?user.
        return Collections.singletonList("user");
    }
    
    @Override
    protected QueryExecution createQueryExecution(HttpAction action, Query query, Dataset dataset) {
        // Server database, not the possibly dynamically built "dataset"
        DatasetGraph dsg = action.getDataset();
        if ( dsg == null )
            return super.createQueryExecution(action, query, dataset);
        if ( ! DataAccessCtl.isAccessControlled(dsg) )
            return super.createQueryExecution(action, query, dataset);

        SecurityContext sCxt = DataAccessLib.getSecurityContext(action, dataset.asDatasetGraph(), requestUser);
        // A QueryExecution for controlled access
        QueryExecution qExec = sCxt.createQueryExecution(query, dsg);
        return qExec;
    }
}

