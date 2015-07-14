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

package org.apache.jena.rdf.model;

import org.apache.jena.graph.BlankNodeId ;

/** System id for an anonymous node.
 * <p>
 * Blank nodes have identity ({@code .equals} tells them apart)
 * but have no web-visible external stable identifier like a URI.
 * <p>
 * The Jena API has traditionally had {@code AnonId}
 * in the RDF API. {@link BlankNodeId} is the SPI equivalent and databases 
 * that need a persistent identifier across JVM instances use that for blank nodes.
 *
 * <p>This id is guaranteed to be unique on this machine.</p>
 */

public class AnonId {
    private final BlankNodeId blankNodeId ;
    
    public static AnonId create()
        { return new AnonId(); }
    
    public static AnonId create( String id )
        { return new AnonId( id ); }

    public AnonId() { 
        blankNodeId = BlankNodeId.create() ;
    } 
    
    /** Create a new AnonId from the string argument supplied
     * @param idStr A string representation of the id to be created.
     */    
    public AnonId( String idStr ) { 
        blankNodeId = BlankNodeId.create(idStr) ;
    }
    
    /** Create a new AnonId from the BlankNodeId argument supplied
     * @param id A string representation of the id to be created.
     */    
    public AnonId( BlankNodeId id ) { blankNodeId = id ; }

    public String getLabelString() { return blankNodeId.getLabelString() ; }
    
    /** Return the system BlankNodeId */
    public BlankNodeId getBlankNodeId() { return blankNodeId ; }
    
    @Override
    public String toString() { return blankNodeId.toString() ; }
    
    @Override
    public int hashCode() {
        final int prime = 31 ;
        int result = 1 ;
        result = prime * result + ((blankNodeId == null) ? 0 : blankNodeId.hashCode()) ;
        return result ;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true ;
        if ( obj == null )
            return false ;
        if ( !(obj instanceof AnonId) )
            return false ;
        AnonId other = (AnonId)obj ;
        if ( blankNodeId == null ) {
            if ( other.blankNodeId != null )
                return false ;
        } else if ( !blankNodeId.equals(other.blankNodeId) )
            return false ;
        return true ;
    }
}
