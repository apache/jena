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
import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprException ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;
import com.hp.hpl.jena.sparql.util.Utils ;

/** 
 *  Filter a stream of bindings by a constraint. */

public class QueryIterFilterExpr extends QueryIterProcessBinding
{
    private final Expr expr ;
    
    public QueryIterFilterExpr(QueryIterator input, Expr expr, ExecutionContext context)
    {
        super(input, context) ;
        this.expr = expr ;
    }
    
    @Override
    public Binding accept(Binding binding)
    {
        try {
            if ( expr.isSatisfied(binding, super.getExecContext()) )
                return binding ;
            return null ;
        } catch (ExprException ex)
        { // Some evaluation exception
            Log.warn(this, "Expression Exception in "+expr, ex) ;
            return null ;
        }
        catch (Exception ex)
        {
            Log.warn(this, "General exception in "+expr, ex) ;
            return null ;
        }
    }

    @Override
    protected void details(IndentedWriter out, SerializationContext cxt)
    { 
        out.print(Utils.className(this)) ;
        out.print(" ") ;
        ExprUtils.fmtSPARQL(out, expr, cxt) ;
    }
       
}
