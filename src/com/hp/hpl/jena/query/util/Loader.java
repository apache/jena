/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.util;

import com.hp.hpl.jena.query.core.ARQConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class Loader
{
    private static Log log = LogFactory.getLog(Loader.class) ;
    
    static public Class loadClass(String classNameOrURI) { return loadClass(classNameOrURI, null) ; }
    
    static public Class loadClass(String classNameOrURI, Class requiredClass)
    {
        if ( classNameOrURI.startsWith("http:") )
            return null ;
        if ( classNameOrURI.startsWith("urn:") )
            return null ;

        String className = classNameOrURI ;
        
        if ( classNameOrURI.startsWith(ARQConstants.javaClassURIScheme) )
            className = classNameOrURI.substring(ARQConstants.javaClassURIScheme.length()) ;
        
        Class classObj = null ;
        
        try {
            classObj = Class.forName(className);
        } catch (ClassNotFoundException ex)
        {
            log.warn("Class not found: "+className);
            return null ;
        }
        
        if ( requiredClass != null && ! requiredClass.isAssignableFrom(classObj) )
        {
            log.warn("Class '"+className+"' found but not a "+Utils.classShortName(requiredClass)) ;
            return null ;
        }
        return classObj ;
    }

    static public Object loadAndInstantiate(String uri, Class requiredClass)
    {
        Class classObj = loadClass(uri, requiredClass) ;
        if ( classObj == null )
            return null ;
        
        Object module = null ;
        try {
            module = classObj.newInstance() ;
        } catch (Exception ex)
        {
            String className = uri.substring(ARQConstants.javaClassURIScheme.length()) ;
            log.warn("Exception during instantiation '"+className+"': "+ex.getMessage()) ;
            return null ;
        }
        return module ;
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