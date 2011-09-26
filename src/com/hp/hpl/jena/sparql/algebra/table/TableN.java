/**
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

package com.hp.hpl.jena.sparql.algebra.table;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterNullIterator ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import com.hp.hpl.jena.sparql.expr.ExprList ;


public class TableN extends TableBase
{
    protected List<Binding> rows = new ArrayList<Binding>() ;
    protected List<Var> vars = new ArrayList<Var>() ;

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

    @Override
    public void addBinding(Binding binding)
    {
        for ( Iterator<Var> names = binding.vars() ; names.hasNext() ; )
        {
            Var v = names.next() ;
            if ( ! vars.contains(v))
                vars.add(v) ;
        }
        rows.add(binding) ;
    }
    
    @Override
    public int size()           { return rows.size() ; }
    @Override
    public boolean isEmpty()    { return rows.isEmpty() ; }

    
    // Note - this table is the RIGHT table, and takes a LEFT binding.
    public QueryIterator matchRightLeft(Binding bindingLeft, boolean includeOnNoMatch,
                                        ExprList conditions,
                                        ExecutionContext execContext)
    {
        List<Binding> out = new ArrayList<Binding>() ;
        for ( Iterator<Binding> iter = rows.iterator() ; iter.hasNext() ; )
        {
            Binding bindingRight = iter.next() ;
            Binding r =  Algebra.merge(bindingLeft, bindingRight) ;
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
    
    @Override
    public void closeTable()
    {
        rows = null ;
        // Don't clear the vars in case code later asks for the variables. 
    }

    public List<String> getVarNames()   { return Var.varNames(vars) ; }

    public List<Var> getVars()          { return  vars ; }
}
