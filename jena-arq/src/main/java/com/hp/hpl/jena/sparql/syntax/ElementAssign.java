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

import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

public class ElementAssign extends Element
{
    private Var var ;
    private Expr expr ;

    public ElementAssign(Var v, Expr expr)
    {
        this.var = v ; 
        this.expr = expr ;
    }

    public Var getVar()
    {
        return var ;
    }

    public Expr getExpr()
    {
        return expr ;
    }

    @Override
    public boolean equalTo(Element el2, NodeIsomorphismMap isoMap)
    {
        if ( ! ( el2 instanceof ElementAssign ) )
            return false ;
        ElementAssign f2 = (ElementAssign)el2 ;
        if ( ! this.getVar().equals(f2.getVar() ))
            return false ;
        if ( ! this.getExpr().equals(f2.getExpr()) )
            return false ;
        return true ;
    }

    @Override
    public int hashCode()
    {
        return var.hashCode()^expr.hashCode();
    }

    @Override
    public void visit(ElementVisitor v)
    {
        v.visit(this) ;
    }
}
