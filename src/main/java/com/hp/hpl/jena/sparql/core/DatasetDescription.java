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

package com.hp.hpl.jena.sparql.core;

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.sparql.util.DatasetUtils ;

// TODO Integrate this
//   use in FROM/FROM NAMED and change Query class.
//   use in DatasetUtils
//   use in tests

public class DatasetDescription
{
    private List<String> defaultGraphURIs = new ArrayList<String>() ;
    private List<String> namedGraphURIs = new ArrayList<String>() ;
    
    public DatasetDescription() {}
    public boolean isEmpty()    { return defaultGraphURIs.isEmpty() && namedGraphURIs.isEmpty() ; }
    
    public void addDefaultGraphURI(String uri)                  { defaultGraphURIs.add(uri) ; }
    public void addAllDefaultGraphURI(Collection<String> uris)  { defaultGraphURIs.addAll(uris) ; }
    
    public void addNamedGraphURI(String uri)                    { namedGraphURIs.add(uri) ; }
    public void addAllNamedGraphURI(Collection<String> uris)    { namedGraphURIs.addAll(uris) ; }
    
    public List<String> getDefaultGraphURIs()                   { return defaultGraphURIs ; }
    public List<String> getNamedGraphURIs()                     { return namedGraphURIs ; }
    
    public Iterator<String> eachDefaultGraphURI()               { return defaultGraphURIs.iterator() ; }
    public Iterator<String> eachNamedGraphURI()                 { return namedGraphURIs.iterator() ; }
    
    /** Create a dataset from the description - reads URLs into an in-memory dataset */ 
    public Dataset create() { return DatasetUtils.createDataset(this) ; }

    /** Create a DatasetGraph from the description - reads URLs into an in-memory DatasetGraph */ 
    public DatasetGraph createDatasetGraph() { return DatasetUtils.createDatasetGraph(this) ; }
}
