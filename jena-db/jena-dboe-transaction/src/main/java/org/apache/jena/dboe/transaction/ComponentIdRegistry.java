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

package org.apache.jena.dboe.transaction;



public class ComponentIdRegistry {
//    // Not stable across JVMs hence "local" 
//    private ComponentId localCid = ComponentId.create("Local", L.uuidAsBytes(UUID.randomUUID())) ;
//    
//    // No! byte[].equals is Object.equals
//    
//    // ComponentId equality is by bytes (not display label)
//    // so this maps bytes to a better form. 
//    private Map<Holder, ComponentId> registry = new ConcurrentHashMap<>() ;
//    
//    public ComponentIdRegistry() { }
//    
//    public ComponentId registerLocal(String label, int index) {
//        return register(localCid, label, index) ;
//    }
//    
//    public ComponentId register(ComponentId base, String label, int index) {
//        byte[] bytes = base.bytes() ;
//        bytes = Arrays.copyOf(bytes, bytes.length) ;
//        int x = Bytes.getInt(bytes, bytes.length - SystemBase.SizeOfInt) ;
//        x = x ^ index ;
//        Bytes.setInt(x, bytes, bytes.length-SystemBase.SizeOfInt) ;
//        ComponentId cid = new ComponentId(label+"-"+index, bytes) ;
//        Holder h = new Holder(bytes) ;
//        registry.put(h, cid) ;
//        return cid ;
//    }
//    
//    public ComponentId lookup(byte[] bytes) {
//        Holder h = new Holder(bytes) ;
//        return registry.get(h) ;
//    }
//    
//    public void reset() {
//        registry.clear() ;
//    }
//    
//    // Makes equality the value of the bytes. 
//    static class Holder {
//        private final byte[] bytes ;
//
//        Holder(byte[] bytes) { this.bytes = bytes ; }
//
//        @Override
//        public int hashCode() {
//            final int prime = 31 ;
//            int result = 1 ;
//            result = prime * result + Arrays.hashCode(bytes) ;
//            return result ;
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if ( this == obj )
//                return true ;
//            if ( obj == null )
//                return false ;
//            if ( getClass() != obj.getClass() )
//                return false ;
//            Holder other = (Holder)obj ;
//            if ( !Arrays.equals(bytes, other.bytes) )
//                return false ;
//            return true ;
//        }
//        
//        @Override
//        public String toString() {
//            return Bytes.asHex(bytes) ;
//        }
//    }
}
