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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.hp.hpl.jena.sparql.core.Var;

public class Scope2 implements Scope
{
    Scope left ; 
    Scope right ;
    //ScopeStatus scopeStatus ;
    
    public Scope2(Scope left, Scope right)
    { 
        this.left = left ; 
        this.right = right ;
        //this.scopeStatus = status ;
    }
    
    @Override
    public boolean hasColumnForVar(Var var)
    { 
        if ( left != null && left.hasColumnForVar(var) )
            return true ;
        if ( right != null && right.hasColumnForVar(var) )
            return true ;
        return false ;
    }
        
    @Override
    public Set<Var> getVars()
    {
        // Better - implement Iterable 
        Set<Var> acc = new LinkedHashSet<Var>() ;
        if ( left != null ) acc.addAll(left.getVars()) ;
        if ( right != null ) acc.addAll(right.getVars()) ;
        return acc ;
    }
    
    @Override
    public boolean isEmpty()
    { return left.isEmpty() && right.isEmpty() ; }

    @Override
    public Set<ScopeEntry> findScopes()
    {
        Set<ScopeEntry> x = new HashSet<ScopeEntry>() ;
        for ( Var v : getVars() )
        {
            ScopeEntry e = findScopeForVar(v) ;
            x.add(e) ;
        }
        return x ;
    }
    
    @Override
    public ScopeEntry findScopeForVar(Var var)
    {
        // Return a fixed ScopeEntry in preference to an optional one.
        // Return a more rightward optional (c.f. coalesce) if both optional
        ScopeEntry c1 = null ;
        
        if ( left != null )
            c1 = left.findScopeForVar(var) ;
        
        if ( c1 != null && c1.getStatus() == ScopeStatus.FIXED )
            return c1 ;
        
        // Got no Scope or one that's optional.
        ScopeEntry c2 = null ;
        
        if ( right != null )
            c2 = right.findScopeForVar(var) ;
        if ( c2 != null && c2.getStatus() == ScopeStatus.FIXED )
            return c2 ;
        
        // No fixed out - return an optional if present.
        // Prefer the rigth to the left 
        
        if ( c2 != null )
            return c2 ;
        if ( c1 != null )
            return c1 ;
        
        
        return null ;
    }
    
    @Override
    public String toString()
    {
        String x = "" ;
        if ( ! left.isEmpty() )
            x = left.toString() ;
        if ( !left.isEmpty() && !right.isEmpty() )
            x = x + " " ;
        if ( ! right.isEmpty() )
            x = x + right.toString();
        return x ;
    }
}
