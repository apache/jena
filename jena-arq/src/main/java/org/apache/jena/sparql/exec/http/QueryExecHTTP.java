/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.exec.http;

import org.apache.jena.sparql.exec.QueryExec;

public interface QueryExecHTTP extends QueryExec {

    public static QueryExecHTTPBuilder newBuilder() { return QueryExecHTTPBuilder.create(); }

    public static QueryExecHTTPBuilder service(String serviceURL) {
        return QueryExecHTTP.newBuilder().endpoint(serviceURL);
    }

    String getAcceptHeaderSelect();

    String getAcceptHeaderAsk();

    String getAcceptHeaderDescribe();

    String getAcceptHeaderConstructGraph();

    String getAcceptHeaderConstructDataset();

    /** The Content-Type response header received (null before the remote operation is attempted). */
    String getHttpResponseContentType();
}
