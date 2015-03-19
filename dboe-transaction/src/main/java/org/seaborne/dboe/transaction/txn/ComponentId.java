/*
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

package org.seaborne.dboe.transaction.txn;

import java.util.Arrays ;
import java.util.UUID ;

import org.apache.jena.atlas.lib.Bytes ;
import org.seaborne.dboe.sys.SystemBase ;

/** 
 * An class to represent a component identifier.
 * Avoid bland byte[] in interfaces. 
 */
public class ComponentId {
    public static final int SIZE = 16 ;
    private final byte[] bytes ;
    private final String displayName ;
    
    /** Create a new ComponentId from the given bytes.
     * The bytes are <em>not</em> copied. 
     * The caller must not modify them after this call.
     * The static method {@link #create(String, byte[])}
     * does copy and is preferred.
     */
    public ComponentId(String label, byte[] bytes) {
        // Ultra safe - 
        //bytes = Arrays.copyOf(bytes, bytes.length) ;
        if ( bytes.length > SIZE )
            throw new IllegalArgumentException("Bytes for ComponentId too long") ;
        if ( bytes.length < SIZE )
            bytes = Arrays.copyOf(bytes, SIZE) ;
        this.bytes = bytes ;
        this.displayName = label ;
    }
    
    public byte[] bytes() { return bytes ; }
    
    public String label() { return displayName ; }
    
    @Override
    public String toString() { return displayName+"["+Bytes.asHex(bytes)+"]" ; }

    
    @Override
    public int hashCode() {
        final int prime = 31 ;
        int result = 1 ;
        result = prime * result + Arrays.hashCode(bytes) ;
        return result ;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true ;
        if ( obj == null )
            return false ;
        if ( getClass() != obj.getClass() )
            return false ;
        ComponentId other = (ComponentId)obj ;
        if ( !Arrays.equals(bytes, other.bytes) )
            return false ;
        return true ;
    }

    /** Create a ComponentId from the given bytes */
    public static ComponentId create(String label, byte[] bytes) {
        bytes = Arrays.copyOf(bytes, bytes.length) ;
        return new ComponentId(label, bytes) ;
    }
    
    /** Given a base componentId, create a derived (different) one.
     * This is deterministically done based on  baseComponentId and index.
     * The label is just for display purposes; the index is appended.
     */ 
    public static ComponentId alloc(ComponentId baseComponentId, String label, int index) {
        //private static ComponentId alloc(byte[] bytes, ComponentId baseComponentId, String label, int index) {
        if (label == null )
            label = baseComponentId.label() ;
        if (label == null )
            label = "Base" ;
        return create(baseComponentId.bytes(), label, index) ;
    }
    
    private static ComponentId create(byte[] bytes, String label, int index) {
        bytes = Arrays.copyOf(bytes, bytes.length) ;
        int x = Bytes.getInt(bytes, bytes.length-SystemBase.SizeOfInt) ;
        x = x ^ index ;
        Bytes.setInt(x, bytes, bytes.length - SystemBase.SizeOfInt) ;
        ComponentId cid = new ComponentId(label+"-"+index, bytes) ;
        return cid ;
    }
    
    static int counter = 0 ;
    /** Return a fresh ComponentId (not preserved across JVM runs) */ 
    public static ComponentId allocLocal() {
        counter++ ;
        UUID uuid = UUID.randomUUID() ;
        return create(L.uuidAsBytes(uuid), "Local", counter) ;
    }

}

