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

package org.apache.jena.sparql.exec;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;

public interface RowSet extends Iterator<Binding> {

    @Override public boolean hasNext() ;

    @Override public Binding next() ;

    public List<Var> getResultVars() ;

    /**
     * Create a {@link RowSetRewindable} from the current position to the end.
     * This consumes this RowSet - the iterator will have ended after a call to this method.
     */
    public default RowSetRewindable rewindable() {
        return RowSetMem.create(this);
    }

    /**
     * Return a {@code RowSet} that is not connected to the original source.
     * This consumes this ResultSet and produces another one.
     */
    public default RowSet materialize() {
        return rewindable();
    }

    /** Return the row number. The first row is row 1. */
    public long getRowNumber();

    public static RowSet adapt(ResultSet resultSet) {
        if ( resultSet instanceof ResultSetAdapter )
            return ((ResultSetAdapter)resultSet).get();
        return new RowSetAdapter(resultSet);
    }

    /**
     * Turn a {@link QueryIterator} into a RowSet.
     * This operation does not materialize the QueryIterator.
     */
    public static RowSet create(QueryIterator qIter, List<Var> vars) {
        return new RowSetStream(vars, qIter);
    }

    /**
     * Normally a RowSet is processed until complete which implicitly closes any
     * underlying resources. This "close" operation exists to explicitly do this in
     * cases where it does onto automatically. There is no need to close RowSets
     * normally - it is the {@link QueryExec} that should be closed.
     */
    public void close();
}
