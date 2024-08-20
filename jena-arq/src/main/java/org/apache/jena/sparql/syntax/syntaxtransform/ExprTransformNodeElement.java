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

package org.apache.jena.sparql.syntax.syntaxtransform;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.*;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementVisitor;

/**
 * Special version of ExprTransform for applying a node transform on syntax
 * (Elements) only
 */
public class ExprTransformNodeElement extends ExprTransformCopy {
    private final NodeTransform    nodeTransform;
    private final ElementTransform elementTransform;
    private final ElementVisitor beforeVisitor;
    private final ElementVisitor afterVisitor;

    public ExprTransformNodeElement(NodeTransform nodeTransform, ElementTransform eltrans) {
        this(nodeTransform, eltrans, null, null);
    }

    public ExprTransformNodeElement(NodeTransform nodeTransform, ElementTransform eltrans,
                                    ElementVisitor beforeVisitor, ElementVisitor afterVisitor) {
        this.nodeTransform = nodeTransform;
        this.elementTransform = eltrans;
        this.beforeVisitor = beforeVisitor;
        this.afterVisitor = afterVisitor;
    }

    @Override
    public Expr transform(ExprVar nv) {
        Node n = nodeTransform.apply(nv.getAsNode());
        if ( n == nv.getAsNode() )
            return nv;
        if ( n instanceof Var ) {
            Var v = Var.alloc(n);
            return new ExprVar(v);
        }
        return NodeValue.makeNode(n);
    }

    @Override
    public Expr transform(NodeValue nv) {
        Node n = nodeTransform.apply(nv.asNode());
        if ( n == nv.asNode() )
            return nv;
        return NodeValue.makeNode(n);
    }

    @Override
    public Expr transform(ExprFunctionOp funcOp, ExprList args, Op opArg) {
        // Syntax phased only - ignore args and opArg
        Element elt = funcOp.getElement();
        Element elt1 = ElementTransformer.transform(elt, elementTransform, this, beforeVisitor, afterVisitor);
        if ( elt == elt1 )
            return funcOp;
        else {
            if ( funcOp instanceof E_Exists )
                return new E_Exists(elt1);
            if ( funcOp instanceof E_NotExists )
                return new E_NotExists(elt1);
            throw new InternalErrorException("Unknown ExprFunctionOp: " + funcOp.getFunctionSymbol());
        }
    }

    @Override
    public Expr transform(ExprAggregator eAgg) {
        return eAgg.applyNodeTransform(nodeTransform);
    }
}
