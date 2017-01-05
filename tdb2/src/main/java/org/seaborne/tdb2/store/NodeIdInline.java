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

package org.seaborne.tdb2.store;

import static org.seaborne.tdb2.store.NodeIdTypes.PTR;
import static org.seaborne.tdb2.store.NodeIdTypes.*;
import static org.seaborne.tdb2.store.NodeIdTypes.XSD_DATE;
import static org.seaborne.tdb2.store.NodeIdTypes.XSD_DATETIME;
import static org.seaborne.tdb2.store.NodeIdTypes.XSD_DECIMAL;
import static org.seaborne.tdb2.store.NodeIdTypes.XSD_FLOAT;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ext.com.google.common.collect.BiMap;
import org.apache.jena.ext.com.google.common.collect.HashBiMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.sparql.graph.NodeConst;
import org.apache.jena.sparql.util.NodeUtils;
import org.seaborne.tdb2.TDBException;
import org.seaborne.tdb2.store.value.*;

public class NodeIdInline {
//    public static NodeId_Long inlineInteger(long v) { return null; }
//    public static NodeId_Long inlineDecimal(BigDecimal decimal) { return null; }
//    public static NodeId_Long inlineDouble(double v) { return null; }
//    public static NodeId_Long inlineFloat(float v) { return null; }
//    public static NodeId_Long inlineDateTime(String dateTime) { return null; }
//    public static NodeId_Long inlineDate(String date) { return null; }
//    public static NodeId_Long inlineBoolean(boolean bool) { return null; }
    
    /** Datatypes that are candidates for inlining */ 
    private static Set<RDFDatatype> datatypes = new HashSet<>();
    static { 
        datatypes.add(XSDDatatype.XSDdecimal);
        datatypes.add(XSDDatatype.XSDinteger);
        // 64 bit
//        XSDDatatype.XSDdouble,
        datatypes.add(XSDDatatype.XSDfloat);

        datatypes.add(XSDDatatype.XSDlong);
        datatypes.add(XSDDatatype.XSDint);
        datatypes.add(XSDDatatype.XSDshort);
        datatypes.add(XSDDatatype.XSDbyte);
        
        datatypes.add(XSDDatatype.XSDpositiveInteger);
        datatypes.add(XSDDatatype.XSDnonPositiveInteger);
        datatypes.add(XSDDatatype.XSDnegativeInteger);
        datatypes.add(XSDDatatype.XSDnonNegativeInteger);

        datatypes.add(XSDDatatype.XSDunsignedLong);
        datatypes.add(XSDDatatype.XSDunsignedInt);
        datatypes.add(XSDDatatype.XSDunsignedShort);
        datatypes.add(XSDDatatype.XSDunsignedByte);
        datatypes.add(XSDDatatype.XSDdateTimeStamp);
        datatypes.add(XSDDatatype.XSDdateTime);
        datatypes.add(XSDDatatype.XSDdate);
        datatypes.add(XSDDatatype.XSDboolean);
    };
    
    // Integer derived types. 
    private static BiMap<RDFDatatype, NodeIdTypes> derivedTypeMap = HashBiMap.create();
    static {
        derivedTypeMap.put(XSDDatatype.XSDlong, NodeIdTypes.XSD_LONG);
        derivedTypeMap.put(XSDDatatype.XSDint, NodeIdTypes.XSD_INT);
        derivedTypeMap.put(XSDDatatype.XSDshort, NodeIdTypes.XSD_SHORT);
        derivedTypeMap.put(XSDDatatype.XSDbyte, NodeIdTypes.XSD_BYTE);
        
        derivedTypeMap.put(XSDDatatype.XSDpositiveInteger, NodeIdTypes.XSD_POSITIVE_INTEGER);
        derivedTypeMap.put(XSDDatatype.XSDnonPositiveInteger, NodeIdTypes.XSD_NON_POSITIVE_INTEGER);
        derivedTypeMap.put(XSDDatatype.XSDnegativeInteger, NodeIdTypes.XSD_NEGATIVE_INTEGER);
        derivedTypeMap.put(XSDDatatype.XSDnonNegativeInteger, NodeIdTypes.XSD_NON_NEGATIVE_INTEGER);
        
        derivedTypeMap.put(XSDDatatype.XSDunsignedLong, NodeIdTypes.XSD_UNSIGNEDLONG);
        derivedTypeMap.put(XSDDatatype.XSDunsignedInt, NodeIdTypes.XSD_UNSIGNEDINT);
        derivedTypeMap.put(XSDDatatype.XSDunsignedShort, NodeIdTypes.XSD_UNSIGNEDSHORT);     
        derivedTypeMap.put(XSDDatatype.XSDunsignedByte, NodeIdTypes.XSD_UNSIGNEDBYTE);
    }
    
