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

package org.apache.jena.sparql.engine.iterator;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.QueryExecException ;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.core.VarExprList ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.serializer.SerializationContext ;

/** Extend each solution by a (var, expression) */

public class QueryIterAssign extends QueryIterProcessBinding
{
    private VarExprList exprs ;
    private final boolean mustBeNewVar ;

    public QueryIterAssign(QueryIterator input, Var var, Expr expr, ExecutionContext qCxt) {
        this(input, new VarExprList(var, expr) , qCxt, false) ;
    }

    public QueryIterAssign(QueryIterator input, VarExprList exprs, ExecutionContext qCxt, boolean mustBeNewVar) {
        // mustBeNewVar : any variable introduced must not already exist.
        // true => BIND
        // false => LET
        // Syntax checking of BIND should have assured this.
        super(input, qCxt) ;
        this.exprs = exprs ;
        this.mustBeNewVar = mustBeNewVar ;
    }

    @Override
    public Binding accept(Binding binding) {
        // XXX Assumes ExprList.get(, Binding, )
        //BindingMap b = BindingFactory.create(binding);
        BindingBuilder b = Binding.builder(binding);
        for ( Var v : exprs.getVars() ) {
            // if "binding", not "b" used, we get (Lisp) "let"
            // semantics, not the desired "let*" semantics
            Node n = exprs.get(v, b.snapshot(), getExecContext());

            if ( n == null )
                // Expression failed to evaluate - no assignment
                continue;

            // Check is already has a value; if so, must be sameValueAs
            if ( b.contains(v) ) {
                // Optimization may linearize to push a stream through an (extend).
                if ( false && mustBeNewVar )
                    throw new QueryExecException("Already set: " + v);
                Node n2 = b.get(v);
                if ( !n2.sameValueAs(n) )
                    //throw new QueryExecException("Already set: "+v) ;
                    // Error in single assignment.
                    return null ;
                continue ;
            }
            try {
                // Add same.
                b.add(v, n) ;
            } catch (ARQInternalErrorException ex) {
                throw ex;
            }
        }
        return b.build() ;
    }

    @Override
    protected void details(IndentedWriter out, SerializationContext cxt) {
        out.print(Lib.className(this));
        out.print(" ");
        out.print(exprs.toString());
    }
}
