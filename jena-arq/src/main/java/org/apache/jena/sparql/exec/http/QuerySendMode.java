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

/** Enum of different ways to send a SPARQL query over HTTP */
public enum QuerySendMode {

    // Use HTTP GET when below the length limit else POST an HTML Form encoding
    asGetWithLimitForm,

    // Use HTTP GET when below the length limit else POST the query as application/sparql-query
    asGetWithLimitBody,

    // Use GET regardless
    asGetAlways,

    // Use POST HTML Form regardless
    asPostForm,

    // POST and application/sparql-query
    asPost;

    public static QuerySendMode systemDefault = QuerySendMode.asGetWithLimitBody;
}