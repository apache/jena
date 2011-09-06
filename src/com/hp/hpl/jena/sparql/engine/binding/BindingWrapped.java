/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.binding;

import java.util.Iterator ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;

/** A binding that wraps another. */


public class BindingWrapped implements Binding
{
    protected Binding binding ;
    
    public BindingWrapped(Binding other) { binding = other; } 
    
    public Binding getWrapped() { return binding ; }
    
    public void add(Var var, Node node)
    { 
        if ( ! Var.isAnonVar(var) )
        binding.add(var, node) ; }

    public void addAll(Binding other)
    { binding.addAll(other) ; }

    public boolean contains(Var var)
    {
        return binding.contains(var) ;
    }

    public Node get(Var var)
    {
        return binding.get(var) ;
    }

    public Iterator<Var> vars()
    {
        return binding.vars() ;
    }
    
    @Override
    public String toString() { return binding.toString(); }

    public int size()           { return binding.size() ; }

    public boolean isEmpty()    { return binding.isEmpty() ; }
    
    @Override
    public int hashCode() { return BindingBase.hashCode(this) ; } 
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;
        if ( ! ( other instanceof Binding) ) return false ;
        Binding binding = (Binding)other ;
        return BindingBase.equals(this, binding) ; 
    }   
    
}

/*
 *  (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
