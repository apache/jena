/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.pfunction;
import java.util.* ;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.core.ARQConstants;
import com.hp.hpl.jena.query.engine.engine1.EngineConfig;
import com.hp.hpl.jena.query.util.Loader;
import org.apache.commons.logging.*;
import com.hp.hpl.jena.query.vocabulary.ListPFunction;
import com.hp.hpl.jena.vocabulary.RDFS;


/** 
 * @author Andy Seaborne
 * @version $Id: PropertyFunctionRegistry.java,v 1.7 2007/01/15 14:23:34 andy_seaborne Exp $
 */

public class PropertyFunctionRegistry
{
    static Log log = LogFactory.getLog(PropertyFunctionRegistry.class) ;
    static PropertyFunctionRegistry globalRegistry = null ;
    
    Map registry = new HashMap() ;
    Set attemptedLoads = new HashSet() ;
    
    public synchronized static PropertyFunctionRegistry standardRegistry()
    {
        PropertyFunctionRegistry reg = new PropertyFunctionRegistry() ;
        reg.loadStdDefs() ;
        return reg ;
    }

    public synchronized static PropertyFunctionRegistry get()
    {
        // Intialize if there is no registry already set 
        PropertyFunctionRegistry reg = 
            (PropertyFunctionRegistry)EngineConfig.getContext().get(EngineConfig.registryPropertyFunctions) ;
        if ( reg == null )
        {
            reg = standardRegistry() ;
            EngineConfig.getContext().set(EngineConfig.registryPropertyFunctions, reg) ;
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
    public void put(String uri, Class extClass)
    { 
        if ( ! PropertyFunction.class.isAssignableFrom(extClass) )
        {
            log.warn("Class "+extClass.getName()+" is not an PropertyFunction" );
            return ; 
        }
        
        registry.put(uri,new PropertyFunctionFactoryAuto(extClass)) ;
    }

    public boolean manages(String uri)
    {
        if ( registry.containsKey(uri) )
            return true ;
        if ( uri.startsWith(ARQConstants.javaClassURIScheme) )
            return true ;
        return false ;
    }
    
    /** Lookup by URI */
    public PropertyFunctionFactory get(String uri)
    {
        PropertyFunctionFactory ext = (PropertyFunctionFactory)registry.get(uri) ;
        if ( ext != null )
            return ext ;
        
        if ( ! uri.startsWith(ARQConstants.javaClassURIScheme) )
            return null ;
        
        if ( attemptedLoads.contains(uri) )
            return null ;

        Class extClass = Loader.loadClass(uri, PropertyFunction.class) ;
        if ( extClass == null )
            return null ;
        // Registry it (does the checking)
        put(uri, extClass) ;
        attemptedLoads.add(uri) ;
        // Call again to get it.
        return (PropertyFunctionFactory)registry.get(uri) ;
    }
    
    public boolean isRegistered(String uri) { return registry.containsKey(uri) ; }
    
    /** Remove by URI */
    public PropertyFunctionFactory remove(String uri) { return (PropertyFunctionFactory)registry.remove(uri) ; } 
    
    /** Iterate over URIs */
    public Iterator keys() { return registry.keySet().iterator() ; }
    
    private void loadStdDefs()
    {
        put(ListPFunction.member.getURI() , com.hp.hpl.jena.query.pfunction.library.listMember.class) ;
        put(ListPFunction.index.getURI() , com.hp.hpl.jena.query.pfunction.library.listIndex.class) ;
        put(ListPFunction.length.getURI() , com.hp.hpl.jena.query.pfunction.library.listLength.class) ;

        put(ListPFunction.listMember.getURI() , com.hp.hpl.jena.query.pfunction.library.listMember.class) ;
        put(ListPFunction.listIndex.getURI() , com.hp.hpl.jena.query.pfunction.library.listIndex.class) ;
        put(ListPFunction.listLength.getURI() , com.hp.hpl.jena.query.pfunction.library.listLength.class) ;

        
        put(RDFS.member.getURI(), com.hp.hpl.jena.query.pfunction.library.container.class) ;
        put(ARQ.arqNS+"splitIRI", com.hp.hpl.jena.query.pfunction.library.splitIRI.class) ;
    }
}


/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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
