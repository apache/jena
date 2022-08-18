/**
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

package org.apache.jena.riot.lang;

/** TriX - see <a href="http://www.hpl.hp.com/techreports/2004/HPL-2004-56.html">HPL-2004-56</a> Jeremy Carroll and Patrick Stickler.
 * Supported:
 * <ul>
 * <li>Basic TriX as per the DTD in HPL-2004-56
 * <li>Typed literal rdf:XMLLiteral with inline XML.
 * <li>&lt;qname&gt; (on reading)
 * </ul>
 */

public class TriX {
    // DTD for TrIX : The schema is a much longer.

    /*
    HPL tech report
    <!-- TriX: RDF Triples in XML -->
    <!ELEMENT TriX (graph*)>
    <!ATTLIST TriX xmlns CDATA #FIXED "http://www.w3.org/2004/03/trix/trix-1/">
    <!ELEMENT graph (uri*, triple*)>
    <!ELEMENT triple ((id|uri|plainLiteral|typedLiteral), uri, (id|uri|plainLiteral|typedLiteral))>
    <!ELEMENT id (#PCDATA)>
    <!ELEMENT uri (#PCDATA)>
    <!ELEMENT plainLiteral (#PCDATA)>
    <!ATTLIST plainLiteral xml:lang CDATA #IMPLIED>
    <!ELEMENT typedLiteral (#PCDATA)>
    <!ATTLIST typedLiteral datatype CDATA #REQUIRED>


    W3C DTD
    <!-- TriX: RDF Triples in XML -->
    <!ELEMENT trix         (graph*)>
    <!ATTLIST trix         xmlns CDATA #FIXED "http://www.w3.org/2004/03/trix/trix-1/">
    <!ELEMENT graph        (uri, triple*)>
    <!ELEMENT triple       ((id|uri|plainLiteral|typedLiteral), uri, (id|uri|plainLiteral|typedLiteral))>
    <!ELEMENT id           (#PCDATA)>
    <!ELEMENT uri          (#PCDATA)>
    <!ELEMENT plainLiteral (#PCDATA)>
    <!ATTLIST plainLiteral xml:lang CDATA #IMPLIED>
    <!ELEMENT typedLiteral (#PCDATA)>
    <!ATTLIST typedLiteral datatype CDATA #REQUIRED>
    */

    /* Constants for TriX */
    public final static String NS              = "http://www.w3.org/2004/03/trix/trix-1/" ;

    /* Element name in the W3C DTD */
    public final static String tagTriX         = "trix" ;
    /* Element name in the HPL tech report */
    public final static String tagTriXAlt       = "TriX" ;

    public final static String tagGraph        = "graph" ;
    public final static String tagTriple       = "triple" ;
    public final static String tagURI          = "uri" ;
    public final static String tagId           = "id" ;
    public final static String tagQName        = "qname" ;
    public final static String tagPlainLiteral = "plainLiteral" ;
    public final static String tagTypedLiteral = "typedLiteral" ;

    public final static String attrXmlLang     = "lang" ;
    public final static String attrDatatype    = "datatype" ;
}

