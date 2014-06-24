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

package com.hp.hpl.jena.sparql.engine;

import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain ;
import com.hp.hpl.jena.sparql.util.Context ;


public class QueryEngineRegistry
{
    List<QueryEngineFactory> factories = new ArrayList<>() ;
    static { init() ; }
    
    // Singleton
    static QueryEngineRegistry registry = null ;
    static public QueryEngineRegistry get()
    { 
        if ( registry == null )
            init() ;
        return registry;
    }
    
    private QueryEngineRegistry() { }
    
    private static synchronized void init()
    {
        registry = new QueryEngineRegistry() ;
        registry.add(QueryEngineMain.getFactory()) ;
        registry.add(QueryEngineFactoryWrapper.get()) ;
    }
    
    /** Locate a suitable factory for this query and dataset from the default registry
     * 
     * @param query   Query 
     * @param dataset Dataset
     * @return A QueryExecutionFactory or null if none accept the request
     */
    
    public static QueryEngineFactory findFactory(Query query, DatasetGraph dataset, Context context)
    { return get().find(query, dataset, context) ; }
    
    /** Locate a suitable factory for this algebra expression
     *  and dataset from the default registry
     * 
     * @param op   Algebra expression 
     * @param dataset DatasetGraph
     * @param context
     * @return A QueryExecutionFactory or null if none accept the request
     */
    
    public static QueryEngineFactory findFactory(Op op, DatasetGraph dataset, Context context)
    { return get().find(op, dataset, context) ; }

    /** Locate a suitable factory for this query and dataset
     * 
     * @param query   Query 
     * @param dataset Dataset
     * @return A QueryExecutionFactory or null if none accept the request
     */
    
    public QueryEngineFactory find(Query query, DatasetGraph dataset)
    { return find(query, dataset, null) ; }

    /** Locate a suitable factory for this query and dataset
     * 
     * @param query   Query 
     * @param dataset Dataset
     * @return A QueryExecutionFactory or null if none accept the request
     */
    
    public QueryEngineFactory find(Query query, DatasetGraph dataset, Context context)
    {
        for ( QueryEngineFactory f : factories )
        {
            if ( f.accept( query, dataset, context ) )
            {
                return f;
            }
        }
        return null ;
    }
    
    /** Locate a suitable factory for this algebra expression and dataset
     * 
     * @param op   Algebra expression 
     * @param dataset DatasetGraph
     * @param context
     * 
     * @return A QueryExecutionFactory or null if none accept the request
     */
    
    public QueryEngineFactory find(Op op, DatasetGraph dataset, Context context)
    {
        for ( QueryEngineFactory f : factories )
        {
            if ( f.accept( op, dataset, context ) )
            {
                return f;
            }
        }
        return null ;
    }
    
    /** Add a QueryExecutionFactory to the default registry */
    public static void addFactory(QueryEngineFactory f) { get().add(f) ; }
    
    /** Add a QueryExecutionFactory */
    public void add(QueryEngineFactory f)
    {
        // Add to low end so that newer factories are tried first
        factories.add(0, f) ; 
    }
    
    /** Remove a QueryExecutionFactory */
    public static void removeFactory(QueryEngineFactory f)  { get().remove(f) ; }
    
    /** Remove a QueryExecutionFactory */
    public void remove(QueryEngineFactory f)  { factories.remove(f) ; }
    
    /** Allow <b>careful</b> manipulation of the factories list */
    public List<QueryEngineFactory> factories() { return factories ; }

    /** Check whether a query engine factory is already registered in the default registry*/
    public static boolean containsFactory(QueryEngineFactory f) { return get().contains(f) ; }

    /** Check whether a query engine factory is already registered */
    public boolean contains(QueryEngineFactory f) { return factories.contains(f) ; }

}
