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
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests modelled on the demo mining dataset.
 * Covers multiple entity types (Report, Borehole, Site), multi-valued keyword
 * fields, sequence paths (authorName), facet wildcard, CQL filters, and
 * combined query+facet SPARQL patterns.
 */
public class TestDemoMiningScenarios {

    private static final String EX = "http://example.org/mining/";
    private static final String FP = "urn:jena:lucene:field#";

    // Predicates
    private static final Node LABEL = RDFS.label.asNode();
    private static final Node COMMODITY = NodeFactory.createURI(EX + "commodity");
    private static final Node STATE = NodeFactory.createURI(EX + "state");
    private static final Node OPERATOR = NodeFactory.createURI(EX + "operator");
    private static final Node STATUS = NodeFactory.createURI(EX + "status");
    private static final Node AUTHORED_BY = NodeFactory.createURI(EX + "authoredBy");
    private static final Node NAME = NodeFactory.createURI(EX + "name");
    private static final Node AUTHORED = NodeFactory.createURI(EX + "authored");
    private static final Node DEPTH = NodeFactory.createURI(EX + "depth");

    // Classes
    private static final Node REPORT_CLASS = NodeFactory.createURI(EX + "MiningReport");
    private static final Node BOREHOLE_CLASS = NodeFactory.createURI(EX + "Borehole");
    private static final Node SITE_CLASS = NodeFactory.createURI(EX + "Site");

    private Dataset dataset;

    @Before
    public void setUp() {
        TextQuery.init();

        // --- Field definitions ---
        FieldDef entityType = new FieldDef("entityType", FieldType.KEYWORD, null,
            true, true, true, false, false, false,
            Collections.singleton(RDF.type.asNode()),
            PathFactory.pathLink(RDF.type.asNode()));

        FieldDef title = new FieldDef("title", FieldType.TEXT, null,
            true, true, false, false, false, true,
            Collections.singleton(LABEL),
            PathFactory.pathLink(LABEL));

        FieldDef commodity = new FieldDef("commodity", FieldType.KEYWORD, null,
            true, true, true, false, true, false,
            Collections.singleton(COMMODITY),
            PathFactory.pathLink(COMMODITY));

        FieldDef state = new FieldDef("state", FieldType.KEYWORD, null,
            true, true, true, false, false, false,
            Collections.singleton(STATE),
            PathFactory.pathLink(STATE));

        FieldDef operator = new FieldDef("operator", FieldType.KEYWORD, null,
            true, true, true, false, false, false,
            Collections.singleton(OPERATOR),
            PathFactory.pathLink(OPERATOR));

        FieldDef status = new FieldDef("status", FieldType.KEYWORD, null,
            true, true, true, false, false, false,
            Collections.singleton(STATUS),
            PathFactory.pathLink(STATUS));

        // Sequence path: (ex:authoredBy / ex:name)
        Path authorNamePath = PathFactory.pathSeq(
            PathFactory.pathLink(AUTHORED_BY),
            PathFactory.pathLink(NAME));
        Set<Node> authorNamePreds = new LinkedHashSet<>();
        authorNamePreds.add(AUTHORED_BY);
        authorNamePreds.add(NAME);
        FieldDef authorName = new FieldDef("authorName", FieldType.KEYWORD, null,
            true, true, true, false, false, false,
            authorNamePreds, authorNamePath);

        // Inverse path: ^ex:authored
        Path authoredByUriPath = PathFactory.pathInverse(PathFactory.pathLink(AUTHORED));
        FieldDef authoredByUri = new FieldDef("authoredByUri", FieldType.KEYWORD, null,
            true, true, false, false, true, false,
            Collections.singleton(AUTHORED), authoredByUriPath);

        FieldDef depth = new FieldDef("depth", FieldType.INT, null,
            true, true, false, true, false, false,
            Collections.singleton(DEPTH),
            PathFactory.pathLink(DEPTH));

        // --- Profiles (shapes) ---
        IndexProfile reportProfile = new IndexProfile(
            NodeFactory.createURI(EX + "MiningReportShape"),
            Collections.singleton(REPORT_CLASS),
            "uri", "docType",
            Arrays.asList(entityType, title, commodity, state, operator, status, authorName, authoredByUri));

        IndexProfile boreholeProfile = new IndexProfile(
            NodeFactory.createURI(EX + "BoreholeShape"),
            Collections.singleton(BOREHOLE_CLASS),
            "uri", "docType",
            Arrays.asList(entityType, title, commodity, state, depth));

        IndexProfile siteProfile = new IndexProfile(
            NodeFactory.createURI(EX + "SiteShape"),
            Collections.singleton(SITE_CLASS),
            "uri", "docType",
            Arrays.asList(entityType, title, commodity, state, status));

        ShaclIndexMapping mapping = new ShaclIndexMapping(
            Arrays.asList(reportProfile, boreholeProfile, siteProfile));
        EntityDefinition defn = ShaclIndexAssembler.deriveEntityDefinition(mapping);

        TextIndexConfig config = new TextIndexConfig(defn);
        config.setShaclMapping(mapping);
        config.setFacetFields(mapping.getFacetFieldNames());
        config.setValueStored(true);

        ByteBuffersDirectory dir = new ByteBuffersDirectory();
        ShaclTextIndexLucene textIndex = new ShaclTextIndexLucene(dir, config);

        Dataset baseDs = DatasetFactory.create();
        ShaclTextDocProducer producer = new ShaclTextDocProducer(
            baseDs.asDatasetGraph(), textIndex, mapping);

        dataset = TextDatasetFactory.create(baseDs, textIndex, true, producer);

        loadDemoData();
    }

