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

import java.util.HashMap ;
import java.util.HashSet ;
import java.util.Map ;
import java.util.Set ;

import org.apache.jena.atlas.iterator.Transform ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn ;
import com.hp.hpl.jena.sparql.core.Var ;

public class ScopeRename implements Scope
{
    private static Logger log = LoggerFactory.getLogger(ScopeRename.class) ; 
    private Scope scope ;
    private Map<Var, SqlColumn> frame = new HashMap<Var, SqlColumn>() ;

    public ScopeRename(Scope oldScope)
    { this.scope = oldScope ; }

    // See ScopeBase for commonality of code
    
    public void setAlias(Var var, SqlColumn col)
    {
        if ( ! scope.hasColumnForVar(var) )
            log.warn("No underlying column for variable "+var) ;
        frame.put(var, col) ;
    }
    
    
    @Override
    public ScopeEntry findScopeForVar(Var var)
    {
        check(var) ;
        if ( ! frame.containsKey(var) )
            return null ;
        
        ScopeEntry e = new ScopeEntry(var, frame.get(var) ) ;
        e.setStatus(scope.findScopeForVar(var).getStatus()) ;
        return e ;
//        ScopeEntry e = scope.findScopeForVar(var) ;
//        if ( e == null )
//            return null ;
//        return converter.convert(e) ;
    }

    @Override
    public Set<ScopeEntry> findScopes()
    {
        Set<ScopeEntry> x = new HashSet<ScopeEntry>() ;
        for ( Var v : frame.keySet() )
        {
            ScopeEntry e = findScopeForVar(v) ;
            x.add(e) ;
        }
        return x ;
//        Set<ScopeEntry> x = scope.findScopes() ;
//        x = toSet(map(x, converter)) ;
//        return x ;
    }

    @Override
    public boolean isEmpty()
    { return frame.isEmpty() ; } 

    @Override
    public Set<Var> getVars()
    {
        return frame.keySet() ;
    }

    @Override
    public boolean hasColumnForVar(Var var)
    {
        check(var) ;
        return frame.containsKey(var) ;
    }

    private void check(Var var)
    {
        if ( true )
        {
            boolean sub = scope.hasColumnForVar(var) ;
            boolean res = frame.containsKey(var) ;
            if ( sub && ! res )
                log.warn("Corruption: in subscope but not in aliases: "+var) ;
            if ( ! sub && res )
                log.warn("Corruption: not in subscope but in aliases: "+var) ;
        }
    }
    
    @Override
    public String toString()
    {
        return frame + " " + scope ; 
    }
    
    private Transform<ScopeEntry, ScopeEntry> converter = 
        new Transform<ScopeEntry, ScopeEntry>(){
            @Override
            public ScopeEntry convert(ScopeEntry entry)
            {
                entry = new ScopeEntry(entry.getVar(), entry.getColumn()) ;
                Var var = entry.getVar() ;
                SqlColumn col = frame.get(var) ;
                if ( col == null )
                {
                    log.warn("No alias for variable "+var) ;
                    return entry ;
                }
                ScopeEntry entry2 = new ScopeEntry(entry.getVar(), entry.getColumn()) ;
                entry2.setStatus(entry.getStatus()) ;
                //entry.reset(var, col, entry.getStatus()) ;
                return entry2 ;
            }} ;
}
