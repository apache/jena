/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.function;
import java.util.*;

import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.MappedLoader;

import com.hp.hpl.jena.query.ARQ;

/** 
 * 
 * @author Andy Seaborne
 */

public class FunctionRegistry //extends HashMap<String, Function>
{
    // Extract a Registry class and do casting and initialization here.
    Map<String, FunctionFactory> registry = new HashMap<String, FunctionFactory>() ;
    Set<String> attemptedLoads = new HashSet<String>() ;
    
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
            ALog.warn(this, "Class "+funcClass.getName()+" is not a Function" );
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


/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
