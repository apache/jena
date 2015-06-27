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
import org.apache.jena.sparql.pfunction.PFuncSimple ;
import org.apache.jena.sparql.util.IterLib ;

/** Relationship between a node (subject) and it's bNode label (object/string) */ 

public class blankNode extends PFuncSimple
{
    @Override
    public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, Node object, ExecutionContext execCxt)
    {
        if ( Var.isVar(subject) )
            throw new ExprEvalException("bnode: subject is an unbound variable") ;
        if ( ! subject.isBlank() )
            return IterLib.noResults(execCxt) ;
        String str = subject.getBlankNodeLabel() ;
        Node obj = NodeFactory.createLiteral(str) ;
        if ( Var.isVar(object) )
            return IterLib.oneResult(binding, Var.alloc(object), obj, execCxt) ;
        
        // Subject and object are concrete 
        if ( object.sameValueAs(obj) )
            return IterLib.result(binding, execCxt) ;
        return IterLib.noResults(execCxt) ;
    }
}
