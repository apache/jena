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

import org.openjena.atlas.lib.IRILib ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.sse.Tags ;

public class E_StrEncodeForURI extends ExprFunction1
{
    private static final String symbol = Tags.tagStrEncodeForURI ;

    public E_StrEncodeForURI(Expr expr)
    {
        super(expr, symbol) ;
    }
    
    @Override
    public NodeValue eval(NodeValue v)
    { 
        Node n = v.asNode() ;
        if ( ! n.isLiteral() )
            throw new ExprEvalException("Not a literal") ;
        if ( n.getLiteralDatatype() != null )
        {
            if ( ! n.getLiteralDatatype().equals(XSDDatatype.XSDstring) )
                throw new ExprEvalException("Not a string literal") ;
        }
        
        String str = n.getLiteralLexicalForm() ;
        String encStr = IRILib.encodeUriComponent(str) ;
        encStr = IRILib.encodeNonASCII(encStr) ;
        
        return NodeValue.makeString(encStr) ;
    }
    

    @Override
    public Expr copy(Expr expr) { return new E_StrEncodeForURI(expr) ; } 
}
