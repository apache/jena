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

package org.apache.jena.rdfconnection;

import static org.junit.Assert.assertEquals;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

public class TestLocalIsolation {

    private static Resource subject = ResourceFactory.createResource();
    private static Property property = ResourceFactory.createProperty("http://example/p");
    private static Resource object = ResourceFactory.createResource("http://example/o");
    
    @Test public void localIsolation_model_1() {
        isolationModel(Isolation.COPY,false);
    }

    @Test public void localIsolation_model_2() {
        isolationModel(Isolation.NONE, true);
    }

    @Test(expected=JenaException.class)
    public void localIsolation_model_3() {
        isolationModel(Isolation.READONLY, true);
    }

    @Test public void localIsolation_dataset_1() {
        isolationDataset(Isolation.COPY,false);
    }

    @Test public void localIsolation_dataset_2() {
        isolationDataset(Isolation.NONE,true);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void localIsolation_dataset_3() {
        isolationDataset(Isolation.READONLY, true);
    }

    private void isolationDataset(Isolation isolation, boolean expected) {
        Dataset base = DatasetFactory.createTxnMem();
        RDFConnection conn1 = RDFConnectionFactory.connect(base, isolation);
        Quad quad = SSE.parseQuad("(:g :s :p :o)") ; 
        try (RDFConnection conn2 = conn1;) {
            Dataset ds = conn2.fetchDataset();
            ds.asDatasetGraph().add(quad);
        }
        assertEquals(expected, base.asDatasetGraph().contains(quad));
    }

    private void isolationModel(Isolation level, boolean expected) {
        Dataset base = DatasetFactory.createTxnMem();
        Statement stmt = base.getDefaultModel().createStatement(subject, property, object); 
        RDFConnection conn1 = RDFConnectionFactory.connect(base, level);
        try (RDFConnection conn2 = conn1;) {
            Model m = conn2.fetch();
            m.add(stmt);
        }
        assertEquals(expected, base.getDefaultModel().contains(stmt));
    }
}
