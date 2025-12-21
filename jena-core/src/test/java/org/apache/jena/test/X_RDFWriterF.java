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

import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.rdf.model.RDFWriterF;
import org.apache.jena.rdf.model.RDFWriterI;
import org.apache.jena.rdf.model.impl.NTripleWriter;
import org.apache.jena.rdfxml.xmloutput.impl.RDFXML_Abbrev;
import org.apache.jena.rdfxml.xmloutput.impl.RDFXML_Basic;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.NoWriterForLangException;


/**
 * For jena-core tests only.
 * <p>
 * This RDFWriterF provides the languages needed by the jena-core test suite.
 * <ul>
 * <li>RDF/XML</li>
 * <li>An N-triples reader<li>
 * </ul>
 */
public class X_RDFWriterF extends Object implements RDFWriterF {
    public static final String DEFAULTLANG = "RDF/XML";
    private static Map<String, Creator<RDFWriterI>> custom = new LinkedHashMap<>();
    static { reset(); }

    /** Creates new RDFReaderFImpl */
    public X_RDFWriterF() {}

    @Override
    public RDFWriterI getWriter(String lang) {
        if (lang==null || lang.equals(""))
            lang = DEFAULTLANG;
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

    private static void reset() {
        @SuppressWarnings("deprecation")
        Creator<RDFWriterI> rdfxmlWriter = RDFXML_Basic::new;
        @SuppressWarnings("deprecation")
        Creator<RDFWriterI> rdfxmlAbbrevWriter = RDFXML_Abbrev::new;
        Creator<RDFWriterI> ntWriter = NTripleWriter::new;

        custom.put("RDF/XML", rdfxmlWriter);
        custom.put("RDF/XML-ABBREV", rdfxmlAbbrevWriter);

        custom.put("N-TRIPLE",  ntWriter);
        custom.put("N-TRIPLES", ntWriter);
        custom.put("N-Triples", ntWriter);
    }
}
