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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

public class RowSetStream implements RowSet {

    private Iterator<Binding> iterator ;
    private List<Var> resultVars ;
    private int rowNumber ;

    public RowSetStream(Iterator<Binding> bindings, List<Var> resultVars) {
        this.iterator = bindings;
        this.resultVars = new ArrayList<>(resultVars);
        rowNumber = 0;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Binding next() {
        rowNumber++;
        return iterator.next();
    }

    @Override
    public List<Var> getResultVars() {
        return resultVars;
    }

    @Override
    public RowSetRewindable rewindable() {
        return RowSetMem.create(this);
    }

    @Override
    public long getRowNumber() {
        return rowNumber;
    }

    @Override
    public void close() {
        Iter.close(iterator);
    }
}
