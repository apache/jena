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

package com.hp.hpl.jena.sparql.function.library;

import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.E_Regex ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.Function ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;

/** Function for XPath fn:matches */

public class FN_Matches implements Function
{
    // Wrapper around an E_Regex. Maybe move actual regex to Function.regex.
    E_Regex regex = null;
    ExprList myArgs = null ;
    
    @Override
    public void build(String uri, ExprList args)
    {
        if ( args.size() != 3 && args.size() != 2 )
            throw new ExprEvalException("matches: Wrong number of arguments: Wanted 2 or 3, got "+args.size()) ;
        myArgs = args ;
        
    }
    
    @Override
    public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env)
    {
        if ( myArgs != args )
            throw new ARQInternalErrorException("matches: Arguments have changed since checking") ;

        Expr expr = args.get(0) ;
        E_Regex regexEval = regex ;
        
        if ( regexEval == null )
        {
            Expr e1 = args.get(1) ;
            Expr e2 = null ;
            if ( args.size() == 3 )
                e2 = args.get(2) ;

            String pattern = e1.eval(binding, env).getString() ;
            String flags = (e2==null)?null : e2.eval(binding, env).getString() ;
            
            regexEval = new E_Regex(expr, pattern, flags) ;

            // Cache if the pattern is fixed and the flags are fixed or non-existant
            if ( e1 instanceof NodeValue && ( e2 == null || e2 instanceof NodeValue ) )
                regex = regexEval ;
        }

        NodeValue nv = regexEval.eval(binding, env) ;
        return nv ;
    }

}
