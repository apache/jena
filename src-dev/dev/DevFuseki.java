/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 */

package dev;

public class DevFuseki
{
    // Feedback when SPARQL Update POST done
    
    // 4. header and trailer to put "if logged in" around form.
    // 5. populate forms with prefixes
    // 6. set dft base name for update/query.
    // clear out pages
    
    // Assemblies:
    //   one jar
    //   SOH
    //   Fuksei.zip
    // File upload
    
    // CORS: Access-Control-Allow-Origin: *
    // http://esw.w3.org/CORS_Enabled
    // Consider configuration.
    // http://hacks.mozilla.org/2009/07/cross-site-xmlhttprequest-with-cors/
    
    // ETags.
    
    // Authentication

    
    // SOH
    //   Refactor into body/no_body send // body/no_body receive
    // All:
    // -v --help --accept --user/--password ( or --auth user:pass) 
    // Drop --service.
    
    //   --accept line/shortname : s-get, s-query
    //   Basic authentication: --user --password
    
    // **** Clean up SPARQL Query results code.

    // Document
    // Wiki reorg
    //   validators
    //   manager
    
    // Pages
    //   Any endpoint, default to self.  Needs javascript.
    
    // Environment variable for target (s-set but needs to shell built-in)
    //   defaults
    //   --service naming seems inconsistent.
    // Testing project?
    
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

    // Build system
    
    // Tests
    //   TestProtocol (HTTP update, query, update), inc status codes.
    //   SPARQL Query servlet / SPARQL Update servlet
    //   TestContentNegotiation - is coveage enough?
    
    // HTTP:
    //   gzip and inflate.   
    //   LastModified headers. 

    // File upload.
}
