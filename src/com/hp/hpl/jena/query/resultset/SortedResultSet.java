/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.resultset;

import java.util.*;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.core.ResultBinding;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine.*;
import com.hp.hpl.jena.query.engine.binding.Binding;
import com.hp.hpl.jena.query.engine.binding.BindingComparator;
import com.hp.hpl.jena.query.engine.binding.BindingMap;
import com.hp.hpl.jena.query.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.rdf.model.RDFNode;


/** Sort a result set. */

public class SortedResultSet implements ResultSet
{
    // See also QueryIterSort
    // This class processes ResultSet's (which may not come from a query)
    // QueryIterSort processes QueryIterators.
    // As we need to unwrap the ResultSet into bindings, we might as well sort at that point,
    // otherwise get two copies - one for downcasting, one for QueryIterSort.
    
    ResultSet resultSet ;
    
    public SortedResultSet(ResultSet rs, List conditions)
    {
        this(rs, new BindingComparator(conditions)) ;
    }
    
    private SortedResultSet(ResultSet rs, Comparator comparator)
    {
        // Put straight into a sorted structure 
        SortedSet sorted = new TreeSet(comparator) ;
        
        for ( ; rs.hasNext() ; )
        {
            QuerySolution qs = rs.nextSolution() ;
            Binding b = null ;
            // Copy if unknown
            if ( qs instanceof ResultBinding )
                b = ((ResultBinding)qs).getBinding() ;
            else
                b = copyToBinding(qs) ; 
            sorted.add(b) ;
        }
            
        QueryIterator qIter = new QueryIterPlainWrapper(sorted.iterator()) ;
        resultSet = new ResultSetStream(rs.getResultVars(), null, qIter) ;
    }
    
    public boolean hasNext()
    {
        return resultSet.hasNext() ;
    }

    public Object next()
    {
        return resultSet.next() ;
    }

    public QuerySolution nextSolution()
    {
        return resultSet.nextSolution() ;
    }

    public int getRowNumber()
    {
        return resultSet.getRowNumber() ;
    }

    public List getResultVars()
    {
        return resultSet.getResultVars() ;
    }

    public boolean isOrdered() { return true ; }
    public boolean isDistinct() { return resultSet.isDistinct() ; }
    
    public void remove()
    {
        throw new UnsupportedOperationException(SortedResultSet.class.getName()+".remove") ;
    }
    
    
    private Binding copyToBinding(QuerySolution qs)
    {
        BindingMap b = new BindingMap() ;
        for ( Iterator iter = qs.varNames() ; iter.hasNext() ; )
        {
            String varName = (String)iter.next() ;
            RDFNode rn = qs.get(varName) ;
            b.add(Var.alloc(varName), rn.asNode()) ;
        }
        return b ;
    }

}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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