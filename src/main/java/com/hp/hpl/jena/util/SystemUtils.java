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

import com.hp.hpl.jena.shared.JenaException ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** 
 * Note: this has been copied here from ARQ.
 */
public class SystemUtils
{
    private static Logger log = LoggerFactory.getLogger ( SystemUtils.class.getName() ) ;
    
    static public ClassLoader chooseClassLoader()
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader() ;
    
        if ( classLoader != null )
            log.trace ( "Using thread classloader" ) ;
        
        if ( classLoader == null )
        {
            classLoader = ClassLoader.getSystemClassLoader() ;
            if ( classLoader != null )
                log.trace ( "Using system classloader" ) ;
        }
        
        if ( classLoader == null )
            throw new JenaException ( "Failed to find a classloader" ) ;

        return classLoader ;
    }
    
}
