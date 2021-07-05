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
package org.apache.jena.arq.querybuilder.rewriters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprLib;

/**
 * The base class for rewriters.
 *
 * Rewriters push and pop items on the stack during processing.
 *
 * @param <T> The type of object being rewritten.
 */
public class AbstractRewriter<T> {
    // The map of variables to nodes.
    protected final Map<Var, Node> values;
    // A stack used in processing.
    private final Stack<T> result = new Stack<>();

    /**
     * Constructor
     * 
     * @param values The values to map.
     */
    protected AbstractRewriter(Map<Var, Node> values) {
        this.values = values;
    }

    /**
     * Push the value on the stack.
     * 
     * @param value The value to push.
     */
    protected final void push(T value) {
        result.push(value);
    }

    /**
     * pop the value from the stack.
     * 
     * @return The value from the top of the stack.
     */
    protected final T pop() {
        return result.pop();
    }

    /**
     * Return true if the stack is empty.
     * 
     * @return true if the stack is empty, false otherwise.
     */
    protected final boolean isEmpty() {
        return result.isEmpty();
    }

    /**
     * Get the result from the rewriter. Returns the top of the stack.
     * 
     * @return The final result or null if there is no answer.
     */
    public final T getResult() {
        if (isEmpty()) {
            return null;
        }
        return pop();
    }

    /**
     * Rewrite a triple path.
     * 
     * @param t The triple path to rewrite.
     * @return the triple path after rewriting.
     */
    protected final TriplePath rewrite(TriplePath t) {
        if (t.getPath() == null) {
            return new TriplePath(
                    new Triple(changeNode(t.getSubject()), changeNode(t.getPredicate()), changeNode(t.getObject())));
        }
        PathRewriter transform = new PathRewriter(values);
        t.getPath().visit(transform);
        return new TriplePath(changeNode(t.getSubject()), transform.getResult(), changeNode(t.getObject()));
    }

    /**
     * Rewrite a triple.
     * 
     * @param t The triple to rewrite.
     * @return The rewritten triple.
     */
    protected final Triple rewrite(Triple t) {
        return new Triple(changeNode(t.getSubject()), changeNode(t.getPredicate()), changeNode(t.getObject()));
    }

    /**
     * If the node is a variable perform any necessary rewrite, otherwise return the
     * node.
     * 
     * @param n The node to rewrite.
     * @return the rewritten node.
     */
    protected final Node changeNode(Node n) {
        if (n == null) {
            return n;
        }
        if (n.isVariable()) {
            Var v = Var.alloc(n);

            if (values.containsKey(v)) {
                return values.get(v);
            }
            return v;
        }
        return n;
    }

    /**
     * Change all the nodes in the list. If a node is a variable perform any
     * necessary rewrite, otherwise return the node.
     * 
     * @param src a list of nodes to change.
     * @return The list of nodes.
     */
    protected final List<Node> changeNodes(List<Node> src) {
        List<Node> lst = new ArrayList<>();
        for (Node t : src) {
            lst.add(changeNode(t));
        }
        return lst;
    }

    /**
     * Rewrite a list of triples.
     * 
     * @param src The list of triples to rewrite.
     * @return The list of rewritten triples.
     */
    public final List<Triple> rewrite(List<Triple> src) {
        List<Triple> lst = new ArrayList<>();
        for (Triple t : src) {
            lst.add(rewrite(t));
        }
        return lst;
    }

    /**
     * Rewrite a binding.
     * 
     * @param binding The binding to rewrite
     * @return The rewritten binding.
     */
    protected final Binding rewrite(Binding binding) {
        BindingBuilder builder = Binding.builder();
        Iterator<Var> iter = binding.vars();
        while (iter.hasNext()) {
            Var v = iter.next();
            Node n = changeNode(binding.get(v));
            n = n.equals(v) ? binding.get(v) : n;
            builder.add(v, n);
        }
        return builder.build();
    }

    /**
     * Rewrite a variable expression list.
     * 
     * @param lst The variable expression list.
     * @return the rewritten variable expression list.
     */
    public final VarExprList rewrite(VarExprList lst) {

        VarExprList retval = new VarExprList();
        for (Var v : lst.getVars()) {
            Node n = values.get(v);
            if (n != null) {
                if (n.isVariable()) {
                    retval.add(Var.alloc(n));
                }
            } else {
                retval.add(v);
            }
        }

        for (Map.Entry<Var, Expr> entry : lst.getExprs().entrySet()) {
            Expr target = ExprLib.nodeToExpr(entry.getKey());
            Node n = values.get(entry.getKey());
            Var v = entry.getKey();
            Expr e = entry.getValue();
            if (n != null) {
                if (n.isVariable()) {
                    v = Var.alloc(n);
                    if (target.equals(e)) {
                        e = ExprLib.nodeToExpr(n);
                    }
                } else {
                    v = null;
                }
            }
            if (v != null) {
                retval.add(v, e);
            }
        }
        return retval;

    }

}
