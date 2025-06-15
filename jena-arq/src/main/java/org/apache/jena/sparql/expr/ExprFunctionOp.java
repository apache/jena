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

import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingLib;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.engine.iterator.QueryIteratorCheck;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformSubst;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;
import org.apache.jena.sparql.syntax.syntaxtransform.ExprTransformNodeElement;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;

/** A "function" that executes over a pattern */

public abstract class ExprFunctionOp extends ExprFunction
{
    private final Op op;
    private Op opRun = null;

    // If element has not been set via the ctor then getElement() will compute it lazily.
    private Element element;

    protected ExprFunctionOp(String fName, Element el, Op op) {
        super(fName);
        this.op = op;
        this.element = el;
    }

    @Override
    public Expr getArg(int i) {
        return null;
    }

    @Override
    public boolean isGraphPattern()     { return true; }
    @Override
    public Op getGraphPattern()         { return op; }

    public Element getElement()         {
        if (element == null) {
            element = OpAsQuery.asElement(op);
        }
        return element;
    }

    @Override
    public int numArgs() { return 0; }

    @Override
    public Expr copySubstitute(Binding binding) {
        Op op2 = Substitute.substitute(getGraphPattern(), binding) ;
        Element elt = getElement();
        Element elt2 = null;
        if (elt != null) {
            Map<Var, Node> map = BindingLib.bindingToMap(binding);
            NodeTransform nodeTransform = new NodeTransformSubst(map);
            ElementTransform eltTransform = new ElementTransformSubst(nodeTransform);
            ExprTransform exprTransform =  new ExprTransformNodeElement(nodeTransform, eltTransform);
            elt2 = ElementTransformer.transform(elt, eltTransform, exprTransform);
        }
        return copy(elt2, op2) ;
    }

    @Override
    public Expr applyNodeTransform(NodeTransform nodeTransform) {
        Op op2 = NodeTransformLib.transform(nodeTransform, getGraphPattern()) ;
        Element elt = getElement();
        Element elt2 = null;
        if (elt != null) {
            ElementTransform eltTransform = new ElementTransformSubst(nodeTransform);
            ExprTransform exprTransform =  new ExprTransformNodeElement(nodeTransform, eltTransform);
            elt2 = ElementTransformer.transform(elt, eltTransform, exprTransform);
        }
        return copy(elt2, op2) ;
    }

    protected abstract Expr copy(Element elt, Op op);

    // ---- Evaluation

    @Override
    public final NodeValue eval(Binding binding, FunctionEnv env) {
        ExecutionContext execCxt = ExecutionContext.fromFunctionEnv(env);
        QueryIterator qIter1 = QueryIterSingleton.create(binding, execCxt);
        QueryIterator qIter = QC.execute(op, qIter1, execCxt);
        // Wrap with something to check for closed iterators.
        qIter = QueryIteratorCheck.check(qIter, execCxt);
        // Call the per-operation functionality.
        try {
            return eval(binding, qIter, env);
        } finally { qIter.close(); }
    }

    protected abstract NodeValue eval(Binding binding, QueryIterator iter, FunctionEnv env);

    public abstract ExprFunctionOp copy(ExprList args, Op x);
    public abstract ExprFunctionOp copy(ExprList args, Element elPattern);
    @Override
    public void visit(ExprVisitor visitor) { visitor.visit(this); }
    public Expr apply(ExprTransform transform, ExprList args, Op x) { return transform.transform(this, args, x); }
}
