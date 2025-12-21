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
import org.apache.jena.rdf.model.RDFWriterF;
import org.apache.jena.rdf.model.RDFWriterI;
import org.apache.jena.rdfxml.xmloutput.impl.RDFXML_Abbrev;
import org.apache.jena.rdfxml.xmloutput.impl.RDFXML_Basic;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.NoWriterForLangException;

public class RDFWriterFImpl extends Object implements RDFWriterF {
    public static final String DEFAULTLANG = "RDF/XML";
    private static Map<String, Creator<RDFWriterI>> custom = new LinkedHashMap<>();
    private static RDFWriterF rewiredAlternative = null;

    // Jena6
    private static final boolean includeRDFXML = false;

    /** Rewire to use an external RDFWriterF (typically, RIOT).
     * Set to null to use old jena-core setup.
     * @param other
     */
    public static void alternative(RDFWriterF other) {
        rewiredAlternative = other;
    }

    /** Return the the current "rewiredAlternative" which may be null, meaning {@code RDFWriterFImpl} is in use. */
    public static RDFWriterF getCurrentRDFWriterF(RDFWriterF other) {
        return rewiredAlternative;
    }

    private static String msgNoRDFXML = "RDF/XML is no longer supported in jena-core. Add jena-arq to the classpath";
    private static Set<String> removedLangs = Set.of("RDF/XML", "RDF/XML-ABBREV");

    static { setup(); }

    /** Creates new RDFReaderFImpl */
    public RDFWriterFImpl() {}

    @Override
    public RDFWriterI getWriter(String lang) {
        if (lang==null || lang.equals(""))
            lang = DEFAULTLANG;
        // If RIOT ->
        if ( rewiredAlternative != null )
            return rewiredAlternative.getWriter(lang);

        if ( ! includeRDFXML ) {
            // Jena6: writing RDF/XML removed from jena-core
            if ( removedLangs.contains(lang) )
                Log.error("RDFWriter", msgNoRDFXML);
        }

        Creator<RDFWriterI> c = custom.get(lang);
        if ( c == null )
            throw new NoWriterForLangException("Writer not found: " + lang);
        try {
            return c.create();
        }
        catch (RuntimeException e) {
            throw new JenaException(e);
        }
    }

    private static void setup() {
        Creator<RDFWriterI> ntWriter     = NTripleWriter::new;
        custom.put("N-TRIPLE",  ntWriter);
        custom.put("N-TRIPLES", ntWriter);
        custom.put("N-Triples", ntWriter);

        if ( includeRDFXML ) {
            // Jena6: RDF/XML writing not installed except for tests.
            @SuppressWarnings("deprecation")
            Creator<RDFWriterI> rdfxmlWriterBasic  = RDFXML_Basic::new;
            @SuppressWarnings("deprecation")
            Creator<RDFWriterI> rdfxmlWriterAbbrev = RDFXML_Abbrev::new;

            custom.put("RDF/XML",        rdfxmlWriterBasic);
            custom.put("RDF/XML-ABBREV", rdfxmlWriterAbbrev);
        }
    }
}
