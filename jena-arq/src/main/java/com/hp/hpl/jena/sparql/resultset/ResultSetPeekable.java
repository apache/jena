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

package com.hp.hpl.jena.sparql.resultset;

import java.util.NoSuchElementException;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

/**
 * Interfaces for result sets that allow peeking ahead
 *
 */
public interface ResultSetPeekable extends ResultSet {

    /**
     * Peek at the next query solution
     * @return Next solution if available
     * @throws NoSuchElementException Thrown if attempting to peek when there are no further elements
     */
    public QuerySolution peek();
    
    /**
     * Peek at the next binding
     * @return Next binding if available
     * @throws NoSuchElementException Thrown if attempting to peek when there are no further elements
     */
    public Binding peekBinding();
}
