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
package org.apache.jena.geosparql.spatial;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.GeometryWrapperFactory;
import org.apache.jena.geosparql.implementation.SRSInfo;
import org.apache.jena.geosparql.implementation.datatype.WKTDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.graph.Node;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.junit.Test;
import org.locationtech.jts.geom.Envelope;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import static org.junit.Assert.*;

public class SpatialIndexTest {


    @Test
    public void testSerdeSpatialIndex() throws IOException, SpatialIndexException {
        // create spatial index
        SpatialIndex index1 = SpatialIndexTestData.createTestIndex();

        // query index 1
        SRSInfo srsInfo1 = index1.getSrsInfo();
        SearchEnvelope searchEnvelope1 = SearchEnvelope.build(GeometryWrapperFactory.createPolygon(srsInfo1.getDomainEnvelope(), WKTDatatype.URI), srsInfo1);
        HashSet<Node> res1 = searchEnvelope1.check(index1);

        // save to tmp file
        File file = new File("/tmp/test-spatial.index"); //File.createTempFile( "jena", "spatial.index");
        // File file = File.createTempFile( "jena", "spatial.index");
        file.deleteOnExit();
        SpatialIndex.save(file, index1);

        // load from tmp file as new index 2
        SpatialIndex index2 = SpatialIndex.load(file);

        // query index 2
        SRSInfo srsInfo2 = index2.getSrsInfo();
        SearchEnvelope searchEnvelope2 = SearchEnvelope.build(GeometryWrapperFactory.createPolygon(srsInfo2.getDomainEnvelope(), WKTDatatype.URI), srsInfo2);
        HashSet<Node> res2 = searchEnvelope2.check(index2);

        assertEquals(srsInfo1, srsInfo2);
        assertEquals(res1, res2);
    }

}
