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

package org.apache.jena.sparql.pfunction.library;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.ExprEvalException ;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions ;
import org.apache.jena.sparql.pfunction.PFuncSimpleAndList ;
import org.apache.jena.sparql.pfunction.PropFuncArg ;
import org.apache.jena.sparql.util.IterLib ;

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
