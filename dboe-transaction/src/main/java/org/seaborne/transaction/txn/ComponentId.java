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

package org.seaborne.transaction.txn;

import java.util.Arrays ;

import org.apache.jena.atlas.lib.Bytes ;

/** 
 * An class to represent a component identifier.
 * Avoid bland byte[] in interfaces. 
 */
public class ComponentId {
    public static final int SIZE = 16 ;
    private final byte[] bytes ;
    private final String displayName ;
    
    public ComponentId(String label, byte[] bytes) {
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
}

