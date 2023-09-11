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

package org.apache.jena.update;

import java.util.Objects;

import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.exec.http.UpdateExecutionHTTP;
import org.apache.jena.sparql.exec.http.UpdateExecutionHTTPBuilder;
import org.apache.jena.sparql.exec.http.UpdateSendMode;
import org.apache.jena.sparql.util.Context ;

/**
 * Create {@link UpdateExecution} execution objects.
 * <p>
 * For more control of building a local or remote {@link UpdateExecution} object see the builder pattern:
 * <ul>
 * <li>{@code UpdateExecution.create(). ... .build()} for querying local data.</li>
 * <li>{@code UpdateProcessorHTTP.service(url). ... .build()} for querying a remote store using HTTP.</li>
 * </ul>
 * <p>
 * See also {@code RDFConnection} for working with SPARQL Query, SPARQL Update and SPARQL Graph Store Protocol together.
 *
 * @see UpdateExecutionDatasetBuilder
 * @see UpdateExecutionHTTPBuilder
 */
public class UpdateExecutionFactory
{
    /**
     * Create an UpdateExecution appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateExecution
     *
     * @param update
     * @param dataset
     * @return UpdateExecution
     */
    public static UpdateExecution create(Update update, Dataset dataset) {
        return create(new UpdateRequest(update), dataset);
    }

    /**
     * Create an UpdateExecution appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateExecution
     *
     * @param updateRequest
     * @param dataset
     * @return UpdateExecution
     */
    public static UpdateExecution create(UpdateRequest updateRequest, Dataset dataset) {
        return make(updateRequest, dataset, null);
    }

    /**
     * Create an UpdateExecution appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateExecution
     *
     * @param updateRequest
     * @param dataset
     * @param context (null means use merge of global and graph store context))
     * @return UpdateExecution
     */
    public static UpdateExecution create(UpdateRequest updateRequest, Dataset dataset, Context context) {
        return make(updateRequest, dataset, context);
    }

    // Everything for local updates comes through one of these two make methods
    private static UpdateExecution make(UpdateRequest updateRequest, Dataset dataset, Context context) {
        return UpdateExecution.dataset(dataset).update(updateRequest).build();
    }

    /**
     * Create an UpdateExecution appropriate to the datasetGraph, or null if no
     * available factory to make an UpdateExecution
     *
     * @param dataset
     * @return UpdateExecution
     */
    public static UpdateProcessorStreaming createStreaming(Dataset dataset) {
        return UpdateStreaming.makeStreaming(dataset.asDatasetGraph(), null, null);
    }

    /**
     * Create an UpdateExecution that sends the update to a remote SPARQL Update
     * service.
     *
     * @param update Updates
     * @param remoteEndpoint Endpoint URL
     * @return Remote Update processor
     */
    public static UpdateExecution createRemote(Update update, String remoteEndpoint) {
        return makeRemote(new UpdateRequest(update), remoteEndpoint, UpdateSendMode.asPost);
    }

    /**
     * Create an UpdateExecution that sends the update request to a remote SPARQL
     * Update service.
     *
     * @param updateRequest Updates
     * @param remoteEndpoint Endpoint URL
     * @return Remote Update processor
     */
    public static UpdateExecution createRemote(UpdateRequest updateRequest, String remoteEndpoint) {
        return makeRemote(updateRequest, remoteEndpoint, UpdateSendMode.asPost);
    }

    /**
     * Create an UpdateExecution that sends the update to a remote SPARQL
     * Update service using an HTML form.  Using an HTML form should be avoided unless the
     * endpoint does not offer POST application/sparql-update.
     *
     * @param update Updates
     * @param remoteEndpoint Endpoint URL
     * @return Remote Update processor
     */
    public static UpdateExecution createRemoteForm(Update update, String remoteEndpoint) {
        return makeRemote(new UpdateRequest(update), remoteEndpoint, UpdateSendMode.asPostForm);
    }

    /**
     * Create an UpdateExecution that sends the update request to a remote SPARQL
     * Update service using an HTML form.  Using an HTML form should be avoided unless the
     * endpoint does not offer POST application/sparql-update.
     *
     * @param updateRequest Updates
     * @param remoteEndpoint Endpoint URL
     * @return Remote Update processor
     */
    public static UpdateExecution createRemoteForm(UpdateRequest updateRequest, String remoteEndpoint) {
        return makeRemote(updateRequest, remoteEndpoint, UpdateSendMode.asPostForm);
    }

    /**
     * Create an UpdateExecution that sends the update request to a remote SPARQL
     * Update service using an HTML form
     *
     * @param updateRequest Updates
     * @param remoteEndpoint Endpoint URL
     * @param context Context
     * @return Remote Update processor
     * Use {@code UpdateExecution.service(remoteEndpoint)...build();}
     */
    private static UpdateExecution makeRemote(UpdateRequest updateRequest, String remoteEndpoint, UpdateSendMode updateSendMode) {
        Objects.requireNonNull(updateRequest, "updateRequest");
        Objects.requireNonNull(remoteEndpoint, "remoteEndpoint");
        Objects.requireNonNull(updateSendMode, "updateSendMode");
        return UpdateExecutionHTTP.create()
                .endpoint(remoteEndpoint)
                .update(updateRequest)
                .sendMode(updateSendMode)
                .build();
    }

}
