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

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.sparql.engine.OpEval ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Visitor class to run over expressions and initialise them */
public class ExprBuild extends ExprVisitorBase 
{
    private Context context ;
    private OpEval opExec ;
    public ExprBuild(Context context)
    { 
        this.context = context ;
    }
    
    @Override
    public void visit(ExprFunctionN func)
    {
        if ( func instanceof E_Function )
        {
            // Causes unbindable functions to complain now, not later.
            E_Function f = (E_Function)func ;
            f.buildFunction(context) ;
        }
    }
}
