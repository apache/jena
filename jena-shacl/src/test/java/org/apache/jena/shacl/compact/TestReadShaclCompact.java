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

package org.apache.jena.shacl.compact;

import static org.junit.Assert.assertTrue;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.shacl.Shapes;

public class TestReadShaclCompact extends AbstractTestShaclCompact {

    @Override
    protected void runTest(String fn, String ttl, String fileBaseName) {
        Shapes shapes = ShaclcParser.parse(fn, BASE);
        //System.out.printf("R = %d : S = %d\n", shapes.numRootShapes(), shapes.numShapes());
        Graph expected = RDFDataMgr.loadGraph(ttl);

        Graph graphGot = shapes.getGraph();
        Graph graphOther = expected;

        boolean isomorphic = graphGot.isIsomorphicWith(graphOther);
        if ( ! isomorphic ) {
            System.err.println("---- "+fn);
            System.err.println("Different (R)");
            System.err.println("graph(jena) = "+graphGot.size());
            System.err.println("graph(ref)  = "+graphOther.size());
            if ( false ) {
                RDFWriter.source(graphGot).format(RDFFormat.TURTLE_PRETTY).output(System.err);
                RDFWriter.source(graphOther).format(RDFFormat.TURTLE_PRETTY).output(System.err);
            }
        }
        assertTrue("test: "+fileBaseName, isomorphic);
    }
}

