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

package com.hp.hpl.jena.sparql.algebra.table;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;

import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterNullIterator ;

public class TableEmpty extends TableBase
{
    public TableEmpty()
    { }
    
    @Override
    public Iterator<Binding> rows() {
        return Iter.nullIterator() ;
    }

    @Override
    public QueryIterator iterator(ExecutionContext execCxt) {
        return QueryIterNullIterator.create(execCxt) ;
    }

    @Override
    public boolean contains(Binding binding) { return false ; }
    
    @Override
    public void closeTable()    { }

    @Override
    public List<String> getVarNames()   { return new ArrayList<>() ; }

    @Override
    public List<Var> getVars()       { return new ArrayList<>() ; }
    
    @Override
    public String toString()    { return "TableEmpty" ; }

    @Override
    public int size()           { return 0 ; }
    @Override
    public boolean isEmpty()    { return true ; }
}
