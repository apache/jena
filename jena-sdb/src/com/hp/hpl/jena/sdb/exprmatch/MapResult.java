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

package com.hp.hpl.jena.sdb.exprmatch;

import java.util.HashMap;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.util.ExprUtils;

public class MapResult extends HashMap<Var, Expr>
{
    public void put(Var var, String v)
    {
        super.put(var, ExprUtils.parse(v)) ;
    }
    
    // Otherwise, Java6 will call .get(Object)
    // Why? Isn't this a bug?
    public Expr get(String varName) { return super.get(Var.alloc(varName)) ; }
}
