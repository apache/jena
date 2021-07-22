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

package org.apache.jena.sparql.algebra;

import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.exec.RowSet;

public interface Table
{
    public void close() ;
    public List<Var> getVars() ;
    public List<String> getVarNames() ;
    public int size() ;
    public boolean isEmpty() ;
    /** Return a QueryIterator over the whole table. */
    public QueryIterator iterator(ExecutionContext execCxt) ;
    /** Return a fresh iterator over the whole table. */
    public Iterator<Binding> rows() ;
    public void addBinding(Binding binding) ;
    public boolean contains(Binding binding) ;
    public RowSet toRowSet();
}
