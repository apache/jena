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

import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.sparql.algebra.Table ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingLib;
import org.apache.jena.sparql.engine.ref.Evaluator ;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetStream;

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
                if ( BindingLib.equals(b, b2) )
                    return true ;
            }
            return false ;
        } finally {
            qIter.close() ;
        }
    }

    @Override
    public RowSet toRowSet() {
        return new RowSetStream(getVars(), rows());
    }

    @Override
    public abstract int size() ;

    @Override
    public abstract boolean isEmpty() ;

    @Override
    public String toString() {
        return TableWriter.asSSE(this) ;
    }

    @Override
    public int hashCode() {
        int hash = 0 ;
        Iterator<Binding> iter = rows();
        try {
            for (  ; iter.hasNext();) {
                Binding binding = iter.next() ;
                hash ^= binding.hashCode() ;
            }
            return hash ;
        } finally { Iter.close(iter);}
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
        if ( !table.getVars().equals(getVars()) )
            return false ;
        Iterator<Binding> iter1 = rows();
        Iterator<Binding> iter2 = table.rows();
        try {
            for (; iter1.hasNext();) {
                Binding bind1 = iter1.next() ;
                Binding bind2 = iter2.next() ;
                if ( !BindingLib.equals(bind1, bind2) )
                    return false ;
            }
            return true ;
        } finally {
            Iter.close(iter1);
            Iter.close(iter2);
        }
    }
}
