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

package org.apache.jena.graph;

import java.util.concurrent.atomic.AtomicInteger ;

import org.apache.jena.rdf.model.AnonId ;
import org.apache.jena.shared.impl.JenaParameters ;

/** System identifier for a blank node.
 * 
 * Blank nodes have identity (you can .equals them apart)
 * but no external stable identifier like a URI.
 * <p>
 * Databases need a persistent identifier for blank nodes - BlankNodeId.
 * <p>
 * The equivalent concept for the API is {@link AnonId}. 
 * Historically, that has been in the org.apache.jena.rdf.model
 * package.  
 *
 * <p>This id is guaranteed to be globally unique.</p>
 * 
 * @see JenaParameters#disableBNodeUIDGeneration
 */

public class BlankNodeId extends java.lang.Object {
    // Jena RIOT parsers also generate labels : see LabelToNode. 
    // This has been in RIOT for a long time (Jena2).
    //
    // Jena used to use java.rmi.UID for API unlabeled blank nodes
    // in BlankNodeId() until v3.3.0 with "(new UID()).toString()".

    protected String id = null;

    /**
     * Support for debugging ONLY: global BlankNodeId counter. The intial value is
     * just to make the output look prettier if it has lots (but not lots and
     * lots) of bnodes in it.
     */
    private static AtomicInteger idCount = new AtomicInteger(100000) ;
    
    /** Creates new BlankNodeId with a fresh internal id */ 
    public static BlankNodeId create() {
        return new BlankNodeId();
    }

    /** Creates new BlankNodeId with the given id */
    public static BlankNodeId create(String id) {
        return new BlankNodeId(id);
    }
    
    protected BlankNodeId() {
        if (JenaParameters.disableBNodeUIDGeneration)
            id = "A" + idCount.getAndIncrement();
        else
            id = java.util.UUID.randomUUID().toString();
    }

    /** Create a new BlankNodeId from the string argument supplied.
     * @param id A string representation of the id to be created.
     */    
    public BlankNodeId( String id ) {
        this.id = id;
    }
    
    protected BlankNodeId( BlankNodeId id ) {
        this.id = id.getLabelString();
    }

    //@Override
    public int hashCode1() {
        final int prime = 31 ;
        int result = 1 ;
        result = prime * result + ((id == null) ? 0 : id.hashCode()) ;
        return result ;
    }

    /** return a hashcode for this id
     * @return the hash code
     */    
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Test whether two id's are the same
     * 
     * @param other  the object to be compared
     * @return true if and only if the two id's are the same
     */    
    @Override
    public boolean equals( Object other ) {
        return other instanceof BlankNodeId && id.equals( ((BlankNodeId) other).id );
    }

    //@Override
    public boolean equals1(Object obj) {
        if ( this == obj )
            return true ;
        if ( obj == null )
            return false ;
        if ( !(obj instanceof BlankNodeId) )
            return false ;
        BlankNodeId other = (BlankNodeId)obj ;
        if ( id == null ) {
            if ( other.id != null )
                return false ;
        } else if ( !id.equals(other.id) )
            return false ;
        return true ;
    }
    
    
    /** return a string representation of the id
     * @return a string representation of the id
     */    
    @Override
    public String toString() {
        return id;
    }
    
    /**
     * Answer the label string of this BlankNodeId. To be used in preference to
     * {@code toString}
     */
    public String getLabelString() {
        return id;
    }
}
