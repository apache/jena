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

import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.Substitute ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;
import com.hp.hpl.jena.sparql.graph.NodeTransformLib ;
import com.hp.hpl.jena.sparql.sse.Tags;
import com.hp.hpl.jena.sparql.syntax.Element ;

public class E_NotExists extends ExprFunctionOp
{
    // Translated to "(not (exists (...)))" 
    private static final String symbol = Tags.tagNotExists ;

    public E_NotExists(Op op)
    {
        this(null, op) ;
    }
    
    public E_NotExists(Element elt)
    {
        this(elt, Algebra.compile(elt)) ;
    }
    
    public E_NotExists(Element el, Op op)
    {
        super(symbol, el, op) ;
    }

    @Override
    public Expr copySubstitute(Binding binding)
    {
        Op op2 = Substitute.substitute(getGraphPattern(), binding) ;
        return new E_NotExists(getElement(), op2) ;
    }

    @Override
    public Expr applyNodeTransform(NodeTransform nodeTransform)
    {
        Op op2 = NodeTransformLib.transform(nodeTransform, getGraphPattern()) ;
        return new E_NotExists(getElement(), op2) ;
    }
    
    @Override
    protected NodeValue eval(Binding binding, QueryIterator qIter, FunctionEnv env)
    {
        boolean b = qIter.hasNext() ;
        return NodeValue.booleanReturn(!b) ;
    }

    @Override
    public int hashCode()
    {
        return symbol.hashCode() ^ getGraphPattern().hashCode() ;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;

        if ( ! ( other instanceof E_NotExists ) )
            return false ;
        
        E_NotExists ex = (E_NotExists)other ;
        return this.getGraphPattern().equals(ex.getGraphPattern()) ;
    }
    
    @Override
    public ExprFunctionOp copy(ExprList args, Op x) { return new E_NotExists(x) ; }

}
