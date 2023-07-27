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

package org.apache.jena.test;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.rdf.model.RDFReaderI;
import org.apache.jena.rdf.model.RDFReaderF;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.NoReaderForLangException;

/** Custom RDFReaderF for jena-core tests. */
public class X_RDFReaderF extends Object implements RDFReaderF {
    public static final String DEFAULTLANG = "RDF/XML" ;
    private static Map<String, Class<? extends RDFReaderI>> custom = new LinkedHashMap<>();
    private static RDFReaderF rewiredAlternative = null ;

    /**
     * Rewire to use an external RDFReaderF (typically, RIOT).
     * Set to null to use old jena-core setup.
     * @param other
     */
    public static void alternative(RDFReaderF other) {
        rewiredAlternative = other ;
    }

    /** Creates new RDFReaderFImpl */
    public X_RDFReaderF() {}

    @Override
    public RDFReaderI getReader()  {
        return getReader(null);
    }

    @Override
    public RDFReaderI getReader(String lang) {
        // Jena model.read rule for defaulting.
        if (lang==null || lang.equals(""))
            lang = DEFAULTLANG ;
        // if RIOT ->
        if ( rewiredAlternative != null )
            return rewiredAlternative.getReader(lang) ;
        Class<? extends RDFReaderI> c = custom.get(lang);
        if ( c == null )
            throw new NoReaderForLangException("Reader not found: " + lang);

        try {
            return c.getConstructor().newInstance();
        }
        catch (Exception e) {
            throw new JenaException(e);
        }
    }

    static {
        // static initializer - set default readers
        reset();
    }

    private static void reset() {
        Class<? extends RDFReaderI> rdfxmlReader = org.apache.jena.rdfxml.xmlinput.RDFXMLReader.class;

        // test use only
        Class<? extends RDFReaderI> ntReader = org.apache.jena.rdf.model.impl.NTripleReader.class;
        // test use only
        Class<? extends RDFReaderI> turtleReader = org.apache.jena.ttl_test.turtle.TurtleReader.class;

        custom.put("RDF", rdfxmlReader);
        custom.put("RDF/XML", rdfxmlReader);
        custom.put("RDF/XML-ABBREV", rdfxmlReader);

        custom.put("N-TRIPLE", ntReader);
        custom.put("N-TRIPLES", ntReader);
        custom.put("N-Triples", ntReader);

        custom.put("N3", turtleReader);
        custom.put("TURTLE", turtleReader);
        custom.put("Turtle", turtleReader);
        custom.put("TTL", turtleReader);
    }

    private static String currentEntry(String lang) {
        Class<? extends RDFReaderI> oldClass = custom.get(lang);
        if ( oldClass != null )
            return oldClass.getName();
        else
            return null;
    }

    private static String remove(String lang) {
        if ( rewiredAlternative != null )
            Log.error(X_RDFReaderF.class, "Rewired RDFReaderFImpl - configuration changes have no effect on reading");

        String oldClassName = currentEntry(lang);
        custom.remove(lang);
        return oldClassName;
    }
}
