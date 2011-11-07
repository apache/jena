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

package com.hp.hpl.jena.sparql.syntax;

import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

/** A constraint (Filter) in a query expression. */

public class ElementFilter extends Element
{
    Expr expr = null ;

    public ElementFilter(Expr expr) { this.expr = expr ; }
    
    public Expr getExpr() { return expr ; }
    
    @Override
    public void visit(ElementVisitor v) { v.visit(this) ; }

    @Override
    public int hashCode() { return expr.hashCode() ; }
    
    @Override
    public boolean equalTo(Element el2, NodeIsomorphismMap isoMap)
    {
        if ( el2 == null ) return false ;

        if ( ! ( el2 instanceof ElementFilter ) )
            return false ;
        ElementFilter f2 = (ElementFilter)el2 ;
        if ( ! this.getExpr().equals(f2.getExpr()) )
            return false ;
        return true ;
    }
}
