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

package org.apache.jena.shacl.engine.constraint;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.other.G;
import org.apache.jena.shacl.engine.ValidationContext;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.Shape;
import org.apache.jena.sparql.expr.ExprNotComparableException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.path.Path;

/**
 * Base of "4.5 Property Pair Constraint Components"
 */
public abstract class ConstraintPairwise implements Constraint {

    protected final Node value;
    private final Node constraintComponent;

    protected ConstraintPairwise(Node value, Node constraintComponent) {
        this.value = value;
        this.constraintComponent = constraintComponent;
    }

    @Override
    final
    public Node getComponent() {
        return constraintComponent;
    }


    @Override
    public void validateNodeShape(ValidationContext vCxt, Graph data, Shape shape, Node focusNode) {
        validatePropertyShape(vCxt, data, shape, focusNode, null, Collections.singleton(focusNode));
    }

    @Override
    final
    public void validatePropertyShape(ValidationContext vCxt, Graph data, Shape shape,
                         Node focusNode, Path path, Set<Node> pathNodes) {
        // pathNodes is (focusNode, sh:path ?V) = valueNodes
        Set<Node> compareNodes = G.allSP(data, focusNode, value);
        validate(vCxt, shape, focusNode, path, pathNodes, compareNodes);
    }

    public abstract void validate(ValidationContext vCxt, Shape shape, Node focusNode, Path path,
                                  Set<Node> pathNodes, Set<Node> compareNodes);

    protected int compare(Node n1, Node n2) {
        NodeValue nv1 = NodeValue.makeNode(n1);
        NodeValue nv2 = NodeValue.makeNode(n2);
        try {
          return NodeValue.compare(nv1, nv2);
      } catch (ExprNotComparableException ex) {
          // Known to be not a Expr compare constant value.
          return -999;
      }
    }

    @Override
    public int hashCode() {
        return Objects.hash(constraintComponent, value);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        ConstraintPairwise other = (ConstraintPairwise)obj;
        return Objects.equals(constraintComponent, other.constraintComponent) && Objects.equals(value, other.value);
    }
}
