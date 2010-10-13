/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev;



public class DevFuseki
{
    // Environment variable for target (s-set but needs to shell built-in)
    
    // Build system
    
    // ** TestProtocol (HTTP update, query, update)
    
    // Tasks:
    //   SOH defaults
    
    // Code examples
    
    // DatasetAccessor : check existence of endpoint. 
    
    // Tests:
    //   Protocol, inc status codes.
    //   SPARQL Query servlet / SPARQL Update servlet
    
    // Not release:
    //   File upload.
    //   execute SPARQL non-dataset servlet.
    //   Static pages
    //   query by POST
    
    // Basic authentication
    //   --user --password
    
    // Check SPARQL_REST for access to dataset to ensure there's a lock even before target created.
    // Clean up SPARQL Query results code.

    // HTTP:
    // gzip and inflate.   
    
    // LastModified headers. 

    // Tests: TestContentNegotiation pass but more needed.
    
    // Java DatasetUpdater:  don't serialise to byte[] and then send. 
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