/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.binding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.query.IndexValues;
import com.hp.hpl.jena.graph.query.VariableIndexes;
import com.hp.hpl.jena.shared.DoesNotExistException;
import com.hp.hpl.jena.sparql.core.Var;

/** com.hp.hpl.jena.query.core.BindingIndex
 *  Convert between Bindings (the core main abstraction)
 *  and Jena's old style internal graph format. 
 * 
 * @author Andy Seaborne
 */

public class BindingIndex implements VariableIndexes, IndexValues
{
    List<String> indexes = new ArrayList<String>() ;
    Binding binding ;
    
    public BindingIndex(Binding b)
    {
        binding = b ; 
        for ( Iterator<Var> iter = binding.vars() ; iter.hasNext() ; )
        {
            Var var = iter.next() ;
            indexes.add(var.getVarName()) ;
        }
    }
    
    /*
     * @see com.hp.hpl.jena.graph.query.VariableIndexes#indexOf(java.lang.String)
     */
    public int indexOf(String varname)
    {
        for ( int i = 0 ; i < indexes.size() ; i++ )
        {
            if ( indexes.get(i).equals(varname) )
                return i ;
        }
        //return -1 ;
        throw new DoesNotExistException("Name not bound: "+varname) ;
    }
    
    /*
     * @see com.hp.hpl.jena.graph.query.IndexValues#get(int)
     */
    public Object get(int index)
    {
        if ( index < 0 || index > indexes.size() )
            return null ;
        String name = indexes.get(index) ;
        // The cast is a check.
        return binding.get(Var.alloc(name)) ;
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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