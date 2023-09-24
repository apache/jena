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

package org.apache.jena.tdb2.store;

import static org.apache.jena.tdb2.store.NodeIdType.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.sparql.graph.NodeConst;
import org.apache.jena.sparql.util.NodeUtils;
import org.apache.jena.sparql.util.Utils;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.store.value.*;

/** Encoding values in a {@link NodeId}.
 *
 * TDB2 encoding:
 * <ul>
 *  <li>High bit (bit 63) 0 means the node is in the object table (PTR).
 *  <li>High bit (bit 63) 1, bit 62 1: double as 62 bits. See {@link DoubleNode62}.
 *  <li>High bit (bit 63) 1, bit 62 0: 6 bits of type, 56 bits of value.
 * </ul>
 *
 * If a value would not fit, it will be stored externally so there is no
 * guarantee that all integers, say, are store inline.
 *
 * <ul>
 * <li>Integer format: signed 56 bit number, the type field has the XSD type.
 * <li>Decimal format: 8 bits scale, 48bits of signed valued. See {@link DecimalNode56}.
 * <li>Date and DateTime
 * <li>Boolean
 * <li>Float
 * </ul>
 *
 * @see IntegerNode
 * @see DecimalNode56
 * @see DoubleNode62
 * @see DateTimeNode DateTimeNode for xsd:date and xsd:DateTime
 * @see FloatNode
 */
public class NodeIdInline {
    /** Datatypes that are candidates for inlining */
    private static Set<RDFDatatype> datatypes = new HashSet<>();
    static {
        datatypes.add(XSDDatatype.XSDdecimal);
        datatypes.add(XSDDatatype.XSDinteger);
        datatypes.add(XSDDatatype.XSDdouble);
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
    private static BidiMap<NodeIdType, RDFDatatype> derivedTypeMap = new DualHashBidiMap<>();
    static {
        derivedTypeMap.put(NodeIdType.XSD_LONG, XSDDatatype.XSDlong);
        derivedTypeMap.put(NodeIdType.XSD_INT, XSDDatatype.XSDint);
        derivedTypeMap.put(NodeIdType.XSD_SHORT, XSDDatatype.XSDshort);
        derivedTypeMap.put(NodeIdType.XSD_BYTE, XSDDatatype.XSDbyte);

        derivedTypeMap.put(NodeIdType.XSD_POSITIVE_INTEGER, XSDDatatype.XSDpositiveInteger);
        derivedTypeMap.put(NodeIdType.XSD_NON_POSITIVE_INTEGER, XSDDatatype.XSDnonPositiveInteger);
        derivedTypeMap.put(NodeIdType.XSD_NEGATIVE_INTEGER, XSDDatatype.XSDnegativeInteger);
        derivedTypeMap.put(NodeIdType.XSD_NON_NEGATIVE_INTEGER, XSDDatatype.XSDnonNegativeInteger);

        derivedTypeMap.put(NodeIdType.XSD_UNSIGNEDLONG, XSDDatatype.XSDunsignedLong);
        derivedTypeMap.put(NodeIdType.XSD_UNSIGNEDINT, XSDDatatype.XSDunsignedInt);
        derivedTypeMap.put(NodeIdType.XSD_UNSIGNEDSHORT, XSDDatatype.XSDunsignedShort);
        derivedTypeMap.put(NodeIdType.XSD_UNSIGNEDBYTE, XSDDatatype.XSDunsignedByte);
    }

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
     * Only inline(Node){@literal ->}NodeId can determine that.
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
                NodeIdType type = derivedTypeMap.inverseBidiMap().getOrDefault(lit.getDatatype(), NodeIdType.XSD_INTEGER);

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

        if ( XSDDatatype.XSDdouble.isValidLiteral(lit) ) {
            double d =  ((Number)lit.getValue()).doubleValue();
            long v = DoubleNode62.pack(d);
            if ( v == DoubleNode62.NO_ENCODING )
                return null;
            // The special byte encoding of XSD_DOUBLE is handled in NodeIdFactory.encode/.decode.
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

        NodeIdType type = nodeId.type();
        if ( type == PTR || type == NodeIdType.SPECIAL)
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
                RDFDatatype dt = derivedTypeMap.getOrDefault(type, XSDDatatype.XSDinteger);
                Node n = NodeFactory.createLiteral(Long.toString(val), dt);
                return n;
            }
            case XSD_DECIMAL : {
                BigDecimal d = DecimalNode56.unpackAsBigDecimal(nodeId.value2);
                String x = d.toPlainString();
                return NodeFactory.createLiteral(x, XSDDatatype.XSDdecimal);
            }
            case XSD_DOUBLE: {
                double d = DoubleNode62.unpack(nodeId.value2);
                String xsdStr = Utils.stringForm(d);
                Node n = NodeFactory.createLiteral(xsdStr, XSDDatatype.XSDdouble);
                return n;
            }
            case XSD_FLOAT: {
                float f = FloatNode.unpack(nodeId.value2);
                String xsdStr = Utils.stringForm(f);
                Node n = NodeFactory.createLiteral(xsdStr, XSDDatatype.XSDfloat);
                return n;
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
