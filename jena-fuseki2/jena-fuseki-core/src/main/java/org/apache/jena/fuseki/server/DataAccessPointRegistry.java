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

package org.apache.jena.fuseki.server;

import org.apache.jena.atlas.lib.Registry ;
import org.apache.jena.fuseki.FusekiException ;

public class DataAccessPointRegistry extends Registry<String, DataAccessPoint>
{
    public static void register(String name, DataAccessPoint accessPt) {
        if ( get().isRegistered(name) )
            throw new FusekiException("Already registered: "+name) ;
        get().put(name, accessPt);
    }
    
    // Debugging
    public static void print(String string) {
        System.out.flush() ;
        if ( string == null )
            string = "DataAccessPointRegistry" ;
        System.err.println("== "+string) ;
        DataAccessPointRegistry.get().keys().iterator().forEachRemaining((k) -> {
            System.err.print("  (key="+k) ;
            DataAccessPoint ref = DataAccessPointRegistry.get().get(k) ;
            System.err.print(", ref="+ref.getName()) ;
            System.err.println(")") ; 
        }) ;
    }
    
    private static DataAccessPointRegistry singleton = new DataAccessPointRegistry() ;

    public static DataAccessPointRegistry get() { return singleton ; }
    
    private DataAccessPointRegistry() {}
}
