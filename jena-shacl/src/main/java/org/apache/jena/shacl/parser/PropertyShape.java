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
import java.util.Objects;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.engine.ShaclPaths;
import org.apache.jena.shacl.engine.Target;
import org.apache.jena.shacl.validation.Severity;
import org.apache.jena.sparql.path.Path;

public class PropertyShape extends Shape {

    private final Path path;
    public PropertyShape(Graph shapeGraph, Node shapeNode, boolean isDeactivated, Severity severity, Collection<Node> messages,
                         Collection<Target> targets, Path path, List<Constraint> constraints, List<PropertyShape> propertyShapes) {
        super(shapeGraph, shapeNode, isDeactivated, severity, messages, targets, constraints, propertyShapes);
        this.path = Objects.requireNonNull(path, "path");
    }

    @Override
    public void visit(ShapeVisitor visitor) { visitor.visit(this); }

    @Override
    public boolean isPropertyShape() {
        return true;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public void printHeader(IndentedWriter out, NodeFormatter nodeFmt) {
        out.print("PropertyShape ");
        ShaclPaths.write(out, path, nodeFmt);
    }

    @Override
    public String toString() {
        //String str = shapeNode.isBlank()? "" : shapeNode.toString();
        String x = "PropertyShape["+getShapeNode()+" -> "+path+"]";
        if ( deactivated() )
            x = x + " deactivated";
        return x;
    }
}
