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

package com.hp.hpl.jena.sparql.syntax;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.algebra.Table ;
import com.hp.hpl.jena.sparql.algebra.table.TableData ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.resultset.ResultSetCompare ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;
import com.hp.hpl.jena.sparql.util.NodeUtils ;

public class ElementData extends Element
{
    private List<Var> vars = new ArrayList<>() ;
    private List<Binding> rows = new ArrayList<>() ;
    
    public ElementData()
    {
    }

    public Table getTable()
    {
        return new TableData(vars, rows) ;
    }

    public List<Var> getVars()      { return vars ; }
    public List<Binding> getRows()  { return rows ; }
    
    public void add(Var var)
    { 
        if ( ! vars.contains(var) ) 
            vars.add(var) ; 
    }
    
    public void add(Binding binding)
    {
        Iterator<Var> iter = binding.vars() ;
        while(iter.hasNext())
        {
            Var v = iter.next() ;
            if ( ! vars.contains(v) )
                throw new ARQException("Variable "+v+" not already declared for ElementData") ;
        }
        rows.add(binding) ;
    }
    
    @Override
    public boolean equalTo(Element el2, NodeIsomorphismMap isoMap)
    {
        if ( ! ( el2 instanceof ElementData ) )
            return false ;
        ElementData f2 = (ElementData)el2 ;
        if ( ! vars.equals(f2.vars) )
            return false ;
        if ( ! ResultSetCompare.equalsByTest(rows, f2.rows, new ResultSetCompare.BNodeIso(NodeUtils.sameTerm)) )
            return false ;
        return true ;
    }

    @Override
    public int hashCode()
    {
        return vars.hashCode()^rows.hashCode() ;
    }

    @Override
    public void visit(ElementVisitor v)
    {
        v.visit(this) ;
    }
}
