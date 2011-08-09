/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.binding;

import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;


/** A mapping from a name to a value such that we can create a tree of levels
 *  with higher (earlier levels) being shared.
 *  Looking up a name is done by looking in the current level,
 *  then trying the parent is not found. */


public class BindingMap extends BindingBase
{
    // Bindings are often small.  Is this overkill? 
    Map<Var, Node> map = new HashMap<Var, Node>() ;
    
    /** Using BindingFactory.create is better */
    public BindingMap(Binding parent) { super(parent) ; }
    /** Using BindingFactory.create is better */
    public BindingMap() { super(BindingRoot.create()) ; } // null?

    /** Add a (name,value) */
    
    @Override
    protected void add1(Var var, Node node)
    {
        if ( ! Var.isAnonVar(var) )
            map.put(var, node) ;
    }

    @Override
    protected int size1() { return map.size() ; }
    
    @Override
    protected boolean isEmpty1() { return map.isEmpty() ; }
    
    /** Iterate over all the names of variables.
     */
    @Override
    public Iterator<Var> vars1() 
    {
        // Assumes that varnames are NOT duplicated.
        Iterator<Var> iter = map.keySet().iterator() ;
        return iter ;
    }
    
    @Override
    public boolean contains1(Var var)
    {
        return map.containsKey(var) ;
    }
    
    @Override
    public Node get1(Var var)
    {
        return map.get(var) ;
    }

    @Override
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
