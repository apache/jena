/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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
    
    public boolean hasColumnForVar(Var var)
    { 
        if ( left != null && left.hasColumnForVar(var) )
            return true ;
        if ( right != null && right.hasColumnForVar(var) )
            return true ;
        return false ;
    }
        
    public Set<Var> getVars()
    {
        // Better - implement Iterable 
        Set<Var> acc = new LinkedHashSet<Var>() ;
        if ( left != null ) acc.addAll(left.getVars()) ;
        if ( right != null ) acc.addAll(right.getVars()) ;
        return acc ;
    }
    
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
        return left.toString() + " " + right.toString(); 
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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