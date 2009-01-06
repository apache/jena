/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.binding;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;

/** A binding that is fixed - used in calculating DISTINCT result sets.
 *  .hashCode and .equals are overridden for content equality semantics (where
 *  "equality" means Node.equals, not Node.sameValueAs).
 * 
 * @author   Andy Seaborne
 */


public class BindingFixed extends BindingWrapped
{
    int varSize = 0 ;
    int calcHashCode = 0 ;
    private boolean haveDoneHashCode = false ; 
    
    public BindingFixed(Binding binding)
    { super(binding) ; }
    
    private int calcHashCode()
    {
        int _hashCode = 0 ;
        for ( Iterator<Var> iter = vars() ; iter.hasNext() ; )
        {
            Var var = iter.next() ;
            Node n = get(var) ;
            if ( n == null )
                continue ;
            // Independent of variable order.
            _hashCode = _hashCode^n.hashCode()^var.hashCode() ; 
            varSize ++ ;
        }
        return _hashCode ;
    }
        
    @Override
    public void add(Var var, Node node)
    { throw new UnsupportedOperationException("BindingFixed.add") ; }

    @Override
    public boolean equals(Object obj)
    {
        if ( this == obj ) return true ;
        
        if ( ! ( obj instanceof BindingFixed) )
            return false ;
        
        BindingFixed b = (BindingFixed)obj ;
        return BindingBase.equals(this, b) ; 
    }
    
    @Override
    public int hashCode()
    {
        if ( ! haveDoneHashCode )
        {
            calcHashCode = calcHashCode() ;
            haveDoneHashCode = true ;
        }
        return calcHashCode ;
    }
    
    protected void checkAdd1(Var v, Node node) { }
}

/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
