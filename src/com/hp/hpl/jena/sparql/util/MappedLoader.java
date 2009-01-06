/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.util.*;

import com.hp.hpl.jena.sparql.ARQConstants;


public class MappedLoader
{
    // Map string => string of prefixes 
    //   e.g. http://jena.hpl.hp.com/ARQ/property# => java:com.hp.hpl.jena.sparql.pfunction.
    
    static Map<String, String> uriMap = new HashMap<String, String>() ;
    
    static {
        uriMap.put(ARQConstants.ARQFunctionLibraryURI,
                   ARQConstants.ARQFunctionLibrary) ;
        uriMap.put(ARQConstants.ARQPropertyFunctionLibraryURI,
                   ARQConstants.ARQPropertyFunctionLibrary) ;
        uriMap.put(ARQConstants.ARQProcedureLibraryURI,
                   ARQConstants.ARQProcedureLibrary) ;
        
        // Old name, new name
        uriMap.put("java:com.hp.hpl.jena.query.function.library.",
                   "java:com.hp.hpl.jena.sparql.function.library.") ;
        
        uriMap.put("java:com.hp.hpl.jena.query.pfunction.library.",
                   "java:com.hp.hpl.jena.sparql.pfunction.library.") ;
    }
    
    public static boolean isPossibleDynamicURI(String uri, Class<?> expectedClass)
    {
        uri = mapDynamicURI(uri) ;
        if ( uri == null )
            return false ;
        // Need to force the load to check everything.
        // Callers (who are expectedClass sesnitive) should have
        // an "alreadyLoaded" cache
        return loadClass(uri, expectedClass) != null ;
    }

    public static String mapDynamicURI(String uri)
    {
        Map.Entry<String, String> e = find(uri) ;
        if ( e == null )
        {
            if ( uri.startsWith(ARQConstants.javaClassURIScheme) )
                return uri ;
            return null ;
        }
        
        String k = e.getKey() ;
        String v = e.getValue();

        uri = uri.substring(k.length()) ;
        uri = v + uri ;
        return uri ;
    }
    
    private static Map.Entry<String,String> find(String uri)
    {
        for ( Iterator<Map.Entry<String,String>> iter = uriMap.entrySet().iterator() ; iter.hasNext() ; )
        {
            Map.Entry<String, String> e = iter.next() ;
            String k = e.getKey() ;
            if ( uri.startsWith(k) )
                return e ;
        }
        return null ;
    }
    
    public static Class<?> loadClass(String uri, Class<?> expectedClass)
    {
        uri = mapDynamicURI(uri) ;
        if ( uri == null )
            return null ;
        
        return Loader.loadClass(uri, expectedClass) ;
    }
    
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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