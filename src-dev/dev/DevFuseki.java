package dev;

public class DevFuseki
{
    // Mem dataset with union graph
    
    // Documentation:
    //   Plan documentation.
    //   MIME types supported
    // Include a data file in the distribution.
    // curl as commandline
    
    // Dataset servers - bulk loader.
    // Access the bulk loader via web. [later]
    
    // ?? Errors that occur and what they mean:
    
    // http://localhost:3030/data/data ***
    // http://localhost:3030/data ***
    
    // ?? Local naming of graphs.
    
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
    
    // CORS: Access-Control-Allow-Origin: *
    // http://esw.w3.org/CORS_Enabled
    // Consider configuration.
    // http://hacks.mozilla.org/2009/07/cross-site-xmlhttprequest-with-cors/
    // http://download.eclipse.org/jetty/stable-7/apidocs/org/eclipse/jetty/servlets/CrossOriginFilter.html
    
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
    
    // **** Clean up SPARQL Query results code.

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
