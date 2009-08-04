/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.table;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.ResultSetStream;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingBase;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils;
import com.hp.hpl.jena.sparql.engine.ref.Evaluator;

public abstract class TableBase implements Table
{
    protected TableBase() {}

    final public
    void close()
    {
        closeTable() ;
    }
    
    protected abstract void closeTable() ;

    final public Table eval(Evaluator evaluator)  { return this ; }
    
   
    public void addBinding(Binding binding)
    { throw new UnsupportedOperationException("Table.add") ; }
    
    public boolean contains(Binding b)
    {
        QueryIterator qIter = iterator(null) ;
        try {
            for ( ; qIter.hasNext() ; )
            {
                Binding b2 = qIter.nextBinding() ;
                if ( BindingUtils.equals(b,b2) )
                    return true ;
            }
            return false ;
        } finally { qIter.close() ; }
    }
    
    public abstract int size() ;
    
    public abstract boolean isEmpty() ;
    
    public ResultSet toResultSet()
    {
        QueryIterator qIter = iterator(null) ;
        ResultSet rs = new ResultSetStream(getVarNames(), null, qIter) ;
        rs = ResultSetFactory.makeRewindable(rs) ;
        qIter.close() ;
        return rs ;
    }
    
    @Override
    public String toString()
    {
        return TableWriter.asSSE(this) ; 
    }
    
    @Override
    public int hashCode()
    { 
        int hash = 0 ;
        QueryIterator qIter = iterator(null) ;
        try {
            for ( ; qIter.hasNext() ; )
            {
                Binding binding = qIter.nextBinding() ;
                hash ^= binding.hashCode();
            }
            return hash ;
        } finally { qIter.close() ; }
    }

    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;
        if ( ! ( other instanceof Table) ) return false ;
        Table table = (Table)other ;
        if ( table.size() != this.size() )
            return false ;
        QueryIterator qIter1 = iterator(null) ;
        QueryIterator qIter2 = table.iterator(null) ;
        try {
            for ( ; qIter1.hasNext() ; )
            {
                Binding bind1 = qIter1.nextBinding() ;
                Binding bind2 = qIter2.nextBinding() ;
                if ( ! BindingBase.equals(bind1, bind2) )
                    return false ; 
            }
            return true ;
        } finally { qIter1.close() ; qIter2.close() ;}
    }

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