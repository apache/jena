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

package org.apache.jena.dboe.transaction.txn;


/** Some fixed, marker, {@link ComponentId}s */
public class ComponentIds {
    
    public static final ComponentId idNull          = make("Monitor",  -9999) ;
    public static final ComponentId idSystem        = make("System",   -9988) ;
    
    private static ComponentId make(String string, int v) {
        return ComponentId.alloc(string, null, v) ;
    }
    
    
//    
//    // Linux : "uuid -v 4"
//    
//    /* For single instance components and base ids for incremental registration */
//    public static final ComponentId idTxnMRSW       = make("MRSW",     "93a58341-ed53-4f0c-bac1-d9969ea38cf3") ;
//    // TODO tie to registration (this predates)
//    public static final ComponentId idTxnCounter    = make("Counter",  "6b901671-e6db-45c5-9217-7506d21a0000") ;
//    public static final ComponentId idMonitor       = make("Monitor",  "c4d8a1e6-052b-413a-8d80-c5a6b442e608") ;
//    public static final ComponentId idNull          = make("Monitor",  "e6e31271-b6dc-452c-b624-d4e099464365") ;
//    public static final ComponentId idSystem        = make("System",   "95e0f729-ad29-48b2-bd70-e3738663c578") ;
//    public static final ComponentId idBlobBase      = make("Blob",     "43436b91-87ce-4d6b-827c-c3b9ea6536ba") ;
//    public static final ComponentId idDev           = make("Blob",     "82a6833a-1475-495a-83ca-10370c7c40cd") ;
//    
//    public static ComponentId make(String label, String uuidStr) {
//        byte[] bytes = L.uuidAsBytes(uuidStr) ;
//        return ComponentId.create(label, bytes) ;
//    }
    
    /* For later
    82a6833a-1475-495a-83ca-10370c7c40cd
    1d32231b-aa11-47ed-8893-6b36673fe04c
    27c4845e-c05a-410d-8c74-278a23b03bbd
    f841ba46-a297-487d-b622-dd452c888dab
    09efba54-7428-4689-929d-b1719a56c345
    a9fbdc3c-442d-4086-8f40-b6ef773871b9
    */


}

