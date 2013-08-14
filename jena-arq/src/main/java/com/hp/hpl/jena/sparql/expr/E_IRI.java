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

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeFunctions ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.sse.Tags;

public class E_IRI extends ExprFunction1
{
    private static final String symbol = Tags.tagIri ;

    public E_IRI(Expr expr)
    {
        super(expr, symbol) ;
    }

    public E_IRI(Expr expr, String altSymbol)
    {
        super(expr, altSymbol) ;
    }
    
    // Use the hook to get the env.
    @Override
    public NodeValue eval(NodeValue v, FunctionEnv env)
    { 
        String baseIRI = null ;
        if ( env.getContext() != null )
        {
            Query query = (Query)env.getContext().get(ARQConstants.sysCurrentQuery) ;
            if ( query != null )
                baseIRI = query.getBaseURI() ;
        }
        return NodeFunctions.iri(v, baseIRI) ;
    }
    
    @Override
    public Expr copy(Expr expr) { return new E_IRI(expr) ; }

    @Override
    public NodeValue eval(NodeValue v)
    {
        throw new ARQInternalErrorException("Should not be called") ;
    } 
}
