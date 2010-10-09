/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev;



public class DevFuseki
{
    // Tasks:
    // EA-Release
    //   Mass rename and reset.
    //   Basic documentation
    //   shu defaults

    // Tests:
    //   Protocol
    
    // Not release:
    // File upload.
    // read-only server.
    // execute non-dataset servlet. 
    
    // SPARQL: Update -> Should it be 204 (and no page)?
    
    // ****
    // SPARQL Query servlet / SPARQL Update servlet
    //   request validation
    //   Testing, testing, testing.

    //** shu : arguments for query and update.
    // Naming: s_*?
    
    // Migrate:
    //  UpdateRemote
    //  (QueryRemote)
    //  ??DatasetUpdater
    // DatasetUpdaterFactory
    
    // Clean up SPARQL Query results code.
    
    // Check basename resolution for "graph="
    // http://en.wikipedia.org/wiki/List_of_HTTP_header_fields
    // gzip and inflate.   
    
    // LastModified headers. 

    // ConNeg:
    // $getAccept="#{$mtTurtle} , #{$mtRDF}"
    // defaults to last, not first (actually hash order?).  No posn?
    // Test for this?

    // Use TypedStream and MediaType widely.
    // Esp. SPARQL qQuery results  handling.
    
    // Tests: TestContentNegotiation pass but more needed.
    // Clean up FusekiLib.
    
    // Java DatasetUpdater:  don't serialise to byte[] and then send. 
    
    // Separate out the GET+dispatch as readonly servlet.
    
    // Basic authentication
    //   --user --password
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