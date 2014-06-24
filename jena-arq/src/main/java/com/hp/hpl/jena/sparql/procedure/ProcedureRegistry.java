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

package com.hp.hpl.jena.sparql.procedure;

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

public class ProcedureRegistry
{
    // Extract a Registry class and do casting and initialization here.
    // Identical to FunctionRegistry except the types.
    // And PropertyFunctionRegistry (which may disappear)
    Map<String, ProcedureFactory> registry = new HashMap<>() ;
    Set<String> attemptedLoads = new HashSet<>() ;
    
    public synchronized static ProcedureRegistry standardRegistry()
    {
        ProcedureRegistry reg = new ProcedureRegistry() ;
        StandardProcedures.loadStdDefs(reg) ;
        return reg ;   
    }
    
    public synchronized static ProcedureRegistry get()
    {
        // Intialize if there is no registry already set 
        ProcedureRegistry reg = get(ARQ.getContext()) ;
        if ( reg == null )
        {
            reg = standardRegistry() ;
            set(ARQ.getContext(), reg) ;
        }

        return reg ;
    }

    public static ProcedureRegistry get(Context context)
    {
        if ( context == null )
            return null ;
        return (ProcedureRegistry)context.get(ARQConstants.registryProcedures) ;
    }
    
    public static void set(Context context, ProcedureRegistry reg)
    {
        context.set(ARQConstants.registryProcedures, reg) ;
    }

    
    /** Insert a ProcedureFactory. Re-inserting with the same URI overwrites the old entry. 
     * 
     * @param uri
     * @param f
     */
    public void put(String uri, ProcedureFactory f) { registry.put(uri,f) ; }

    /** Insert a class that is the procedure implementation 
     * 
     * @param uri           String URI
     * @param procClass     Class for the procedure (new instance called).
     */
    public void put(String uri, Class< ? > procClass)
    { 
        if ( ! Procedure.class.isAssignableFrom(procClass) )
        {
            Log.warn(this, "Class "+procClass.getName()+" is not a Procedure" );
            return ; 
        }
        
        registry.put(uri, new ProcedureFactoryAuto(procClass)) ;
    }
    
    /** Lookup by URI */
    public ProcedureFactory get(String uri)
    {
        ProcedureFactory procedure = registry.get(uri) ;
        if ( procedure != null )
            return procedure ;

        if ( attemptedLoads.contains(uri) )
            return null ;

        Class<?> procedureClass = MappedLoader.loadClass(uri, Procedure.class) ;
        if ( procedureClass == null )
            return null ;
        // Registry it
        put(uri, procedureClass) ;
        attemptedLoads.add(uri) ;
        // Call again to get it.
        return registry.get(uri) ;
    }
    
    public boolean isRegistered(String uri) { return registry.containsKey(uri) ; }
    
    /** Remove by URI */
    public ProcedureFactory remove(String uri) { return registry.remove(uri) ; } 
    
    /** Iterate over URIs */
    public Iterator<String> keys() { return registry.keySet().iterator() ; }
}
