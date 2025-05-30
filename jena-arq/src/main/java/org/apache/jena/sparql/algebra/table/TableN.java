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

package org.apache.jena.sparql.algebra.table ;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Objects ;

import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper ;

public class TableN extends TableBase {
    protected List<Binding> rows = new ArrayList<>() ;
    protected List<Var>     vars = new ArrayList<>() ;

    public TableN() {}

    public TableN(List<Var> vars) {
        if ( vars != null )
            this.vars = vars ;
    }

    /**
     * Build table from an iterator.
     * This operation reads the QueryIterator to
     * completion when creating the table.
     */
    public TableN(QueryIterator qIter) {
        materialize(qIter) ;
    }

    protected TableN(List<Var> variables, List<Binding> rows) {
        this.vars = Objects.requireNonNull(variables) ;
        this.rows = Objects.requireNonNull(rows) ;
    }

    private void materialize(QueryIterator qIter) {
        qIter.forEachRemaining(this::addBinding);
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
        return QueryIterPlainWrapper.create(rows.iterator(), execCxt) ;
    }

    @Override
    public void closeTable() {
        // Don't release rows - the same TableN object may be used in multiple places.
    }

    @Override
    public List<String> getVarNames() {
        return Var.varNames(vars) ;
    }

    @Override
    public List<Var> getVars() {
        return vars ;
    }

    public List<Binding> getRows() {
        return rows;
    }
}
