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
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.DatasetUtils ;

// TODO Integrate this further
//   use in FROM/FROM NAMED and change Query class.
//   use in DatasetUtils
//   use in tests

public class DatasetDescription
{
    private List<String> defaultGraphURIs = new ArrayList<>() ;
    private List<String> namedGraphURIs = new ArrayList<>() ;
 
    public static DatasetDescription create(List<String> defaultGraphURIs ,  List<String> namedGraphURIs) 
    {
        return new DatasetDescription(defaultGraphURIs, namedGraphURIs) ;
    }

    /** Create a dataset description, given a query.
     * If the query does not have a dataset description, return null.
     */
    public static DatasetDescription create(Query query) { return create(query, null) ; }
    
    /** Create a dataset description, given a context.
     * If the context does not have a dataset description, return null.
     * The context uses the key {@link ARQConstants#sysDatasetDescription}.
     */
    public static DatasetDescription create(Context context) { return create(null, context) ; }

    /** Create a dataset description, given a query and context.
     * The context overrides the query FROM/FROM NAMED.
     * If neither the context nor query has a dataset description, return null.
     * The context uses the key {@link ARQConstants#sysDatasetDescription}.
     */
    public static DatasetDescription create(Query query, Context context)
    {
        if ( context != null && context.isDefined(ARQConstants.sysDatasetDescription) )
        {
            try {
                return (DatasetDescription)context.get(ARQConstants.sysDatasetDescription) ;
            } catch (ClassCastException ex)
            {
                throw new ARQException("Unexpected type (expected DatasetDescription): "+ex.getMessage()) ;
            }
        }
        
        if ( query != null && query.hasDatasetDescription() )
            return query.getDatasetDescription() ;
        
        return null ;
    }
    
    public DatasetDescription() {}
    public DatasetDescription(List<String> defaultGraphURIs ,  List<String> namedGraphURIs) 
    {
        this() ;
        addAllDefaultGraphURIs(defaultGraphURIs) ;
        addAllNamedGraphURIs(namedGraphURIs) ;
    }
    
    public boolean isEmpty()    { return defaultGraphURIs.isEmpty() && namedGraphURIs.isEmpty() ; }
    
    public void addDefaultGraphURI(String uri)                  { defaultGraphURIs.add(uri) ; }
    public void addAllDefaultGraphURIs(Collection<String> uris)  { defaultGraphURIs.addAll(uris) ; }
    
    public void addNamedGraphURI(String uri)                    { namedGraphURIs.add(uri) ; }
    public void addAllNamedGraphURIs(Collection<String> uris)    { namedGraphURIs.addAll(uris) ; }
    
    public List<String> getDefaultGraphURIs()                   { return defaultGraphURIs ; }
    public List<String> getNamedGraphURIs()                     { return namedGraphURIs ; }
    
    public Iterator<String> eachDefaultGraphURI()               { return defaultGraphURIs.iterator() ; }
    public Iterator<String> eachNamedGraphURI()                 { return namedGraphURIs.iterator() ; }
    
    /** Create a dataset from the description - reads URLs into an in-memory dataset */ 
    public Dataset createDataset() { return DatasetUtils.createDataset(this) ; }

    /** Create a DatasetGraph from the description - reads URLs into an in-memory DatasetGraph */ 
    public DatasetGraph createDatasetGraph() { return DatasetUtils.createDatasetGraph(this) ; }
}
