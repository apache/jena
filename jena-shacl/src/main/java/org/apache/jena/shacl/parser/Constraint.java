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

import java.util.Set;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.shacl.compact.writer.ShaclNotCompactException;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.sparql.path.Path;

public interface Constraint {
    // Print - internal format
    public default void print(IndentedWriter out, NodeFormatter nodeFmt) {
        out.print(toString());
    }

    public default void printCompact(IndentedWriter out, NodeFormatter nodeFmt) {
        throw new ShaclNotCompactException("Not supported in compact syntax: "+getClass().getSimpleName());
    }

    /** Execute a Constraint - node shape. */
    public void validateNodeShape(ValidationContext vCxt, Graph data, Shape shape, Node focusNode);

    /** Execute a Constraint - property shape. */
    public void validatePropertyShape(ValidationContext vCxt, Graph data, Shape shape, Node focusNode, Path path, Set<Node> valueNodes);

    public Node getComponent();

    public void visit(ConstraintVisitor visitor);

}
