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

package org.apache.jena.sparql.graph;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.walker.ApplyTransformVisitor;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.*;

/** An {@link ExprTransform} that applies a {@link NodeTransform}
 * to {@link NodeValue} and {@link ExprVar} inside expressions.
 * <p>
 * This does not transform triple patterns in {@link ExprFunctionOp}
 * which is done as part of {@link ApplyTransformVisitor}.
 *
 * @see NodeTransformOp
 */
public class NodeTransformExpr extends ExprTransformCopy {
    private final NodeTransform transform ;
    public NodeTransformExpr(NodeTransform transform)
    {
        this.transform = transform ;
    }

    @Override
    public Expr transform(NodeValue nv) {
        return transform(nv.asNode());
    }

    @Override
    public Expr transform(ExprVar exprVar) {
        return transform(exprVar.getAsNode());
    }

    /** Transform node then create a {@link ExprVar} or {@link NodeValue}. */
    private Expr transform(Node input) {
        Node n = transform.apply(input);
        if ( n == null )
            throw new InternalErrorException("NodeTransform creates a null");
        if ( ! Var.isVar(n) )
            return NodeValue.makeNode(n);
        String name = Var.alloc(n).getVarName();
        return new ExprVar(n.getName());
    }
}
