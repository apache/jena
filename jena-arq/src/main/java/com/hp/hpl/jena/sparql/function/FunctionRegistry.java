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

package com.hp.hpl.jena.sparql.function;
import java.util.HashMap ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.Map ;
import java.util.Set ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.ARQConstants ;

import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.MappedLoader ;

public class FunctionRegistry //extends HashMap<String, Function>
{
    // Extract a Registry class and do casting and initialization here.
    Map<String, FunctionFactory> registry = new HashMap<>() ;
    Set<String> attemptedLoads = new HashSet<>() ;
    
    public synchronized static FunctionRegistry standardRegistry()
    {
        FunctionRegistry reg = new FunctionRegistry() ;
        StandardFunctions.loadStdDefs(reg) ;
        return reg ;   
    }
    
    public synchronized static FunctionRegistry get()
    {
        // Intialize if there is no registry already set 
        FunctionRegistry reg = get(ARQ.getContext()) ;
        if ( reg == null )
        {
            reg = standardRegistry() ;
            set(ARQ.getContext(), reg) ;
        }

        return reg ;
    }

    public static FunctionRegistry get(Context context)
    {
        if ( context == null )
            return null ;
        return (FunctionRegistry)context.get(ARQConstants.registryFunctions) ;
    }
    
    public static void set(Context context, FunctionRegistry reg)
    {
        context.set(ARQConstants.registryFunctions, reg) ;
    }

    public FunctionRegistry()
    {}
    /** Insert a function. Re-inserting with the same URI overwrites the old entry. 
     * 
     * @param uri
     * @param f
     */
    public void put(String uri, FunctionFactory f) { registry.put(uri,f) ; }

    /** Insert a class that is the function implementation 
     * 
     * @param uri           String URI
     * @param funcClass     Class for the function (new instance called).
     */
    public void put(String uri, Class<?> funcClass)
    { 
        if ( ! Function.class.isAssignableFrom(funcClass) )
        {
            Log.warn(this, "Class "+funcClass.getName()+" is not a Function" );
            return ; 
        }
        
        registry.put(uri, new FunctionFactoryAuto(funcClass)) ;
    }
    
    /** Lookup by URI */
    public FunctionFactory get(String uri)
    {
        FunctionFactory function = registry.get(uri) ;
        if ( function != null )
            return function ;

        if ( attemptedLoads.contains(uri) )
            return null ;

        Class<?> functionClass = MappedLoader.loadClass(uri, Function.class) ;
        if ( functionClass == null )
            return null ;
        // Registry it
        put(uri, functionClass) ;
        attemptedLoads.add(uri) ;
        // Call again to get it.
        return registry.get(uri) ;
    }
    
    public boolean isRegistered(String uri) { return registry.containsKey(uri) ; }
    
    /** Remove by URI */
    public FunctionFactory remove(String uri) { return registry.remove(uri) ; } 
    
    /** Iterate over URIs */
    public Iterator<String> keys() { return registry.keySet().iterator() ; }

}
