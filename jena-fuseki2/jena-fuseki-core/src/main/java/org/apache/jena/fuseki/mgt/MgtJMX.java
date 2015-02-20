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

package org.apache.jena.fuseki.mgt ;


public class MgtJMX
{
  public static void removeJMX() {  }
    
//    public static void addJMX() {
//        DatasetRegistry registry = DatasetRegistry.get() ;
//        for (String ds : registry.keys()) {
//            DataAccessPoint dsRef = registry.get(ds) ;
//            addJMX(dsRef) ;
//        }
//    }
//
//    private static void addJMX(DataAccessPoint dapt) {
//        String x = datasetNames ;
//        // if ( x.startsWith("/") )
//        // x = x.substring(1) ;
//        ARQMgt.register(Fuseki.PATH + ".dataset:name=" + x, dapt) ;
//        // For all endpoints
//        for (ServiceRef sRef : dapt.getServiceRefs()) {
//            ARQMgt.register(Fuseki.PATH + ".dataset:name=" + x + "/" + sRef.name, sRef) ;
//        }
//    }
//
//    public static void removeJMX() {
//        DatasetRegistry registry = DatasetRegistry.get() ;
//        for (String ds : registry.keys()) {
//            DataAccessPoint ref = registry.get(ds) ;
//            removeJMX(ref) ;
//        }
//    }
//
//    private static void removeJMX(DatasetRef dsRef) {
//        String x = dsRef.getName() ;
//        ARQMgt.unregister(Fuseki.PATH + ".dataset:name=" + x) ;
//        for (ServiceRef sRef : dsRef.getServiceRefs()) {
//            ARQMgt.unregister(Fuseki.PATH + ".dataset:name=" + x + "/" + sRef.name) ;
//        }
//    }

}
