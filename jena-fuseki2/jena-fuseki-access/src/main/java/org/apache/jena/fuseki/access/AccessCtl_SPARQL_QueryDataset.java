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

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.fuseki.servlets.*;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.sparql.core.*;
import org.apache.jena.sparql.core.DynamicDatasets.DynamicDatasetGraph;

/** A Query {@link ActionService} that inserts a security filter on each query. */
final
public class AccessCtl_SPARQL_QueryDataset extends SPARQL_QueryDataset {
    private final Function<HttpAction, String> requestUser;

    public AccessCtl_SPARQL_QueryDataset(Function<HttpAction, String> requestUser) {
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
    protected Pair<DatasetGraph, Query> decideDataset(HttpAction action, Query query, String queryStringLog) {
        DatasetGraph dsg = action.getActiveDSG();
        if ( ! DataAccessCtl.isAccessControlled(dsg) )
            return super.decideDataset(action, query, queryStringLog);

        DatasetDescription dsDesc0 = SPARQLProtocol.getDatasetDescription(action, query);
        SecurityContext sCxt = DataAccessLib.getSecurityContext(action, dsg, requestUser);
        DatasetGraph dsg2 = dynamicDataset(action, query, dsg, dsDesc0, sCxt);
        return Pair.create(dsg2,  query);
    }

    private DatasetGraph dynamicDataset(HttpAction action, Query query, DatasetGraph dsg0, DatasetDescription dsDesc0, SecurityContext sCxt) {
        if ( dsDesc0 == null )
            return dsg0;
        if ( ! ALLOW_FROM )
            ServletOps.errorBadRequest("Use GRAPH. (FROM/FROM NAMED is not compatible with data access control.)");

        DatasetDescription dsDesc1 = DatasetDescription.create(
            mask(dsDesc0.getDefaultGraphURIs(), sCxt),
            mask(dsDesc0.getNamedGraphURIs(),   sCxt));
        if ( dsDesc1.isEmpty() )
            return DatasetGraphZero.create();

        // Fix up the union graph in the graphs if in FROM.
        // (FROM NAMED <union graph> is done by DynamicDatasets).
        if ( dsDesc1.getDefaultGraphURIs().contains(Quad.unionGraph.getURI())) {
            dsDesc1.getDefaultGraphURIs().remove(Quad.unionGraph.getURI());
            dsDesc1.getDefaultGraphURIs().addAll(sCxt.visibleGraphNames());
        }

        DatasetGraph dsg1 = DynamicDatasets.dynamicDataset(dsDesc1, dsg0, false);
        if ( query.hasDatasetDescription() ) {
             query.getGraphURIs().clear();
             query.getNamedGraphURIs().clear();
        }
        return dsg1;
    }

    // Pass only those graphURIs in the security context.
    private List<String> mask(List<String> graphURIs, SecurityContext sCxt) {
        Collection<String> names = sCxt.visibleGraphNames();
        if ( names == null )
            return graphURIs;
        return graphURIs.stream()
            .filter(gn->names.contains(gn)
                        || ( sCxt.visableDefaultGraph() && Quad.defaultGraphIRI.getURI().equals(gn))
                        || ( Quad.unionGraph.getURI().equals(gn) )
                        )
            .collect(toList());
    }

    @Override
    protected QueryExecution createQueryExecution(HttpAction action, Query query, DatasetGraph target) {
        if ( ! ALLOW_FROM ) {
            if ( target instanceof DynamicDatasetGraph )
                // Protocol query/FROM should have been caught by decideDataset
                // but specialised setups might have DynamicDatasetGraph as the base dataset.
                ServletOps.errorBadRequest("FROM/FROM NAMED is not compatible with data access control.");
        }

        // Dataset of the service, not computed by decideDataset.
        DatasetGraph dsg = action.getActiveDSG();
        if ( dsg == null )
            return super.createQueryExecution(action, query, target);
        if ( ! DataAccessCtl.isAccessControlled(dsg) )
            return super.createQueryExecution(action, query, target);

        SecurityContext sCxt = DataAccessLib.getSecurityContext(action, dsg, requestUser);
        // A QueryExecution for controlled access
        QueryExecution qExec = sCxt.createQueryExecution(action, query, target);
        return qExec;
    }
}
