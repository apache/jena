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

package org.apache.jena.rdfxml.xmloutput.impl;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.SplitIRI;

/*
 * Utilities for RDF/XML writing.
 * Use these functions and don't call SplitIRI,
 * Resource.getNameSpace or Resource.getLocalName
 * directly. The Resource methods call SplitIRI.
 *
 * The functions here will continue to provide XML 1.0.
 */
class SplitRDFXML {

    @SuppressWarnings("deprecation")
    static int splitXML10(String uri) {
        return SplitIRI.splitXML10(uri);
    }

    static String namespace(Resource resource) {
        return namespace(resource.getURI());
    }

    static String namespace(String uriStr) {
        return namespaceXML10(uriStr);
    }

    static String localname(Resource resource) {
        return localname(resource.getURI());
    }

    static String localname(String uriStr) {
        return localnameXML10(uriStr);
    }

    /**
     * Namespace, according to XML 1.0 qname rules.
     * Use with {@link #localnameXML}.
     */
    private static String namespaceXML10(String string) {
        int i = splitXML10(string);
        return string.substring(0, i);
    }

    /** Localname, according to XML 1.0 qname rules. */
    private  static String localnameXML10(String string) {
        int i = splitXML10(string);
        return string.substring(i);
    }
}