    // ---- Encoding special - inlines.
    /* TDB1 encoding:
     * The long is formatted as:
     * 8 bits of type
     * 56 bits of value
     * 
     * TDB2 encoding
     *  High bit 0 means the node is in the object table (PTR).
     *  High bit 1, 7 bit type, means store the value of the node in the remaining 56 or 88 bits remaining.
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
    /* Long encoding:
     * Integer: 64 bits
     * Decimal: 
     */
    
    /** Encode a node as an inline literal.  Return null if it can't be done */
    public static NodeId inline(Node node) {
        if ( node == null ) {
            Log.warn(NodeId.class, "Null node: " + node);
            return null;
        }
    
        if ( !NodeId.enableInlineLiterals )
            return null;
    
        if ( !node.isLiteral() )
            return null;
    
        if ( NodeUtils.isSimpleString(node) || NodeUtils.isLangString(node) )
            return null;
        
        try { return inline$(node); }
        catch (Throwable th) {
            Log.warn(NodeId.class, "Failed to process "+node);
            return null; 
        }
    }

    /** Return true if this node has a datatype that look like it is inlineable.
     * The node may still be out of range (e.g. very large integer).
     * Only inline(Node)->NodeId can determine that. 
     */
    public static boolean hasInlineDatatype(Node node) {
        if ( ! node.isLiteral() )
            return false;
        RDFDatatype dtn = node.getLiteralDatatype();
        return datatypes.contains(dtn);
    }

    private static NodeId inline$(Node node) {
        if ( ! hasInlineDatatype(node) )
            return null;
        LiteralLabel lit = node.getLiteral();
        // Decimal is a valid supertype of integer but we handle integers and decimals
        // differently.
    
        if ( node.getLiteralDatatype().equals(XSDDatatype.XSDdecimal) ) {
            // Check lexical form.
            if ( !XSDDatatype.XSDdecimal.isValidLiteral(lit) )
                return null;
            
            // Not lit.getValue() because that may be a narrower type e.g. Integer.
            // .trim is how Jena does it but it rather savage. spc, \n \r \t.
            // But at this point we know it's a valid literal so the excessive
            // chopping by .trim is safe.
            BigDecimal decimal = new BigDecimal(lit.getLexicalForm().trim());
            
            // Does range checking.
            DecimalNode56 dn = DecimalNode56.valueOf(decimal);
            // null is "does not fit"
            if ( dn != null )
                // setType
                return NodeId.createRaw(XSD_DECIMAL, dn.pack());
            else
                return null;
        } else { 
            // Not decimal.
            if ( XSDDatatype.XSDinteger.isValidLiteral(lit) ) {
                // Check length of lexical form to see if it's in range of a long.
                // Long.MAX_VALUE =  9223372036854775807
                // Long.MIN_VALUE = -9223372036854775808
                // 9,223,372,036,854,775,807 is 19 digits.
                
                // Quick check.
                if ( lit.getLexicalForm().length() > 19 )
                    return null;
    
                // Derived types.
                NodeIdTypes type = derivedTypeMap.getOrDefault(lit.getDatatype(), NodeIdTypes.XSD_INTEGER);
                
                try {
                    long v = ((Number)lit.getValue()).longValue();
                    v = IntegerNode.pack56(v);
                    // Value -1 is "does not fit"
                    if ( v == -1 )
                        return null;
                    return NodeId.createRaw(type, v);
                }
                // Out of range for the type, not a long etc etc.
                catch (Throwable ex) { return null; }
            }
        }
        
        if ( false && XSDDatatype.XSDdouble.isValidLiteral(lit) ) {
            // Not ready yet.
            double d =  ((Number)lit.getValue()).doubleValue();
            long v = DoubleNode62.pack(d);
            // The special encoding of XSD_DOUBLE is handling in the "toBytes" and "toByteBuffer" operations.  
            return NodeId.createRaw(XSD_DOUBLE, v);
        }
        
        if ( XSDDatatype.XSDfloat.isValidLiteral(lit) ) {
            float f =  ((Number)lit.getValue()).floatValue();
            long v = FloatNode.pack(f);
            return NodeId.createRaw(XSD_FLOAT, v);
        }
        
        if ( lit.getDatatype().equals(XSDDatatype.XSDdateTimeStamp) && XSDDatatype.XSDdateTimeStamp.isValidLiteral(lit) ) {
            long v = DateTimeNode.packDateTime(lit.getLexicalForm());
            if ( v == -1 )
                return null; 
            return NodeId.createRaw(XSD_DATETIMESTAMP, v);
        }
        if ( XSDDatatype.XSDdateTime.isValidLiteral(lit) ) {
            // Could use the Jena/XSDDateTime object here rather than reparse the lexical form.
            // But this works and it's close to a release ... 
            long v = DateTimeNode.packDateTime(lit.getLexicalForm());
            if ( v == -1 )
                return null; 
            return NodeId.createRaw(XSD_DATETIME, v);
        }
        
        if ( XSDDatatype.XSDdate.isValidLiteral(lit) ) {
            long v = DateTimeNode.packDate(lit.getLexicalForm());
            if ( v == -1 )
                return null;
            return NodeId.createRaw(XSD_DATE, v);
        }
    
        if ( XSDDatatype.XSDboolean.isValidLiteral(lit) ) {
            long v = 0;
            boolean b = (Boolean)lit.getValue();
            // return new NodeValueBoolean(b, node);
            if ( b )
                v = v | 0x01;
            return NodeId.createRaw(XSD_BOOLEAN, v);
        }
        
        return null;
    }