    private void loadDemoData() {
        dataset.begin(ReadWrite.WRITE);
        try {
            Model m = dataset.getDefaultModel();

            // --- Authors (must exist before reports for sequence paths) ---
            addAuthor(m, "author-jones", "Dr Sarah Jones");
            addAuthor(m, "author-chen", "Prof Wei Chen");
            addAuthor(m, "author-williams", "James Williams");
            addAuthor(m, "author-patel", "Dr Priya Patel");

            // Authored relationships (for inverse path)
            addAuthored(m, "author-jones", "report-mia-2023");
            addAuthored(m, "author-jones", "report-od-2024");
            addAuthored(m, "author-chen", "report-mia-2021");
            addAuthored(m, "author-chen", "report-bh-1985");
            addAuthored(m, "author-williams", "report-pil-2024");
            addAuthored(m, "author-williams", "report-pil-exploration");
            addAuthored(m, "author-patel", "report-bod-2022");
            addAuthored(m, "author-patel", "report-cad-2023");

            // --- Sites ---
            addSite(m, "site-mount-isa", "Mount Isa Mine",
                new String[]{"commodity/Copper", "commodity/Lead", "commodity/Zinc"},
                "state/QLD", "status/Active");
            addSite(m, "site-olympic-dam", "Olympic Dam",
                new String[]{"commodity/Copper", "commodity/Uranium", "commodity/Gold"},
                "state/SA", "status/Active");
            addSite(m, "site-boddington", "Boddington Gold Mine",
                new String[]{"commodity/Gold", "commodity/Copper"},
                "state/WA", "status/Active");
            addSite(m, "site-broken-hill", "Broken Hill Mine",
                new String[]{"commodity/Lead", "commodity/Zinc", "commodity/Silver"},
                "state/NSW", "status/Historical");
            addSite(m, "site-pilbara-iron", "Pilbara Iron Ore Hub",
                new String[]{"commodity/Iron-Ore"},
                "state/WA", "status/Active");
            addSite(m, "site-ok-tedi", "Ok Tedi Mine",
                new String[]{"commodity/Copper", "commodity/Gold"},
                "state/PNG", "status/Active");

            // --- Boreholes ---
            addBorehole(m, "bh-mia-001", "MIA-DDH-001 Mount Isa Diamond Drill Hole",
                new String[]{"commodity/Copper"}, "state/QLD", 450);
            addBorehole(m, "bh-od-001", "OD-RC-001 Olympic Dam Reverse Circulation Hole",
                new String[]{"commodity/Copper", "commodity/Uranium"}, "state/SA", 280);
            addBorehole(m, "bh-bod-001", "BOD-DDH-001 Boddington Deep Diamond Hole",
                new String[]{"commodity/Gold"}, "state/WA", 600);
            addBorehole(m, "bh-bh-001", "BHM-DDH-001 Broken Hill Legacy Drill Hole",
                new String[]{"commodity/Lead", "commodity/Silver"}, "state/NSW", 210);
            addBorehole(m, "bh-pil-001", "PIL-RC-001 Pilbara Iron Ore Reverse Circulation",
                new String[]{"commodity/Iron-Ore"}, "state/WA", 150);

            // --- Reports (added last — depends on authors for sequence paths) ---
            addReport(m, "report-mia-2023", "Mount Isa Copper Resource Estimation 2023",
                new String[]{"commodity/Copper"}, "state/QLD", "operator/Glencore", "status/Current",
                "author-jones");
            addReport(m, "report-mia-2021", "Mount Isa Lead-Zinc Exploration Summary",
                new String[]{"commodity/Lead", "commodity/Zinc"}, "state/QLD", "operator/Glencore", "status/Current",
                "author-chen");
            addReport(m, "report-od-2024", "Olympic Dam Expansion Feasibility Study",
                new String[]{"commodity/Copper", "commodity/Uranium"}, "state/SA", "operator/BHP", "status/Current",
                "author-jones");
            addReport(m, "report-bod-2022", "Boddington Gold Production Report 2022",
                new String[]{"commodity/Gold", "commodity/Copper"}, "state/WA", "operator/Newmont", "status/Current",
                "author-patel");
            addReport(m, "report-bh-1985", "Broken Hill Lead-Zinc Historical Assessment",
                new String[]{"commodity/Lead", "commodity/Zinc", "commodity/Silver"},
                "state/NSW", "operator/BHP", "status/Historical",
                "author-chen");
            addReport(m, "report-cad-2023", "Cadia Valley Gold-Copper Resource Update",
                new String[]{"commodity/Gold", "commodity/Copper"}, "state/NSW", "operator/Newcrest", "status/Current",
                "author-patel");
            addReport(m, "report-pil-2024", "Pilbara Iron Ore Reserves Statement 2024",
                new String[]{"commodity/Iron-Ore"}, "state/WA", "operator/Rio-Tinto", "status/Current",
                "author-williams");
            addReport(m, "report-pil-exploration", "Pilbara Greenfield Exploration Program Results",
                new String[]{"commodity/Iron-Ore"}, "state/WA", "operator/Rio-Tinto", "status/Current",
                "author-williams");

            dataset.commit();
        } finally {
            dataset.end();
        }
    }

