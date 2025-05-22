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

package org.apache.jena.fuseki.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.apache.jena.fuseki.build.DatasetDescriptionMap;
import org.apache.jena.fuseki.build.FusekiConfig;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.assembler.VocabTDB2;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.Test;

public class TestDatasetDescriptionMap {

    private static final Model dsModel;
    private static final Resource dsDesc1;
    private static final Resource dsDesc2;
    private DatasetDescriptionMap registry = new DatasetDescriptionMap();

    static {
        dsModel = ModelFactory.createDefaultModel();
        dsDesc1 = dsModel.createResource()
                .addProperty(RDF.type, VocabTDB2.tDatasetTDB)
                .addProperty(VocabTDB2.pLocation, "--mem--");
        dsDesc2 = dsModel.createResource()
                .addProperty(RDF.type, VocabTDB2.tDatasetTDB)
                .addProperty(VocabTDB2.pLocation, "--mem--");
    }
    @Test
    public void testVerifySameDatasetObjectForSameDescription() {
        DatasetDescriptionMap registry = new DatasetDescriptionMap();
        DatasetGraph ds1 = FusekiConfig.getDataset(dsDesc1.getModel().getGraph(), dsDesc1.asNode(), registry);
        DatasetGraph ds2 = FusekiConfig.getDataset(dsDesc1.getModel().getGraph(), dsDesc1.asNode(), registry);
        assertEquals(ds1, ds2);
    }

    @Test
    public void testVerifyDifferentDatasetObjectsForDifferentDescriptions() {
        DatasetDescriptionMap registry = new DatasetDescriptionMap();
        DatasetGraph ds1 = FusekiConfig.getDataset(dsDesc1.getModel().getGraph(), dsDesc1.asNode(), registry);
        DatasetGraph ds2 = FusekiConfig.getDataset(dsDesc2.getModel().getGraph(), dsDesc2.asNode(), registry);
        assertNotEquals(ds1, ds2);
    }
}