    /** Decode an inline nodeID, return null if not an inline node */
    public static Node extract(NodeId nodeId) {
        if ( nodeId == NodeId.NodeDoesNotExist )
            return null;
        
        NodeIdTypes type = nodeId.type();
        if ( type == PTR || type == NodeIdTypes.SPECIAL)
            return null;
        switch (type) {
//            case PTR:       return null;
//            case SPECIAL:   return null;
            case XSD_INTEGER :
                // Derived from integer
            case XSD_POSITIVE_INTEGER:
            case XSD_NEGATIVE_INTEGER:
            case XSD_NON_NEGATIVE_INTEGER:
            case XSD_NON_POSITIVE_INTEGER:
            case XSD_LONG:
            case XSD_INT:
            case XSD_SHORT:
            case XSD_BYTE:
            case XSD_UNSIGNEDLONG:
            case XSD_UNSIGNEDINT:
            case XSD_UNSIGNEDSHORT:
            case XSD_UNSIGNEDBYTE:
            {
                long val = IntegerNode.unpack56(nodeId.value2);
                RDFDatatype dt = derivedTypeMap.inverse().getOrDefault(type, XSDDatatype.XSDinteger);
                Node n = NodeFactory.createLiteral(Long.toString(val), dt);
                return n;
            }
            case XSD_DECIMAL : {
                BigDecimal d = DecimalNode56.unpackAsBigDecimal(nodeId.value2);
                String x = d.toPlainString();
                return NodeFactory.createLiteral(x, XSDDatatype.XSDdecimal);
            }
            // Not 56 bits.
//            case XSD_DOUBLE: {
//                double d = DoubleNode.unpack(nodeId.value2);
//                Node n = NodeFactory.createLiteral(Double.toString(d), XSDDatatype.XSDdouble);
//                return n ;
//            }
            case XSD_FLOAT: {
                float f = FloatNode.unpack(nodeId.value2);
                Node n = NodeFactory.createLiteral(Float.toString(f), XSDDatatype.XSDfloat);
                return n ;
            }
            case XSD_DATETIMESTAMP:
            case XSD_DATETIME: {
                RDFDatatype dt = (type==XSD_DATETIMESTAMP) ? XSDDatatype.XSDdateTimeStamp : XSDDatatype.XSDdateTime; 
                long val = nodeId.getValue2();
                String lex = DateTimeNode.unpackDateTime(val);
                return NodeFactory.createLiteral(lex, dt);
            }
            case XSD_DATE : {
                long val = nodeId.getValue2();
                String lex = DateTimeNode.unpackDate(val);
                return NodeFactory.createLiteral(lex, XSDDatatype.XSDdate);
            }
            case XSD_BOOLEAN : {
                long val = nodeId.getValue2();
                if ( val == 0 )
                    return NodeConst.nodeFalse;
                if ( val == 1 )
                    return NodeConst.nodeTrue;
                throw new TDBException("Unrecognized boolean node id : " + val);
            }
            default :
                throw new TDBException("Unrecognized node id type: " + type);
        }
    }
    
 
}
