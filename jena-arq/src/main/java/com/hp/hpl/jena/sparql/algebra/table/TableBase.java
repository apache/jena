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

import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.sparql.algebra.Table ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.ResultSetStream ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingBase ;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils ;
import com.hp.hpl.jena.sparql.engine.ref.Evaluator ;

public abstract class TableBase implements Table {
    protected TableBase() {}

    @Override
    final public void close() {
        closeTable() ;
    }

    protected abstract void closeTable() ;

    final public Table eval(Evaluator evaluator) {
        return this ;
    }

    @Override
    public void addBinding(Binding binding) {
        throw new UnsupportedOperationException("Table.add") ;
    }

    @Override
    public boolean contains(Binding b) {
        QueryIterator qIter = iterator(null) ;
        try {
            for (; qIter.hasNext();) {
                Binding b2 = qIter.nextBinding() ;
                if ( BindingUtils.equals(b, b2) )
                    return true ;
            }
            return false ;
        } finally {
            qIter.close() ;
        }
    }

    @Override
    public abstract int size() ;

    @Override
    public abstract boolean isEmpty() ;

    @Override
    public ResultSet toResultSet() {
        QueryIterator qIter = iterator(null) ;
        ResultSet rs = new ResultSetStream(getVarNames(), null, qIter) ;
        rs = ResultSetFactory.makeRewindable(rs) ;
        qIter.close() ;
        return rs ;
    }

    @Override
    public String toString() {
        return TableWriter.asSSE(this) ;
    }

    @Override
    public int hashCode() {
        int hash = 0 ;
        QueryIterator qIter = iterator(null) ;
        try {
            for (; qIter.hasNext();) {
                Binding binding = qIter.nextBinding() ;
                hash ^= binding.hashCode() ;
            }
            return hash ;
        } finally {
            qIter.close() ;
        }
    }

    @Override
    public boolean equals(Object other) {
        if ( this == other )
            return true ;
        if ( !(other instanceof Table) )
            return false ;
        Table table = (Table)other ;
        if ( table.size() != this.size() )
            return false ;
        QueryIterator qIter1 = iterator(null) ;
        QueryIterator qIter2 = table.iterator(null) ;
        try {
            for (; qIter1.hasNext();) {
                Binding bind1 = qIter1.nextBinding() ;
                Binding bind2 = qIter2.nextBinding() ;
                if ( !BindingBase.equals(bind1, bind2) )
                    return false ;
            }
            return true ;
        } finally {
            qIter1.close() ;
            qIter2.close() ;
        }
    }

}
