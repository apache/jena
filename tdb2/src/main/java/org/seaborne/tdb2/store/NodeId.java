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

import static org.apache.jena.sparql.expr.Expr.CMP_EQUAL;
import static org.seaborne.tdb2.store.NodeIdTypes.PTR;
import static org.seaborne.tdb2.store.NodeIdTypes.SPECIAL;
import static org.seaborne.tdb2.store.NodeIdTypes.isSpecial;

import org.apache.jena.atlas.lib.BitsInt;
import org.apache.jena.atlas.lib.BitsLong;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.graph.Node;
import org.seaborne.tdb2.sys.SystemTDB;

final
public class NodeId implements Comparable<NodeId>
{
    public static final int SIZE = SystemTDB.SizeOfLong ;
    
    public static final NodeId NodeIdInvalid        = makeSpecial(0xA1);
    public static final NodeId NodeDoesNotExist     = makeSpecial(0xA3);
    public static final NodeId NodeIdAny            = makeSpecial(0xA4);
    public static final NodeId NodeIdDefined        = makeSpecial(0xA5);
    public static final NodeId NodeIdUndefined      = makeSpecial(0xA6);
    
    private static NodeId makeSpecial(long v) {
        // Careful of an initialzer loop (create(type, v1, v2) looks at specials).
        return NodeIdFactory.createValue(SPECIAL, v);
    }
    
    /*package*/ static final boolean enableInlineLiterals = SystemTDB.enableInlineLiterals;
    
    // Internal consistency checks.
    private static final boolean CHECKING = true;
    
    // Encoding:
    //   8 bits type
    //   24 bits int
    //   64 bit value
    // The high byte of value1==type as integer value
    // In-memory:
    // Long: 
    //   value1 is 24 bits
    //   value2 is 64 bits
    //   type is a type and is OR'ed into value 1 to store.
    // 64 bit: 
    //   value1 is 0
    //   value2 is 56 bits
    //   type is a type and is OR'ed into value2  to store.
    
    // XXX CHECK!!!
    // XXX TESTS!!!
    
    final NodeIdTypes type;
    final int  value1;
    final long value2;
    
    public boolean isPtr() { return type == PTR; }
    
//    public long getPtrLocation() { return value2; }
//    public long getPtrLo() { return value2; }
//    public int  getPtrHi() { return value1 & 0x00FFFFFF; }

    // 64 bit
    public long getPtrLocation()    { return getValueNoType(); }
    public long getPtrLo()          { return getValueNoType(); }
    public int  getPtrHi()          { return value1 & 0x00FFFFFF; }
    
    public long getValueNoType() { return value2 & 0x00FFFFFFFFFFFFFFL; }
    
    public int getTypeValue() { return type.type(); }
    
    public boolean isInline() {
        return isInline(this);
    }
    
    public static boolean isInline(NodeId nodeId) {
        return NodeIdTypes.isInline(nodeId.type);
    }
        
    public boolean isValue() {
        return type != PTR && NodeIdTypes.isStorable(type); 
    }

    // Migration
    public static NodeId inline(Node node) { return NodeIdInline.inline(node); }
    public static boolean hasInlineDatatype(Node node) { return NodeIdInline.hasInlineDatatype(node); }
    public static Node extract(NodeId nodeId) { return NodeIdInline.extract(nodeId); }
    
    // XXX Later.
//    //Static forms only?
//    public boolean isAny()          { return isAny(this); }
//    public boolean isDoesNotExist() { return isDoesNotExist(this); }
//    public boolean isDefined()      { return isDefined(this); }
//    public boolean isUndefined()    { return isDefined(this); }
//
    public static final boolean isAny(NodeId nodeId)           { return nodeId == NodeIdAny || nodeId == null; }
    public static final boolean isDoesNotExist(NodeId nodeId)  { return nodeId == NodeDoesNotExist; }
//    public static boolean isDefined(NodeId nodeId)             { return nodeId == NodeIdDefined; }
//    public static boolean isUndefined(NodeId nodeId)           { return nodeId == NodeIdUndefined; }
//    
//    public static boolean isInteger(NodeId nodeId) {
//        return NodeIdTypes.isInteger(nodeId.type());
//    }
//
//    public static boolean isDecimal(NodeId nodeId) {
//        return NodeIdTypes.isDecimal(nodeId.type());
//    }
//    
//    public static boolean isDouble(NodeId nodeId) {
//        return NodeIdTypes.isDouble(nodeId.type());
//    }
//    
//    public static boolean isFloat(NodeId nodeId) {
//        return NodeIdTypes.isFloat(nodeId.type());
//    }
//
//    public static boolean isNumber(NodeId nodeId) {
//        return NodeIdTypes.isDecimal(nodeId.type());
//    }
    
    // XXX Chance for a cache?
    private static NodeId create(int v1, long v2) {
        int t = v1 >> 24;
        NodeIdTypes type = NodeIdTypes.intToEnum(t);
        return create(type, v1, v2);
    }
    
