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

package org.apache.jena;

import java.util.* ;

/**
 * Methods and constants that define features of the current the environment.
 * Primarily for other parts of the Jena framework.
 */

public class JenaRuntime
{
    /**
     * <p>
     * The global {@code isRDF11} exists only to support development in Jena2.
     * It is not expected that this flag will be "true" for Jena2 releases.
     * </p>
     * <p>
     * Jena2 provides RDF 1.0 (AKA RDF 2004) and as much of RDF 1.1 that can be provided without
     * invalidating existing data and applications.  For example, the Turtle family parsers
     * cover the RDF 1.1 defined grammars.
     * </p>
     * <p>
     * RDF 1.1 does make some changes that will effect some applications. The RDF Working Group
     * do not expect these to be that major but they are visible in some situations.
     * </p>
     * <p>
     * One of these changes is that literals always have a datatype.  RDF 1.0 plain literals
     * (e.g. {@code "foo"} and {@code "foo"@en}) do not have datatype.
     * </p>
     * <p>
     * In RDF 1.1:
     * <ul>
     * <li>string literals without language tag have datatype {@code xsd:string}</li>
     * <li>string literals with language tag have datatype {@code rdf:langString}.
     *      They still have a language tag.</li>
     * </ul>
     * <p>
     * In RDF 1.0, {@code "foo"} and {@code "foo"^^xsd:string} are different RDF terms.
     * Triples {@code :s :p "foo"} and {@code :s :p "foo"^^xsd:string} are two different RDF triples.
     * Jena memory models provide "same value" semantics, so these can both be found looking for
     * object of {@code "foo"} but two such triples are found.
     * </p>
     * <p>
     * Other storage implementations do not provide these "same value" semantics.
     * Two triples are stored in a graph or in a database.
     * </p>
     * <p>
     * In RDF 1.1, {@code "foo"} and {@code "foo"^^xsd:string} are the same RDF term; it is just
     * two different ways to write it.  The example triples above are the same triple.
     * Only one triple would be stored in a graph or on disk.
     * </p>
     * <p>
     * It is common for applications to work either with RDF 1.0 untyped strings or with typed
     * {@code xsd:strings}. Mixed working is less common.  Mixed working applications will be
     * affected by the changes in RDF 1.1.
     * </p>
     * <p>
     * Default full RDF 1.1 behaviour is expected in Jena3, with the change of major version
     * number used to indicate the application-visible change.
     * </p>
     */

    public static boolean isRDF11 = true ;

    // --------------------------------------------------------------

    /** @deprecated To be removed */
    @Deprecated(since="4.9.0")
    public static String getMetadata(String key, String defaultValue) { return null; }

    /** @deprecated To be removed */
    @Deprecated(since="4.9.0")
    public static final String featureNoSecurity = "http://jena.hpl.hp.com/2004/07/feature/noSecurity" ;

    /** @deprecated To be removed */
    @Deprecated(since="4.9.0")
    public static final String featureNoCharset = "http://jena.hpl.hp.com/2004/07/feature/noCharset" ;

    static Map<String, String> features = new HashMap<>() ;
    static {
            // Note getSystemProperty uses featureNoSecurity but works if it
            // has not been initialized
            if ( getSystemProperty(featureNoSecurity) != null )
                setFeature(featureNoSecurity) ;

            if ( getSystemProperty(featureNoCharset) != null )
                setFeature(featureNoCharset) ;
    }

    /** @deprecated To be removed */
    @Deprecated(since="4.9.0")
    public static void setFeature(String featureName) { features.put(featureName, "true") ; }


    static final String lineSeparator = getSystemProperty("line.separator", "\n") ;
    public static String getLineSeparator() {
        return lineSeparator;
    }

    public static String getSystemProperty(String propName) {
        return getSystemProperty(propName, null);
    }

    public static String getSystemProperty(final String propName, String defaultValue) {
        return System.getProperty(propName, defaultValue);
    }
}
