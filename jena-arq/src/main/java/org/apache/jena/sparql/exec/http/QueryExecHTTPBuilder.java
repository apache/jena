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

package org.apache.jena.sparql.exec.http;

import static org.apache.jena.http.HttpLib.copyArray;

import java.net.http.HttpClient;
import java.util.HashMap;
import java.util.Objects;

import org.apache.jena.http.HttpEnv;
import org.apache.jena.http.sys.ExecHTTPBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;

public class QueryExecHTTPBuilder extends ExecHTTPBuilder<QueryExec, QueryExecHTTPBuilder> {

    public static QueryExecHTTPBuilder newBuilder() { return new QueryExecHTTPBuilder(); }

    private QueryExecHTTPBuilder() {}

    @Override
    public QueryExecHTTP build() {
      Objects.requireNonNull(serviceURL, "No service URL");
      if ( queryString == null && query == null )
          throw new QueryException("No query for QueryExecHTTP");
      HttpClient hClient = HttpEnv.getHttpClient(serviceURL, httpClient);

      Query queryActual = query;

      if ( substitutionMap != null && ! substitutionMap.isEmpty() ) {
          if ( query == null )
              throw new QueryException("Substitution only supported if a Query object was provided");

          queryActual = QueryTransformOps.transform(query, substitutionMap);
      }


      return new QueryExecHTTP(serviceURL, queryActual, queryString, urlLimit,
                               hClient, new HashMap<>(httpHeaders), Params.create(params), context,
                               copyArray(defaultGraphURIs),
                               copyArray(namedGraphURIs),
                               sendMode, appAcceptHeader,
                               timeout, timeoutUnit);
    }

    @Override
    protected QueryExecHTTPBuilder thisBuilder() {
        return this;
    }
}
