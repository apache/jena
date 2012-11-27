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

import java.util.Set ;

import org.apache.jena.atlas.iterator.Iter ;

import com.hp.hpl.jena.sparql.core.Var ;

public class ScopeOptional implements Scope
{
    private Scope scope ;
    private ScopeStatus scopeStatus = ScopeStatus.OPTIONAL ;

    // May be better to copy this and mutate the scopen status 
    public ScopeOptional(Scope subScope)
    { this.scope = subScope ; }
    
    @Override
    public ScopeEntry findScopeForVar(Var var)
    {
        ScopeEntry e = scope.findScopeForVar(var) ;
        if ( e == null )
            return null ;
        e = e.duplicate() ; // Copy - we're going to mutate it.
        e.setStatus(scopeStatus) ;
        return e ;
    }

    @Override
    public Set<Var> getVars()
    {
        return scope.getVars() ;
    }

    @Override
    public boolean isEmpty()
    { return scope.isEmpty() ; }
    
    @Override
    public Set<ScopeEntry> findScopes()
    {
        Set<ScopeEntry> x = scope.findScopes() ;
        Iter.apply(x, ScopeEntry.SetOpt) ;
        return x ;
    }
    
    @Override
    public boolean hasColumnForVar(Var var)
    {
        return scope.hasColumnForVar(var) ;
    }

    @Override
    public String toString()
    {
        return "Opt("+scope.toString()+")" ;
    }
}
