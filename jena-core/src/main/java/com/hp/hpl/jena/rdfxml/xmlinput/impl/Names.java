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

package com.hp.hpl.jena.rdfxml.xmlinput.impl;


public interface Names {
    String rdfns = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
            .intern();

    String xmlns = "http://www.w3.org/XML/1998/namespace".intern();
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
}
