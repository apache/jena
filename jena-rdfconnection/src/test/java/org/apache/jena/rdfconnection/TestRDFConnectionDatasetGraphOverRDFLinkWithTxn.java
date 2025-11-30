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

package org.apache.jena.rdfconnection;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.dataset.DatasetGraphOverRDFLink;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;

public class TestRDFConnectionDatasetGraphOverRDFLinkWithTxn extends AbstractTestRDFConnection {
    @Override
    protected boolean supportsAbort() { return false; }

    @Override
    protected RDFConnection connection() {
        DatasetGraph backendDsg = DatasetGraphFactory.create();
        DatasetGraphOverRDFLink frontendDsg = DatasetGraphOverRDFLink.newBuilder()
            .linkCreator(() -> RDFLink.connect(backendDsg))
            .supportsTransactions(true)
            .supportsTransactionAbort(true)
            .build();
        Dataset dataset = DatasetFactory.wrap(frontendDsg);

        // Here, RDFConnection.connect internally creates an RDFLinkDataset to the frontendDsg (DatasetGraphOverRDFLink).
        // DatasetGraphOverRDFLink could be extended with a flag that makes
        // RDFLink.connect directly return frontendDsg.newLink().
        return RDFConnection.connect(dataset);
    }
}
