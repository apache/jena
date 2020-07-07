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

import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.function.FunctionEnv ;
import org.apache.jena.sparql.sse.Tags ;

public class E_Bound extends ExprFunction1
{
    private static final String symbol = Tags.tagBound ;
    boolean isBound = false ;

    public E_Bound(Expr expr)
    {
        super(expr, symbol) ;
    }
    
    @Override
    public NodeValue evalSpecial(Binding binding, FunctionEnv env) { 
        // See also ExprLib.evalOrNull.
        // This code predates that; it handles exceptions 
        // slightly differently (VariableNotBoundException not
        // a general ExprEvalException).
        
        if ( expr.isConstant() )
            // The case of the variable having been substituted for a constant.
            // Note: this has often been optimized away by constant folding 
            // (ExprTransformConstantFold) which called eval(NodeValue x) -> TRUE.
            return NodeValue.TRUE ;
        
        if ( expr.isVariable() )
            // The case of the expr being a single variable. 
            return NodeValue.booleanReturn(binding.contains(expr.asVar())) ; 
        
        // General expression. This case can't be written in SPARQL
        // but we keep the code general in case some optimization rewrite
        // or algebra expression uses the generalized feature.
		try {
			expr.eval(binding, env) ;
            return NodeValue.TRUE ;
		} catch (VariableNotBoundException ex) {
			return NodeValue.FALSE ;
		}
    }

    @Override
    public NodeValue eval(NodeValue x) { return NodeValue.TRUE ; }
    
    @Override
    public Expr copy(Expr expr) { return new E_Bound(expr) ; } 
}
