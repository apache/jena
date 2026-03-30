/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.query.text;

import static org.junit.Assert.*;

import java.util.*;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.*;
import org.apache.jena.query.text.ShaclIndexMapping.FieldDef;
import org.apache.jena.query.text.ShaclIndexMapping.FieldType;
import org.apache.jena.query.text.ShaclIndexMapping.IndexProfile;
import org.apache.jena.query.text.assembler.ShaclIndexAssembler;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for per-field query analyzer support (idx:queryAnalyzer).
 * Verifies that edge n-gram indexing with keyword querying works for identifier fields.
 */
public class TestPerFieldQueryAnalyzer {

    private static final String NS = "http://example.org/";
    private static final Node SPECIMEN_CLASS = NodeFactory.createURI(NS + "Specimen");
    private static final Node IDENTIFIER_PRED = NodeFactory.createURI(NS + "identifier");
    private static final Node LABEL_PRED = NodeFactory.createURI(NS + "label");

    private Dataset dataset;

    /** Edge n-gram analyzer for indexing: keyword tokenizer → lowercase → edge n-grams (1-20). */
    private static Analyzer edgeNgramIndexAnalyzer() {
        return new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                Tokenizer tokenizer = new KeywordTokenizer();
                TokenStream stream = new LowerCaseFilter(tokenizer);
                stream = new EdgeNGramTokenFilter(stream, 1, 20, false);
                return new TokenStreamComponents(tokenizer, stream);
            }
        };
    }

    /** Simple lowercase analyzer for querying: keyword tokenizer → lowercase (no n-gramming). */
    private static Analyzer lowercaseKeywordAnalyzer() {
        return new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                Tokenizer tokenizer = new KeywordTokenizer();
                TokenStream stream = new LowerCaseFilter(tokenizer);
                return new TokenStreamComponents(tokenizer, stream);
            }
        };
    }

    @Before
    public void setUp() {
        FieldDef identifierField = new FieldDef("identifier", FieldType.TEXT,
            edgeNgramIndexAnalyzer(), lowercaseKeywordAnalyzer(),
            true, true, false, false, false, true,
            Collections.singleton(IDENTIFIER_PRED), null, null);

        FieldDef labelField = new FieldDef("label", FieldType.TEXT, null,
            true, true, false, false, false, false,
            Collections.singleton(LABEL_PRED));

        IndexProfile specimenProfile = new IndexProfile(
            NodeFactory.createURI(NS + "SpecimenShape"),
            Collections.singleton(SPECIMEN_CLASS),
            "uri", "docType",
            Arrays.asList(identifierField, labelField));

        ShaclIndexMapping mapping = new ShaclIndexMapping(Collections.singletonList(specimenProfile));
        EntityDefinition defn = ShaclIndexAssembler.deriveEntityDefinition(mapping);

        TextIndexConfig config = new TextIndexConfig(defn);
        config.setShaclMapping(mapping);
        config.setValueStored(true);

        ByteBuffersDirectory dir = new ByteBuffersDirectory();
        ShaclTextIndexLucene textIndex = new ShaclTextIndexLucene(dir, config);

        Dataset baseDs = DatasetFactory.create();
        ShaclTextDocProducer producer = new ShaclTextDocProducer(
            baseDs.asDatasetGraph(), textIndex, mapping);

        dataset = TextDatasetFactory.create(baseDs, textIndex, true, producer);

        loadTestData();
    }

    private void loadTestData() {
        dataset.begin(ReadWrite.WRITE);
        try {
            Model model = dataset.getDefaultModel();
            addSpecimen(model, "s1", "GSWA-12345-A", "Rock sample from Pilbara");
            addSpecimen(model, "s2", "GSWA-67890-B", "Mineral specimen");
            addSpecimen(model, "s3", "WAM-R-54321", "Reptile specimen");
            addSpecimen(model, "s4", "WAM-M-11111", "Mammal specimen");
            dataset.commit();
        } finally {
            dataset.end();
        }
    }

    private void addSpecimen(Model model, String id, String identifier, String label) {
        Resource specimen = ResourceFactory.createResource(NS + id);
        model.add(specimen, RDF.type, ResourceFactory.createResource(NS + "Specimen"));
        model.add(specimen, ResourceFactory.createProperty(NS + "identifier"), identifier);
        model.add(specimen, ResourceFactory.createProperty(NS + "label"), label);
    }

    @After
    public void tearDown() {
        if (dataset != null) {
            dataset.close();
        }
    }

    @Test
    public void testPrefixSearchFindsMatchingIdentifiers() {
        // "GSWA" should match both GSWA-prefixed identifiers via edge n-gram
        Set<String> results = queryIdentifier("GSWA");
        assertEquals(2, results.size());
        assertTrue(results.contains(NS + "s1"));
        assertTrue(results.contains(NS + "s2"));
    }

    @Test
    public void testLongerPrefixNarrowsResults() {
        // "GSWA-123" should only match GSWA-12345-A
        Set<String> results = queryIdentifier("GSWA-123");
        assertEquals(1, results.size());
        assertTrue(results.contains(NS + "s1"));
    }

    @Test
    public void testDifferentPrefixMatchesDifferentResults() {
        // "WAM" should match both WAM-prefixed identifiers
        Set<String> results = queryIdentifier("WAM");
        assertEquals(2, results.size());
        assertTrue(results.contains(NS + "s3"));
        assertTrue(results.contains(NS + "s4"));
    }

    @Test
    public void testExactIdentifierMatches() {
        // Full identifier should still match
        Set<String> results = queryIdentifier("WAM-R-54321");
        assertEquals(1, results.size());
        assertTrue(results.contains(NS + "s3"));
    }

    @Test
    public void testCaseInsensitivePrefixSearch() {
        // Lowercase prefix should match uppercase identifiers
        Set<String> results = queryIdentifier("gswa");
        assertEquals(2, results.size());
    }

    @Test
    public void testSingleCharPrefixSearch() {
        // "G" should match GSWA identifiers (min n-gram = 1)
        Set<String> results = queryIdentifier("G");
        assertEquals(2, results.size());
    }

    @Test
    public void testNoMatchForWrongPrefix() {
        Set<String> results = queryIdentifier("XYZ");
        assertTrue(results.isEmpty());
    }

    @Test
    public void testNormalFieldStillWorksAlongsidePrefixField() {
        // The "label" field uses the standard analyzer (no queryAnalyzer override).
        // Searching it should still work via the default search field.
        String queryStr =
            "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?s WHERE {\n" +
            "  (?s ?score) luc:query ('urn:jena:lucene:field#label' 'Pilbara') .\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qexec = QueryExecutionFactory.create(queryStr, dataset)) {
                ResultSet rs = qexec.execSelect();
                Set<String> results = new HashSet<>();
                while (rs.hasNext()) {
                    results.add(rs.next().getResource("s").getURI());
                }
                assertEquals("Only s1 has 'Pilbara' in label", 1, results.size());
                assertTrue(results.contains(NS + "s1"));
            }
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testPrefixSearchDoesNotMatchLabelField() {
        // "GSWA" is only in the identifier field. Searching the label field
        // should not return results for that term.
        String queryStr =
            "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?s WHERE {\n" +
            "  (?s ?score) luc:query ('urn:jena:lucene:field#label' 'GSWA') .\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qexec = QueryExecutionFactory.create(queryStr, dataset)) {
                ResultSet rs = qexec.execSelect();
                assertFalse("GSWA should not match label field", rs.hasNext());
            }
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testFieldDefWithoutQueryAnalyzerUsesIndexAnalyzer() {
        // When queryAnalyzer is null, getQueryAnalyzer() returns null —
        // the system falls back to using the index analyzer for queries.
        FieldDef noOverride = new FieldDef("test", FieldType.TEXT,
            edgeNgramIndexAnalyzer(),
            true, true, false, false, false, false,
            Collections.singleton(LABEL_PRED));
        assertNull("queryAnalyzer should be null when not set", noOverride.getQueryAnalyzer());
        assertNotNull("index analyzer should be present", noOverride.getAnalyzer());
    }

    @Test
    public void testEntityDefinitionQueryAnalyzerWiring() {
        // Verify that deriveEntityDefinition correctly wires queryAnalyzer
        // from FieldDef into EntityDefinition
        FieldDef idField = new FieldDef("identifier", FieldType.TEXT,
            edgeNgramIndexAnalyzer(), lowercaseKeywordAnalyzer(),
            true, true, false, false, false, true,
            Collections.singleton(IDENTIFIER_PRED), null, null);

        IndexProfile profile = new IndexProfile(
            NodeFactory.createURI(NS + "TestShape"),
            Collections.singleton(SPECIMEN_CLASS),
            "uri", "docType",
            Collections.singletonList(idField));

        ShaclIndexMapping mapping = new ShaclIndexMapping(Collections.singletonList(profile));
        EntityDefinition defn = ShaclIndexAssembler.deriveEntityDefinition(mapping);

        assertNotNull("EntityDefinition should have query analyzer for identifier",
            defn.getQueryAnalyzer("identifier"));
    }

    private Set<String> queryIdentifier(String prefix) {
        String queryStr =
            "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?s WHERE {\n" +
            "  (?s ?score) luc:query ('urn:jena:lucene:field#identifier' '" + prefix + "') .\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            Query query = QueryFactory.create(queryStr);
            try (QueryExecution qexec = QueryExecutionFactory.create(query, dataset)) {
                ResultSet rs = qexec.execSelect();
                Set<String> results = new HashSet<>();
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    results.add(sol.getResource("s").getURI());
                }
                return results;
            }
        } finally {
            dataset.end();
        }
    }
}
