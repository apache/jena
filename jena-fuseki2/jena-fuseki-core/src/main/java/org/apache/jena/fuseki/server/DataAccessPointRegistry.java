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

import javax.servlet.ServletContext ;

import org.apache.jena.atlas.lib.Registry ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException ;

public class DataAccessPointRegistry extends Registry<String, DataAccessPoint>
{
    public DataAccessPointRegistry() {}
    
    public DataAccessPointRegistry(DataAccessPointRegistry other) {
        other.forEach((name, accessPoint)->register(name, accessPoint));
    }
    
    // Add error checking.
    public void register(String name, DataAccessPoint accessPt) {
        if ( isRegistered(name) )
            throw new FusekiException("Already registered: "+name) ;
        super.put(name, accessPt);
    }
    
    // Debugging
    public void print(String string) {
        System.out.flush() ;
        if ( string == null )
            string = "DataAccessPointRegistry" ;
        System.out.println("== "+string) ;
        this.forEach((k,ref)->{
            System.out.printf("  (key=%s, ref=%s)\n", k, ref.getName()) ;
            ref.getDataService().getOperations().forEach((op)->{
                ref.getDataService().getEndpoints(op).forEach(ep->{
                    System.out.printf("     %s : %s\n", op, ep.getEndpoint()) ;
                });
            });
        }) ;
    }

    // The server DataAccessPointRegistry is held in the ServletContext for the server.
    
    public static DataAccessPointRegistry get(ServletContext cxt) {
        DataAccessPointRegistry registry = (DataAccessPointRegistry)cxt.getAttribute(Fuseki.attrNameRegistry) ;
        if ( registry == null )
            Log.warn(DataAccessPointRegistry.class, "No data access point registry for ServletContext") ;
        return registry ;
    }
    
    public static void set(ServletContext cxt, DataAccessPointRegistry registry) {
        cxt.setAttribute(Fuseki.attrNameRegistry, registry) ;
    }
}
