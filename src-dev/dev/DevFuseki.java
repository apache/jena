/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 */

package dev;

public class DevFuseki
{
    // SOH
    //   Refactor into body/no_body send // body/no_body receive
    //   --accept line/shortname : s-get, s-query
    //   Basic authentication: --user --password
    
    // **** Clean up SPARQL Query results code.

    // Environment variable for target (s-set but needs to shell built-in)
    //   defaults
    //   --service naming seems inconsistent.
    // Testing project?
    
    // Locking => transaction support (via default model?)
    //   HttpAction.beginRead() etc.
    
    // Java clients:
    //   DatasetAccessor: don't serialise to byte[] and then send. 
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

    // Query, Update : doHead
    
    // File upload.
    // Static pages
    

}