    // XXX Chance for a cache?
    private static NodeId create(NodeIdTypes type, int v1, long v2) {
        if ( isSpecial(type) ) {
            if ( equals(NodeDoesNotExist, v1, v2) )
                return NodeDoesNotExist;
            if ( equals(NodeIdAny, v1, v2) )
                return NodeIdAny;
            if ( equals(NodeIdDefined, v1, v2) )
                return NodeIdDefined;
            if ( equals(NodeIdDefined, v1, v2) )
                return NodeIdDefined;
            if ( equals(NodeIdUndefined, v1, v2) )
                return NodeIdUndefined;

            throw new IllegalArgumentException("Special not recognized");
        }
        return new NodeId(type, v1, v2);
    }
    
    /** Create from a long-encoded value */
    /*package*/ static NodeId createValue(NodeIdTypes type, long value) {
        // XXX Incorrect long id version: pass type, 24 bit int and long
        //create(type, type.type()<<24, value);
        // 64 bits
        long v2 = BitsLong.pack(value, type.type(), 56, 64);
        return create(type, 0, v2);
    }
    
    /** Create from a (int,long)-encoded value */
    private static NodeId createValue(NodeIdTypes type, int value1, long value2) {
        value1 = BitsInt.clear(value1, 24, 32);                 // XXX CONST
        value1 = BitsInt.pack(value1, type.type(), 24, 32);     // XXX CONST
        return create(type, value1, value2);
    }

    public static NodeId createPtr(int hi, long lo) {
        return create(PTR, hi, lo);
    }
    
    /* package */ NodeId(NodeIdTypes type, int v1, long v2) {
        this.type = type;
        value1 = v1;
        if ( CHECKING ) check(type, v1, v2);
        value2 = v2;
    } 

    private final void check(NodeIdTypes type, int v1, long v2) {
        if ( type == SPECIAL )
            return;
        // Long
//        int x = BitsInt.unpack(v1, 24, 32); // Hibyte
//        if ( x != type.type() )
//            FmtLog.warn(getClass(), "Mismatch type=0x%02X : hi=0x%02X", type.type(), x);
        // 64 bit.
        int x = (int)BitsLong.unpack(v2, 56, 64); // Hibyte
        if ( x != type.type() )
            FmtLog.warn(getClass(), "Mismatch type=0x%02X : hi=0x%02X", type.type(), x);
    }
    
    public NodeIdTypes type() { return type; } 

    /*package*/ int  getValue1() { return value1; }
    /*package*/ long getValue2() { return value2; }
    
    @Override
    public int hashCode() {
        // Ensure all parts have an effect on the 32 bit hash value.
        return value1 ^ ((int)value2) ^ ((int)(value2 >> 32));
    }

    @Override
    public boolean equals(Object other) {
        if ( this == other ) return true;
        if ( other == null ) return false;
        if ( !(other instanceof NodeId) ) return false;
        NodeId nOther = ((NodeId)other);
        return equals(nOther, value1, value2);  
    }
    
    public boolean equals(NodeId nodeIdOther) {
        if ( nodeIdOther == null ) return false;
        if ( this == nodeIdOther ) return true;
        return equals(nodeIdOther, value1, value2);  
    }
    
    /*package*/ static boolean equals(NodeId nodeId, int v1, long v2) {
        return v2 == nodeId.value2 && v1 == nodeId.value1;  
    }
    @Override
    public String toString() { 
        if ( this == NodeDoesNotExist ) return "[DoesNotExist]";
        if ( this == NodeIdAny ) return "[Any]";
        if ( this == NodeIdInvalid ) return "[Invalid]";
        if ( this == NodeIdDefined ) return "[Defined]";
        if ( this == NodeIdUndefined ) return "[Undefined]";
        
        if ( this.isInline() ) {
            String displayName = this.type().toString();
            return String.format("[%s 0x%014X]", displayName, BitsLong.clear(value2,56,64));
        }
        // XXX 64 bits
        //return String.format("[%08X-%016X]", value1, value2);
        return String.format("[0x%16X]", value2);
    }
    
    // ---- Encoding special - inlines.

   /** Compare - provides an ordering of {@code NodeIds}. */ 
    @Override
    public int compareTo(NodeId other) {
        return compare(this, other);
    }
    
    /** Compare - provides an ordering of {@code NodeIds}. */ 
    public static int compare(NodeId n1, NodeId n2) {
        int x = Integer.compare(n1.value1, n2.value1);
        if ( x == 0 )
            return CMP_EQUAL;
        return Long.compare(n1.value2, n2.value2);
    }
    
    public final boolean isConcrete() { return isConcrete(this); }
    
    public static final boolean isConcrete(NodeId nodeId) { 
        return ! NodeIdTypes.isSpecial(nodeId.type);
    }
}
