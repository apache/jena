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

package org.apache.jena.rdf.model.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.rdf.model.RDFReaderF;
import org.apache.jena.rdf.model.RDFReaderI;
import org.apache.jena.rdfxml.arp1.RDFXMLReader;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.NoReaderForLangException;

public class RDFReaderFImpl extends Object implements RDFReaderF {
    public static final String DEFAULTLANG = "RDF/XML";
    private static Map<String, Creator<RDFReaderI>> custom = new LinkedHashMap<>();
    private static RDFReaderF rewiredAlternative = null;

    // Jena6
    private static final boolean includeRDFXML = false;

    /** Rewire to use an external RDFReaderF (typically, RIOT).
     * Set to null to use old jena-core setup.
     * @param other
     */
    public static void alternative(RDFReaderF other) {
        rewiredAlternative = other;
    }

    private static String msgNoRDFXML = "RDF/XML is no longer supported in jena-core. Add jena-arq to the classpath";
    private static Set<String> removedLangs = Set.of("RDF", "RDF/XML", "RDF/XML-ABBREV");

    /** Creates new RDFReaderFImpl */
    public RDFReaderFImpl() {}

    static { setup(); }

    @Override
    public RDFReaderI getReader(String lang) {
        // Jena model.read rule for defaulting.
        if (lang==null || lang.equals(""))
            lang = DEFAULTLANG;
        // if RIOT ->
        if ( rewiredAlternative != null )
            return rewiredAlternative.getReader(lang);

        if ( ! includeRDFXML ) {
            // Jena6: reading RDF/XML removed from jena-core
            if ( removedLangs.contains(lang) )
                Log.error("RDFReader", msgNoRDFXML);
        }

        Creator<RDFReaderI> c = custom.get(lang);
        if ( c == null )
            throw new NoReaderForLangException("Reader not found: " + lang);

        try {
            return c.create();
        }
        catch (RuntimeException e) {
            throw new JenaException(e);
        }
    }

    private static void setup() {
        // Turtle moved to test-only
        // Jena6: ARP1 (RDF/XML) not installed except for tests

        Creator<RDFReaderI> ntReader     = NTripleReader::new;
        custom.put("N-TRIPLE",  ntReader);
        custom.put("N-TRIPLES", ntReader);
        custom.put("N-Triples", ntReader);

        if ( includeRDFXML ) {
          @SuppressWarnings("removal")
          Creator<RDFReaderI> rdfxmlReader = RDFXMLReader::new;
          custom.put("RDF",            rdfxmlReader);
          custom.put("RDF/XML",        rdfxmlReader);
          custom.put("RDF/XML-ABBREV", rdfxmlReader);
        }
    }
}
