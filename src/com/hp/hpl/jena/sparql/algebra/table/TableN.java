/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterNullIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.sparql.expr.ExprList;


public class TableN extends TableBase
{
    List rows = new ArrayList() ;
    List vars = new ArrayList() ;

    public TableN() {}
    
    public TableN(QueryIterator qIter)
    {
        materialize(qIter) ;
    }

    public void materialize(QueryIterator qIter)
    {
        while ( qIter.hasNext() )
        {
            Binding binding = qIter.nextBinding() ;
            addBinding(binding) ;
        }
        qIter.close() ;
    }

    public void addBinding(Binding binding)
    {
        for ( Iterator names = binding.vars() ; names.hasNext() ; )
        {
            Var v = (Var)names.next() ;
            if ( ! vars.contains(v))
                vars.add(v) ;
        }
        rows.add(binding) ;
    }
    
    public int size() { return rows.size() ; }
    
    // Note - this table is the RIGHT table, and takes a LEFT binding.
    public QueryIterator matchRightLeft(Binding bindingLeft, boolean includeOnNoMatch,
                                        ExprList conditions,
                                        ExecutionContext execContext)
    {
        List out = new ArrayList() ;
        for ( Iterator iter = rows.iterator() ; iter.hasNext() ; )
        {
            Binding bindingRight = (Binding)iter.next() ;
            
            Binding r =  merge(bindingLeft, bindingRight) ;
            if ( r == null )
                continue ;
            // This does the conditional part. Theta-join.
            if ( conditions == null || conditions.isSatisfied(r, execContext) )
                out.add(r) ;
        }
                
        if ( out.size() == 0 && includeOnNoMatch )
            out.add(bindingLeft) ;
        
        if ( out.size() == 0 )
            return new QueryIterNullIterator(execContext) ;
        return new QueryIterPlainWrapper(out.iterator(), execContext) ;
    }

 
    public QueryIterator iterator(ExecutionContext execCxt)
    {
        return new QueryIterPlainWrapper(rows.iterator(), execCxt) ;
    }
    
    public void closeTable()
    {
        rows = null ;
        // Don't clear the vars in case code later asks for the variables. 
    }

    public List getVarNames()   { return vars ; }

    public List getVars()       { return Var.varNames(vars) ; }
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