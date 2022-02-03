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

import java.util.function.Function;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.fuseki.system.DataUploader;
import org.apache.jena.fuseki.system.FusekiNetLib;
import org.apache.jena.fuseki.system.UploadDetails;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.shared.OperationDeniedException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;

/**
 * Upload files to a server.
 * <p>
 * Supports:
 * <ul>
 * <li>HTTP body + content type(c.f GSP- write to a dataset)
 * <li>HTML file input upload (multipart/form-data)
 * </ul>
 * Using GSP style (Content-type + data in the HTTP body) is preferred.
 * @see DataUploader
 */
public class Upload extends ActionREST {

    // Only support PUT and POST
    @Override protected void doGet(HttpAction action)       { unsupported(action); }
    @Override protected void doHead(HttpAction action)      { unsupported(action); }
    @Override protected void doDelete(HttpAction action)    { unsupported(action); }
    @Override protected void doPatch(HttpAction action)     { unsupported(action); }

    private void unsupported(HttpAction action) {
        ServletOps.errorMethodNotAllowed(action.getMethod());
    }

    @Override
    public void validate(HttpAction action) {}

    @Override
    public void doOptions(HttpAction action) {
        ActionLib.setCommonHeadersForOptions(action);
        action.setResponseHeader(HttpNames.hAllow, "OPTIONS,PUT,POST");
        ServletOps.success(action);
    }

    @Override
    protected void doPut(HttpAction action) {
        execPutPost(action, true);
    }

    @Override
    protected void doPost(HttpAction action) {
        execPutPost(action, false);
    }

    private void execPutPost(HttpAction action, boolean replaceOperation) {
        ContentType ct = ActionLib.getContentType(action);
        if ( ct == null )
            ServletOps.errorBadRequest("No Content-Type:");

        if ( !action.getDataService().allowUpdate() )
            ServletOps.errorMethodNotAllowed(action.getMethod());

        // "multipart/form-data" is supported.
//        if ( matchContentType(ctMultipartFormData, ct) )
//            ServletOps.error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415, "multipart/form-data not supported");

        if ( action.isTransactional() )
            quadsPutPostTxn(action, replaceOperation);
        else
            quadsPutPostNonTxn(action, replaceOperation);
    }


    // ---- Library : transactional
    /**
     * Load data using a transaction into the dataset of an action. if the data is bad,
     * abort the transaction.
     */
    public static void quadsPutPostTxn(HttpAction action, boolean replaceOperation) {
        quadsPutPostTxn(action, a->a.getDataset(), replaceOperation);
    }

    /**
     * Load data using a transaction into the dataset of an action. if the data is bad,
     * abort the transaction.
     * <p>
     * Delayed choice of dataset via a function so that the decision is made inside the transaction.
     */
    public static void quadsPutPostTxn(HttpAction action, Function<HttpAction, DatasetGraph> decideDataset, boolean replaceOperation) {
        UploadDetails details = null;
        action.beginWrite();
        try {
            DatasetGraph dsg = decideDataset.apply(action);
            if ( replaceOperation )
                dsg.clear();
            StreamRDF dest = StreamRDFLib.dataset(dsg);
            details = DataUploader.incomingData(action, dest);
            action.commit();
            ServletOps.uploadResponse(action, details);
        } catch (RiotException ex) {
            // Parse error
            action.abortSilent();
            ServletOps.errorBadRequest(ex.getMessage());
        } catch (OperationDeniedException ex) {
            action.abortSilent();
            throw ex;
        } catch (ActionErrorException ex) {
            action.abortSilent();
            throw ex;
        } catch (Exception ex) {
            // Something else went wrong. Backout.
            action.abortSilent();
            ServletOps.errorOccurred(ex.getMessage());
        } finally {
            action.end();
        }
    }

    // ---- Library : non-transactional
    /**
     * Load data, without assuming the dataset of an action is transactional -
     * specifically, whether it supports "abort". This requires loading the data into
     * a temporary dataset, which means we check the data is legal RDF, then copying
     * it into the finally destination.
     */

    public static void quadsPutPostNonTxn(HttpAction action, boolean replaceOperation) {
        quadsPutPostNonTxn(action, a->a.getDataset(), replaceOperation);
    }

    /**
     * Load data, without assuming the dataset of an action is transactional -
     * specifically, whether it supports "abort". This requires loading the data into
     * a temporary dataset, which means we check the data is legal RDF, then copying
     * it into the finally destination.
     * <p>
     * Delayed choice of dataset via a function so that the decision is made inside the transaction updating the data.
     */
    public static void quadsPutPostNonTxn(HttpAction action, Function<HttpAction, DatasetGraph> decideDataset, boolean replaceOperation) {
        DatasetGraph dsgTmp = DatasetGraphFactory.create();
        StreamRDF dest = StreamRDFLib.dataset(dsgTmp);

        UploadDetails details;
        try {
            details = DataUploader.incomingData(action, dest);
        } catch (RiotException ex) {
            ServletOps.errorBadRequest(ex.getMessage());
            return;
        }
        // Now insert into dataset
        action.beginWrite();
        try {
            DatasetGraph dsg = decideDataset.apply(action);
            if ( replaceOperation )
                dsg.clear();
            FusekiNetLib.addDataInto(dsgTmp, dsg);
            action.commit();
        } catch (OperationDeniedException ex) {
            action.abortSilent();
            throw ex;
        } catch (Exception ex) {
            // We're in a non-transactional upload so this probably will not
            // work but there still may be transaction state tracking.
            // There is no harm safely trying.
            action.abortSilent();
            ServletOps.errorOccurred(ex.getMessage());
        } finally {
            action.end();
        }
        ServletOps.uploadResponse(action, details);
    }

}
