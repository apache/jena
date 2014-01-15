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

package com.hp.hpl.jena.util;

import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class LocatorClassLoader  implements Locator
{
    static Logger log = LoggerFactory.getLogger(LocatorClassLoader.class) ;

    private final ClassLoader classLoader ;
    public LocatorClassLoader(ClassLoader _classLoader)
    {
        classLoader =_classLoader ;
    }
    
    @Override
    public boolean equals( Object other )
    {
        return 
            other instanceof LocatorClassLoader 
            && classLoader == ((LocatorClassLoader) other).classLoader;
    }
    
    @Override
    public int hashCode()
        { return classLoader.hashCode(); }
    
    @Override
    public TypedStream open(String filenameOrURI)
    {
        if ( classLoader == null )
            return null ;
            
        String fn = filenameOrURI ;
//        String fn = FileUtils.toFilename(filenameOrURI) ;
//        if ( fn == null )
//        {
//            if ( FileManager.logAllLookups && log.isTraceEnabled() )
//                log.trace("Not found: "+filenameOrURI) ; 
//            return null ;
//        }
        InputStream in = classLoader.getResourceAsStream(fn) ;
        if ( in == null )
        {
            if ( FileManager.logAllLookups && log.isTraceEnabled() )
                log.trace("Failed to open: "+filenameOrURI) ;
            return null ;
        }
        
        if ( FileManager.logAllLookups  && log.isTraceEnabled() )
            log.trace("Found: "+filenameOrURI) ;
        
        // base = classLoader.getResource(fn).toExternalForm ;       
        return new TypedStream(in) ;
    }

    public ClassLoader getClassLoader()
    {
        return classLoader ;
    }

    @Override
    public String getName() { return "ClassLoaderLocator" ; }
}
