/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.pfunction;
import java.util.*;

import com.hp.hpl.jena.vocabulary.RDFS;

import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.MappedLoader;
import com.hp.hpl.jena.sparql.vocabulary.ListPFunction;

import com.hp.hpl.jena.query.ARQ;


/** 
 * @author Andy Seaborne
 */

public class PropertyFunctionRegistry
{
    static PropertyFunctionRegistry globalRegistry = null ;
    
    Map<String, PropertyFunctionFactory> registry = new HashMap<String, PropertyFunctionFactory>() ;
    Set<String> attemptedLoads = new HashSet<String>() ;
    
    public synchronized static PropertyFunctionRegistry standardRegistry()
    {
        PropertyFunctionRegistry reg = new PropertyFunctionRegistry() ;
        reg.loadStdDefs() ;
        return reg ;
    }

    public static PropertyFunctionRegistry get(Context context)
    { 
        if ( context == null )
            return null ;
        return (PropertyFunctionRegistry)context.get(ARQConstants.registryPropertyFunctions) ;
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
    
    
    /** Insert an PropertyFunction factory. Re-inserting with the same URI 
     * overwrites the old entry. 
     * 
     * @param uri        String URI for the PropertyFunction
     * @param factory    Factory to make PropertyFunction instances
     */
    public void put(String uri, PropertyFunctionFactory factory) { registry.put(uri,factory) ; }

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
            ALog.warn(this, "Class "+extClass.getName()+" is not an PropertyFunction" );
            return ; 
        }
        
        registry.put(uri,new PropertyFunctionFactoryAuto(extClass)) ;
    }

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
        PropertyFunctionFactory ext = registry.get(uri) ;
        if ( ext != null )
            return ext ;
        
        if ( attemptedLoads.contains(uri) )
            return null ;

        Class<?> extClass = MappedLoader.loadClass(uri, PropertyFunction.class) ;
        if ( extClass == null )
            return null ;
        // Register it
        put(uri, extClass) ;
        attemptedLoads.add(uri) ;
        // Call again to get it.
        return registry.get(uri) ;
    }
    
    public boolean isRegistered(String uri) { return registry.containsKey(uri) ; }
    
    /** Remove by URI */
    public PropertyFunctionFactory remove(String uri) { return registry.remove(uri) ; } 
    
    /** Iterate over URIs */
    public Iterator<String> keys() { return registry.keySet().iterator() ; }
    
    private void loadStdDefs()
    {
        put(ListPFunction.member.getURI() , com.hp.hpl.jena.sparql.pfunction.library.listMember.class) ;
        put(ListPFunction.index.getURI() , com.hp.hpl.jena.sparql.pfunction.library.listIndex.class) ;
        put(ListPFunction.length.getURI() , com.hp.hpl.jena.sparql.pfunction.library.listLength.class) ;

        put(ListPFunction.listMember.getURI() , com.hp.hpl.jena.sparql.pfunction.library.listMember.class) ;
        put(ListPFunction.listIndex.getURI() , com.hp.hpl.jena.sparql.pfunction.library.listIndex.class) ;
        put(ListPFunction.listLength.getURI() , com.hp.hpl.jena.sparql.pfunction.library.listLength.class) ;
        
        put(RDFS.member.getURI(), com.hp.hpl.jena.sparql.pfunction.library.container.class) ;
    }
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
