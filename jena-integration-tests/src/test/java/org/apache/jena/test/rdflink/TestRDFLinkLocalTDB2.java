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

package org.apache.jena.test.rdflink;

import org.apache.jena.rdfconnection.Isolation;
import org.apache.jena.rdflink.AbstractTestRDFLink;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.DatabaseMgr;

public class TestRDFLinkLocalTDB2 extends AbstractTestRDFLink {

    @Override
    protected boolean supportsAbort() { return true ; }

    @Override
    protected RDFLink link() {
        DatasetGraph dsg = DatabaseMgr.createDatasetGraph() ;
        return RDFLinkFactory.connect(dsg, Isolation.COPY) ;
    }
}

