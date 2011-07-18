/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.binding;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;

public abstract class BindingProjectBase extends BindingBase
{
    private Binding binding ;
    private List<Var> actualVars = null ;

    public BindingProjectBase(Binding bind)
    { 
        this(bind, null) ;
    }
    
    public BindingProjectBase(Binding bind, Binding parent)
    { 
        super(parent) ;
        binding = bind ;
    }

    @Override
    protected void add1(Var var, Node node)
    { throw new UnsupportedOperationException("BindingProject.add1") ; }

    @Override
    protected void checkAdd1(Var var, Node node)
    {}

    protected abstract boolean accept(Var var) ; 
    
    @Override
    protected boolean contains1(Var var)
    {
        return accept(var) && binding.contains(var) ;
    }

    @Override
    protected Node get1(Var var)
    {
        if ( ! accept(var) )
            return null ; 
        return binding.get(var) ;
    }

    @Override
    protected Iterator<Var> vars1()
    {
        return actualVars().iterator() ;
    }

    private List<Var> actualVars()
    {
        if ( actualVars == null )
        {
            actualVars = new ArrayList<Var>() ;
            Iterator<Var> iter = binding.vars() ;
            for ( ; iter.hasNext() ; )
            {
                Var v = iter.next() ;
                if ( accept(v) )
                    actualVars.add(v) ;
            }
        }
        return actualVars ;
    }
    
    @Override
    protected int size1()
    {
        return actualVars().size() ;
    }
    
    
    
    // NB is the projection and the binding don't overlap it is also empty. 
    @Override
    protected boolean isEmpty1()
    {
        if ( binding.isEmpty() ) return true ;
        if ( size1() == 0  ) return true ;
        return false ;
//        for ( Var v : projectionVars )
//        {
//            if ( binding.contains(v))  
//                return false ;
//        }
//        return true ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
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