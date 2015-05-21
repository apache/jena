/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package tdbdev;

import java.util.* ;

import org.seaborne.dboe.migrate.L ;
import org.seaborne.dboe.transaction.txn.ComponentId ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

// Consistent name ->  ComponentId

public class ComponentIdMgr {
    private static Logger log = LoggerFactory.getLogger(ComponentIdMgr.class) ; 
    
    /* nextComponentId unit names:
        nodes
        nodes-data
        SPO
        POS
        OSP
        GSPO
        GPOS
        GOSP
        POSG
        OSPG
        SPOG
        prefixes
        prefixes-data
        GPU
        */
    
    private static Map<String, Integer> names = new HashMap<>() ;  
    static {
        //Well know names.
        setup(1, "SPO") ;
        setup(2, "POS") ;
        setup(3, "OSP") ;
        setup(4, "GSPO") ;
        setup(5, "GPOS") ;
        setup(6, "GOSP") ;
        setup(7, "POSG") ;
        setup(8, "OSPG") ;
        setup(9, "SPOG") ;
        setup(10, "GPU") ;
        setup(11, "prefixes") ;
        setup(12, "prefixes-data") ;
        setup(13, "nodes") ;
        setup(14, "nodes-data") ;
    }
    
    static void setup(int idx, String unitName) {
        if ( names.containsKey(unitName) )
            log.error("Name '"+unitName+"' is already registered") ;
        names.put(unitName, idx) ;
    }
    
    private static Map<String, ComponentId> allocated = new HashMap<>() ;
    
    // Name to index mapping. 
    static Map<String, String> mapper = new HashMap<>() ;
    private final UUID base ;
    private final ComponentId baseComponentId ;
    
    public ComponentIdMgr(UUID base) {
        this.base = base ;
        this.baseComponentId = ComponentId.create("base", L.uuidAsBytes(base)) ;
    }
    
    public ComponentId getComponentId(String name) {
        if ( ! names.containsKey(name))
            log.error("Name '"+name+"' is not registered") ;
        if ( allocated.containsKey(name) ) {
            log.error("ComponentId for '"+name+"' has already been allocated") ;
            return allocated.get(name) ;
        }
        ComponentId cid = ComponentId.alloc(baseComponentId, name, names.get(name)) ;
        allocated.put(name, cid) ;
        return cid ;
    }
    
    
    static class ComponentId2 {
        
    }
}

