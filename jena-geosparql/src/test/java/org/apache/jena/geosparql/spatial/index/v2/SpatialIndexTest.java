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
package org.apache.jena.geosparql.spatial.index.v2;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.geosparql.implementation.GeometryWrapperFactory;
import org.apache.jena.geosparql.implementation.SRSInfo;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.geosparql.spatial.SearchEnvelope;
import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.geosparql.spatial.SpatialIndexItem;
import org.apache.jena.geosparql.spatial.SpatialIndexTestData;
import org.apache.jena.geosparql.spatial.index.compat.SpatialIndexIo;
import org.apache.jena.geosparql.spatial.index.v1.SpatialIndexV1;
import org.apache.jena.geosparql.spatial.index.v2.GeometryGenerator.GeometryType;
import org.apache.jena.graph.Node;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.junit.Assert;
import org.junit.Test;
import org.locationtech.jts.geom.Envelope;

@SuppressWarnings("removal")
public class SpatialIndexTest {

    @Test
    public void testLegacyLoading() throws IOException, SpatialIndexException {
        Path file = Files.createTempFile("jena-", ".spatial.index");
        try {
            List<SpatialIndexItem> items = SpatialIndexTestData.getTestItems();

            SpatialIndexV1.save(file.toFile(), items, SRS_URI.DEFAULT_WKT_CRS84);

            SpatialIndex index = SpatialIndexIo.load(file, true);
            Envelope envelope = new Envelope(-90, 0, 0, 90);
            Collection<Node> actual = index.query(envelope, null);
            Set<Node> expected = Set.of(SpatialIndexTestData.LONDON_FEATURE.asNode(), SpatialIndexTestData.NEW_YORK_FEATURE.asNode());
            Assert.assertEquals(expected, actual);
        } finally {
            Files.delete(file);
        }
    }

    @Test
    public void testSerdeSpatialIndex() throws IOException, SpatialIndexException {
        // create spatial index
        SpatialIndexPerGraph index1 = SpatialIndexTestData.createTestIndex();

        // query index 1
        SRSInfo srsInfo1 = index1.getSrsInfo();
        SearchEnvelope searchEnvelope1 = SearchEnvelope.build(GeometryWrapperFactory.createPolygon(srsInfo1.getDomainEnvelope(), WKTDatatype.URI), srsInfo1);
        Collection<Node> res1 = searchEnvelope1.check(index1);

        // save to tmp file
        // File file = new File("/tmp/test-spatial.index"); //File.createTempFile( "jena", "spatial.index");
        Path file = Files.createTempFile("jena-", ".spatial-index");
        try {
            SpatialIndexIoKryo.save(file, index1);

            // load from tmp file as new index 2
            SpatialIndex index2 = SpatialIndexIo.load(file);

            // query index 2
            SRSInfo srsInfo2 = index2.getSrsInfo();
            SearchEnvelope searchEnvelope2 = SearchEnvelope.build(GeometryWrapperFactory.createPolygon(srsInfo2.getDomainEnvelope(), WKTDatatype.URI), srsInfo2);
            Collection<Node> res2 = searchEnvelope2.check(index2);

            assertEquals(srsInfo1, srsInfo2);
            assertEquals(res1, res2);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    /**
     * Generate a dataset with geometries of all sorts,
     * build the spatial index for it and
     * attempt to save/load it.
     */
    @Test
    public void testSerdeSpatialIndex2() throws IOException, SpatialIndexException {
        DatasetGraph dsg = DatasetGraphFactory.create();

        Envelope envelope = new Envelope(-170, 170, -85, 85);
        Map<GeometryType, Number> config = GeometryGenerator.createConfig(100);
        long expectedItemCount = GeometryGenerator.generateGraph(dsg.getDefaultGraph(), envelope, config);

        Path file = Files.createTempFile("jena-", ".spatial-index");
        try {
            // Query the built index (no IO)
            SpatialIndexPerGraph indexA = (SpatialIndexPerGraph)SpatialIndexLib.buildSpatialIndex(dsg);
            long itemCountA = indexA.query(envelope, null).size();
            Assert.assertEquals(expectedItemCount, itemCountA);

            // Save the index with the latest format and load from file.
            SpatialIndexIoKryo.save(file, indexA);
            SpatialIndex indexB = SpatialIndexIoKryo.load(file);
            long itemCountB = indexB.query(envelope, null).size();
            Assert.assertEquals(expectedItemCount, itemCountB);

            // Save the index with legacy format and load from file.
            // File must not exist or the index might/will attempt to load it.
            Files.delete(file);
            SpatialIndexV1.buildSpatialIndex(DatasetFactory.wrap(dsg), file.toFile());
            SpatialIndex indexC = SpatialIndexIo.load(file, true);
            long itemCountC = indexC.query(envelope, null).size();
            Assert.assertEquals(expectedItemCount, itemCountC);
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
