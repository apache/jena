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

package com.hp.hpl.jena.sparql.modify;

import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.update.GraphStore ;

public class UpdateEngineRegistry
{
    List<UpdateEngineFactory> factories = new ArrayList<>() ;
    static { init() ; }
    
    // Singleton
    static UpdateEngineRegistry registry = null ;
    static public UpdateEngineRegistry get()
    { 
        if ( registry == null )
            init() ;
        return registry;
    }
    
    private UpdateEngineRegistry() { }
    
    private static synchronized void init()
    {
        registry = new UpdateEngineRegistry() ;
        registry.add(UpdateEngineMain.getFactory()) ;
    }
    
    /** Locate a suitable factory for this dataset from the default registry
     * 
     * @param graphStore The graph store
     * @return A QueryExecutionFactory or null if none accept the request
     */
    public static UpdateEngineFactory findFactory(GraphStore graphStore, Context context)
    { return get().find(graphStore, context) ; }
    
    /** Locate a suitable factory for this dataset
     * 
     * @param graphStore    A GraphStore
     * @return A UpdateProcessorFactroy or null if none accept the request
     */
    public UpdateEngineFactory find(GraphStore graphStore, Context context)
    {
        for ( UpdateEngineFactory f : factories )
        {
            if ( f.accept(graphStore, context) )
                return f ;
        }
        return null ;
    }
    
    /** Add a QueryExecutionFactory to the default registry */
    public static void addFactory(UpdateEngineFactory f) { get().add(f) ; }
    
    /** Add a QueryExecutionFactory */
    public void add(UpdateEngineFactory f)
    {
        // Add to low end so that newer factories are tried first
        factories.add(0, f) ; 
    }
    
    /** Remove a QueryExecutionFactory */
    public static void removeFactory(UpdateEngineFactory f)  { get().remove(f) ; }
    
    /** Remove a QueryExecutionFactory */
    public void remove(UpdateEngineFactory f)  { factories.remove(f) ; }
    
    /** Allow <b>careful</b> manipulation of the factories list */
    public List<UpdateEngineFactory> factories() { return factories ; }

    /** Check whether a query engine factory is already registered in the default registry*/
    public static boolean containsFactory(UpdateEngineFactory f) { return get().contains(f) ; }

    /** Check whether a query engine factory is already registered */
    public boolean contains(UpdateEngineFactory f) { return factories.contains(f) ; }

}
