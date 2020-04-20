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

package org.apache.jena.shacl.parser;

import java.util.Collection;
import java.util.List;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.shacl.engine.Target;
import org.apache.jena.shacl.validation.Severity;

public class NodeShape extends Shape {

    public NodeShape(Graph shapeGraph, Node shapeNode, boolean deactivated,
                     Severity severity, List<Node> messages, Collection<Target> targets,
                     List<Constraint> constraints, List<PropertyShape> propertyShapes) {
        super(shapeGraph, shapeNode, deactivated, severity, messages, targets, constraints, propertyShapes);
    }

    @Override
    public void visit(ShapeVisitor visitor) {  visitor.visit(this); }

    @Override
    public void printHeader(IndentedWriter out) {
        out.print("NodeShape");
    }

    @Override
    public String toString() {
        String x = "NodeShape["+shapeNode+"]";
        if ( deactivated() )
            x = x + " deactivated";
        return x;
    }
}
