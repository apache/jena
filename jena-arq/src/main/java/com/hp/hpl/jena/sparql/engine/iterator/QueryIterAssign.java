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

package com.hp.hpl.jena.sparql.engine.iterator;

import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.QueryExecException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.Utils ;

/** Extend each solution by a (var, expression) */ 

public class QueryIterAssign extends QueryIterProcessBinding
{
    private VarExprList exprs ;
    private final boolean mustBeNewVar ;
    
    public QueryIterAssign(QueryIterator input, Var var, Expr expr, ExecutionContext qCxt)
    {
        this(input, new VarExprList(var, expr) , qCxt, false) ;
    }
    
    public QueryIterAssign(QueryIterator input, VarExprList exprs, ExecutionContext qCxt, boolean mustBeNewVar)
    {
        // mustBeNewVar : any variable introduced must not already exist.
        // true => BIND
        // false => LET 
        // Syntax checking of BIND should have assured this.
        super(input, qCxt) ;
        this.exprs = exprs ;
        this.mustBeNewVar = mustBeNewVar ;
    }
    
    @Override
    public Binding accept(Binding binding)
    {
        BindingMap b = BindingFactory.create(binding) ;
        for ( Var v : exprs.getVars() )
        {
            // Not this, where expressions do not see the new bindings.
            // Node n = exprs.get(v, bind, funcEnv) ;
            // which gives (Lisp) "let" semantics, not "let*" semantics 
            Node n = exprs.get(v, b, getExecContext()) ;
            
            if ( n == null )
                // Expression failed to evaluate - no assignment
                continue ;
                
            // Check is already has a value; if so, must be sameValueAs
            if ( b.contains(v) )
            {
                // Optimization may linearize to push a stream through an (extend).  
                if ( false && mustBeNewVar )
                    throw new QueryExecException("Already set: "+v) ;
                
                Node n2 = b.get(v) ;
                if ( ! n2.sameValueAs(n) )
                    //throw new QueryExecException("Already set: "+v) ;
                    // Error in single assignment.
                    return null ;
                continue ;
            }
            b.add(v, n) ;
        }
        return b ;
    }
    
    @Override
    protected void details(IndentedWriter out, SerializationContext cxt)
    { 
        out.print(Utils.className(this)) ;
        out.print(" ") ;
        out.print(exprs.toString()) ;
    }
       
}
