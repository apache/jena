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

package org.apache.jena.tdb2.setup;

import java.util.HashMap ;
import java.util.Map ;
import java.util.UUID ;

import org.apache.jena.dboe.transaction.txn.ComponentId;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

// Consistent name ->  ComponentId

public class ComponentIdMgr {
    private static Logger log = LoggerFactory.getLogger(ComponentIdMgr.class) ; 
    
    private static Map<String, Integer> names = new HashMap<>() ;  
    static {
        // Well know names.
        setup(1, "SPO") ;
        setup(2, "POS") ;
        setup(3, "PSO") ;
        setup(4, "OSP") ;
        
        setup(11, "GSPO") ;
        setup(12, "GPOS") ;
        setup(13, "GPSO") ;
        setup(14, "GOSP") ;
        
        setup(21, "POSG") ;
        setup(22, "PSOG") ;
        setup(23, "OSPG") ;
        setup(24, "SPOG") ;
        
        setup(30, "GPU") ;
        
        setup(40, "prefixes") ;
        setup(41, "prefixes-data") ;
        
        setup(50, "nodes") ;
        setup(51, "nodes-data") ;
    }
    
    static void setup(int idx, String unitName) {
        if ( names.containsKey(unitName) )
            log.error("Name '"+unitName+"' is already registered") ;
        names.put(unitName, idx) ;
    }
    
    private Map<String, ComponentId> allocated = new HashMap<>() ;
    
    // Name to index mapping. 
    static Map<String, String> mapper = new HashMap<>() ;
    private final UUID base ;
    
    public ComponentIdMgr(UUID base) {
        this.base = base ;
    }
    
    public static int getIndex(String name) {
        Integer idx = names.get(name) ;
        if ( idx == null ) {
            log.error("Unregistered '"+name+"'") ;
            return -1 ;
        }
        return idx ;
    }
    
    public ComponentId getComponentId(String name) {
//        // Trace duplicates
//        final String tracename = "SPO" ;
//        if ( tracename.equals(name) )
//            log.info("Name '"+name+"'") ;
        if ( ! names.containsKey(name))
            log.error("Name '"+name+"' is not registered") ;
        if ( allocated.containsKey(name) ) {
            log.error("ComponentId for '"+name+"' has already been allocated") ;
            return allocated.get(name) ;
        }
        ComponentId cid = ComponentId.alloc(name, base, names.get(name)) ;
        allocated.put(name, cid) ;
        return cid ;
    }
}

