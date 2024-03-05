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

package org.apache.jena.riot.rowset;

import java.util.List;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.RowSet;

public class RowSetWrapper implements RowSet {

    private final RowSet other;
    public RowSet get() {
        return other;
    }

    public RowSetWrapper(RowSet other) {
        this.other = other;
    }

    @Override
    public boolean hasNext() {
        return other.hasNext();
    }

    @Override
    public Binding next() {
        return other.next();
    }

    @Override
    public List<Var> getResultVars() {
        return other.getResultVars();
    }

    @Override
    public long getRowNumber() {
        return other.getRowNumber();
    }

    @Override
    public void close() {
        other.close();
    }
}
