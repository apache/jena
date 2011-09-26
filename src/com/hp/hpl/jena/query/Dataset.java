/**
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

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;

/** Query is over a Dataset, a collection of named graphs
 *  and a background graph (also called the default
 *  graph or unnamed graph). */

public interface Dataset
{
    /** Get the default graph as a Jena Model */
    public Model getDefaultModel() ;

    /** Get a graph by name as a Jena Model */
    public Model getNamedModel(String uri) ;

    /** Does the dataset contain a model with the name supplied? */ 
    public boolean containsNamedModel(String uri) ;

    /** List the names */
    public Iterator<String> listNames() ;
    
    /** Get the lock for this dataset */
    public Lock getLock() ;
    
    /** Get the dataset in graph form */
    public DatasetGraph asDatasetGraph() ; 
    
    /** Close the dataset, potentially releasing any associated resources.
     *  The dataset can not be used for query after this call.
     */
    public void close() ;
}