    // --- Helper methods ---

    private void addAuthor(Model m, String id, String name) {
        Resource author = ResourceFactory.createResource(EX + id);
        m.add(author, RDF.type, ResourceFactory.createResource(EX + "Author"));
        m.add(author, ResourceFactory.createProperty(EX + "name"), name);
    }

    private void addAuthored(Model m, String authorId, String reportId) {
        m.add(ResourceFactory.createResource(EX + authorId),
            ResourceFactory.createProperty(EX + "authored"),
            ResourceFactory.createResource(EX + reportId));
    }

    private void addSite(Model m, String id, String label, String[] commodities,
                         String stateId, String statusId) {
        Resource site = ResourceFactory.createResource(EX + id);
        m.add(site, RDF.type, ResourceFactory.createResource(EX + "Site"));
        m.add(site, RDFS.label, label);
        for (String c : commodities) {
            m.add(site, ResourceFactory.createProperty(EX + "commodity"),
                ResourceFactory.createResource(EX + c));
        }
        m.add(site, ResourceFactory.createProperty(EX + "state"),
            ResourceFactory.createResource(EX + stateId));
        m.add(site, ResourceFactory.createProperty(EX + "status"),
            ResourceFactory.createResource(EX + statusId));
    }

    private void addBorehole(Model m, String id, String label, String[] commodities,
                             String stateId, int depth) {
        Resource bh = ResourceFactory.createResource(EX + id);
        m.add(bh, RDF.type, ResourceFactory.createResource(EX + "Borehole"));
        m.add(bh, RDFS.label, label);
        for (String c : commodities) {
            m.add(bh, ResourceFactory.createProperty(EX + "commodity"),
                ResourceFactory.createResource(EX + c));
        }
        m.add(bh, ResourceFactory.createProperty(EX + "state"),
            ResourceFactory.createResource(EX + stateId));
        m.add(bh, ResourceFactory.createProperty(EX + "depth"),
            ResourceFactory.createTypedLiteral(depth));
    }

