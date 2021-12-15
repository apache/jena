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

package org.apache.jena.jdbc.remote.http;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.sparql.ARQException ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.update.UpdateRequest ;

/**
 * UpdateProcess that send the request to a SPARQL endpoint by using POST of application/sparql-update.
 * @deprecated Use {@code UpdateExecutionHTTP} created with {@code UpdateExecutionHTTPBuilder}.
 */
@Deprecated
public class UpdateProcessRemote extends UpdateProcessRemoteBase
{
    /**
     * Creates a new remote update processor that uses the application/sparql-update submission method
     * @param request Update request
     * @param endpoint Update endpoint
     * @param context Context
     */
    public UpdateProcessRemote(UpdateRequest request, String endpoint, Context context )
    {
        super(request, endpoint, context);
    }

    /**
     * Creates a new remote update processor that uses the application/sparql-update submission method
     * @param request Update request
     * @param endpoint Update endpoint
     * @param context Context
     * @param client HTTP client
     * @param httpContext HTTP Context
     */
    public UpdateProcessRemote(UpdateRequest request, String endpoint, Context context, HttpClient client, HttpContext httpContext)
    {
        this(request, endpoint, context);
        // Don't want to overwrite config we may have picked up from
        // service context in the parent constructor if the specified
        // client is null
        if (client != null) this.setClient(client);
        if (httpContext != null) this.setHttpContext(httpContext);
    }

    @Override
    public void execute()
    {
        // Validation
        if ( this.getEndpoint() == null )
            throw new ARQException("Null endpoint for remote update") ;
        if ( this.getUpdateRequest() == null )
            throw new ARQException("Null update request for remote update") ;

        // Build endpoint URL
        String endpoint = this.getEndpoint();
        String querystring = this.getUpdateString();
        if (querystring != null && !querystring.equals("")) {
            endpoint = endpoint.contains("?") ? endpoint + "&" + querystring : endpoint + "?" + querystring;
        }

        // Execution
        String reqStr = this.getUpdateRequest().toString() ;
        HttpOp1.execHttpPost(endpoint, WebContent.contentTypeSPARQLUpdate, reqStr, getClient(), getHttpContext()) ;
    }
}
