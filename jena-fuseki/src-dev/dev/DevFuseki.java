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

package dev;

public class DevFuseki
{
    // SPARQL_QueryDataset.vialidate query -- but FROM etc is important for TDB. 
    // DatasetDescription from protocol
    //   createDataset from the ARQ.
    //   SPARQL_QueryGeneral.datasetFromDescription
    
    // Config:
    //   fuseki:name ==> fuseki:serviceName of fuseki:endpointBase 
    //   rdfs:label for log files.
    
    // sparql.jsp needs to switch on presence and name of service endpoints.
    // --accept for to soh for construct queries (check can get CONSTRUCT in TTL).
    
    // application/json for application/sparql-results+json. 
    // application/xml for application/sparql-results+xml. 
    
    // LimitingGraph, LimitingBulkUpdateHandler --> change to use a limiting Sink<>
    // Finish: SPARQL_QueryGeneral
    //    Parse errors and etc need to be passed out.
    // --jetty-config documentation
    
    // Rework arguments.
    //   Explicit install pages.
    //   Pages for read-only.
    
    // RDF/XML_ABBREV in ResponseModel
    // Config Jetty from a file?
    //   Alternatibe way to run Fuseki
    
	// Flint?
	// Pages for publish mode.

	// Multiple Accept headers
    // WebContent and ContentType clean up.
    
	// SOH default to not needing 'default'
	// More error handling.

    // Migrate ContentType to RIOT
    // use in WebContent.
    
    //soh : --accept application/turtle for CONSTRUCT queries.
    
    // Direct naming.
    // Use absence/presence of a query string to switch.
    
    // sparql.jsp ==> if no dataset, go to choosing page or error page linking to.
    
    // Better handling of bad URI name for a graph http:/example/X => stacktrace.
    
    // FUSEKI_ROOT

    // Documentation:
    //   Plan documentation.
    //   MIME types supported
    // Include a data file in the distribution.
    // curl as commandline
    
    // Dataset servers - bulk loader.
    // Access the bulk loader via web. [later]
    
    // Structure pages, different static content servers
    // /Main - index.html = fuseki.html
    // /validate
    // /admin
    
    // Server-local graph naming
    
    // Plain text error pages
    
    // Deploy to sparql.org -- need query form for read-only mode.
    
    // + LARQ
    
    // Testing:
    //   No file -> error.
    
    // Remove from package conneg - use code from ARQ/Atlas.
    // TypedStream TypedInoputStrea, TypedOutputStream, MediaType, MediaRange
    
    // Bundle tdb scripts with Fuseki.

    // ParserFor - share between SPARQL_REST and SPARQL_Upload
    // UploadTo dataset (TriG, N-Quads)
   
    // populate forms with prefixes (later)
    
    // Tests
    //   TestProtocol (HTTP update, query, update), inc status codes.
    //   SPARQL Query servlet / SPARQL Update servlet
    //   TestContentNegotiation - is coveage enough?
    
    // ?? Slug header:
    // http://bitworking.org/projects/atom/rfc5023.html#rfc.section.9.7
    
    // ETags.
    
    // Authentication
    
    // SOH
    //   Refactor into body/no_body send & body/no_body receive
    // All:
    // -v --help --accept --user/--password ( or --auth user:pass) 
    // Drop --service.
    // Local config file - read to get service settings. 
    
    //   --accept line/shortname : s-get, s-query
    //   Basic authentication: --user --password
    
    // Argument names: --service naming seems inconsistent.
    
    // Plug-ins:
    //   Dataset (query, Update), HttpInternalIF?
    //   "Connection"
    // Locking => transaction support (via default model?)
    //   HttpAction.beginRead() etc.
    
    // Java clients:
    //   DatasetAccessor : don't serialise to byte[] and then send. 
    //   DatasetAccessor : check existence of endpoint. 

    // Content-Length: SHOULD
    //   Transfer-Encoding: identity
    // "chunked" encoding
    // gzip
    
    // Code examples
}
