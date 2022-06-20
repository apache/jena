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

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkAdapter;
import org.junit.Assert;
import org.junit.Test;

public class TestRDFConnectionRewrapping {

    /** RDFLinkAdapter prior to jena 4.6.0 did not delegate the newUpdate / newQuery methods */
    @Test
    public void testLinkOverConnBuilders() {
        Dataset ds = DatasetFactory.create();

        // Create an RDFConnection instance that is NOT an RDFLink adapter
        // (otherwise unwrapping will detect the RDFLink and use that instead)
        RDFConnection conn = new RDFConnectionWrapper(RDFConnection.connect(ds));
        RDFLink link = RDFLinkAdapter.adapt(conn);

        link.newUpdate().update("INSERT DATA { <urn:s> <urn:p> <urn:o>}").build().execute();
        Assert.assertTrue(link.newQuery().query("ASK { ?s ?p ?o }").build().ask());
    }


    @Test
    public void testLinkOverConnGetDataset() {
        // Create a connection that returns a null dataset to indicate that it
        // is not backed by a dataset
        // (or that fetching a dataset is infeasible such as from  dbpedia)
        RDFConnection conn = new RDFConnectionWrapper(RDFConnection.connect(DatasetFactory.create())) {
            @Override
            public Dataset fetchDataset() {
                return null;
            }
        };

        RDFLink link = RDFLinkAdapter.adapt(conn);
        /* Prior to jena 4.6.0 calling getDataset() would raise a NPE */
        link.getDataset();
    }
}
