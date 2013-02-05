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

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.shared.Lock ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.util.Context ;

/** Query is over a Dataset, a collection of named graphs
 *  and a background graph (also called the default
 *  graph or unnamed graph). */

public interface Dataset // extends Transactional
{
    // Rather than pull in the internal "Tranactional" interface, we duplicate it here. 
    /** Get the default graph as a Jena Model */
    public Model getDefaultModel() ;
    
    /** Set the background graph.  Can be set to null for none.  */
    public void  setDefaultModel(Model model) ;

    /** Get a graph by name as a Jena Model */
    public Model getNamedModel(String uri) ;

    /** Does the dataset contain a model with the name supplied? */ 
    public boolean containsNamedModel(String uri) ;

    /** Set a named graph. */
    public void  addNamedModel(String uri, Model model) throws LabelExistsException ;

    /** Remove a named graph. */
    public void  removeNamedModel(String uri) ;

    /** Change a named graph for another using the same name */
    public void  replaceNamedModel(String uri, Model model) ;
    
    /** List the names */
    public Iterator<String> listNames() ;
    
    /** Get the lock for this dataset */
    public Lock getLock() ;
    
    /** Get the context associated with this dataset */
    public Context getContext() ;

    /** Does this dataset support transactions?
     *  Supporting transactions mean that the dataset implementation
     *  provides {@link #begin}, {@link #commit}, {@link #abort}, {@link #end}
     *  which otherwise may throw {@link UnsupportedOperationException}
     */
    public boolean supportsTransactions() ;
    
    /** Start either a READ or WRITE transaction */ 
    public void begin(ReadWrite readWrite) ;
    
    /** Commit a transaction - finish the transaction and make any changes permanent (if a "write" transaction) */  
    public void commit() ;
    
    /** Abort a transaction - finish the transaction and undo any changes (if a "write" transaction) */  
    public void abort() ;

    /** Say whether a transaction is active */ 
    public boolean isInTransaction() ;
    
    /** Finish the transaction - if a write transaction and commit() has not been called, then abort */  
    public void end() ;
    
    /** Get the dataset in graph form */
    public DatasetGraph asDatasetGraph() ; 
    
    /** Close the dataset, potentially releasing any associated resources.
     *  The dataset can not be used for query after this call.
     */
    public void close() ;
}
