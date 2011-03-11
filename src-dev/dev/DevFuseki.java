package dev;

public class DevFuseki
{
    // Bug: upload no file.
    // "Bug" text/plain chrome then => "N-Triples", not "N-TRIPLES"
    // Lang -> WriterName.
    
    // Bug; upload no file.
    // Chrome: Accept: application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5

    
    // Migrate ContentType to RIOT
    // use in WebContent.
    
    //soh : --accept application/turtle for CONSTRUCT queries.
    
    // Direct naming.
    // Use absence/presence of a query string to switch.
    
    // sparql.jsp ==> if no dataset, go to choosing page or error page linking to.
    
    // Better handling of bad URI name for a graph http:/example/X => stacktrace.
    
    // FUSEKI_ROOT

    // Mem dataset with union graph (ARQ)
    
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
