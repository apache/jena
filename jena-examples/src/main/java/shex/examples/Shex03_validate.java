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

package shex.examples;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shex.*;
import org.apache.jena.shex.sys.ShexLib;
import org.apache.jena.vocabulary.RDF;

/** Validate based on building a shape map in the code. */
public class Shex03_validate {
    static { LogCtl.setLogging(); }

    public static void main(String ...args) {
        String SHAPES = "examples/schema.shex";
        String DATA = "examples/data.ttl";

        System.out.println("Read data");
        Graph dataGraph = RDFDataMgr.loadGraph(DATA);

        System.out.println("Read shapes");
        ShexSchema shapes = Shex.readSchema(SHAPES);

        // Shapes map.
        Node myClass = NodeFactory.createURI("http://example/myClass");
        Triple instancesOfFoo = Triple.create(Shex.FOCUS, RDF.type.asNode(), myClass);
        Node shape1 = NodeFactory.createURI("http://example/shapes#shape1");

        ShapeMap shapeMap = ShapeMap.newBuilder()
                .add(instancesOfFoo, shape1)
                .build();

        // Equivalent helper for map with one ShapeMap entry
        ShapeMap shapeMapAlt = ShapeMap.record(instancesOfFoo, shape1);

        // Validate
        System.out.println();
        System.out.println("Validate");
        ShexReport report = ShexValidator.get().validate(dataGraph, shapes, shapeMap);
        System.out.println();
        ShexLib.printReport(report);
    }
}
