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
import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterNullIterator ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSingleton ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

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
    
    @Override
    public QueryIterator iterator(ExecutionContext execCxt)
    {
        // Root binding?
        Binding binding = BindingFactory.binding(var, value) ;
        QueryIterator qIter = QueryIterSingleton.create(binding, var, value, execCxt) ;
        return qIter ;
    }

    @Override
    public QueryIterator matchRightLeft(Binding bindingLeft, boolean includeOnNoMatch,
                                        ExprList conditions,
                                        ExecutionContext execContext)
    {
        boolean matches = true ;
        Node other = bindingLeft.get(var) ;
        
        if ( other == null )
        {
            // Not present - return the merge = the other binding + this (var/value)
            Binding mergedBinding = BindingFactory.binding(bindingLeft, var, value) ;
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

    @Override
    public List<Var> getVars()
    {
        List<Var> x = new ArrayList<Var>() ;
        x.add(var) ;
        return x ;
    }
    
    @Override
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
