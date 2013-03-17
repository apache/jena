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

package com.hp.hpl.jena.sparql.pfunction.library;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeFunctions ;
import com.hp.hpl.jena.sparql.pfunction.PFuncSimpleAndList ;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg ;
import com.hp.hpl.jena.sparql.util.IterLib ;

public class concat extends PFuncSimpleAndList
{
    @Override
    public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, PropFuncArg object,
                                       ExecutionContext execCxt)
    {
        if ( ! Var.isVar(subject) )
            throw new ExprEvalException("Subject is not a variable ("+subject+")") ;
        
        String x = "" ;
        for ( Node node : object.getArgList() )
        {
            if ( Var.isVar(node) )
                return IterLib.noResults(execCxt) ;
            String str = NodeFunctions.str(node) ;
            x = x+str ;
        }
         
        return IterLib.oneResult(binding, Var.alloc(subject), NodeFactory.createLiteral(x), execCxt) ;
    }

}
