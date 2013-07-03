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

package com.hp.hpl.jena.sparql.modify;

import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.riot.web.HttpResponseLib;

import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.engine.http.HttpParams;
import com.hp.hpl.jena.sparql.engine.http.Params;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

/**
 * UpdateProcess that send the request to a SPARQL endpoint by using an HTML
 * form and POST Use of application/sparql-update via @link{UpdateProcessRemote}
 * is preferred.
 */
public class UpdateProcessRemoteForm extends UpdateProcessRemoteBase implements UpdateProcessor {

    /**
     * Creates a new remote update processor that uses the form URL encoded
     * submission method
     * 
     * @param request
     *            Update request
     * @param endpoint
     *            Update endpoint
     * @param context
     *            Context
     */
    public UpdateProcessRemoteForm(UpdateRequest request, String endpoint, Context context) {
        super(request, endpoint, context);
    }

    /**
     * Creates a new remote update processor that uses the form URL encoded
     * submission method
     * 
     * @param request
     *            Update request
     * @param endpoint
     *            Update endpoint
     * @param context
     *            Context
     * @param authenticator
     *            HTTP Authenticator
     */
    public UpdateProcessRemoteForm(UpdateRequest request, String endpoint, Context context, HttpAuthenticator authenticator) {
        this(request, endpoint, context);
        // Don't want to overwrite credentials we may have picked up from
        // service context in the parent constructor if the specified
        // authenticator is null
        if (authenticator != null)
            this.setAuthenticator(authenticator);
    }

    @Override
    public void execute() {
        // Validation
        if (this.getEndpoint() == null)
            throw new ARQException("Null endpoint for remote update by form");
        if (this.getUpdateRequest() == null)
            throw new ARQException("Null update request for remote update");

        // Execution
        String reqStr = this.getUpdateRequest().toString();
        Params ps = new Params(this.getParams());
        ps.addParam(HttpParams.pUpdate, reqStr);
        HttpOp.execHttpPostForm(this.getEndpoint(), ps, null, HttpResponseLib.nullResponse, null, getHttpContext(),
                getAuthenticator());
    }
}
