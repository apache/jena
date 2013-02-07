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

package com.hp.hpl.jena.sparql.algebra.optimize;

import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import com.hp.hpl.jena.sparql.expr.E_Exists ;
import com.hp.hpl.jena.sparql.expr.E_NotExists ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprFunctionOp ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.expr.ExprTransformCopy ;

/** A copying transform that applies an Op Transform to the algebra operator of E_Exist and E_NoExists */
public class ExprTransformApplyTransform extends ExprTransformCopy
{
    private final Transform transform ;
    private OpVisitor beforeVisitor ;
    private OpVisitor afterVisitor ;
    
    public ExprTransformApplyTransform(Transform transform)
    {
        this(transform, null, null) ;
    }
    
    public ExprTransformApplyTransform(Transform transform, OpVisitor beforeVisitor, OpVisitor afterVisitor)
    {
        this.transform = transform ;
        this.beforeVisitor = beforeVisitor ;
        this.afterVisitor = afterVisitor ;
    }
    
    @Override
    public Expr transform(ExprFunctionOp funcOp, ExprList args, Op opArg)
    {
        Op opArg2 = Transformer.transform(transform, opArg, beforeVisitor, afterVisitor) ;
        if ( opArg2 == opArg )
            return super.transform(funcOp, args, opArg) ;
        if ( funcOp instanceof E_Exists )
            return new E_Exists(opArg2) ;
        if ( funcOp instanceof E_NotExists )
            return new E_NotExists(opArg2) ;
        throw new ARQInternalErrorException("Unrecognized ExprFunctionOp: \n"+funcOp) ;
    }
}