    private void addReport(Model m, String id, String label, String[] commodities,
                           String stateId, String operatorId, String statusId,
                           String authorId) {
        Resource report = ResourceFactory.createResource(EX + id);
        m.add(report, RDF.type, ResourceFactory.createResource(EX + "MiningReport"));
        m.add(report, RDFS.label, label);
        for (String c : commodities) {
            m.add(report, ResourceFactory.createProperty(EX + "commodity"),
                ResourceFactory.createResource(EX + c));
        }
        m.add(report, ResourceFactory.createProperty(EX + "state"),
            ResourceFactory.createResource(EX + stateId));
        m.add(report, ResourceFactory.createProperty(EX + "operator"),
            ResourceFactory.createResource(EX + operatorId));
        m.add(report, ResourceFactory.createProperty(EX + "status"),
            ResourceFactory.createResource(EX + statusId));
        m.add(report, ResourceFactory.createProperty(EX + "authoredBy"),
            ResourceFactory.createResource(EX + authorId));
    }

    @After
    public void tearDown() {
        if (dataset != null) {
            dataset.close();
        }
    }

    // ================================================================
    // Full-text search
    // ================================================================

    @Test
    public void testSearchCopper() {
        Set<String> results = lucQuery("copper", null, 50);
        // "copper" appears in titles of multiple reports, boreholes, and sites
        assertFalse("Should find results for 'copper'", results.isEmpty());
        assertTrue("Should find Mount Isa report",
            results.contains(EX + "report-mia-2023"));
    }

    @Test
    public void testSearchGold() {
        Set<String> results = lucQuery("gold", null, 50);
        assertFalse("Should find results for 'gold'", results.isEmpty());
        assertTrue("Should find Boddington site",
            results.contains(EX + "site-boddington"));
        assertTrue("Should find Boddington report",
            results.contains(EX + "report-bod-2022"));
    }

    @Test
    public void testSearchIronOre() {
        Set<String> results = lucQuery("iron ore", null, 50);
        assertFalse("Should find results for 'iron ore'", results.isEmpty());
        assertTrue("Should find Pilbara site",
            results.contains(EX + "site-pilbara-iron"));
    }

    @Test
    public void testWildcardMatchAll() {
        Set<String> results = lucQuery("*", null, 100);
        // 6 sites + 5 boreholes + 8 reports = 19 entities
        assertEquals("Wildcard should match all 19 entities", 19, results.size());
    }

    // ================================================================
    // Filtered search (CQL)
    // ================================================================

