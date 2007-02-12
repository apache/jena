/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine.ref.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.binding.Binding;
import com.hp.hpl.jena.query.engine.binding.BindingMap;
import com.hp.hpl.jena.query.engine.iterator.QueryIterNullIterator;
import com.hp.hpl.jena.query.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.query.expr.ExprList;

public class TableSimple extends TableBase
{
    boolean iteratorReturned = false ;
    private QueryIterator input ;
    
    public TableSimple(QueryIterator qIter)
    {
        input = qIter ;
    }
    
    // Note - this table is the RIGHT table, and takes a LEFT binding.
    public QueryIterator matchRightLeft(Binding bindingLeft, boolean includeOnNoMatch,
                                        ExprList conditions,
                                        ExecutionContext execContext)
    {
        materialize() ;
        List out = new ArrayList() ;
        for ( Iterator iter = rows.iterator() ; iter.hasNext() ; )
        {
            Binding bindingRight = (Binding)iter.next() ;
            
            Binding r =  merge(bindingLeft, bindingRight) ;
            if ( r == null )
                continue ;
            // This does the conditional part. Theta-join.
            if ( conditions == null || conditions.isSatisfied(r, execContext.getContext()) )
                out.add(r) ;
        }
                
        if ( out.size() == 0 && includeOnNoMatch )
            out.add(bindingLeft) ;
        
        if ( out.size() == 0 )
            return new QueryIterNullIterator(execContext) ;
        return new QueryIterPlainWrapper(out.iterator(), execContext) ;
    }

    // This is the SPARQL merge rule. 
    private static Binding merge(Binding bindingLeft, Binding bindingRight)
    {
        // Test to see if compatible: Iterate over variables in left
        boolean matches = true ;
        for ( Iterator vIter = bindingLeft.vars() ; vIter.hasNext() ; )
        {
            Var v = (Var)vIter.next();
            Node nLeft  = bindingLeft.get(v) ; 
            Node nRight = bindingRight.get(v) ;
            
            if ( nRight != null && ! nRight.equals(nLeft) )
            {
                matches = false ;
                break ;
            }
        }
        if ( ! matches ) 
            return null ;
        
        // If compatible, merge. Iterate over variables in right but not in left.
        Binding b = new BindingMap(bindingLeft) ;
        for ( Iterator vIter = bindingRight.vars() ; vIter.hasNext() ; )
        {
            Var v = (Var)vIter.next();
            Node n = bindingRight.get(v) ;
            if ( ! bindingLeft.contains(v) )
                b.add(v, n) ;
        }
        return b ;
    }
    
    public QueryIterator createIterator(ExecutionContext execCxt)
    {
        if ( iteratorReturned )
        {
            LogFactory.getLog(TableSimple.class).fatal("Iterator already returned") ;
            return null ;
        }
        iteratorReturned = true ;
        return input ;
    }
    
    public void closeTable()
    {
        if (input != null ) input.close() ;
        input = null ;
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