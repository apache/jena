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
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.text.ShaclIndexMapping.FieldDef;
import org.apache.jena.query.text.ShaclIndexMapping.FieldType;
import org.apache.jena.query.text.ShaclIndexMapping.IndexProfile;
import org.apache.jena.query.text.assembler.ShaclIndexAssembler;
import org.apache.jena.query.text.cql.CqlExpression;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for spatial filtering via WKT literals and LatLonShape fields.
 */
public class TestSpatialFiltering {

    private static final String NS = "http://example.org/";
    private static final String FP = "urn:jena:lucene:field#";
    private static final String GEO = "http://www.opengis.net/ont/geosparql#";
    private static final Node SITE_CLASS = NodeFactory.createURI(NS + "Site");
    private static final Node TITLE_PRED = NodeFactory.createURI(NS + "title");
    private static final Node ASWKT_PRED = NodeFactory.createURI(GEO + "asWKT");

    private Dataset dataset;
    private ShaclTextIndexLucene textIndex;
    private ShaclIndexMapping mapping;

    @Before
    public void setUp() {
        FieldDef titleField = new FieldDef("title", FieldType.TEXT, null,
            true, true, false, false, false, true,
            Collections.singleton(TITLE_PRED));

        FieldDef locationField = new FieldDef("location", FieldType.LATLON, null,
            true, true, false, false, false, false,
            Collections.singleton(ASWKT_PRED));

        IndexProfile siteProfile = new IndexProfile(
            NodeFactory.createURI(NS + "SiteShape"),
            Collections.singleton(SITE_CLASS),
            "uri", "docType",
            Arrays.asList(titleField, locationField));

        mapping = new ShaclIndexMapping(Collections.singletonList(siteProfile));
        EntityDefinition defn = ShaclIndexAssembler.deriveEntityDefinition(mapping);

        TextIndexConfig config = new TextIndexConfig(defn);
        config.setShaclMapping(mapping);
        config.setValueStored(true);

        ByteBuffersDirectory dir = new ByteBuffersDirectory();
        textIndex = new ShaclTextIndexLucene(dir, config);

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

            // Mount Isa, QLD — EPSG:4326 (lat/lon order)
            addSite(model, "mount-isa", "Mount Isa Mine",
                "<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(-20.73 139.49)");

            // Olympic Dam, SA — EPSG:4326
            addSite(model, "olympic-dam", "Olympic Dam",
                "<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(-30.43 136.88)");

            // Boddington, WA — EPSG:4326
            addSite(model, "boddington", "Boddington Gold Mine",
                "<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(-32.77 116.35)");

            // Cadia Valley, NSW — CRS84 (bare WKT, lon/lat order)
            addSite(model, "cadia-valley", "Cadia Valley Operations",
                "POINT(148.99 -33.47)");

            // Auckland, NZ — outside Australia bbox (should be excluded)
            addSite(model, "auckland", "Auckland Site",
                "<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(-36.85 174.76)");

            dataset.commit();
        } finally {
            dataset.end();
        }
    }

    private void addSite(Model model, String id, String title, String wkt) {
        Resource site = ResourceFactory.createResource(NS + id);
        model.add(site, RDF.type, ResourceFactory.createResource(NS + "Site"));
        model.add(site, ResourceFactory.createProperty(NS, "title"), title);
        model.addLiteral(site, ResourceFactory.createProperty(GEO, "asWKT"),
            ResourceFactory.createTypedLiteral(wkt,
                org.apache.jena.datatypes.TypeMapper.getInstance()
                    .getSafeTypeByName(GEO + "wktLiteral")));
    }

    @After
    public void tearDown() {
        if (dataset != null) {
            dataset.close();
        }
    }

    @Test
    public void testBboxReturnsEntitiesWithinBounds() {
        // Australia bbox: [112, -44, 154, -10] (swLon, swLat, neLon, neLat)
        CqlExpression filter = new CqlExpression.CqlSpatial(
            "s_intersects", FP + "location", "{\"bbox\":[112,-44,154,-10]}");

        List<TextHit> results = textIndex.queryWithCql(
            null, "*", filter, null, null, null, 100, null);

        Set<String> uris = new HashSet<>();
        for (TextHit hit : results) {
            uris.add(hit.getNode().getURI());
        }

        // 4 Australian sites should match
        assertTrue("Mount Isa should be in results", uris.contains(NS + "mount-isa"));
        assertTrue("Olympic Dam should be in results", uris.contains(NS + "olympic-dam"));
        assertTrue("Boddington should be in results", uris.contains(NS + "boddington"));
        assertTrue("Cadia Valley should be in results", uris.contains(NS + "cadia-valley"));
        // Auckland is outside Australia
        assertFalse("Auckland should NOT be in results", uris.contains(NS + "auckland"));
    }

    @Test
    public void testBboxExcludesEntitiesOutsideBounds() {
        // Small bbox around WA only: [115, -34, 120, -20]
        CqlExpression filter = new CqlExpression.CqlSpatial(
            "s_intersects", FP + "location", "{\"bbox\":[115,-34,120,-20]}");

        List<TextHit> results = textIndex.queryWithCql(
            null, "*", filter, null, null, null, 100, null);

        Set<String> uris = new HashSet<>();
        for (TextHit hit : results) {
            uris.add(hit.getNode().getURI());
        }

        // Only Boddington is in WA bbox
        assertTrue("Boddington should be in results", uris.contains(NS + "boddington"));
        assertFalse("Mount Isa should NOT be in WA bbox", uris.contains(NS + "mount-isa"));
        assertFalse("Olympic Dam should NOT be in WA bbox", uris.contains(NS + "olympic-dam"));
    }

    @Test
    public void testCombinedTextAndSpatialFilter() {
        // Text search for "mine" + spatial filter for Australia
        CqlExpression filter = new CqlExpression.CqlSpatial(
            "s_intersects", FP + "location", "{\"bbox\":[112,-44,154,-10]}");

        List<TextHit> results = textIndex.queryWithCql(
            null, "mine", filter, null, null, null, 100, null);

        Set<String> uris = new HashSet<>();
        for (TextHit hit : results) {
            uris.add(hit.getNode().getURI());
        }

        // "Mount Isa Mine" and "Boddington Gold Mine" contain "mine"
        assertTrue("Mount Isa Mine should match", uris.contains(NS + "mount-isa"));
        assertTrue("Boddington Gold Mine should match", uris.contains(NS + "boddington"));
        // "Olympic Dam" doesn't contain "mine"
        assertFalse("Olympic Dam should NOT match text 'mine'", uris.contains(NS + "olympic-dam"));
    }

    @Test
    public void testCrs84AxisSwap() {
        // Cadia Valley was indexed with bare WKT (CRS84: lon/lat order).
        // Verify it's findable with a bbox around its location.
        // Cadia is at ~(-33.47, 148.99) in lat/lon
        CqlExpression filter = new CqlExpression.CqlSpatial(
            "s_intersects", FP + "location", "{\"bbox\":[148,-34,150,-33]}");

        List<TextHit> results = textIndex.queryWithCql(
            null, "*", filter, null, null, null, 100, null);

        Set<String> uris = new HashSet<>();
        for (TextHit hit : results) {
            uris.add(hit.getNode().getURI());
        }

        assertTrue("Cadia Valley (CRS84) should be found in its bbox", uris.contains(NS + "cadia-valley"));
    }

    @Test
    public void testEpsg4326NoSwap() {
        // Mount Isa was indexed with EPSG:4326 (lat/lon order).
        // Verify it's at the correct location: lat=-20.73, lon=139.49
        CqlExpression filter = new CqlExpression.CqlSpatial(
            "s_intersects", FP + "location", "{\"bbox\":[139,-21,140,-20]}");

        List<TextHit> results = textIndex.queryWithCql(
            null, "*", filter, null, null, null, 100, null);

        Set<String> uris = new HashSet<>();
        for (TextHit hit : results) {
            uris.add(hit.getNode().getURI());
        }

        assertTrue("Mount Isa (EPSG:4326) should be found", uris.contains(NS + "mount-isa"));
    }

    @Test
    public void testUnsupportedSpatialOpIsResidual() {
        // s_within is not yet supported — should produce residual, not error
        CqlExpression filter = new CqlExpression.CqlSpatial(
            "s_within", FP + "location", "{\"bbox\":[112,-44,154,-10]}");

        // Should not throw — residual ops are logged as warnings and ignored
        List<TextHit> results = textIndex.queryWithCql(
            null, "*", filter, null, null, null, 100, null);

        // All entities returned (no spatial filter applied, just text *)
        assertTrue("Should return results when spatial op is residual", results.size() >= 4);
    }

    @Test
    public void testParseWktToLuceneFieldsPoint() {
        List<org.apache.lucene.index.IndexableField> fields =
            ShaclTextIndexLucene.parseWktToLuceneFields("location",
                "<http://www.opengis.net/def/crs/EPSG/0/4326> POINT(-33.87 151.21)", true);

        assertFalse("Should produce fields for a point", fields.isEmpty());
        // Should have LatLonShape fields + LatLonPoint + StoredField
        boolean hasStored = false;
        for (org.apache.lucene.index.IndexableField f : fields) {
            if (f instanceof org.apache.lucene.document.StoredField) {
                hasStored = true;
            }
        }
        assertTrue("Should include stored field", hasStored);
    }

    @Test
    public void testParseWktToLuceneFieldsPolygon() {
        String wkt = "<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON((-22.8 118.0, -22.8 119.2, -21.8 119.2, -21.8 118.0, -22.8 118.0))";
        List<org.apache.lucene.index.IndexableField> fields =
            ShaclTextIndexLucene.parseWktToLuceneFields("location", wkt, false);

        assertFalse("Should produce fields for a polygon", fields.isEmpty());
    }

    @Test
    public void testInvalidWktProducesNoFields() {
        List<org.apache.lucene.index.IndexableField> fields =
            ShaclTextIndexLucene.parseWktToLuceneFields("location", "NOT_WKT", false);

        assertTrue("Invalid WKT should produce empty fields", fields.isEmpty());
    }
}
