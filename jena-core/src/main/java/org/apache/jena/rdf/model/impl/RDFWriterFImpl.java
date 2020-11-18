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

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdf.model.RDFWriterF;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.NoWriterForLangException ;

/**
 */
public class RDFWriterFImpl extends Object implements RDFWriterF {
    public static final String DEFAULTLANG = "RDF/XML";
    private static Map<String, Class<? extends RDFWriter>> custom = new LinkedHashMap<>();
    private static RDFWriterF rewiredAlternative = null ;
    /** Rewire to use an external RDFWriterF (typically, RIOT).
     * Set to null to use old jena-core setup.
     * @param other
     */
    public static void alternative(RDFWriterF other) {
        rewiredAlternative = other ;
    }

    /** Return the the current "rewiredAlternative" which may be null, meaning {@code RDFWriterFImpl} is in use. */
    public static RDFWriterF getCurrentRDFWriterF(RDFWriterF other) {
        return rewiredAlternative;
    }

    /** Creates new RDFReaderFImpl */
    public RDFWriterFImpl() {}

    @Override
    public RDFWriter getWriter() {
        return getWriter(DEFAULTLANG);
    }

    @Override
    public RDFWriter getWriter(String lang) {
        // If RIOT ->
        if ( rewiredAlternative != null )
            return rewiredAlternative.getWriter(lang) ;
        if (lang==null || lang.equals(""))
            lang = DEFAULTLANG ;
        Class<? extends RDFWriter> c = custom.get(lang);
        if ( c == null )
            throw new NoWriterForLangException("Writer not found: " + lang);
        try {
            return c.getConstructor().newInstance();
        }
        catch (Exception e) {
            throw new JenaException(e);
        }
    }

    static { 
        reset();
    }

    private static void reset() {
        Class<? extends RDFWriter> rdfxmlWriter = org.apache.jena.rdfxml.xmloutput.impl.Basic.class;
        Class<? extends RDFWriter> rdfxmlAbbrevWriter = org.apache.jena.rdfxml.xmloutput.impl.Abbreviated.class;
        Class<? extends RDFWriter> ntWriter = org.apache.jena.rdf.model.impl.NTripleWriter.class;

        custom.put("RDF/XML", rdfxmlWriter);
        custom.put("RDF/XML-ABBREV", rdfxmlAbbrevWriter);

        custom.put("N-TRIPLE",  ntWriter);
        custom.put("N-TRIPLES", ntWriter);
        custom.put("N-Triples", ntWriter);
    }

    private static String currentEntry(String lang) {
        Class<? extends RDFWriter> oldClass = custom.get(lang);
        if ( oldClass != null )
            return oldClass.getName();
        else
            return null;
    }

    private static String remove(String lang) {
        if ( rewiredAlternative != null )
            Log.error(RDFWriterFImpl.class, "Rewired RDFWriterFImpl2 - configuration changes have no effect on writing");
        String oldClassName = currentEntry(lang);
        custom.remove(lang);
        return oldClassName;
    }
}
