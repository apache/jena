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
import org.apache.jena.query.Query;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.dataset.DatasetGraphOverRDFLink;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.update.UpdateRequest;

public class TestRDFConnectionDatasetGraphOverRDFLinkWithTxn extends AbstractTestRDFConnection {
    @Override
    protected boolean supportsAbort() { return false; }

    @Override
    protected RDFConnection connection() {
        DatasetGraph backendDsg = DatasetGraphFactory.create();
        DatasetGraph frontendDsg = new DatasetGraphOverRDFLink(() -> RDFLink.connect(backendDsg), true, true) {
            @Override
            protected QueryExec query(Query query) {
                // Future: With RDFLink unwrapping in jena-rdfconnection this method should be bypassed
                //         using an internal call to frontendDsg.newLink().newQuery().
                // throw new UnsupportedOperationException("Should not be called");

                return super.query(query);
            }

            @Override
            protected UpdateExec update(UpdateRequest updateRequest) {
                // Future: With RDFLink unwrapping in jena-rdfconnection this method should be bypassed
                //         using an internal call to frontendDsg.newLink().newUpdate().
                // throw new UnsupportedOperationException("Should not be called");

                return super.update(updateRequest);
            }
        };
        Dataset dataset = DatasetFactory.wrap(frontendDsg);
        return RDFConnection.connect(dataset);
    }
}
