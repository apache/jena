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

package com.hp.hpl.jena.rdf.model;

import java.rmi.server.UID;

import com.hp.hpl.jena.shared.impl.JenaParameters;

/** Create a new id for an anonymous node.
 *
 * <p>This id is guaranteed to be unique on this machine.</p>
 */

// This version contains experimental modifications by der to 
// switch off normal UID allocation for bNodes to assist tracking
// down apparent non-deterministic behaviour.

public class AnonId extends java.lang.Object {
    
    // Support for running in environments, like Google App Engine, where
    // java.rmi.server.UID is not available
    // Will be obsoleted by improved AnonId handling
    static boolean UIDok = true;
    static {
        try { new UID() ; }
        catch (Throwable ex) { UIDok = false ; }
    }
    
    protected String id = null;

    /** 
        Support for debugging: global anonID counter. The intial value is just to
        make the output look prettier if it has lots (but not lots and lots) of bnodes
        in it.
    */
    private static int idCount = 100000;
    
    public static AnonId create()
        { return new AnonId(); }
    
    public static AnonId create( String id )
        { return new AnonId( id ); }
    
    /** 
        Creates new AnonId. Normally this id is guaranteed to be unique on this 
        machine: it is time-dependant. However, sometimes [incorrect] code is
        sensitive to bnode ordering and produces bizarre bugs. Hence the
        disableBNodeUIDGeneration flag, which allows bnode IDs to be predictable.
    */
    public AnonId() {
        if (JenaParameters.disableBNodeUIDGeneration) {
            synchronized (AnonId.class) {
                id = "A" + idCount++; // + rand.nextLong();
            }
        } else if (!UIDok) {
            id = java.util.UUID.randomUUID().toString(); 
        } else {
            id = (new UID()).toString();
        }
    }
    
    /** Create a new AnonId from the string argument supplied
     * @param id A string representation of the id to be created.
     */    
    public AnonId( String id ) {
        this.id = id;
    }
    
    /** Test whether two id's are the same
        @param o the object to be compared
        @return true if and only if the two id's are the same
    */    
    @Override
    public boolean equals( Object o ) {
        return o instanceof AnonId && id.equals( ((AnonId) o).id );
    }
    
    /** return a string representation of the id
     * @return a string representation of the id
     */    
    @Override
    public String toString() {
        return id;
    }
    
    /**
        Answer the label string of this AnonId. To be used in preference to
        toString().
    */
    public String getLabelString() {
        return id;
    }
    
    /** return a hashcode for this id
     * @return the hash code
     */    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
