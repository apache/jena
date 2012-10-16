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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;

public class ScopeBase implements Scope
{
    private Map<Var, SqlColumn> frame = new HashMap<Var, SqlColumn>() ;
    private Scope parent = null ;
    
    public ScopeBase() { parent = null ; }
    
//    public ScopeBase(Scope parent)
//    { 
//        this.parent = parent ;
//    }
    
    @Override
    public boolean hasColumnForVar(Var var)
    { 
        if ( frame.containsKey(var) )
            return true ;
        if ( parent != null )
            return parent.hasColumnForVar(var) ;
        return false ;
    }
        
    @Override
    public Set<Var> getVars()
    {
        Set<Var> x = new HashSet<Var>() ;
        x.addAll(frame.keySet()) ;
        if ( parent != null )
            x.addAll(parent.getVars()) ;
        return x ;
    }
    
    @Override
    public boolean isEmpty()
    { return frame.isEmpty() ; }
    
    @Override
    public Set<ScopeEntry> findScopes()
    {
        Set<ScopeEntry> x = new HashSet<ScopeEntry>() ;
        for ( Var v : frame.keySet() )
        {
            ScopeEntry e = findScopeForVar(v) ;
            x.add(e) ;
        }
        if ( parent != null )
            x.addAll(parent.findScopes()) ;
        return x ;
    }
    
    @Override
    public ScopeEntry findScopeForVar(Var var)
    { 
        if ( frame.containsKey(var) )
        {
            ScopeEntry e = new ScopeEntry(var, frame.get(var)) ;
            return e ;
        }
        if ( parent != null )
            return parent.findScopeForVar(var) ;
        return null ;
    }

    public void setColumnForVar(Var var, SqlColumn column)
    { 
        // Only check the frame.
        if ( frame.containsKey(var) )
        {
            LoggerFactory.getLogger(Scope.class).warn("Already has an alias: "+var+" => "+findScopeForVar(var)) ;
            return ;
        }
        frame.put(var, column) ;
    }
    
    @Override
    public String toString()
    {
        String str = "" ;
        String sep = "" ;
        for ( Var v : frame.keySet() )
        {
            SqlColumn c = frame.get(v) ;
            str = str + sep + v + ":"+c ;
            sep = " " ;
        }
        if ( parent != null )
            str = str + "=>" + parent.toString() ;
        return str ;
    }
}
