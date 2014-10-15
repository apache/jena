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

package com.hp.hpl.jena.tdb.store;

import java.math.BigDecimal ;
import java.nio.ByteBuffer ;

import org.apache.jena.atlas.lib.BitsLong ;
import org.apache.jena.atlas.lib.Bytes ;
import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.impl.LiteralLabel ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

final
public class NodeId
{
    // SPECIALs - never stored.
    public static final NodeId NodeDoesNotExist = new NodeId(-8) ;
    public static final NodeId NodeIdAny = new NodeId(-9) ;
    
    private static final boolean enableInlineLiterals = SystemTDB.enableInlineLiterals ;
    
    public static final int SIZE = SystemTDB.SizeOfLong ;
    final long value ;
    
    public static NodeId create(long value)
    {
        // All creation of NodeIds must go through this.
        if ( value == NodeDoesNotExist.value )
            return NodeDoesNotExist ;
        if ( value == NodeIdAny.value )
            return NodeIdAny ;
        return new NodeId(value) ;
    }
    
    public static NodeId create(byte[] b)       { return create(b, 0) ; } 
    public static NodeId create(ByteBuffer b)   { return create(b, 0) ; } 
    
    // Chance for a cache? (Small Java objects are really not that expensive these days.)
    public static NodeId create(byte[] b, int idx)
    {
        long value = Bytes.getLong(b, idx) ;
        return create(value) ;
    }
    
    public static NodeId create(ByteBuffer b, int idx)
    {
        long value = b.getLong(idx) ;
        return create(value) ;
    }
    public NodeId(long v) { value = v ;}
    
    public void toByteBuffer(ByteBuffer b, int idx) { b.putLong(idx, value) ; }
    
    public void toBytes(byte[] b, int idx) { Bytes.setLong(value, b, idx) ; }
 
    public boolean isDirect() { return type() != NONE && type() != SPECIAL ; }
                                                       
    public int type()
    {
        return (int)BitsLong.unpack(value, 56, 64) ;
    }

    static long setType(long value, int type)
    {
        return BitsLong.pack(value, type, 56, 64) ;
    }

    
    // Masked?
    public long getId()     { return value ; }
    
