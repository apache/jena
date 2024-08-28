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

package org.apache.jena.sparql.syntax;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

public class ElementUnfold extends Element
{
    private Expr expr ;
    private Var var1 ;
    private Var var2 ;

    public ElementUnfold(Expr expr, Var v1, Var v2)
    {
        this.expr = expr ;
        this.var1 = v1 ;
        this.var2 = v2 ;
    }

    public Expr getExpr()
    {
        return expr ;
    }

    public Var getVar1()
    {
        return var1 ;
    }

    public Var getVar2()
    {
        return var2 ;
    }

    @Override
    public boolean equalTo(Element el2, NodeIsomorphismMap isoMap)
    {
        if ( ! ( el2 instanceof ElementUnfold ) )
            return false ;
        ElementUnfold f2 = (ElementUnfold)el2 ;
        if ( ! this.getVar1().equals(f2.getVar1()) )
            return false ;
        if ( this.getVar2() == null && f2.getVar2() != null )
            return false ;
        if ( this.getVar2() != null && this.getVar2().equals(f2.getVar2()) )
            return false ;
        if ( ! this.getExpr().equals(f2.getExpr()) )
            return false ;
        return true ;
    }

    @Override
    public int hashCode()
    {
        if ( var2 == null )
            return var1.hashCode()^expr.hashCode();
        else
            return var1.hashCode()^var2.hashCode()^expr.hashCode();
    }

    @Override
    public void visit(ElementVisitor v)
    {
        v.visit(this) ;
    }

}
