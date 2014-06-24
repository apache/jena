/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        SortedSet<Binding> sorted = new TreeSet<>(comparator) ;
        
        for ( ; rs.hasNext() ; )
        {
            Binding b = rs.nextBinding() ;
            sorted.add(b) ;
        }
            
        qIter = new QueryIterPlainWrapper(sorted.iterator()) ;
        resultVars = rs.getResultVars() ;
        //resultSet = new ResultSetStream(rs.getResultVars(), null, qIter) ;
    }
    
    @Override
    public boolean hasNext()
    {
        return qIter.hasNext() ;
    }

    @Override
    public QuerySolution next()
    {
        return new ResultBinding(model, nextBinding()) ;
    }

    @Override
    public Binding nextBinding()
    {
        rowNumber++ ;
        return qIter.nextBinding() ;
    }

    @Override
    public QuerySolution nextSolution()
    {
        return new ResultBinding(null, nextBinding()) ;
    }
    
    @Override
    public int getRowNumber()
    {
        return rowNumber ;
    }

    @Override
    public List<String> getResultVars()
    {
        return resultVars ;
    }

    public boolean isOrdered() { return true ; }
    
    @Override
    public Model getResourceModel()
    {
        return model ;
    }

    @Override
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
