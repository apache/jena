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
package org.apache.jena.geosparql.spatial.index;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.jena.geosparql.configuration.GeoSPARQLOperations;
import org.apache.jena.geosparql.spatial.SpatialIndexException;
import org.apache.jena.geosparql.spatial.index.v2.SpatialIndexIoKryo;
import org.apache.jena.geosparql.spatial.index.v2.SpatialIndexPerGraph;
import org.apache.jena.geosparql.spatial.index.v2.SpatialIndexLib;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.sparql.core.DatasetGraph;
import org.junit.Assert;
import org.locationtech.jts.geom.Envelope;

public class SpatialIndexCurrent
    implements SpatialIndexLifeCycle
{
    protected DatasetGraph dsg;
    protected Envelope envelope;
    protected String srs;
    protected boolean validate;

    public SpatialIndexCurrent(DatasetGraph dsg, Envelope envelope, String srs, boolean validate) {
        super();
        this.dsg = dsg;
        this.envelope = envelope;
        this.validate = validate;
        this.srs = srs;
    }

    public static SpatialIndexCurrent setup(String data, Envelope envelope, String srs, boolean validate) throws SpatialIndexException {
        DatasetGraph dsg = RDFParserBuilder.create().fromString(data).lang(Lang.TURTLE).toDatasetGraph();
        return new SpatialIndexCurrent(dsg, envelope, srs, validate);
    }

    protected Path indexFile;
    protected String finalSrs = null;
    protected SpatialIndexPerGraph indexA;
    protected SpatialIndexPerGraph indexB;

    @Override
    public void init() {
        SpatialIndexLib.setSpatialIndex(dsg.getContext(), null);
    }

    @Override
    public void findSrs() {
        finalSrs = srs == null
            ? GeoSPARQLOperations.findModeSRS(DatasetFactory.wrap(dsg))
            : srs;
    }

    @Override
    public void build() throws Exception {
        indexFile = Files.createTempFile("jena-", ".spatial-index");
        indexA = SpatialIndexLib.buildSpatialIndex(dsg, finalSrs);
        SpatialIndexIoKryo.save(indexFile, indexA);
    }

    @Override
    public void load() throws Exception {
        indexB = SpatialIndexIoKryo.load(indexFile);
    }

    @Override
    public void close() throws Exception {
        Files.deleteIfExists(indexFile);

        if (validate) {
            int itemCountA = indexA.query(envelope, null).size();
            int itemCountB = indexB.query(envelope, null).size();
            // Assert.assertTrue(itemCountA > 0);
            Assert.assertEquals(itemCountA, itemCountB);
        }
    }
}
