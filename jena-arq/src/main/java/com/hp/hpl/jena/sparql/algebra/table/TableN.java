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

import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper ;

public class TableN extends TableBase {
    protected List<Binding> rows = new ArrayList<>() ;
    protected List<Var>     vars = new ArrayList<>() ;

    public TableN() {}

    public TableN(List<Var> vars) {
        if ( vars != null )
            this.vars = vars ;
    }

    public TableN(QueryIterator qIter) {
        materialize(qIter) ;
    }

    protected TableN(List<Var> variables, List<Binding> rows) {
        this.vars = variables ;
        this.rows = rows ;
    }

    private void materialize(QueryIterator qIter) {
        while (qIter.hasNext()) {
            Binding binding = qIter.nextBinding() ;
            addBinding(binding) ;
        }
        qIter.close() ;
    }

    @Override
    public void addBinding(Binding binding) {
        for (Iterator<Var> names = binding.vars(); names.hasNext();) {
            Var v = names.next() ;
            if ( !vars.contains(v) )
                vars.add(v) ;
        }
        rows.add(binding) ;
    }

    @Override
    public int size() {
        return rows.size() ;
    }

    @Override
    public boolean isEmpty() {
        return rows.isEmpty() ;
    }

    @Override
    public Iterator<Binding> rows() {
        return rows.iterator() ;
    }

    @Override
    public QueryIterator iterator(ExecutionContext execCxt) {
        return new QueryIterPlainWrapper(rows.iterator(), execCxt) ;
    }

    @Override
    public void closeTable() {
        rows = null ;
        // Don't clear the vars in case code later asks for the variables.
    }

    @Override
    public List<String> getVarNames() {
        return Var.varNames(vars) ;
    }

    @Override
    public List<Var> getVars() {
        return vars ;
    }
}
