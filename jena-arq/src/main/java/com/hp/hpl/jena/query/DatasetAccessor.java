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

import com.hp.hpl.jena.rdf.model.Model ;

/** Accessor to a dataset as a collection of models, providing 
 * whole model operations. Models can be added, remopve and replaced.   
 *  This interface provides a uniform interface to local and remote datasets and is based on 
 *  <a href="http://www.w3.org/TR/sparql11-http-rdf-update/">SPARQL 1.1 Graph Store HTTP Protocol</a>.
 *  The factory methods in {@link DatasetAccessorFactory} provides creators for 
 *  {@code DatasetAccessor}s to local and remote (over HTTP) data.
 *  local na dremote   
 *  
 *  @see DatasetAccessorFactory
 */
public interface DatasetAccessor
{
    /** Get the default model of a Dataset */
    public Model getModel() ; 
    
    /** Get a named model of a Dataset */
    public Model getModel(String graphUri) ;

//    /** Does the Dataset contain a default graph? */
//    public boolean containsDefault() ;

    /** Does the Dataset contain a named graph? */
    public boolean containsModel(String graphURI) ;
    
    /** Put (replace) the default model of a Dataset */
    public void putModel(Model data) ;
    
    /** Put (create/replace) a named model of a Dataset */
    public void putModel(String graphUri, Model data) ;

    /** Delete (which means clear) the default model of a Dataset */
    public void deleteDefault() ;
    
    /** Delete a named model of a Dataset */
    public void deleteModel(String graphUri) ;

//    /** Clear the default graph, delete all the named models */
//    public void reset() ;

    /** Add statements to the default model of a Dataset */
    public void add(Model data) ;
    
    /** Add statements to a named model of a Dataset */
    public void add(String graphUri, Model data) ;
}