    @Override
    public int hashCode()
    { 
        // Ensure the type byte has an effect on the bottom 32 bits.
        return ((int)value) ^ ((int)(value >> 32)) ; 
    }
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;
        if ( !(other instanceof NodeId ) ) return false ;
        return value == ((NodeId)other).value ;
    }
    
    @Override
    public String toString()
    { 
        if ( this == NodeDoesNotExist ) return "[DoesNotExist]" ;
        if ( this == NodeIdAny ) return "[Any]" ;
        
        return String.format("[%016X]", value) ; 
    }
    
    // ---- Encoding special - inlines.
    /* The long is formated as:
     * 8 bits of type
     * 56 bits of value
     * 
     * (potential change
     *   1 bit: 0 => reference, 1 => inline
     *   7 bits of inline type.
     *   56 bits fo value)  
     *  
     *  Type 0 means the node is in the object table.
     *  Types 1+ store the value of the node in the 56 bits remaining.
     *  
     *  If a value would not fit, it will be stored externally so there is no
     *  guarantee that all integers, say, are store inline. 
     *  
     *  Integer format: signed 56 bit number.
     *  Decimal format: 8 bits scale, 48bits of signed valued.

     *  Date format:
     *  DateTime format:
     *  Boolean format:
     */
    
    // Type codes.
    // Better would be high bit 1 => encoded value.
    // enums.
    public static final int NONE               = 0 ;
    public static final int INTEGER            = 1 ;
    public static final int DECIMAL            = 2 ;
    public static final int DATE               = 3 ;
    public static final int DATETIME           = 4 ;
    public static final int BOOLEAN            = 5 ;
    public static final int SHORT_STRING       = 6 ;
    public static final int SPECIAL            = 0xFF ;
    
    /** Encode a node as an inline literal.  Return null if it can't be done */
    public static NodeId inline(Node node)
    {
        if ( node == null )
        {
            Log.warn(NodeId.class, "Null node: "+node) ;
            return null ;
        }
        
        if ( ! node.isLiteral() ) return null ;
        if ( node.getLiteralDatatype() == null ) return null ;
        
        if ( ! enableInlineLiterals ) return null ;
        
        try { return inline$(node) ; }
        catch (Throwable th) {
            Log.warn(NodeId.class, "Failed to process "+node) ;
            return null ; 
        }
    }
     
    private static NodeId inline$(Node node)
    {
        LiteralLabel lit = node.getLiteral() ;
        // Decimal is a valid supertype of integer but we handle integers and decimals differently.
        
        if ( node.getLiteralDatatype().equals(XSDDatatype.XSDdecimal) )
        {
            // Check lexical form.
            if ( ! XSDDatatype.XSDdecimal.isValidLiteral(lit) ) 
                return null ;
            
            // Not lit.getValue() because that may be a narrower type e.g. Integer.
            // .trim is how Jena does it but it rather savage. spc, \n \r \t.
            // But at this point we know it's a valid literal so the excessive
            // chopping by .trim is safe.
            BigDecimal decimal = new BigDecimal(lit.getLexicalForm().trim()) ;
            // Does range checking.
            DecimalNode dn = DecimalNode.valueOf(decimal) ;
            // null is "does not fit"
            if ( dn != null )
                // setType
                return new NodeId(dn.pack()) ;
            else
                return null ;
        }
        else    // Not decimal.
        {
            if ( XSDDatatype.XSDinteger.isValidLiteral(lit) )
            {
                // Check length of lexical form to see if it's in range of a long.
                // Long.MAX_VALUE =  9223372036854775807
                // Long.MIN_VALUE = -9223372036854775808
                // 9,223,372,036,854,775,807 is 19 digits.
                
                if ( lit.getLexicalForm().length() > 19 )
                    return null ;

                try {
                    long v = ((Number)lit.getValue()).longValue() ;
                    v = IntegerNode.pack(v) ;
                    // Value -1 is "does not fit"
                    if ( v != -1 )
                        return new NodeId(v) ;
                    else
                        return null ;
                }
                // Out of range for the type, not a long etc etc.
                catch (Throwable ex) { return null ; }
            }
        }
        
        if ( XSDDatatype.XSDdateTime.isValidLiteral(lit) ) 
        {
            // Could use the Jena/XSDDateTime object here rather than reparse the lexical form.
            // But this works and it's close to a release ... 
            long v = DateTimeNode.packDateTime(lit.getLexicalForm()) ;
            if ( v == -1 )
                return null ; 
            v = setType(v, DATETIME) ; 
            return new NodeId(v) ;
        }
        
        if ( XSDDatatype.XSDdate.isValidLiteral(lit) )
        {
            long v = DateTimeNode.packDate(lit.getLexicalForm()) ;
            if ( v == -1 )
                return null ; 
            v = setType(v, DATE) ; 
            return new NodeId(v) ;
        }
        
        if ( XSDDatatype.XSDboolean.isValidLiteral(lit) )
        {
            long v = 0 ;
            boolean b = (Boolean) lit.getValue();
            //return new NodeValueBoolean(b, node) ;
            v = setType(v, BOOLEAN) ;
            if ( b )
                v = v | 0x01 ;
            return new NodeId(v) ;
        }
        
        return null ;
    }
    
    public static boolean isInline(NodeId nodeId)
    {
        if ( nodeId == NodeId.NodeDoesNotExist )
            return false ;
        
        long v = nodeId.value ;
        int type = nodeId.type() ;
        
        switch (type)
        {
            case NONE:      return false ;
            case SPECIAL:   return false ;
                
            case INTEGER:
            case DECIMAL:
            case DATETIME:
            case DATE:
            case BOOLEAN:
                return true ;
            default:
                throw new TDBException("Unrecognized node id type: "+type) ;
        }
    }
    
    /** Decode an inline nodeID, return null if not an inline node */
    public static Node extract(NodeId nodeId)
    {
        if ( nodeId == NodeId.NodeDoesNotExist )
            return null ;
        
        long v = nodeId.value ;
        int type = nodeId.type() ;

        switch (type) {
            case NONE:      return null ;
            case SPECIAL:   return null ;
                
            case INTEGER : {
                long val = IntegerNode.unpack(v) ;
                Node n = NodeFactory.createLiteral(Long.toString(val), null, XSDDatatype.XSDinteger) ;
                return n ;
            }
            case DECIMAL : {
                BigDecimal d = DecimalNode.unpackAsBigDecimal(v) ;
                String x = d.toPlainString() ;
                return NodeFactory.createLiteral(x, null, XSDDatatype.XSDdecimal) ;
            }
            case DATETIME : {
                long val = BitsLong.clear(v, 56, 64) ;
                String lex = DateTimeNode.unpackDateTime(val) ;
                return NodeFactory.createLiteral(lex, null, XSDDatatype.XSDdateTime) ;
            }
            case DATE : {
                long val = BitsLong.clear(v, 56, 64) ;
                String lex = DateTimeNode.unpackDate(val) ;
                return NodeFactory.createLiteral(lex, null, XSDDatatype.XSDdate) ;
            }
            case BOOLEAN : {
                long val = BitsLong.clear(v, 56, 64) ;
                if ( val == 0 )
                    return NodeConst.nodeFalse ;
                if ( val == 1 )
                    return NodeConst.nodeTrue ;
                throw new TDBException("Unrecognized boolean node id : " + val) ;
            }
            default :
                throw new TDBException("Unrecognized node id type: " + type) ;
        }
    }
    
    public final boolean isConcrete() { return !isAny(this) && !isDoesNotExist(this) ; }
    
    public static final boolean isConcrete(NodeId nodeId)
    { 
        if ( nodeId == null ) return false ;
        if ( nodeId == NodeIdAny ) return false ;
        if ( nodeId == NodeDoesNotExist ) return false ;
        return true ;
    }
    
    public static final boolean isAny(NodeId nodeId) { return nodeId == NodeIdAny || nodeId == null ; }
    public static final boolean isDoesNotExist(NodeId nodeId) { return nodeId == NodeDoesNotExist ; }
    
    //public reset(long value) { this.value = value ; }
}
