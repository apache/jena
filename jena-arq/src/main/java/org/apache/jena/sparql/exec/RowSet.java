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
     * This consumes this RowSet - the iterator will have ended after a call to this method.
     */
    public default RowSet materialize() {
//        Iterator<Binding> bindings = Iter.materialize(this);
//        return new RowSetStream(bindings, getResultVars());
        return rewindable();
    }

    /** Return the row number. The first row is row 1. */
    public long getRowNumber();

    public static RowSet adapt(ResultSet resultSet) {
        if ( resultSet instanceof ResultSetAdapter ) {
            return ((ResultSetAdapter)resultSet).get();
        }
        return new RowSetAdapter(resultSet);
    }

    // [QExec] Migrate to ResultSet
    public static ResultSet adapt(RowSet rowSet) {
        if ( rowSet instanceof RowSetAdapter ) {
            return ((RowSetAdapter)rowSet).get();
        }
        return new ResultSetAdapter(rowSet);
    }
}
