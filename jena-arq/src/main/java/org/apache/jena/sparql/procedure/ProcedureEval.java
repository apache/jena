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

package org.apache.jena.sparql.procedure ;

import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.NodeValue ;

public abstract class ProcedureEval extends ProcedureBase
{
    @Override
    public QueryIterator exec(Binding binding, Node name, ExprList args, ExecutionContext execCxt)
    {
        // Eval if possible.
        ExprList evalArgs = new ExprList() ;
        for (Expr e : args)
        {
            if ( e.isVariable() )
            {
                Var v = e.getExprVar().asVar() ;
                // Special case - allow unevaluated variables.
                if ( binding.contains(v) )
                    evalArgs.add(e.eval(binding, execCxt)) ;
                else
                    evalArgs.add(e) ;
            }
            else
            {
                NodeValue x = e.eval(binding, execCxt) ;
                evalArgs.add(x) ;
            }
        }
        return execEval(binding, evalArgs, execCxt) ;
    }
    
    public abstract QueryIterator execEval(Binding binding, ExprList args, ExecutionContext execCxt) ;

}
