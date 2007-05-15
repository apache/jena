/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.binding;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;

/** A binding helper that can be a key in a Set or Map.
 *  Adding while in such a structure is chnaging is not supported.  
 * 
 * @author   Andy Seaborne
 * @version  $Id: BindingWrapped.java,v 1.1 2007/02/06 17:06:05 andy_seaborne Exp $
 */


public class BindingKey
{
    public Binding binding ;
    public BindingKey(Binding binding)
    { 
        this.binding = binding ;
        hashCode() ;
    }

    // Beware that changing the binding changes the .equals relationships.
    public Binding getBinding() { return binding ; } 
    
    private boolean validHashCode = false ;
    private int keyHashCode = 0 ;
    
    public int hashCode()
    {
        if ( ! validHashCode )
        {
            keyHashCode = calcHashCode(binding) ;
            validHashCode = true ;
        }
        return keyHashCode ;
    }
    
    public boolean equals(Object other)
    {
        if ( ! ( other instanceof BindingKey ) )
            return false ;
        Binding binding2 = ((BindingKey)other).getBinding() ;
        
        return BindingBase.same(binding, binding2) ;
    }
    
    private static final int EmptyBindingHashCode = 123 ;
    private static int calcHashCode(Binding binding)
    {
        int calcHashCode = EmptyBindingHashCode ;
        for ( Iterator iter = binding.vars() ; iter.hasNext() ; )
        {
            Var var = (Var)iter.next() ;
            Node n = binding.get(var) ;
            if ( n == null )
                continue ;
            // Must be independent of variable order.
            calcHashCode = calcHashCode^n.hashCode()^var.hashCode() ; 
            binding.add(var, n) ;
        }
        return calcHashCode ;
    }    
}

/*
 *  (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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
