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

package com.hp.hpl.jena.sparql.core.describe;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.util.Context ;

/** A DescribeHandler provides the description of a resource.
 *  DESCRIBE queries return RDF that describes the resource found, either
 *  from the query pattern or explicitly named in the DESCRIBE clause.
 *  For each resource, any handlers are called to builds the RDF model
 *  that is to be the result of the query. */

public interface DescribeHandler
{
    /**
     * Start the describe process, passing in the result model.
     * @param accumulateResultModel
     * @param qContext Query execution context
     */
    public void start(Model accumulateResultModel, Context qContext) ;
    
    /** Called on everything resource found by a query.
     *  Can add more RDF to the model provided.  May choose to add nothing.
     * 
     * @param resource               resource to describe
     */
    
    public void describe(Resource resource) ;

    /** Finish the description process for thsis query execution
     */
    public void finish() ;
}
