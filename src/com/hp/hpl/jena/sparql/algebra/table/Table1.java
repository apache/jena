/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.table;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.Binding0 ;
import com.hp.hpl.jena.sparql.engine.binding.Binding1;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterNullIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSingleton;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.util.FmtUtils;

/** A table of one row of one binding */ 
public class Table1 extends TableBase
{
    private Var var ;
    private Node value ;

    public Table1(Var var, Node value)
    {
        this.var = var ;
        this.value = value ;
    }
    
    public QueryIterator iterator(ExecutionContext execCxt)
    {
        // Root binding?
        QueryIterator qIter = QueryIterSingleton.create(new Binding0(), var, value, execCxt) ;
        return qIter ;
    }

    public QueryIterator matchRightLeft(Binding bindingLeft, boolean includeOnNoMatch,
                                        ExprList conditions,
                                        ExecutionContext execContext)
    {
        boolean matches = true ;
        Node other = bindingLeft.get(var) ;
        
        if ( other == null )
        {
            // Not present - return the merge = the other binding + this (var/value)
            Binding mergedBinding = new Binding1(bindingLeft, var, value) ;
            return QueryIterSingleton.create(mergedBinding, execContext) ;
        }
        
        if ( ! other.equals(value) )
            matches = false ;
        else
        {
            if ( conditions != null )
                matches = conditions.isSatisfied(bindingLeft, execContext) ;
        }
        
        if ( ! matches && ! includeOnNoMatch)
            return new QueryIterNullIterator(execContext) ;
        // Matches, or does not match and it's a left join - return the left binding. 
        return QueryIterSingleton.create(bindingLeft, execContext) ;
    }

    @Override
    public void closeTable()        {}

    public List<Var> getVars()
    {
        List<Var> x = new ArrayList<Var>() ;
        x.add(var) ;
        return x ;
    }
    
    public List<String> getVarNames()
    {
        List<String> x = new ArrayList<String>() ;
        x.add(var.getVarName()) ;
        return x ;
    }
    
    @Override
    public int size()           { return 1 ; }
    @Override
    public boolean isEmpty()    { return false ; }
    
    @Override
    public String toString()    { return "Table1("+var+","+FmtUtils.stringForNode(value)+")" ; }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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