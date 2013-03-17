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
import com.hp.hpl.jena.query.QueryExecException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeFunctions ;
import com.hp.hpl.jena.sparql.pfunction.PFuncSimple ;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg ;
import com.hp.hpl.jena.sparql.util.IterLib ;

/** Property function to turn an RDF term (but not a blank node) into a string
      <pre>
      ?x :str "foo"@en
      </pre>
*/ 

public class str extends PFuncSimple
{
    public str() {}

    @Override
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt)
    { }
    
    @Override
    public QueryIterator execEvaluated(Binding binding,
                                       Node subject, Node predicate, Node object,
                                       ExecutionContext execCxt)
    {
        // Subject bound to something other a literal. 
        if ( subject.isURI() || subject.isBlank() )
            return IterLib.noResults(execCxt) ;

        if ( Var.isVar(subject) && Var.isVar(object) )
            throw new QueryExecException("str: Both subject and object are unbound variables") ;
        
        if ( Var.isVar(object) )
            throw new QueryExecException("str: Object is an unbound variables") ;
        
        if ( object.isBlank() )
            throw new QueryExecException("str: object is a blank node") ;
        
        Node strValue =  NodeFactory.createLiteral(NodeFunctions.str(object)) ;
        
        if ( Var.isVar(subject) )
            return IterLib.oneResult(binding, Var.alloc(subject), strValue, execCxt) ;
        else
        {
            // Subject bound : check it.
            if ( subject.equals(strValue) )
                return IterLib.result(binding, execCxt) ;
            return IterLib.noResults(execCxt) ;
        }
    }
}
