/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev;

public class DevFuseki
{
    // SOH
    //  Refactor into body/no_body send // body/no_body receive
    
    //   Environment variable for target (s-set but needs to shell built-in)
    //   defaults
    //   --service naming seems inconsistent.
    // Testing project?
    
    // Java clients:
    //   DatasetAccessor: don't serialise to byte[] and then send. 
    //   DatasetAccessor : check existence of endpoint. 

    // Code examples
    
    // Replace sendError with setStatus code + own plain text.
    
    // Build system
    
    // Tests
    //   TestProtocol (HTTP update, query, update), inc status codes.
    //   SPARQL Query servlet / SPARQL Update servlet
    //   TestContentNegotiation - is coveage enough?
    
    // HTTP:
    //   gzip and inflate.   
    //   LastModified headers. 
    
    
    // Not release:
    //   File upload.
    //   Static pages
    //   query by POST
    
    // Basic authentication
    //   --user --password

    // Clean up SPARQL Query results code.
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */