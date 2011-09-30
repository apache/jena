/**
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

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.TransformWrapper ;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.expr.ExprTransformer ;

/** Take transform and apply it to the algebra operator of E_Exist and E_NoExists in an OpFilter.
 * Normally, the wrapped Transform is one derived from TransformCopy. 
 */
public class TransformApplyInsideExprFunctionOp extends TransformWrapper
{
    final private ExprTransformApplyTransform exprTransform ; 
    public TransformApplyInsideExprFunctionOp(Transform transform)
    {
        super(transform) ;
        exprTransform = new ExprTransformApplyTransform(transform) ;
    }
    
    @Override
    public Op transform(OpFilter opFilter, Op x) 
    {
        ExprList ex = new ExprList() ;
        for ( Expr e : opFilter.getExprs() )
        {
            Expr e2 = ExprTransformer.transform(exprTransform, e) ;
            ex.add(e2) ;
        }
        OpFilter f = (OpFilter)OpFilter.filter(ex, x) ;
        return super.transform(f, x) ;
    }
}
