/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.binding;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;

/** A binding helper that can be a key in a Set or Map.
 *  Changing the key binding while in such a structure is not supported.  
 * 
 * @author   Andy Seaborne
 */

public class BindingKey
{
    public Binding key ;
    public Binding value ;
    
    public BindingKey(Binding binding)
    { this(binding, binding) ; }
    
    public BindingKey(Binding keyBinding, Binding valueBinding)
    { 
        this.key = keyBinding ;
        this.value = valueBinding ;
        hashCode() ;
    }

    public Binding getBinding() { return value ; } 
    public Binding getKey()     { return key ; }
    
    private boolean validHashCode = false ;
    private int keyHashCode = 0 ;
    
    @Override
    public int hashCode()
    {
        if ( ! validHashCode )
        {
            keyHashCode = calcHashCode(key) ;
            validHashCode = true ;
        }
        return keyHashCode ;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;
        if ( ! ( other instanceof BindingKey ) )
            return false ;
        Binding binding2 = ((BindingKey)other).getKey() ;
        
        return BindingBase.equals(key, binding2) ;
    }
    
    private static final int EmptyBindingHashCode = 123 ;
    private static int calcHashCode(Binding binding)
    {
        int calcHashCode = EmptyBindingHashCode ;
        for ( Iterator<Var> iter = binding.vars() ; iter.hasNext() ; )
        {
            Var var = iter.next() ;
            Node n = binding.get(var) ;
            if ( n == null )
                continue ;
            // Must be independent of variable order.
            calcHashCode = calcHashCode^n.hashCode()^var.hashCode() ; 
            //binding.add(var, n) ;
        }
        return calcHashCode ;
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
