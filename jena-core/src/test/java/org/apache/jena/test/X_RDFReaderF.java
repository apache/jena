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

import org.apache.jena.rdf.model.RDFReaderI;
import org.apache.jena.rdf.model.RDFReaderF;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.NoReaderForLangException;

/**
 * For jena-core tests only.
 * <p>
 * The RDFReaderF provides the languages needed by the jena-core test suite.
 * <ul>
 * <li>RDF/XML</li>
 * <li>test-only Turtle (not fully RDF 1.1 compliant)<li>
 * <li>An N-triples reader<li>
 * </ul>
 */
public class X_RDFReaderF extends Object implements RDFReaderF {
    public static final String DEFAULTLANG = "RDF/XML" ;
    private static Map<String, Class<? extends RDFReaderI>> custom = new LinkedHashMap<>();

    /** Creates new RDFReaderFImpl */
    public X_RDFReaderF() {}

    @Override
    public RDFReaderI getReader(String lang) {
        // Jena model.read rule for defaulting.
        if (lang==null || lang.equals(""))
            lang = DEFAULTLANG ;
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
        Class<? extends RDFReaderI> rdfxmlReader = org.apache.jena.rdfxml.xmlinput1.RDFXMLReader.class;

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
}
