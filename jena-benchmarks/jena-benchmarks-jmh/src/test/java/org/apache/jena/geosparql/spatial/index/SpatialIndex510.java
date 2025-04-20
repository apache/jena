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

import org.apache.shadedJena510.geosparql.configuration.GeoSPARQLOperations;
import org.apache.shadedJena510.geosparql.spatial.SpatialIndex;
import org.apache.shadedJena510.geosparql.spatial.SpatialIndexException;
import org.apache.shadedJena510.query.Dataset;
import org.apache.shadedJena510.riot.Lang;
import org.apache.shadedJena510.riot.RDFParserBuilder;
import org.junit.Assert;
import org.locationtech.jts.geom.Envelope;

public class SpatialIndex510
    implements SpatialIndexLifeCycle
{
    protected Dataset ds;
    protected Envelope envelope;
    protected String srs;
    protected boolean validate;

    public SpatialIndex510(Dataset ds, Envelope envelope, String srs, boolean validate) {
        super();
        this.ds = ds;
        this.envelope = envelope;
        this.validate = validate;
        this.srs = srs;
    }

    public static SpatialIndex510 setup(String data, Envelope envelope, String srs, boolean validate) throws SpatialIndexException {
        Dataset ds = RDFParserBuilder.create().fromString(data).lang(Lang.TURTLE).toDataset();
        return new SpatialIndex510(ds, envelope, srs, validate);
    }

    protected Path indexFile;
    protected String finalSrs = null;
    protected SpatialIndex indexA;
    protected SpatialIndex indexB;

    @Override
    public void init() {
        ds.getContext().remove(SpatialIndex.SPATIAL_INDEX_SYMBOL);
    }

    @Override
    public void findSrs() {
        finalSrs = srs == null
            ? GeoSPARQLOperations.findModeSRS(ds)
            : srs;
    }

    @Override
    public void build() throws Exception {
        indexFile = Files.createTempFile("jena-", ".spatial-index");
        Files.deleteIfExists(indexFile); // buildSpatialIndex in v1 will attempt to load the file first

        indexA = SpatialIndex.buildSpatialIndex(ds, finalSrs, indexFile.toFile());
    }

    @Override
    public void load() throws Exception {
        indexB = SpatialIndex.load(indexFile.toFile());
    }

    @Override
    public void close() throws Exception {
        Files.deleteIfExists(indexFile);

        if (validate) {
            int itemCountA = indexA.query(envelope).size();
            int itemCountB = indexB.query(envelope).size();
            // Assert.assertTrue(itemCountA > 0);
            Assert.assertEquals(itemCountA, itemCountB);
        }
    }
}
