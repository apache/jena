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

import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.expr.ExprFunctionOp ;
import com.hp.hpl.jena.sparql.expr.ExprVisitorBase ;

/** An expr visitor that applies a OpVisitor to the algebra operator of E_Exist and E_NoExists */
public class ExprVisitorApplyVisitor extends ExprVisitorBase
{
    // See also ExprTransformer.transform(ExprFunctionOp func, ..) and ExprTransformOp
    private final OpVisitor visitor ;
    
    public ExprVisitorApplyVisitor(OpVisitor visitor)
    {
        this.visitor = visitor ;
    }
    
    @Override
    public void visit(ExprFunctionOp funcOp)
    {
        funcOp.getGraphPattern().visit(visitor) ;
    }
}
