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

package org.apache.jena.sys;

import org.apache.jena.vocabulary.OWL ;
import org.apache.jena.vocabulary.RDF ;
import org.apache.jena.vocabulary.RDFS ;

public class InitJenaCore  implements JenaSubsystemLifecycle {
    private static volatile boolean initialized = false ;
    private static Object           initLock    = new Object() ;

    @Override
    public void start() {
        init() ;
    }

    @Override
    public void stop() {}
    
    @Override
    public int level() {
        return 10 ;
    }
    
    public static void init() {
        if ( initialized )
            return ;
        synchronized (initLock) {
            if ( initialized ) {
                JenaSystem.logLifecycle("JenaCore.init - skip") ;
                return ;
            }
            initialized = true ;
            JenaSystem.logLifecycle("JenaCore.init - start") ;

            // Initialization
            // Touch classes with constants.  
            // This isn't necessary but it makes it more deterministic.
            // These constants are reused in various places.  
            RDF.getURI() ;
            RDFS.getURI() ;
            OWL.getURI() ;
            JenaSystem.logLifecycle("JenaCore.init - finish") ;
        }
    }
}
