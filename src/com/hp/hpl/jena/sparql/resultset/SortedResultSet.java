/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.resultset;

import java.util.Comparator ;
import java.util.Iterator ;
import java.util.List ;
import java.util.SortedSet ;
import java.util.TreeSet ;

import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.SortCondition ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.sparql.core.ResultBinding ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingComparator ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper ;


/** Sort a result set. */

public class SortedResultSet implements ResultSet
{
    // See also QueryIterSort
    // This class processes ResultSet's (which may not come from a query)
    // QueryIterSort processes QueryIterators.
    // As we need to unwrap the ResultSet into bindings, we might as well sort at that point,
    // otherwise get two copies - one for downcasting, one for QueryIterSort.
    
    QueryIterator qIter ;
    int rowNumber = 0 ;
    List<String> resultVars = null ;
    Model model ;
    
    // Caution: this does not have the ful context available so soem conditions 
    public SortedResultSet(ResultSet rs, List<SortCondition> conditions)
    {
        // Caution: this does not have the ful context available so some conditions may get upset. 
        this(rs, new BindingComparator(conditions)) ;
    }
    
    private SortedResultSet(ResultSet rs, Comparator<Binding> comparator)
    {
        model = rs.getResourceModel() ;
        // Put straight into a sorted structure 
        SortedSet<Binding> sorted = new TreeSet<Binding>(comparator) ;
        
        for ( ; rs.hasNext() ; )
        {
            Binding b = rs.nextBinding() ;
            sorted.add(b) ;
        }
            
        qIter = new QueryIterPlainWrapper(sorted.iterator()) ;
        resultVars = rs.getResultVars() ;
        //resultSet = new ResultSetStream(rs.getResultVars(), null, qIter) ;
    }
    
    public boolean hasNext()
    {
        return qIter.hasNext() ;
    }

    public QuerySolution next()
    {
        return new ResultBinding(model, nextBinding()) ;
    }

    public Binding nextBinding()
    {
        rowNumber++ ;
        return qIter.nextBinding() ;
    }

    public QuerySolution nextSolution()
    {
        return new ResultBinding(null, nextBinding()) ;
    }
    
    public int getRowNumber()
    {
        return rowNumber ;
    }

    public List<String> getResultVars()
    {
        return resultVars ;
    }

    public boolean isOrdered() { return true ; }
    
    public Model getResourceModel()
    {
        return model ;
    }

    public void remove()
    {
        throw new UnsupportedOperationException(SortedResultSet.class.getName()+".remove") ;
    }
    
    
    private Binding copyToBinding(QuerySolution qs)
    {
        BindingMap b = BindingFactory.create() ;
        for ( Iterator<String> iter = qs.varNames() ; iter.hasNext() ; )
        {
            String varName = iter.next() ;
            RDFNode rn = qs.get(varName) ;
            b.add(Var.alloc(varName), rn.asNode()) ;
        }
        return b ;
    }

}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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