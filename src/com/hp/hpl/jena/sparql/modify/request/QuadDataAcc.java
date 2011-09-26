/**
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

package com.hp.hpl.jena.sparql.modify.request;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Var ;

/** Accumulate quads (excluding allowing variables) during parsing. */
public class QuadDataAcc extends QuadAcc
{
    @Override
    protected void check(Triple t)
    {
        if ( Var.isVar(getGraph()) )
            throw new QueryParseException("Variables not permitted in data quad", -1, -1) ;   
        if ( Var.isVar(t.getSubject()) || Var.isVar(t.getPredicate()) || Var.isVar(t.getObject())) 
            throw new QueryParseException("Variables not permitted in data quad", -1, -1) ;   
    }
    
    @Override
    protected void check(Quad t)
    {
        if ( Var.isVar(t.getGraph()) || 
             Var.isVar(t.getSubject()) || 
             Var.isVar(t.getPredicate()) || 
             Var.isVar(t.getObject())) 
            throw new QueryParseException("Variables not permitted in data quad", -1, -1) ;   
    }
}
