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

import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.expr.*;
import org.apache.jena.sparql.syntax.Element;

/**
 * A copying transform that applies an ElementTransform syntax pattern of
 * E_Exist and E_NoExists
 * */
public class ExprTransformApplyElementTransform extends ExprTransformCopy
{
    private final ElementTransform transform;

    public ExprTransformApplyElementTransform(ElementTransform transform) {
        this(transform, false);
    }

    public ExprTransformApplyElementTransform(ElementTransform transform, boolean alwaysDuplicate) {
        super(alwaysDuplicate);
        this.transform = transform;
    }

    @Override
    public Expr transform(ExprFunctionOp funcOp, ExprList args, Op opArg)
    {
        // If the element is null then an attempt is made to obtain it from the algebra.
        // A given element takes precedence over the algebra.
        Element el1 = funcOp.getElement();
        if (el1 == null) {
            Op op = funcOp.getGraphPattern();
            el1 = op == null ? null : OpAsQuery.asElement(op);
        }
        // ElementTransformer will warn should it happen that null is passed to it.
        Element el2 = ElementTransformer.transform(el1, transform, this);

        if ( el2 == funcOp.getElement() )
            return super.transform(funcOp, args, opArg);
        if ( funcOp instanceof E_Exists )
            return new E_Exists(el2);
        if ( funcOp instanceof E_NotExists )
            return new E_NotExists(el2);
        throw new ARQInternalErrorException("Unrecognized ExprFunctionOp: \n"+funcOp);
    }
}

