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
import org.apache.jena.query.QueryExecException ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions ;
import org.apache.jena.sparql.pfunction.PFuncSimple ;
import org.apache.jena.sparql.pfunction.PropFuncArg ;
import org.apache.jena.sparql.util.IterLib ;

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
