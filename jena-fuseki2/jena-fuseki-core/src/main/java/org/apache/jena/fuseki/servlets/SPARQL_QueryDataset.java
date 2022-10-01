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

package org.apache.jena.fuseki.servlets;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DynamicDatasets;

public class SPARQL_QueryDataset extends SPARQLQueryProcessor {

    public SPARQL_QueryDataset() { }

    @Override
    protected void validateRequest(HttpAction action) { }

    @Override
    protected void validateQuery(HttpAction action, Query query) { }

    @Override
    protected Pair<DatasetGraph, Query> decideDataset(HttpAction action, Query query, String queryStringLog) {
        return decideDatasetDynamic(action, query, queryStringLog);
    }

    /**
     * Function to return the {@code Pair<DatasetGraph, Query>} based on processing any dataset description as {@link DynamicDatasets a dynamic dataset}.
     * The query is modified to remove any dataset description.
     *
     * @param action
     * @param query
     * @param queryStringLog
     * @return Pair&lt;DatasetGraph, Query&gt;
     */
    public Pair<DatasetGraph, Query> decideDatasetDynamic(HttpAction action, Query query, String queryStringLog) {
        DatasetGraph dsg = getDataset(action);
        DatasetDescription dsDesc = SPARQLProtocol.getDatasetDescription(action, query);
        Query query2 = query;
        if ( dsDesc != null ) {
            dsg = DynamicDatasets.dynamicDataset(dsDesc, dsg, false);
            if ( query.hasDatasetDescription() ) {
                query2 = query2.cloneQuery();
                query2.getGraphURIs().clear();
                query2.getNamedGraphURIs().clear();
            }
        }
        return Pair.create(dsg, query2);
    }

    protected DatasetGraph getDataset(HttpAction action) {
        return action.getActiveDSG();
    }
}
