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

package org.apache.jena.sparql.pfunction;
import java.util.* ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.sparql.ARQConstants ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.MappedLoader ;


public class PropertyFunctionRegistry
{
    static PropertyFunctionRegistry globalRegistry = null ;
    
    Map<String, PropertyFunctionFactory> registry = new HashMap<>() ;
    Set<String> attemptedLoads = new HashSet<>() ;
    
    public static PropertyFunctionRegistry standardRegistry()
    {
        PropertyFunctionRegistry reg = get(ARQ.getContext()) ;
        return reg ;   
    }
    
    public static void init() {
        // Intialize if there is no registry already set 
        PropertyFunctionRegistry reg = new PropertyFunctionRegistry() ;
        StandardPropertyFunctions.loadStdDefs(reg) ;
        set(ARQ.getContext(), reg) ;
    }

    public static PropertyFunctionRegistry get(Context context)
    { 
        if ( context == null )
            return null ;
        return (PropertyFunctionRegistry)context.get(ARQConstants.registryPropertyFunctions) ;
    }
    
    /** Get the PropertyFunctionRegistry, defaulting to the global one */
    public static PropertyFunctionRegistry chooseRegistry(Context context)
    {
        PropertyFunctionRegistry registry = PropertyFunctionRegistry.get(context) ;
        // Else global
        if ( registry == null )
            registry = PropertyFunctionRegistry.get() ;
        return registry ;
    }
    
    public static void set(Context context, PropertyFunctionRegistry reg)
    { context.set(ARQConstants.registryPropertyFunctions, reg) ; }
    
    public synchronized static PropertyFunctionRegistry get()
    {
        // Intialize if there is no registry already set 
        PropertyFunctionRegistry reg = get(ARQ.getContext()) ;
        if ( reg == null )
        {
            reg = standardRegistry() ;
            set(ARQ.getContext(), reg) ; 
        }
        return reg ;
    }
    
    
    /** Insert an PropertyFunction class.
     *  Re-inserting with the same URI overwrites the old entry.
     *  New instance created on retrieval (auto-factory)  
     * 
     * @param uri        String URI for the PropertyFunction
     * @param extClass   The Java class
     */
    public void put(String uri, Class<?> extClass)
    { 
        if ( ! PropertyFunction.class.isAssignableFrom(extClass) )
        {
            Log.warn(this, "Class "+extClass.getName()+" is not an PropertyFunction" );
            return ; 
        }
        
        put(uri,new PropertyFunctionFactoryAuto(extClass)) ;
    }

    /** Insert an PropertyFunction factory. Re-inserting with the same URI 
     * overwrites the old entry. 
     * 
     * @param uri        String URI for the PropertyFunction
     * @param factory    Factory to make PropertyFunction instances
     */
    public void put(String uri, PropertyFunctionFactory factory) { registry.put(uri,factory) ; }

    public boolean manages(String uri)
    {
        if ( registry.containsKey(uri) )
            return true ;
        if ( MappedLoader.isPossibleDynamicURI(uri, PropertyFunction.class) )
            return true ;
        return false ;
    }
    
    /** Lookup by URI */
    public PropertyFunctionFactory get(String uri)
    {
        // Is it mapped?
        String mappedUri = MappedLoader.mapDynamicURI(uri) ;
        if ( mappedUri != null )
            uri = mappedUri ;

        // Plain registration, after mapping.
        PropertyFunctionFactory factory = registry.get(uri) ;
        if ( factory != null )
            return factory;

        if ( attemptedLoads.contains(uri) )
            return null ;
        
        Class<?> extClass = MappedLoader.loadClass(uri, PropertyFunction.class) ;
        if ( extClass == null )
            return null ;
        // Register it
        put(mappedUri, extClass) ;
        attemptedLoads.add(uri) ;
        // Call again to get it.
        return registry.get(uri) ;
    }
    
    public boolean isRegistered(String uri) { return registry.containsKey(uri) ; }
    
    /** Remove by URI */
    public PropertyFunctionFactory remove(String uri) { return registry.remove(uri) ; } 
    
    /** Iterate over URIs */
    public Iterator<String> keys() { return registry.keySet().iterator() ; }
}
