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

package org.apache.jena.arq.junit.riot;

import org.apache.jena.arq.junit.manifest.ManifestEntry;

/**
 * Tuning for RIOT tests.
 */
public class RiotTestsConfig {

    /**
     * Tune tests for warnings and variations. Normally, tests runs are warning sensitive.
     * The allowWarning rules are maintained here and used in RiotEvalTests and RiotSyntaxTests.
     */

    /*package*/  static boolean allowWarnings(ManifestEntry testEntry) {
        if ( RiotTests.equalsType(testEntry.getTestType(), VocabLangRDF.TestPositiveRDFXML) ) {
            // RDF/XML
            // Various warnings in eval tests.

            String name = testEntry.getName();

            if ( name.equals("datatypes-test002") )
                return true;

            if ( name.equals("rdfms-empty-property-elements-test016") )
                // Processing instruction warning.
                return true;

            if ( name.equals("rdfms-rdf-names-use-test-015") )
                //rdf:_1 is being used on a typed node.
                return true;

            if ( name.startsWith("rdfms-rdf-names-use-warn-") )
                // "is not a recognized RDF property or type."
                // "is not a recognized RDF property."
                return true;

            if ( name.startsWith("unrecognised-xml-attributes-test00") )
                // XML attribute: xml:foo is not known
                return true;

            return false;
        }

        String fragment = fragment(testEntry.getURI());
        if ( fragment == null )
            return false;

        // rdf-tests-cg/sparql11-query/syntax-query/
        // rdf-tests-cg/ntriples/manifest.ttl
        // rdf-tests-cg/nquads/manifest.ttl
        // rdf-tests-cg/turtle/manifest.ttl
        // rdf-tests-cg/trig/manifest.ttl
        // jena-shex
        if ( fragment.endsWith("UTF8_boundaries") || fragment.endsWith("character_boundaries") )
            // Boundaries of the Unicode allowed character blocks.
            return true;
        // rdf-tests Turtle and Trig
        if ( fragment.contains("IRI-resolution") )
            return true;
        return false;
    }

    /*package*/ static String fragment(String uri) {
        if ( uri == null )
            return null;
        int j = uri.lastIndexOf('#');
        String frag = (j >= 0) ? uri.substring(j) : uri;
        return frag;
    }

}
