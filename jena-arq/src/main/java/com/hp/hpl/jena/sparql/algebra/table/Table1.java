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

package com.hp.hpl.jena.sparql.algebra.table ;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.riot.out.NodeFmtLib ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSingleton ;

/** A table of one row of one binding */
public class Table1 extends TableBase {
    private Var  var ;
    private Node value ;

    public Table1(Var var, Node value) {
        this.var = var ;
        this.value = value ;
    }

    @Override
    public Iterator<Binding> rows() {
        Binding b = BindingFactory.binding(var, value) ;
        return Iter.singleton(b) ;
    }

    @Override
    public QueryIterator iterator(ExecutionContext execCxt) {
        // Root binding?
        Binding binding = BindingFactory.binding(var, value) ;
        QueryIterator qIter = QueryIterSingleton.create(binding, var, value, execCxt) ;
        return qIter ;
    }

    @Override
    public void closeTable() {}

    @Override
    public List<Var> getVars() {
        List<Var> x = new ArrayList<>() ;
        x.add(var) ;
        return x ;
    }

    @Override
    public List<String> getVarNames() {
        List<String> x = new ArrayList<>() ;
        x.add(var.getVarName()) ;
        return x ;
    }

    @Override
    public int size() {
        return 1 ;
    }

    @Override
    public boolean isEmpty() {
        return false ;
    }

    @Override
    public String toString() {
        return "Table1(" + var + "," + NodeFmtLib.displayStr(value) + ")" ;
    }
}
