/*
 *  (c) Copyright 2001 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * $Id: AnonId.java,v 1.4 2004-03-18 14:04:01 der Exp $
 */

package com.hp.hpl.jena.rdf.model;

import java.rmi.server.UID;

import com.hp.hpl.jena.shared.impl.JenaParameters;

/** Create a new id for an anonymous node.
 *
 * <p>This id is guaranteed to be unique on this machine.</p>
 *
 * @author bwm
 * @version $Name: not supported by cvs2svn $ $Revision: 1.4 $ $Date: 2004-03-18 14:04:01 $
 */

// This version contains experimental modifications by der to 
// switch off normal UID allocation for bNodes to assist tracking
// down apparent non-deterministic behaviour.

public class AnonId extends java.lang.Object {
    
    String id = null;

    /** Support for debugging: global anonID counter */
    private static int idCount = 0;
    
    /** Creates new AnonId.
     *
     * <p>This id is guaranteed to be unique on this machine.</p>
     */
    public AnonId() {
        if (JenaParameters.disableBNodeUIDGeneration) {
            synchronized (AnonId.class) {
                id = "A" + idCount++; // + rand.nextLong();
            }
        } else {
            id = (new UID()).toString();
        }
    }
    
/** Create a new AnonId from the string argument supplied
 * @param id A string representation of the id to be created.
 */    
    public AnonId(String id) {
        this.id = id;
    }
    
/** Test whether two id's are the same
 * @param o the object to be compared
 * @return true if and only if the two id's are the same
 */    
    public boolean equals(Object o) {
        return (o instanceof AnonId && id.equals(((AnonId)o).id));
    }
    
/** return a string representation of the id
 * @return a string representation of the id
 */    
    public String toString() {
        return id;
    }
    
/** return a hashcode for this id
 * @return the hash code
 */    
    public int hashCode() {
        return id.hashCode();
    }
}
