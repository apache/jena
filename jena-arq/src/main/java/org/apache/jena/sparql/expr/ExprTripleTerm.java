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

package org.apache.jena.sparql.expr;

import java.util.Objects;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.function.FunctionEnv ;
import org.apache.jena.sparql.graph.NodeTransform ;

/**
 * RDF-star triple term in an expression (AKA embedded triple).
 * It can still have variables in it.
 */
public class ExprTripleTerm extends ExprNode {

    private final Node_Triple tripleTerm;
    private final NodeValue nvTripleTerm;

    public ExprTripleTerm(Node_Triple tripleTerm) {
        this.tripleTerm = tripleTerm;
        this.nvTripleTerm = ( tripleTerm.isConcrete() ) ?  NodeValue.makeNode(tripleTerm) : null;
    }

    @Override public void visit(ExprVisitor visitor) { visitor.visit(this); }

    @Override public NodeValue eval(Binding binding, FunctionEnv env) {
        if ( nvTripleTerm != null )
            return nvTripleTerm;
        Triple t1 = tripleTerm.getTriple();
        Triple t2 = Substitute.substitute(t1, binding);
        if ( t2.isConcrete() ) {
            Node_Triple tripleTerm2 = new Node_Triple(t2);
            return NodeValue.makeNode(tripleTerm2);
        }
        throw new VariableNotBoundException("Not concrete: triple "+tripleTerm) ;
    }

    public Node getNode() {
        return tripleTerm;
    }

    public Triple getTriple() {
        return tripleTerm.getTriple();
    }

    @Override
    public int hashCode() {
        return tripleTerm.hashCode();
    }

    @Override
    public Expr copySubstitute(Binding binding) {
        Triple t1 = tripleTerm.getTriple();
        Triple t2 = Substitute.substitute(tripleTerm.getTriple(), binding);
        if ( t2 == t1 )
            return this;
        Node_Triple nodeTriple = new Node_Triple(t2);
        return new ExprTripleTerm(nodeTriple);
    }

    @Override
    public Expr applyNodeTransform(NodeTransform transform) {
        Node n = transform.apply(tripleTerm);
        return ExprLib.nodeToExpr(n);
    }

    @Override
    public boolean equals(Expr obj, boolean bySyntax) {
        if ( this == obj )
            return true;
        if ( getClass() != obj.getClass() )
            return false;
        ExprTripleTerm other = (ExprTripleTerm)obj;
        return Objects.equals(tripleTerm, other.tripleTerm);
    }
}
