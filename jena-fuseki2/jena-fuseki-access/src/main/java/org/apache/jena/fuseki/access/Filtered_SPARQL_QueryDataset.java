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
import java.util.List;
import java.util.function.Function;
import static java.util.stream.Collectors.toList;

import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.fuseki.servlets.SPARQL_QueryDataset;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphZero;
import org.apache.jena.sparql.core.DynamicDatasets;
import org.apache.jena.sparql.core.DynamicDatasets.DynamicDatasetGraph;

/** A Query {@link ActionService} that inserts a security filter on each query. */
final
public class Filtered_SPARQL_QueryDataset extends SPARQL_QueryDataset {
    private final Function<HttpAction, String> requestUser;

    public Filtered_SPARQL_QueryDataset(Function<HttpAction, String> requestUser) {
        this.requestUser = requestUser; 
    }

    private static boolean ALLOW_FROM = true; 
    
    @Override
    protected Collection<String> customParams() {
        // The additional ?user.
        return Collections.singletonList("user");
    }
    
    /** Decide the dataset - this modifies the query
     *  If the query has a dataset description.
     */
    @Override
    protected DatasetGraph decideDataset(HttpAction action, Query query, String queryStringLog) {
        DatasetGraph dsg = action.getActiveDSG() ;
        DatasetDescription dsDesc = getDatasetDescription(action, query) ;
        if ( dsDesc == null )
            return dsg;
        if ( ! ALLOW_FROM )
            ServletOps.errorBadRequest("Use GRAPH. (FROM/FROM NAMED is not compatible with data access control.)");
        
        DatasetDescription dsDesc0 = getDatasetDescription(action, query);
        if ( dsDesc0 == null )
            return dsg;
        // Filter the DatasetDescription by the SecurityContext
        SecurityContext sCxt = DataAccessLib.getSecurityContext(action, dsg, requestUser);
        DatasetDescription dsDesc1 = DatasetDescription.create(
            mask(dsDesc0.getDefaultGraphURIs(), sCxt),
            mask(dsDesc0.getNamedGraphURIs(),   sCxt));
        // dsDesc1 != null.
        if ( dsDesc1.isEmpty() )
            return DatasetGraphZero.create();
        
        dsg = DynamicDatasets.dynamicDataset(dsDesc1, dsg, false) ;
        if ( query.hasDatasetDescription() ) {
            query.getGraphURIs().clear() ;
            query.getNamedGraphURIs().clear() ;
        }
        return dsg ;
    }

    // Pass only those graphURIs in the security context.
    private List<String> mask(List<String> graphURIs, SecurityContext sCxt) {
        Collection<String> names = sCxt.visibleGraphNames();
        return graphURIs.stream()
            .filter(gn->names.contains(gn))
            .collect(toList()) ;
    }

    @Override
    protected QueryExecution createQueryExecution(HttpAction action, Query query, DatasetGraph target) {
        if ( target instanceof DynamicDatasetGraph ) {
            // Protocol query/FROM should have been caught by decideDataset
            // but specialised setups might have DynamicDatasetGraph as the base dataset.
            if ( ! ALLOW_FROM )
                ServletOps.errorBadRequest("FROM/FROM NAMED is not compatible with data access control.");
        }
        
        // Database defined for this service, not the possibly dynamically built "dataset"
        DatasetGraph dsg = action.getDataset();
        if ( dsg == null )
            return super.createQueryExecution(action, query, target);
        if ( ! DataAccessCtl.isAccessControlled(dsg) )
            return super.createQueryExecution(action, query, target);

        SecurityContext sCxt = DataAccessLib.getSecurityContext(action, dsg, requestUser);
        // A QueryExecution for controlled access
        QueryExecution qExec = sCxt.createQueryExecution(query, target);
        return qExec;
    }
}

