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
import org.apache.jena.rdf.model.RDFReaderF;
import org.apache.jena.rdf.model.RDFReaderI;
import org.apache.jena.rdf.model.impl.NTripleReader;
import org.apache.jena.rdfxml.arp1.RDFXMLReader;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.NoReaderForLangException;
import org.apache.jena.ttl_test.turtle.TurtleReader;

/**
 * For jena-core tests only.
 * <p>
 * This RDFReaderF provides the languages needed by the jena-core test suite.
 * <ul>
 * <li>RDF/XML</li>
 * <li>test-only Turtle (not fully RDF 1.1 compliant)<li>
 * <li>An N-triples reader<li>
 * </ul>
 */
public class X_RDFReaderF extends Object implements RDFReaderF {
    public static final String DEFAULTLANG = "RDF/XML" ;
    private static Map<String, Creator<RDFReaderI>> custom = new LinkedHashMap<>();
    private static RDFReaderF rewiredAlternative = null ;
    static { reset(); }

    public X_RDFReaderF() {}

    @Override
    public RDFReaderI getReader(String lang) {
        // Jena model.read rule for defaulting.
        if (lang==null || lang.equals(""))
            lang = DEFAULTLANG ;
        // if RIOT ->
        if ( rewiredAlternative != null )
            return rewiredAlternative.getReader(lang) ;
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


    private static void reset() {
        @SuppressWarnings("removal")
        Creator<RDFReaderI> rdfxmlReader = RDFXMLReader::new;
        Creator<RDFReaderI> ntReader     = NTripleReader::new;
        Creator<RDFReaderI> turtleReader = TurtleReader::new;

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
}
