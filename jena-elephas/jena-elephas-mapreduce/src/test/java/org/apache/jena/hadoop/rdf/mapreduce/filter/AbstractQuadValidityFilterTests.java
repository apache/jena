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

package org.apache.jena.hadoop.rdf.mapreduce.filter;

import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.sparql.core.Quad ;

/**
 * Abstract tests for triple filter mappers that check triple validity
 * 
 * 
 * 
 */
public abstract class AbstractQuadValidityFilterTests extends AbstractNodeTupleFilterTests<Quad, QuadWritable> {

    @Override
    protected QuadWritable createValidValue(int i) {
        return new QuadWritable(
                new Quad(Quad.defaultGraphNodeGenerated, NodeFactory.createURI("http://subjects/" + i),
                        NodeFactory.createURI("http://predicate"), NodeFactory.createLiteral(Integer.toString(i),
                                XSDDatatype.XSDinteger)));
    }

    @Override
    protected QuadWritable createInvalidValue(int i) {
        switch (i % 8) {
        case 0:
            // Invalid to use Literal as Graph
            return new QuadWritable(new Quad(NodeFactory.createLiteral("invalid"), NodeFactory.createURI("http://subjects/" + i),
                    NodeFactory.createURI("http://predicate"), NodeFactory.createLiteral(Integer.toString(i),
                            XSDDatatype.XSDinteger)));
        case 1:
            // Invalid to use Variable as Graph
            return new QuadWritable(new Quad(NodeFactory.createVariable("invalid"),
                    NodeFactory.createURI("http://subjects/" + i), NodeFactory.createURI("http://predicate"),
                    NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger)));
        case 2:
            // Invalid to use Literal as Subject
            return new QuadWritable(new Quad(Quad.defaultGraphNodeGenerated, NodeFactory.createLiteral("invalid"),
                    NodeFactory.createURI("http://predicate"), NodeFactory.createLiteral(Integer.toString(i),
                            XSDDatatype.XSDinteger)));
        case 3:
            // Invalid to use Variable as Subject
            return new QuadWritable(new Quad(Quad.defaultGraphNodeGenerated, NodeFactory.createVariable("invalid"),
                    NodeFactory.createURI("http://predicate"), NodeFactory.createLiteral(Integer.toString(i),
                            XSDDatatype.XSDinteger)));
        case 4:
            // Invalid to use Blank Node as Predicate
            return new QuadWritable(new Quad(Quad.defaultGraphNodeGenerated, NodeFactory.createURI("http://subjects/" + i),
                    NodeFactory.createBlankNode(), NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger)));
        case 5:
            // Invalid to use Literal as Predicate
            return new QuadWritable(new Quad(Quad.defaultGraphNodeGenerated, NodeFactory.createURI("http://subjects/" + i),
                    NodeFactory.createLiteral("invalid"), NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger)));
        case 6:
            // Invalid to use Variable as Predicate
            return new QuadWritable(
                    new Quad(Quad.defaultGraphNodeGenerated, NodeFactory.createURI("http://subjects/" + i),
                            NodeFactory.createVariable("invalid"), NodeFactory.createLiteral(Integer.toString(i),
                                    XSDDatatype.XSDinteger)));
        default:
            // Invalid to use Variable as Object
            return new QuadWritable(new Quad(Quad.defaultGraphNodeGenerated, NodeFactory.createURI("http://subjects/" + i),
                    NodeFactory.createURI("http://predicate"), NodeFactory.createVariable("invalid")));
        }
    }
}
