/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine.ref.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.ResultSetStream;
import com.hp.hpl.jena.query.engine.binding.Binding;
import com.hp.hpl.jena.query.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.query.engine.ref.Table;
import com.hp.hpl.jena.query.util.PrintUtils;
import com.hp.hpl.jena.query.util.Utils;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public abstract class TableBase implements Table
{
    // Materialization support
    private boolean materialized = false ;
    protected List rows = null ;
    protected List vars = null ;  // Not necessary : could be a set
    
    protected TableBase() {}

    final public
    void close()
    {
        closeTable() ;
        rows = null ; 
        vars = null ;
    }
    
    protected abstract void closeTable() ;

    public void dump()
    {   
        System.out.println("Table: "+Utils.className(this)) ;
        materialize() ;
        if ( rows.size() == 0 )
        { 
            if ( vars.size() == 0 )
                System.out.println("++ Empty table, no variables") ;
            else
            {
                System.out.print("++ Empty table, with variables:") ;
                PrintUtils.printList(System.out, vars) ;
                System.out.println() ;
            }
        }
        else
            ResultSetFormatter.out(toResultSet()) ;
    }

    final
    public QueryIterator iterator(ExecutionContext execCxt)
    {
        if ( !materialized )
            // Can't then materialize
            return createIterator(execCxt) ;
        return new QueryIterPlainWrapper(rows.iterator(), execCxt) ;
    }

    // Contract - call this at most once.
    protected abstract QueryIterator createIterator(ExecutionContext execCxt) ;

    public void materialize()
    {
        if ( rows != null ) return ;
        materialized = true ;
        rows = new ArrayList() ;
        vars = new ArrayList() ;
        
        QueryIterator source = createIterator(null) ;
        
        while ( source.hasNext() )
        {
            Binding b = source.nextBinding() ;
            for ( Iterator names = b.vars() ; names.hasNext() ; )
            {
                Var v = (Var)names.next() ;
                if ( ! vars.contains(v))
                    vars.add(v) ;
            }
            rows.add(b) ;
        }
        source.close() ;
    }
    
    private ResultSet toResultSet()
    {
        materialize() ;
        return new ResultSetStream(vars, ModelFactory.createDefaultModel(), iterator(null)) ;
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