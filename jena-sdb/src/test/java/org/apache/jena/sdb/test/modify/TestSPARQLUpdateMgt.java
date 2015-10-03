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

package org.apache.jena.sdb.test.modify;

import junit.framework.JUnit4TestAdapter ;
import junit.framework.TestSuite ;
import org.apache.jena.sdb.SDBFactory ;
import org.apache.jena.sdb.Store ;
import org.apache.jena.sdb.test.junit.SDBTestUtils ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.modify.AbstractTestUpdateGraphMgt ;

public class TestSPARQLUpdateMgt extends AbstractTestUpdateGraphMgt
{

    public static TestSuite suite() {
        TestSuite ts = new TestSuite();
        ts.addTest(new JUnit4TestAdapter(TestSPARQLUpdateMgt.class));
        return ts;
    }
    
    @Override
    protected DatasetGraph getEmptyDatasetGraph() {
        Store store = SDBTestUtils.createInMemoryStore() ;
        return SDBFactory.connectDataset(store).asDatasetGraph() ;
    }
}
