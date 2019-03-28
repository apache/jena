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

package org.apache.jena.tdb2.graph;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.AbstractTestGraphOverDatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.tdb2.junit.TL;
import org.junit.After ;

/** This is the view-graph test suite run over a TDB DatasetGraph to check compatibility */
public class TestGraphOverDatasetTDB extends AbstractTestGraphOverDatasetGraph
{
    DatasetGraph dsg = null;
    @After public void after2() {
        if ( dsg == null )
            return;
        dsg.abort();
        dsg.end();
        TL.expel(dsg);
    }
    
    @Override
    protected DatasetGraph createBaseDSG() {
        // Called in initialization.
        if ( dsg == null ) {
            dsg = TL.createTestDatasetGraphMem() ;
            dsg.begin(ReadWrite.WRITE);
        }
        return dsg ;
    }
    
    @Override
    protected Graph makeNamedGraph(DatasetGraph dsg, Node gn)
    {
        return dsg.getGraph(gn) ;
    }

    @Override
    protected Graph makeDefaultGraph(DatasetGraph dsg)
    {
        return  dsg.getDefaultGraph() ;
    }
}

