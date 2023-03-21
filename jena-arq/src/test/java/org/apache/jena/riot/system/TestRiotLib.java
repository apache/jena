/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.riot.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestRiotLib {

    private PrefixMap prefixMap;

    @Before
    public void setupMaps() {
        // Mappings from requirement JENA-1262
        this.prefixMap = new PrefixMapStd();
        this.prefixMap.add("lcc-3166-1", "http://www.omg.org/spec/LCC/Countries/ISO3166-1-CountryCodes/");
        this.prefixMap.add("dct", "http://purl.org/dc/terms/");
        this.prefixMap.add("owl", "http://www.w3.org/2002/07/owl#");
        this.prefixMap.add("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        this.prefixMap.add("lcc-3166-2", "http://www.omg.org/spec/LCC/Countries/ISO3166-2-SubdivisionCodes/");
        this.prefixMap.add("xsd", "http://www.w3.org/2001/XMLSchema#");
        this.prefixMap.add("skos", "http://www.w3.org/2004/02/skos/core#");
        this.prefixMap.add("sm", "http://www.omg.org/techprocess/ab/SpecificationMetadata/");
        this.prefixMap.add("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        this.prefixMap.add("lcc-lr", "http://www.omg.org/spec/LCC/Languages/LanguageRepresentation/");
        this.prefixMap.add("lcc-cr", "http://www.omg.org/spec/LCC/Countries/CountryRepresentation/");
    }

    // Expected result transformed into new style from requirement JENA-1262
    private static final String expectedNewStyle = "PREFIX dct:        <http://purl.org/dc/terms/>\n" +
            "PREFIX lcc-3166-1: <http://www.omg.org/spec/LCC/Countries/ISO3166-1-CountryCodes/>\n" +
            "PREFIX lcc-3166-2: <http://www.omg.org/spec/LCC/Countries/ISO3166-2-SubdivisionCodes/>\n" +
            "PREFIX lcc-cr:     <http://www.omg.org/spec/LCC/Countries/CountryRepresentation/>\n" +
            "PREFIX lcc-lr:     <http://www.omg.org/spec/LCC/Languages/LanguageRepresentation/>\n" +
            "PREFIX owl:        <http://www.w3.org/2002/07/owl#>\n" +
            "PREFIX rdf:        <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX rdfs:       <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX skos:       <http://www.w3.org/2004/02/skos/core#>\n" +
            "PREFIX sm:         <http://www.omg.org/techprocess/ab/SpecificationMetadata/>\n" +
            "PREFIX xsd:        <http://www.w3.org/2001/XMLSchema#>\n";

    // Expected result from requirement JENA-1262
    private static final String expectedOldStyle = "@prefix dct:        <http://purl.org/dc/terms/> .\n" +
            "@prefix lcc-3166-1: <http://www.omg.org/spec/LCC/Countries/ISO3166-1-CountryCodes/> .\n" +
            "@prefix lcc-3166-2: <http://www.omg.org/spec/LCC/Countries/ISO3166-2-SubdivisionCodes/> .\n" +
            "@prefix lcc-cr:     <http://www.omg.org/spec/LCC/Countries/CountryRepresentation/> .\n" +
            "@prefix lcc-lr:     <http://www.omg.org/spec/LCC/Languages/LanguageRepresentation/> .\n" +
            "@prefix owl:        <http://www.w3.org/2002/07/owl#> .\n" +
            "@prefix rdf:        <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
            "@prefix rdfs:       <http://www.w3.org/2000/01/rdf-schema#> .\n" +
            "@prefix skos:       <http://www.w3.org/2004/02/skos/core#> .\n" +
            "@prefix sm:         <http://www.omg.org/techprocess/ab/SpecificationMetadata/> .\n" +
            "@prefix xsd:        <http://www.w3.org/2001/XMLSchema#> .\n";

    @Test
    public void sortPrefixesNewStyle() {
        IndentedLineBuffer writer = new IndentedLineBuffer();
        RiotLib.writePrefixes(writer, prefixMap, true);

        String result = writer.asString();

        Assert.assertEquals(expectedNewStyle, result);
    }

    @Test
    public void sortPrefixesOldStyle() {
        IndentedLineBuffer writer = new IndentedLineBuffer();
        RiotLib.writePrefixes(writer, prefixMap, false);

        String result = writer.asString();

        Assert.assertEquals(expectedOldStyle, result);
    }

    @Test
    public void bnodeAsURI_1() {
        // Start at blank node.
        Node bn = NodeFactory.createBlankNode();
        Node n1 = RiotLib.blankNodeToIri(bn);
        assertTrue(n1.isURI());
        Node n2 = RiotLib.fromIRIorBNode(n1);
        assertTrue(n2.isBlank());
        assertEquals(bn, n2);
    }

    @Test
    public void bnodeAsURI_2() {
        // Start at URI.
        String x = "_:bdd2e1f1-dea6-4822-8018-5c5e97530595";
        Node n0 = NodeFactory.createURI(x);
        Node n1 = RiotLib.fromIRIorBNode(n0);
        assertTrue(n1.isBlank());
        Node n2 = RiotLib.fromIRIorBNode(n0);
        assertTrue(n2.isBlank());
        Node n3 = RiotLib.blankNodeToIri(n2);
        assertTrue(n3.isURI());
        assertEquals(x, n3.getURI());
    }
}
