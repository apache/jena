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

package org.seaborne.tdb2.store;

import static org.seaborne.tdb2.store.NodeIdTypes.PTR;
import static org.seaborne.tdb2.store.NodeIdTypes.isSpecial;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jena.atlas.lib.BitsLong;
import org.apache.jena.atlas.lib.Bytes;

/** Factory for NodeIds, including to/from disk forms via ByteBuffers and byte[].*/
final
public class NodeIdFactory
{
    // On-disk: may be shorter (controlled by get/set ByteBuffer and byte[]).
    // In-emmeory - always int-long
    
    // XXX Adjustable length:
    // 1+3+8
    // 1+7
    // Hashing switch.
    
    // XXX Chance for a cache?
    private static NodeId xcreate(int v1, long v2) {
        // XXX 64bit
        int t = v1 >> 24;
        NodeIdTypes type = NodeIdTypes.intToEnum(t);
        return create(type, v1, v2);
    }
    
    // XXX Chance for a small cache?
    private static NodeId create(NodeIdTypes type, int v1, long v2) {
        if ( isSpecial(type) ) {
            if ( NodeId.equals(NodeId.NodeDoesNotExist, v1, v2) )
                return NodeId.NodeDoesNotExist;
            if ( NodeId.equals(NodeId.NodeIdAny, v1, v2) )
                return NodeId.NodeIdAny;
            if ( NodeId.equals(NodeId.NodeIdDefined, v1, v2) )
                return NodeId.NodeIdDefined;
            if ( NodeId.equals(NodeId.NodeIdDefined, v1, v2) )
                return NodeId.NodeIdDefined;
            if ( NodeId.equals(NodeId.NodeIdUndefined, v1, v2) )
                return NodeId.NodeIdUndefined;
            //throw new IllegalArgumentException("Special not recognized");
        }
        return new NodeId(type, v1, v2);
    }

    public static NodeId createValue(NodeIdTypes type, long value) {
        // 64 bit.
        value = BitsLong.pack(value, type.type(), 56, 64);
        return new NodeId(type, 0, value);
    }
    
    public static NodeId createPtr(int hi, long lo) {
        return create(PTR, hi, lo);
    }
    
    // ------- On-disk forms.
    // 
    
    /** Not relative - get from position zero */
    public static NodeId get(byte[] b) { return get(b,0); } 

    // **** 64 bit NodeId ****
    /** Relative {@code ByteBuffer} {@code get} */
    public static NodeId get(ByteBuffer b)   { 
        long value2 = b.getLong();
        return make(value2);
    }

    public static NodeId get(byte[] b, int idx) {
        long value2 = Bytes.getLong(b, idx);
        return make(value2);
    }

    public static NodeId get(ByteBuffer b, int idx) {
        long value2 = b.getLong(idx);
        return make(value2);
    }

    // Make : 64 bit version,
    private static NodeId make(long value2) {
        int x = (int)BitsLong.unpack(value2, 56, 64);
        NodeIdTypes t = NodeIdTypes.intToEnum(x);
        return new NodeId(t, 0, value2);
    }

    /** Not relative - set at position zero */
    public static void set(NodeId nodeId, byte[] b) {
        Bytes.setLong(nodeId.value2, b, 0);
    }

    /** Relative {@code set} */
    public static void set(NodeId nodeId, ByteBuffer b) {
        b.putLong(nodeId.value2);
    }

    public static void set(NodeId nodeId, byte[] b, int idx) {
        Bytes.setLong(nodeId.value2, b, idx);
    }

    public static void set(NodeId nodeId, ByteBuffer b, int idx) {
        b.putLong(idx, nodeId.value2);
    }
    
    
    public static void setNext(NodeId nodeId, byte[] b, int idx) {
        Bytes.setLong(nodeId.value2+1, b, idx);
    }


//    /** Relative {@code ByteBuffer} {@code get} */
//    public static NodeId get(ByteBuffer b)   { 
//        int value1 = b.getInt();
//        long value2 = b.getLong();
//        return create(value1,value2);
//    }
//    
//    public static NodeId get(byte[] b, int idx) {
//        int value1 = Bytes.getInt(b, idx);
//        long value2 = Bytes.getLong(b, idx+SystemTDB.SizeOfInt);
//        return create(value1, value2);
//    }
//
//    public static NodeId get(ByteBuffer b, int idx) {
//        int value1 = b.getInt(idx);
//        long value2 = b.getLong(idx+SystemTDB.SizeOfInt);
//        return create(value1,value2);
//    }
//    
//    /** Not relative - set at position zero */
//    public static void set(NodeId nodeId, byte[] b) {
//        Bytes.setInt(nodeId.value1, b, 0);
//        Bytes.setLong(nodeId.value2, b, SystemTDB.SizeOfInt);
//    }
//
//    /** Relative {@code set} */
//    public static void set(NodeId nodeId, ByteBuffer b) {
//        b.putInt(nodeId.value1);
//        b.putLong(nodeId.value2);
//    }
//
//    public static void set(NodeId nodeId, byte[] b, int idx) {
//        Bytes.setInt(nodeId.value1, b, idx);
//        Bytes.setLong(nodeId.value2, b, idx+SystemTDB.SizeOfInt);
//    }
//
//    public static void set(NodeId nodeId, ByteBuffer b, int idx) {
//        b.putInt(idx, nodeId.value1);
//        b.putLong(idx+SystemTDB.SizeOfInt, nodeId.value2);
//    }
//    
//    public static void setNext(NodeId nodeId, byte[] b, int idx) {
//        int v1 = nodeId.value1;
//        long v2 = nodeId.value2;
//        // Unsigned 96 big add! 
//        if ( v2 < 0 ) {
//            if ( v2 == Long.MIN_VALUE )
//                v1++ ;
//            else
//                v2--;
//        } else
//            v2++ ;
//        Bytes.setInt(v1, b, idx);
//        Bytes.setLong(v2, b, idx+Integer.SIZE);
//    }


    private static AtomicInteger counter = new AtomicInteger(0xB0);
    public static NodeId genUnique() {
        return NodeIdFactory.create(NodeIdTypes.SPECIAL, counter.incrementAndGet(), 0);
    }
}
