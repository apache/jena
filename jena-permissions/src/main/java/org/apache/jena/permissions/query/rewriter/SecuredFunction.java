/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.permissions.query.rewriter;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.shared.AuthenticationRequiredException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.*;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.graph.NodeTransform;

public class SecuredFunction extends ExprFunctionN {
    private final SecurityEvaluator securityEvaluator;
    private final List<Node> variables;
    private final List<Triple> bgp;
    private final Node graphIRI;

    private static ExprList createArgs(List<Node> variables) {
        ExprList retval = new ExprList();
        for (Node n : variables) {
            retval.add(new ExprVar(n));
        }
        return retval;
    }

    public SecuredFunction(final Node graphIRI, final SecurityEvaluator securityEvaluator, final List<Node> variables,
            final List<Triple> bgp) {
        super(String.format("<java:%s>", SecuredFunction.class.getName()), createArgs(variables));
        this.securityEvaluator = securityEvaluator;
        this.variables = variables;
        this.bgp = bgp;
        this.graphIRI = graphIRI;
    }

    private boolean checkAccess(Binding values) throws AuthenticationRequiredException {
        Object principal = securityEvaluator.getPrincipal();
        for (final Triple t : bgp) {
            final Triple secT = resolveTriple(t, values);
            if (!securityEvaluator.evaluate(principal, Action.Read, graphIRI, secT)) {
                return false;
            }
        }
        return true;
    }

    private Triple resolveTriple(final Triple t, final Binding values) {
        int idx = variables.indexOf(t.getSubject());

        final Node s = idx == -1 ? t.getSubject() : values.get(Var.alloc(variables.get(idx)));

        idx = variables.indexOf(t.getPredicate());
        final Node p = idx == -1 ? t.getPredicate() : values.get(Var.alloc(variables.get(idx)));
        idx = variables.indexOf(t.getObject());
        final Node o = idx == -1 ? t.getObject() : values.get(Var.alloc(variables.get(idx)));
        return new Triple(s, p, o);
    }

    @Override
    public Expr copySubstitute(Binding binding) {
        return this;
    }

    @Override
    public Expr applyNodeTransform(NodeTransform transform) {
        return this;
    }

    @Override
    public void visit(ExprVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public NodeValue eval(List<NodeValue> args) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Expr copy(ExprList newArgs) {
        return this;
    }

    @Override
    protected NodeValue evalSpecial(Binding binding, FunctionEnv env) {
        return NodeValue.booleanReturn(checkAccess(binding));
    }

}
