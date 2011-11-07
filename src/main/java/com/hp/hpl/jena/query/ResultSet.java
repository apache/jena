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

package com.hp.hpl.jena.query;

import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;

/** Results from a query in a table-like manner for SELECT queries.
 *  Each row corresponds to a set of bindings which fulfil the conditions
 *  of the query.  Access to the results is by variable name.
 *
 * @see Query
 * @see QueryExecution
 * @see QuerySolution
 */

public interface ResultSet extends Iterator<QuerySolution>
{
    // Could have a ResultSetBase that does all the Node=>Resource (= ResultBinding)  
    /**
     * Is there another result?
     */
    @Override
    public boolean hasNext() ;

    /** Moves onto the next result. */
    
    @Override
    public QuerySolution next() ;

    /** Moves onto the next result (legacy - use .next()). */
    public QuerySolution nextSolution() ;

    /** Move to the next binding (low level) */
    public Binding nextBinding() ;
    
    /** Return the "row" number for the current iterator item */
    public int getRowNumber() ;
    
    /** Get the variable names for the projection. Not all query
     *  solutions from a result have every variable defined. 
     */
    public List<String> getResultVars() ;

    /** Get the model that resources are created against - may be null */
    public Model getResourceModel() ;
}
