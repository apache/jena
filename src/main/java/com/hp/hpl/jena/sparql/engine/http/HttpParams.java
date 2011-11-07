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

package com.hp.hpl.jena.sparql.engine.http;

/** Constants related to SPARQL over HTTP */

public class HttpParams
{
    /** Parameter for the SPARQL query string */
    public static final String pQuery               = "query" ;
    
//    /** Parameter for a URI pointing to a SPARQL query in a document */
//    public static final String pQueryUri       = "query-uri" ;
    
    /** Parameter for a URI identifying the graph (or one of the graphs) */
    public static final String pDefaultGraph        = "default-graph-uri" ;
    
    /** Parameter for a URI identifying the graph (or one of the graphs) */
    public static final String pNamedGraph          = "named-graph-uri" ;
    
    // ----------------- Non-SPARQL parameters

    public static final String pStylesheet      = "stylesheet" ;

    /** Parameter for query language URI */
    public static final String pQueryLang      = "lang" ;

    // -------- Constants
    
    public static final String contentTypeAppN3        = "application/n3" ;
    public static final String contentTypeTurtle       = "application/turtle" ;
    public static final String contentTypeRDFXML       = "application/rdf+xml" ;
    public static final String contentTypeNTriples     = "application/n-triples" ;
    public static final String contentTypeXML          = "application/xml" ;
    public static final String contentTypeResultsXML   = "application/sparql-results+xml" ;

    public static final String contentTypeTextPlain    = "text/plain" ;
    public static final String contentTypeTextN3       = "text/n3" ;
    public static final String contentTypeForText      = contentTypeTextPlain ;
    
    public static final String charsetUTF8             = "utf-8" ;

    
}
