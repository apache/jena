/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core;

import static com.hp.hpl.jena.sdb.iterator.Streams.map;
import static com.hp.hpl.jena.sdb.iterator.Streams.toSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.iterator.Transform;
import com.hp.hpl.jena.sparql.core.Var;

public class ScopeRename implements Scope
{
    private static Log log = LogFactory.getLog(ScopeRename.class) ; 
    private Scope scope ;
    private Map<Var, SqlColumn> frame = new HashMap<Var, SqlColumn>() ;

    public ScopeRename(Scope oldScope)
    { this.scope = oldScope ; }

    public void setAlias(Var var, SqlColumn col)
    {
        if ( ! scope.hasColumnForVar(var) )
            log.warn("No underlying column for variable "+var) ;
        frame.put(var, col) ;
    }
    
    
    public ScopeEntry findScopeForVar(Var var)
    {
        check(var) ;
        ScopeEntry e = scope.findScopeForVar(var) ;
        if ( e == null )
            return null ;
        return converter.convert(e) ;
    }

    public Set<ScopeEntry> findScopes()
    {
        Set<ScopeEntry> x = scope.findScopes() ;
        x = toSet(map(x, converter)) ;
        return x ;
    }

    public boolean isEmpty()
    { return frame.isEmpty() ; } 

    public Set<Var> getVars()
    {
        return frame.keySet() ;
    }

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
            public ScopeEntry convert(ScopeEntry entry)
            {
                Var var = entry.getVar() ;
                SqlColumn col = frame.get(var) ;
                if ( col == null && entry != null )
                {
                    log.warn("No alias for variable "+var) ;
                    return entry ;
                }
                if ( col != null && entry == null )
                {
                    log.warn("No underlying column for alias: "+var) ;
                    return entry ;
                }
                
                if ( entry == null )
                    return null ;
                entry.reset(var, col, entry.getStatus()) ;
                return entry ;
            }} ;
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */