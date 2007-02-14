/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.extension;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.core.ARQConstants;
import com.hp.hpl.jena.query.util.Loader;

/** 
 * 
 * @author Andy Seaborne
 * @version $Id: ExtensionRegistry.java,v 1.14 2007/01/02 11:20:41 andy_seaborne Exp $
 */

public class ExtensionRegistry  // extends Map<String, ExtensionPoint>
{
    static Log log = LogFactory.getLog(ExtensionRegistry.class) ;
    static ExtensionRegistry globalRegistry = null ;
    Map registry = new HashMap() ;
    Set attemptedLoads = new HashSet() ;
    
    public synchronized static ExtensionRegistry standardRegistry()
    {
        ExtensionRegistry reg = new ExtensionRegistry() ;
        reg.loadStdDefs() ;
        return reg ;
    }

    public synchronized static ExtensionRegistry get()
    {
        // Intialize if there is no registry already set 
        ExtensionRegistry reg = 
            (ExtensionRegistry)ARQ.getContext().get(ARQConstants.registryExtensions) ;
        if ( reg == null )
        {
            reg = standardRegistry() ;
            ARQ.getContext().set(ARQConstants.registryExtensions, reg) ;
        }

        return reg ;
    }
    
    
    /** Insert an extension factory. Re-inserting with the same URI 
     * overwrites the old entry. 
     * 
     * @param uri        String URI for the extension
     * @param factory    Factory to make extension instances
     */
    public void put(String uri, ExtensionFactory factory) { registry.put(uri,factory) ; }

    /** Insert an extension class.
     *  Re-inserting with the same URI overwrites the old entry.
     *  New instance created on retrieval (auto-factory)  
     * 
     * @param uri        String URI for the extension
     * @param extClass   The Java class
     */
    public void put(String uri, Class extClass)
    { 
        if ( ! Extension.class.isAssignableFrom(extClass) )
        {
            log.warn("Class "+extClass.getName()+" is not an Extension" );
            return ; 
        }
        
        registry.put(uri,new ExtensionFactoryAuto(extClass)) ;
    }

    
    /** Lookup by URI */
    public ExtensionFactory get(String uri)
    {
        ExtensionFactory ext = (ExtensionFactory)registry.get(uri) ;
        if ( ext != null )
            return ext ;
        
        if ( ! uri.startsWith(ARQConstants.javaClassURIScheme) )
            return null ;
        
        if ( attemptedLoads.contains(uri) )
            return null ;

        Class extClass = Loader.loadClass(uri, Extension.class) ;
        if ( extClass == null )
            return null ;
        // Registry it (does the checking)
        put(uri, extClass) ;
        attemptedLoads.add(uri) ;
        // Call again to get it.
        return (ExtensionFactory)registry.get(uri) ;
    }
    
    public boolean isRegistered(String uri) { return registry.containsKey(uri) ; }
    
    /** Remove by URI */
    public ExtensionFactory remove(String uri) { return (ExtensionFactory)registry.remove(uri) ; } 
    
    /** Iterate over URIs */
    public Iterator keys() { return registry.keySet().iterator() ; }
    
    private void loadStdDefs()
    {
        //put(ExtensionDebug.getURI(), new ExtensionDebug()) ;
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
