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

import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.RDFNode ;


public interface ResultSetProcessor
{
    /** Start result set */
    public void start(ResultSet rs);
    /** Finish result set */
    public void finish(ResultSet rs) ;
    
    /**  Start query solution (row in result set) */
    public void start(QuerySolution qs);

    /**  Finish query solution (row in result set) */
    public void finish(QuerySolution qs);
    
    
    /** A single (variable, value) pair in a query solution
     *  - the value may be null indicating that the variable
     *  was not present in this solution. 
     *   
     * @param varName
     * @param value
     */
    public void binding(String varName, RDFNode value) ;
}
