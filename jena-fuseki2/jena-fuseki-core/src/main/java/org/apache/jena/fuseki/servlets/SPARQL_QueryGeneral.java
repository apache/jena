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

import static java.lang.String.format;

import java.util.List;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.system.GraphLoadUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RiotException;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphZero;

public class SPARQL_QueryGeneral extends ServletAction {

    // As a servlet.
    public SPARQL_QueryGeneral() {
        super(new SPARQL_QueryProc(), Fuseki.actionLog);
    }

    public static class SPARQL_QueryProc extends SPARQLQueryProcessor {
        // In order to use as much of the main SPARQL code as possible,
        // this freestanding ActionProcessor mimics being a dataset service
        // using an empty, immutable dataset

        final static int MaxTriples = 100 * 1000;

        public SPARQL_QueryProc() { super(); }

        static private DataService dSrv = emptyDataService();
        static private DataAccessPoint dap = new DataAccessPoint("SPARQL_General", dSrv);

        static DataService emptyDataService() {
            DataService dSrv = DataService.newBuilder(DatasetGraphZero.create()).build();
            dSrv.goActive();
            return dSrv;
        }

        @Override
        public void executeLifecycle(HttpAction action) {
            action.setRequest(dap, dSrv);
            super.executeLifecycle(action);
        }

        @Override
        protected void validateRequest(HttpAction action) {}

        @Override
        protected void validateQuery(HttpAction action, Query query) {}

        @Override
        protected Pair<DatasetGraph, Query> decideDataset(HttpAction action, Query query, String queryStringLog) {
            return decideDatasetProtocol(action, query, queryStringLog);
        }

        /** Decide the {@code DatasetGraph} using only the protocol and query to calculate the dataset. */
        private static Pair<DatasetGraph, Query> decideDatasetProtocol(HttpAction action, Query query, String queryStringLog) {
            DatasetDescription datasetDesc = SPARQLProtocol.getDatasetDescription(action, query);
            if ( datasetDesc == null ) {
                //ServletOps.errorBadRequest("No dataset description in protocol request or in the query string");
                return Pair.create(DatasetGraphZero.create(), query);
            }

            // These will have been taken care of by the "getDatasetDescription"
            if ( query.hasDatasetDescription() ) {
                // Don't modify input.
                query = query.cloneQuery();
                query.getNamedGraphURIs().clear();
                query.getGraphURIs().clear();
            }
            return Pair.create(datasetFromDescriptionWeb(action, datasetDesc), query);
        }

        /**
         * Construct a Dataset based on a dataset description.
         * Loads graph from the web.
         */
        private static DatasetGraph datasetFromDescriptionWeb(HttpAction action, DatasetDescription datasetDesc) {
            try {
                if ( datasetDesc == null )
                    return null;
                if ( datasetDesc.isEmpty() )
                    return null;

                List<String> graphURLs = datasetDesc.getDefaultGraphURIs();
                List<String> namedGraphs = datasetDesc.getNamedGraphURIs();

                if ( graphURLs.size() == 0 && namedGraphs.size() == 0 )
                    return null;

                Dataset dataset = DatasetFactory.create();
                // Look in cache for loaded graphs!!

                // ---- Default graph
                {
                    Model model = ModelFactory.createDefaultModel();
                    for ( String uri : graphURLs ) {
                        if ( uri == null || uri.equals("") )
                            throw new InternalErrorException("Default graph URI is null or the empty string");

                        try {
                            GraphLoadUtils.loadModel(model, uri, MaxTriples);
                            action.log.info(format("[%d] Load (default graph) %s", action.id, uri));
                        }
                        catch (RiotException ex) {
                            action.log.info(format("[%d] Parsing error loading %s: %s", action.id, uri, ex.getMessage()));
                            ServletOps.errorBadRequest("Failed to load URL (parse error) " + uri + " : " + ex.getMessage());
                        }
                        catch (Exception ex) {
                            action.log.info(format("[%d] Failed to load (default) %s: %s", action.id, uri, ex.getMessage()));
                            ServletOps.errorBadRequest("Failed to load URL " + uri);
                        }
                    }
                    dataset.setDefaultModel(model);
                }
                // ---- Named graphs
                if ( namedGraphs != null ) {
                    for ( String uri : namedGraphs ) {
                        if ( uri == null || uri.equals("") )
                            throw new InternalErrorException("Named graph URI is null or the empty string");

                        try {
                            Model model = ModelFactory.createDefaultModel();
                            GraphLoadUtils.loadModel(model, uri, MaxTriples);
                            action.log.info(format("[%d] Load (named graph) %s", action.id, uri));
                            dataset.addNamedModel(uri, model);
                        }
                        catch (RiotException ex) {
                            action.log.info(format("[%d] Parsing error loading %s: %s", action.id, uri, ex.getMessage()));
                            ServletOps.errorBadRequest("Failed to load URL (parse error) " + uri + " : " + ex.getMessage());
                        }
                        catch (Exception ex) {
                            action.log.info(format("[%d] Failed to load (named graph) %s: %s", action.id, uri, ex.getMessage()));
                            ServletOps.errorBadRequest("Failed to load URL " + uri);
                        }
                    }
                }

                return dataset.asDatasetGraph();

            }
            catch (ActionErrorException ex) {
                throw ex;
            }
            catch (Exception ex) {
                action.log.info(format("[%d] SPARQL parameter error: " + ex.getMessage(), action.id, ex));
                ServletOps.errorBadRequest("Parameter error: " + ex.getMessage());
                return null;
            }
        }
    }

}
