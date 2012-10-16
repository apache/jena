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

package com.hp.hpl.jena.sdb.core;

import com.hp.hpl.jena.sdb.exprmatch.ActionMatch;
import com.hp.hpl.jena.sdb.exprmatch.ExprMatcher;
import com.hp.hpl.jena.sdb.exprmatch.MapAction;
import com.hp.hpl.jena.sdb.exprmatch.MapResult;
import com.hp.hpl.jena.sdb.shared.SDBInternalError;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.util.ExprUtils;

public class ExprPattern
{
    Expr pattern ;
    MapAction mapAction;
    
    public ExprPattern(String pattern ,
                       Var[] vars,
                       ActionMatch[] actions)
    {
        this.pattern = ExprUtils.parse(pattern) ;
        if ( vars.length != actions.length )
            throw new SDBInternalError("Variable and action arrays are different lengths") ;  
        mapAction = new MapAction() ;
        for ( int i = 0 ; i < vars.length ; i++ )
        {
            Var var = vars[i] ;
            ActionMatch a = actions[i] ;
            mapAction.put(var, a) ;
        }
    }
    
    public MapResult match(Expr expression)
    {
        return ExprMatcher.match(expression, pattern, mapAction) ;
    }
    
}
