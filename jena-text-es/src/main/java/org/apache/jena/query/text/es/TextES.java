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

package org.apache.jena.query.text.es;

import org.apache.jena.query.text.es.assembler.TextAssemblerES;
import org.apache.jena.sys.JenaSystem;

public class TextES {
    private static volatile boolean initialized = false ;
    private static Object lock = new Object() ;
    
    public static void init() 
    {
        if ( initialized ) 
            return ;
        synchronized(lock) {
            if ( initialized ) {
                JenaSystem.logLifecycle("TextES.init - skip") ;
                return ; 
            }
            initialized = true ;
            JenaSystem.logLifecycle("TextES.init - start") ;
            TextAssemblerES.init() ;
            
//            SystemInfo sysInfo = new SystemInfo(IRI, PATH, VERSION, BUILD_DATE) ;
//            SystemARQ.registerSubSystem(sysInfo) ;
            
            JenaSystem.logLifecycle("TextES.init - finish") ;
        }
    }
}
