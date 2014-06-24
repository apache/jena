/**
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

package com.hp.hpl.jena.rdf.model.impl;

import java.lang.reflect.Method ;


public class IO_Ctl
{
    private static volatile boolean initialized = false ;
    private static final Object initLock = new Object() ;
    public static void init()
    {
        if ( initialized )
            return ;
        synchronized(initLock)
        {
            try {
                initialized = true ;
                callByRefection("org.apache.jena.riot.RIOT", "init") ;
            } catch (ExceptionInInitializerError e)
            {
                e.printStackTrace(System.err) ;
                if ( e.getCause() != null )
                {
                    System.err.println("****") ;
                    e.getCause().printStackTrace(System.err) ;
                }
                System.exit(99) ;
            }
        }
    }

    // Call a static of no arguments by reflection.
    private static void callByRefection(String className, String staticMethod)
    {
        Class<? > cls = null ;
        try { cls = Class.forName(className) ; }
        catch (ClassNotFoundException ex) {
            // System.err.println(className+" not on the classpath") ;
            return ;
        }
        
        Method m = null ;
        try
        {
            m = cls.getMethod(staticMethod ) ;
            m.invoke(null, (Object[])null) ;
        } catch (Exception e) 
        {
            e.printStackTrace();
        }        //System.err.println("INIT 2") ;
    }
}

