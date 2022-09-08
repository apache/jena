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

package org.apache.jena.sparql.engine.http;

import org.apache.jena.riot.web.HttpNames;

/** Constants related to SPARQL over HTTP */

public class HttpParams
{
    /** Parameter for the SPARQL query string */
    public static final String pQuery               = HttpNames.paramQuery;

    /** Parameter for the SPARQL update string */
    public static final String pUpdate              = HttpNames.paramUpdate;

//    /** Parameter for a URI pointing to a SPARQL query in a document */
//    public static final String pQueryUri       = "query-uri" ;

    /** Parameter for a URI identifying the default graph (or one of the graphs) for SPARQL queries */
    public static final String pDefaultGraph        = HttpNames.paramDefaultGraphURI;

    /** Parameter for a URI identifying the named graph (or one of the graphs) for SPARQL queries */
    public static final String pNamedGraph          = HttpNames.paramNamedGraphURI;

    /** Parameter for a URI identifying the default graph (or one of the graphs) for SPARQL updates */
    public static final String pUsingGraph          = HttpNames.paramUsingGraphURI ;

    /** Parameter for a URI identifying the named graph (or one of the graphs) for SPARQL updates */
    public static final String pUsingNamedGraph     = HttpNames.paramUsingNamedGraphURI ;

    // ----------------- Non-SPARQL parameters

    public static final String pStylesheet      = HttpNames.paramStyleSheet;

    /** Parameter for query language URI */
    public static final String pQueryLang      = HttpNames.paramLang ;
}
