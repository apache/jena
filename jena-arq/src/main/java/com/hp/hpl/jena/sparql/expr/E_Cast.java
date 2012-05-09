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

import com.hp.hpl.jena.sparql.ARQNotImplemented ;


public class E_Cast extends ExprFunction2
{
    // See E_StrDatatype
    private static final String symbol = "cast" ;

    private E_Cast(Expr expr1, Expr expr2)
    {
        super(expr1, expr2, symbol) ;
    }

    @Override
    public NodeValue eval(NodeValue x, NodeValue y)
    {
        if ( ! x.isString() ) throw new ExprEvalException("cast: arg 2 is not a string: "+x) ;
        if ( ! y.isIRI() ) throw new ExprEvalException("cast: arg 2 is not a URI: "+y) ;
        
        String lex = x.getString() ;
        y.asNode().getURI() ;
        
        throw new ARQNotImplemented() ;
    }

    @Override
    public Expr copy(Expr arg1, Expr arg2)
    { return new E_Cast(arg1, arg2) ; }
}
