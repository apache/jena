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

package com.hp.hpl.jena.sparql.util;

import java.util.HashMap ;
import java.util.Map ;

import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.function.library.leviathan.LeviathanConstants;


public class MappedLoader
{
    // Map string => string of prefixes 
    //   e.g. http://jena.hpl.hp.com/ARQ/property# => java:com.hp.hpl.jena.sparql.pfunction.
    
    static Map<String, String> uriMap = new HashMap<>() ;
    
    static {
        // ARQ library
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
        
        // Leviathan library
        uriMap.put(LeviathanConstants.LeviathanFunctionLibraryURI, 
                   LeviathanConstants.LeviathanFunctionLibrary) ;
    }
    
    public static boolean isPossibleDynamicURI(String uri, Class<?> expectedClass)
    {
        uri = mapDynamicURI(uri) ;
        if ( uri == null )
            return false ;
        // Need to force the load to check everything.
        // Callers (who are expectedClass sensitive) should have
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
        for ( Map.Entry<String, String> e : uriMap.entrySet() )
        {
            String k = e.getKey();
            if ( uri.startsWith( k ) )
            {
                return e;
            }
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