    @Test
    public void testFilterByState() {
        String filter = "{\"op\":\"=\",\"args\":[{\"property\":\"" + FP + "state\"},\"" + EX + "state/QLD\"]}";
        Set<String> results = lucQuery("*", filter, 50);
        // QLD entities: site-mount-isa, bh-mia-001, report-mia-2023, report-mia-2021, bh-mia-002 (not present)
        assertTrue("Should find Mount Isa site", results.contains(EX + "site-mount-isa"));
        assertTrue("Should find Mount Isa borehole", results.contains(EX + "bh-mia-001"));
        assertTrue("Should find Mount Isa report 2023", results.contains(EX + "report-mia-2023"));
        // WA entities should not appear
        assertFalse("Boddington (WA) should not appear", results.contains(EX + "site-boddington"));
    }

    @Test
    public void testFilterByCommodity() {
        String filter = "{\"op\":\"=\",\"args\":[{\"property\":\"" + FP + "commodity\"},\"" + EX + "commodity/Gold\"]}";
        Set<String> results = lucQuery("*", filter, 50);
        assertTrue("Should find Olympic Dam (has Gold)", results.contains(EX + "site-olympic-dam"));
        assertTrue("Should find Boddington (has Gold)", results.contains(EX + "site-boddington"));
        assertTrue("Should find Ok Tedi (has Gold)", results.contains(EX + "site-ok-tedi"));
        assertFalse("Mount Isa (no Gold) should not appear", results.contains(EX + "site-mount-isa"));
        assertFalse("Pilbara (Iron-Ore only) should not appear", results.contains(EX + "site-pilbara-iron"));
    }

    @Test
    public void testFilterByEntityType() {
        String filter = "{\"op\":\"=\",\"args\":[{\"property\":\"" + FP + "entityType\"},\"" + EX + "Site\"]}";
        Set<String> results = lucQuery("*", filter, 50);
        assertEquals("Should find exactly 6 sites", 6, results.size());
        for (String uri : results) {
            assertTrue("All results should be sites", uri.contains("site-"));
        }
    }

    @Test
    public void testFilterCombinedAnd() {
        // Gold in WA
        String filter = "{\"op\":\"and\",\"args\":[" +
            "{\"op\":\"=\",\"args\":[{\"property\":\"" + FP + "commodity\"},\"" + EX + "commodity/Gold\"]}," +
            "{\"op\":\"=\",\"args\":[{\"property\":\"" + FP + "state\"},\"" + EX + "state/WA\"]}" +
            "]}";
        Set<String> results = lucQuery("*", filter, 50);
        assertTrue("Should find Boddington site (Gold in WA)",
            results.contains(EX + "site-boddington"));
        assertTrue("Should find Boddington borehole (Gold in WA)",
            results.contains(EX + "bh-bod-001"));
        assertFalse("Olympic Dam (SA) should not appear",
            results.contains(EX + "site-olympic-dam"));
    }

    @Test
    public void testFilterFtsAndCql() {
        // "copper" text + state=QLD
        String filter = "{\"op\":\"=\",\"args\":[{\"property\":\"" + FP + "state\"},\"" + EX + "state/QLD\"]}";
        Set<String> results = lucQuery("copper", filter, 50);
        for (String uri : results) {
            // All results should be QLD entities that mention copper
            assertFalse("No WA results", uri.contains("boddington") || uri.contains("pilbara"));
        }
    }

    // ================================================================
    // Facet counts
    // ================================================================

    @Test
    public void testFacetCountsCommodity() {
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) luc:facet (\"default\" \"*\" '[\"" + FP + "commodity\"]' 20)\n" +
            "}";

        Map<String, Long> counts = facetCountMap(sparql);
        assertFalse("Should have commodity facets", counts.isEmpty());
        // Copper appears in most entities
        assertTrue("Should have Copper facet", counts.containsKey(EX + "commodity/Copper"));
        assertTrue("Copper count should be > 1", counts.get(EX + "commodity/Copper") > 1);
    }

