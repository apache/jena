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

package org.apache.jena.tdb2.store;

import org.apache.jena.graph.Graph ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.graph.AbstractTestGraphAddDelete ;
import org.apache.jena.tdb2.junit.TL;
import org.junit.After ;
import org.junit.Before ;

/** Programmatic tests on graphs */
public class TestGraphTDB extends AbstractTestGraphAddDelete
{
    private Dataset dataset ;
    private Graph   graph ;

    @Before
    public void before() {
        dataset = TL.createTestDatasetMem() ;
        dataset.begin(ReadWrite.WRITE);
        graph = dataset.getDefaultModel().getGraph() ;
    }

    @After
    public void after() {
        dataset.abort();
        dataset.end();
        TL.expel(dataset) ;
    }

    @Override
    protected Graph emptyGraph() {
        graph.clear() ;
        return graph ;
    }
    
    @Override
    protected void returnGraph(Graph g)
    {}
}
