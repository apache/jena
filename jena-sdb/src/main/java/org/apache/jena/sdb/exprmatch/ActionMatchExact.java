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

package org.apache.jena.sdb.exprmatch;

import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.expr.Expr ;
import org.apache.jena.sparql.util.ExprUtils ;

public class ActionMatchExact extends ActionMatchBind
{
    Expr exprMatch ;
    
    public ActionMatchExact(String exprStr)
    {
        this(ExprUtils.parse(exprStr)) ;
    }
    
    public ActionMatchExact(Expr expr)
    {
        this.exprMatch = expr ;
    }
    
    @Override
    public boolean match(Var var, Expr expr, MapResult resultMap)
    {
        if ( ! exprMatch.equals(expr) )
            throw new NoExprMatch("ActionMatchExact: Do not match: Expected: "+exprMatch+" : Got: "+expr) ;
        // Assign is anyway : otherwise just write a constant
        return super.match(var, expr, resultMap) ;
    }
}