    @Test
    public void testFacetCountsState() {
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) luc:facet (\"default\" \"*\" '[\"" + FP + "state\"]' 20)\n" +
            "}";

        Map<String, Long> counts = facetCountMap(sparql);
        // QLD: site-mount-isa, bh-mia-001, report-mia-2023, report-mia-2021 = 4
        assertEquals("QLD should have 4 entities", Long.valueOf(4), counts.get(EX + "state/QLD"));
        // WA: site-boddington, site-pilbara-iron, bh-bod-001, bh-pil-001,
        //     report-bod-2022, report-pil-2024, report-pil-exploration = 7
        assertEquals("WA should have 7 entities", Long.valueOf(7), counts.get(EX + "state/WA"));
        // NSW: site-broken-hill, bh-bh-001, report-bh-1985, report-cad-2023 = 4
        assertEquals("NSW should have 4 entities", Long.valueOf(4), counts.get(EX + "state/NSW"));
        // SA: site-olympic-dam, bh-od-001, report-od-2024 = 3
        assertEquals("SA should have 3 entities", Long.valueOf(3), counts.get(EX + "state/SA"));
        // PNG: site-ok-tedi = 1
        assertEquals("PNG should have 1 entity", Long.valueOf(1), counts.get(EX + "state/PNG"));
    }

