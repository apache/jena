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

package org.apache.jena.atlas.lib;

import org.apache.jena.atlas.AtlasException ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class SystemUtils
{
    private static Logger log = LoggerFactory.getLogger(SystemUtils.class.getName());
    // Unfortunately, this tends to cause confusing logging.
    private static boolean logging = false ;
    
    static public ClassLoader chooseClassLoader()
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    
        if ( logging && classLoader != null )
            log.trace("Using thread classloader") ;
        
//        if (classLoader == null)
//        {
//            classLoader = this.getClass().getClassLoader();
//            if ( classLoader != null )
//                logger.trace("Using 'this' classloader") ;
//        }
        
        if ( classLoader == null )
        {
            classLoader = ClassLoader.getSystemClassLoader() ;
            if ( logging && classLoader != null )
                log.trace("Using system classloader") ;
        }
        
        if ( classLoader == null )
            throw new AtlasException("Failed to find a classloader") ;
        return classLoader ;
    }
    
}
