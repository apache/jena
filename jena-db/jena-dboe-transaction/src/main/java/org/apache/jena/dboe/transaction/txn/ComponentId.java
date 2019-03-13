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

import java.util.Arrays;
import java.util.UUID;

import org.apache.jena.atlas.lib.Bytes;

/** A {@code ComponentId} consists of two parts: a globally unique context
 * (roughly - the domain where the {@code ComponentId} is valid)
 * and an id within the context.
 * Context is often a single coordinator but can be, for example,
 * a distributed transaction coordinator.
 */

public class ComponentId {
    // Fixed size.
    public static final int SIZE = 4;
    private final UUID coordinatorId;
    private final byte[] bytes;
    // Just a helper for development.  Not persisted in the journal.
    private final String displayName;

    /** Create a new ComponentId from the given bytes.
     * The bytes are <em>not</em> copied.
     * The caller must not modify them after this call.
     * The static method {@link #create(String, byte[])}
     * does the copy.
     */
    private ComponentId(String label, UUID coordinatorId, byte[] bytes) {
        this.coordinatorId = coordinatorId;
        if ( label == null )
            label = "";
        if ( bytes.length > SIZE )
            throw new IllegalArgumentException("Bytes for ComponentId too long "+bytes.length+" > "+SIZE);
        if ( bytes.length < SIZE )
            // Make safe.
            bytes = Arrays.copyOf(bytes, SIZE);
        this.bytes = bytes;
        this.displayName = label;
    }

    public byte[] getBytes() { return bytes; }

    public UUID getBaseId() { return coordinatorId; }

    public String label() { return displayName; }

    @Override
    public String toString() { return displayName+"["+Bytes.asHex(bytes)+"]"; }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(bytes);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        ComponentId other = (ComponentId)obj;
        if ( !Arrays.equals(bytes, other.bytes) )
            return false;
        return true;
    }

    /** Create a ComponentId from the given bytes */
    public static ComponentId create(UUID coordinatorBase, byte[] bytes) {
        bytes = Arrays.copyOf(bytes, bytes.length);
        return new ComponentId(null, coordinatorBase, bytes);
    }

    /** Given a base componentId, create a derived (different) one.
     * This is deterministically done based on  baseComponentId and index.
     * The label is just for display purposes.
     */
    public static ComponentId alloc(String label, UUID coordinatorBase, int index) {
        byte[] bytes = Bytes.intToBytes(index);
        return new ComponentId(label, coordinatorBase, bytes);
    }

//    private static ComponentId create(byte[] bytes, String label, int index) {
//        bytes = Arrays.copyOf(bytes, bytes.length);
//        int x = Bytes.getInt(bytes, bytes.length-SystemBase.SizeOfInt);
//        x = x ^ index;
//        Bytes.setInt(x, bytes, bytes.length - SystemBase.SizeOfInt);
//        ComponentId cid = new ComponentId(label+"-"+index, bytes);
//        return cid;
//    }

    static int counter = 0;
    /** Return a fresh ComponentId (not preserved across JVM runs) */
    public static ComponentId allocLocal() {
        counter++;
        UUID uuid = UUID.randomUUID();
        return alloc("Local-"+counter, uuid, counter);
    }

}