    @Test
    public void testFacetCountsMultipleFields() {
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) luc:facet (\"default\" \"*\" '[\"" + FP + "state\", \"" + FP + "commodity\"]' 20)\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                boolean foundState = false;
                boolean foundCommodity = false;
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    String field = sol.getResource("f").getURI();
                    if (field.equals(FP + "state")) foundState = true;
                    if (field.equals(FP + "commodity")) foundCommodity = true;
                }
                assertTrue("Should have state facets", foundState);
                assertTrue("Should have commodity facets", foundCommodity);
            }
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testFacetCountsWithFilter() {
        // Facets for Gold entities only
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) luc:facet (\"default\" \"*\" '[\"" + FP + "state\"]' " +
            "'{\"op\":\"=\",\"args\":[{\"property\":\"" + FP + "commodity\"},\"" + EX + "commodity/Gold\"]}' 20)\n" +
            "}";

        Map<String, Long> counts = facetCountMap(sparql);
        assertFalse("Should have state facets for Gold entities", counts.isEmpty());
        // QLD has no Gold entities
        assertFalse("QLD should not appear (no Gold)", counts.containsKey(EX + "state/QLD"));
    }

    // ================================================================
    // Facet wildcard ["*"] — PR #40
    // ================================================================

    @Test
    public void testFacetWildcardExpandsAllFields() {
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) luc:facet (\"default\" \"*\" '[\"*\"]' 10)\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                Set<String> fields = new HashSet<>();
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    fields.add(sol.getResource("f").getURI());
                }
                // All facetable fields should appear
                assertTrue("Should have entityType", fields.contains(FP + "entityType"));
                assertTrue("Should have commodity", fields.contains(FP + "commodity"));
                assertTrue("Should have state", fields.contains(FP + "state"));
                assertTrue("Should have operator", fields.contains(FP + "operator"));
                assertTrue("Should have status", fields.contains(FP + "status"));
                assertTrue("Should have authorName", fields.contains(FP + "authorName"));
            }
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testFacetWildcardMatchesExplicitFields() {
        // Verify wildcard returns same results as listing all facetable fields explicitly
        String wildcardSparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) luc:facet (\"default\" \"*\" '[\"*\"]' 0)\n" +
            "}";
        String explicitSparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) luc:facet (\"default\" \"*\" '[\"" +
            FP + "entityType\", \"" +
            FP + "commodity\", \"" +
            FP + "state\", \"" +
            FP + "operator\", \"" +
            FP + "status\", \"" +
            FP + "authorName\"]' 0)\n" +
            "}";

        Map<String, Map<String, Long>> wildcardResults = facetResultMap(wildcardSparql);
        Map<String, Map<String, Long>> explicitResults = facetResultMap(explicitSparql);

        assertEquals("Wildcard and explicit should produce same fields",
            explicitResults.keySet(), wildcardResults.keySet());

        for (String field : explicitResults.keySet()) {
            assertEquals("Counts should match for " + field,
                explicitResults.get(field), wildcardResults.get(field));
        }
    }

    @Test
    public void testFacetWildcardWithTextSearch() {
        // Wildcard facets with text search for "copper"
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) luc:facet (\"default\" \"copper\" '[\"*\"]' 10)\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                Set<String> fields = new HashSet<>();
                int count = 0;
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    fields.add(sol.getResource("f").getURI());
                    count++;
                }
                assertTrue("Should have facet results", count > 0);
                assertTrue("Should have state facets", fields.contains(FP + "state"));
            }
        } finally {
            dataset.end();
        }
    }

    @Test
    public void testFacetWildcardWithCqlFilter() {
        // Wildcard facets with CQL filter
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) luc:facet (\"default\" \"*\" '[\"*\"]' " +
            "'{\"op\":\"=\",\"args\":[{\"property\":\"" + FP + "state\"},\"" + EX + "state/WA\"]}' 10)\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                Set<String> fields = new HashSet<>();
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    fields.add(sol.getResource("f").getURI());
                }
                assertTrue("Should have multiple facet fields", fields.size() > 1);
                assertTrue("Should have commodity facets", fields.contains(FP + "commodity"));
            }
        } finally {
            dataset.end();
        }
    }

    // ================================================================
    // Sequence path — authorName
    // ================================================================

    @Test
    public void testAuthorNameFacetCounts() {
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) luc:facet (\"default\" \"*\" '[\"" + FP + "authorName\"]' 10)\n" +
            "}";

        Map<String, Long> counts = facetCountMap(sparql);
        // Dr Sarah Jones: report-mia-2023, report-od-2024 = 2
        assertEquals("Dr Sarah Jones should have 2 reports",
            Long.valueOf(2), counts.get("Dr Sarah Jones"));
        // Prof Wei Chen: report-mia-2021, report-bh-1985 = 2
        assertEquals("Prof Wei Chen should have 2 reports",
            Long.valueOf(2), counts.get("Prof Wei Chen"));
        // James Williams: report-pil-2024, report-pil-exploration = 2
        assertEquals("James Williams should have 2 reports",
            Long.valueOf(2), counts.get("James Williams"));
        // Dr Priya Patel: report-bod-2022, report-cad-2023 = 2
        assertEquals("Dr Priya Patel should have 2 reports",
            Long.valueOf(2), counts.get("Dr Priya Patel"));
    }

    @Test
    public void testFilterByAuthorName() {
        String filter = "{\"op\":\"=\",\"args\":[{\"property\":\"" + FP + "authorName\"},\"Dr Sarah Jones\"]}";
        Set<String> results = lucQuery("*", filter, 50);
        assertEquals("Dr Sarah Jones authored 2 reports", 2, results.size());
        assertTrue(results.contains(EX + "report-mia-2023"));
        assertTrue(results.contains(EX + "report-od-2024"));
    }

    // ================================================================
    // Combined query + facet in same SPARQL query
    // ================================================================

    @Test
    public void testCombinedQueryAndFacet() {
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?entity ?score ?f ?v ?c WHERE {\n" +
            "  { (?entity ?score) luc:query (\"default\" \"iron ore\" 20) }\n" +
            "  UNION\n" +
            "  { (?f ?v ?c) luc:facet (\"default\" \"iron ore\" '[\"" + FP + "state\"]' 10) }\n" +
            "}";

        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                Set<String> entities = new HashSet<>();
                Map<String, Long> facets = new HashMap<>();
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    if (sol.contains("entity")) {
                        entities.add(sol.getResource("entity").getURI());
                    }
                    if (sol.contains("f") && sol.get("f") != null) {
                        facets.put(sol.get("v").toString(), sol.getLiteral("c").getLong());
                    }
                }
                assertFalse("Should have entity results", entities.isEmpty());
                assertFalse("Should have facet results", facets.isEmpty());
            }
        } finally {
            dataset.end();
        }
    }

    // ================================================================
    // Multi-valued fields
    // ================================================================

    @Test
    public void testMultiValuedCommodityFilter() {
        // Olympic Dam has Copper, Uranium, Gold — filter by Uranium should find it
        String filter = "{\"op\":\"=\",\"args\":[{\"property\":\"" + FP + "commodity\"},\"" + EX + "commodity/Uranium\"]}";
        Set<String> results = lucQuery("*", filter, 50);
        assertTrue("Should find Olympic Dam site", results.contains(EX + "site-olympic-dam"));
        assertTrue("Should find Olympic Dam borehole", results.contains(EX + "bh-od-001"));
        assertTrue("Should find Olympic Dam report", results.contains(EX + "report-od-2024"));
        // Nothing else has Uranium
        assertEquals("Only 3 entities have Uranium", 3, results.size());
    }

    @Test
    public void testEntityTypeDistribution() {
        String sparql = "PREFIX luc: <urn:jena:lucene:index#>\n" +
            "SELECT ?f ?v ?c WHERE {\n" +
            "  (?f ?v ?c) luc:facet (\"default\" \"*\" '[\"" + FP + "entityType\"]' 10)\n" +
            "}";

        Map<String, Long> counts = facetCountMap(sparql);
        assertEquals("Should have 6 Sites", Long.valueOf(6), counts.get(EX + "Site"));
        assertEquals("Should have 5 Boreholes", Long.valueOf(5), counts.get(EX + "Borehole"));
        assertEquals("Should have 8 MiningReports", Long.valueOf(8), counts.get(EX + "MiningReport"));
    }

    // ================================================================
    // Helpers
    // ================================================================

    private Set<String> lucQuery(String queryText, String filter, int limit) {
        StringBuilder sb = new StringBuilder();
        sb.append("PREFIX luc: <urn:jena:lucene:index#>\n");
        sb.append("SELECT ?s WHERE {\n");
        sb.append("  (?s ?score) luc:query (\"default\" \"").append(queryText).append("\"");
        if (filter != null) {
            sb.append(" '").append(filter).append("'");
        }
        sb.append(" ").append(limit).append(")\n}");

        Set<String> results = new HashSet<>();
        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sb.toString(), dataset)) {
                ResultSet rs = qe.execSelect();
                while (rs.hasNext()) {
                    results.add(rs.next().getResource("s").getURI());
                }
            }
        } finally {
            dataset.end();
        }
        return results;
    }

    private Map<String, Long> facetCountMap(String sparql) {
        Map<String, Long> counts = new HashMap<>();
        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    String value = sol.get("v").isURIResource()
                        ? sol.getResource("v").getURI()
                        : sol.getLiteral("v").getString();
                    counts.put(value, sol.getLiteral("c").getLong());
                }
            }
        } finally {
            dataset.end();
        }
        return counts;
    }

    private Map<String, Map<String, Long>> facetResultMap(String sparql) {
        Map<String, Map<String, Long>> result = new HashMap<>();
        dataset.begin(ReadWrite.READ);
        try {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    String field = sol.getResource("f").getURI();
                    String value = sol.get("v").isURIResource()
                        ? sol.getResource("v").getURI()
                        : sol.getLiteral("v").getString();
                    long count = sol.getLiteral("c").getLong();
                    result.computeIfAbsent(field, k -> new HashMap<>()).put(value, count);
                }
            }
        } finally {
            dataset.end();
        }
        return result;
    }
}
