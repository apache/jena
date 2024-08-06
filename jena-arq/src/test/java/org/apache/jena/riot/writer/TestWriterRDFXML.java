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
 * distributed under the License is distributed on an "AS IS" BASIS,rdf:parseType="Literal"
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.riot.writer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

/**
 * Additional tests for RDF/XML writing.
 */
public class TestWriterRDFXML {
    @Test public void testRDFXMLLIterals_1() {
        // Valid RDF XMLLiteral.
        testXMLLiteral("<ns:text xmlns:ns='http://example/ns#'>ABC</ns:text>", true);
    }

    @Test public void testRDFXMLLIterals_2() {
        // Valid RDF XMLLiteral. Document fragment.
        testXMLLiteral("<ns:text xmlns:ns='http://example/ns#'>ABC</ns:text><content>XYZ</content>", true);
    }

    @Test public void testRDFXMLLIterals_bad_1() {
        // Invalid RDF XMLLiteral. Not self contained XML - no namespaces
        testXMLLiteral("<ns:text>ABC</ns:text>", false);
    }

    @Test public void testRDFXMLLIterals_bad_2() {
        // Invalid RDF XMLLiteral. Not valid XML - no namespaces
        testXMLLiteral("<ns:text>ABC", false);
    }

    private static void testXMLLiteral(String lex, boolean expectValid) {
        var m = ModelFactory.createDefaultModel();//createOntologyModel();
        m.createResource().addProperty(RDFS.comment, m.createTypedLiteral(lex, RDF.dtXMLLiteral));
        String s = RDFWriter.source(m).format(RDFFormat.RDFXML_ABBREV).toString();

        // Valid RDF XML Literal - write as rdf:parseType Literal
        // Invalid RDF XML Literal - write as rdf:datatype

        assertEquals(expectValid, s.contains("rdf:parseType=\"Literal\""));
        assertEquals(expectValid, ! s.contains("rdf:datatype="));

        LogCtl.withLevel(SysRIOT.getLogger(), "ERROR", ()->{
            Graph g = RDFParser.fromString(s, Lang.RDFXML).toGraph();
        });
    }
}
