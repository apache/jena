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

import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;


public class Loader
{
    static public Class<?> loadClass(String classNameOrURI) { return loadClass(classNameOrURI, null) ; }
    
    static public Class<?> loadClass(String classNameOrURI, Class<?> requiredClass)
    {
        if ( classNameOrURI == null )
            throw new ARQInternalErrorException("Null classNameorIRI") ;
        
        if ( classNameOrURI.startsWith("http:") )
            return null ;
        if ( classNameOrURI.startsWith("urn:") )
            return null ;

        String className = classNameOrURI ;
        
        if ( classNameOrURI.startsWith(ARQConstants.javaClassURIScheme) )
            className = classNameOrURI.substring(ARQConstants.javaClassURIScheme.length()) ;
        
        Class<?> classObj = null ;
        
        try {
            classObj = Class.forName(className);
        } catch (ClassNotFoundException ex)
        {
            Log.warn(Loader.class, "Class not found: "+className);
            return null ;
        }
        
        if ( requiredClass != null && ! requiredClass.isAssignableFrom(classObj) )
        {
            Log.warn(Loader.class, "Class '"+className+"' found but not a "+Utils.classShortName(requiredClass)) ;
            return null ;
        }
        return classObj ;
    }

    static public Object loadAndInstantiate(String uri, Class<?> requiredClass)
    {
        Class<?> classObj = loadClass(uri, requiredClass) ;
        if ( classObj == null )
            return null ;
        
        Object module = null ;
        try {
            module = classObj.newInstance() ;
        } catch (Exception ex)
        {
            String className = uri.substring(ARQConstants.javaClassURIScheme.length()) ;
            Log.warn(Loader.class, "Exception during instantiation '"+className+"': "+ex.getMessage()) ;
            return null ;
        }
        return module ;
    }
}
