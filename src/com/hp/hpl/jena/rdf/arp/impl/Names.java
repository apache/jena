/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.impl;


public interface Names {
    String rdfns = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
            .intern();

    String xmlns = "http://www.w3.org/XML/1998/namespace".intern();
    String damlns = "http://www.daml.org/2001/03/daml+oil#";
    String xmlnsns = "http://www.w3.org/2000/xmlns/";
    
    int A_XMLBASE = 1;

    int A_XMLLANG = 2;

    int A_XML_OTHER = 4;
    
    int A_XMLNS = 32768;

    int A_ID = 8;

    int A_ABOUT = 16;

    int A_NODEID = 32;

    int A_RESOURCE = 64;

    int A_PARSETYPE = 128;

    int A_DATATYPE = 256;

    int A_TYPE = 512;

    int A_DEPRECATED = 1024;
    
    int A_BAGID = 16384;

    int E_LI = 2048;

    int E_RDF = 4096;

    int E_DESCRIPTION = 8192;

    // see sections 7.2.[2-5] of RDF Syntax (Revised)
    int CoreAndOldTerms = E_RDF | A_DEPRECATED | A_ABOUT | A_ID | A_NODEID
            | A_RESOURCE | A_PARSETYPE | A_DATATYPE | A_BAGID;

    int A_BADATTRS = CoreAndOldTerms | E_LI | E_DESCRIPTION;

    ANode RDF_STATEMENT = URIReference.createNoChecks(rdfns + "Statement");

    ANode RDF_TYPE = URIReference.createNoChecks((rdfns + "type"));

    ANode RDF_SUBJECT = URIReference.createNoChecks((rdfns + "subject"));

    ANode RDF_PREDICATE = URIReference.createNoChecks(rdfns + "predicate");

    ANode RDF_OBJECT = URIReference.createNoChecks((rdfns + "object"));

    ANode RDF_NIL        = URIReference.createNoChecks(rdfns+"nil");
    
    ANode RDF_FIRST = URIReference.createNoChecks(rdfns+"first");
    ANode RDF_REST       = URIReference.createNoChecks(rdfns+"rest");

    ANode DAML_NIL        = URIReference.createNoChecks(damlns+"nil");
        
    ANode DAML_FIRST = URIReference.createNoChecks(damlns+"first");
    ANode DAML_REST   = URIReference.createNoChecks(damlns+"rest");
    ANode DAML_LIST  = URIReference.createNoChecks(damlns+"List");
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

