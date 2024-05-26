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

import org.apache.jena.rdf.model.RDFWriterF;
import org.apache.jena.rdf.model.RDFWriterI;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.NoWriterForLangException ;

/**
 */
public class X_RDFWriterF extends Object implements RDFWriterF {
    public static final String DEFAULTLANG = "RDF/XML";
    private static Map<String, Class<? extends RDFWriterI>> custom = new LinkedHashMap<>();

    /** Creates new RDFReaderFImpl */
    public X_RDFWriterF() {}

    @Override
    public RDFWriterI getWriter(String lang) {
        if (lang==null || lang.equals(""))
            lang = DEFAULTLANG ;
        Class<? extends RDFWriterI> c = custom.get(lang);
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
        Class<? extends RDFWriterI> rdfxmlWriter = org.apache.jena.rdfxml.xmloutput.impl.RDFXML_Basic.class;
        Class<? extends RDFWriterI> rdfxmlAbbrevWriter = org.apache.jena.rdfxml.xmloutput.impl.RDFXML_Abbrev.class;
        Class<? extends RDFWriterI> ntWriter = org.apache.jena.rdf.model.impl.NTripleWriter.class;

        custom.put("RDF/XML", rdfxmlWriter);
        custom.put("RDF/XML-ABBREV", rdfxmlAbbrevWriter);

        custom.put("N-TRIPLE",  ntWriter);
        custom.put("N-TRIPLES", ntWriter);
        custom.put("N-Triples", ntWriter);
    }

    private static String currentEntry(String lang) {
        Class<? extends RDFWriterI> oldClass = custom.get(lang);
        if ( oldClass != null )
            return oldClass.getName();
        else
            return null;
    }
}
