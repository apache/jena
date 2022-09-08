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

import static org.apache.jena.tdb2.store.NodeIdType.TYPES.*;

import org.apache.jena.tdb2.store.value.DoubleNode62;

/** Constants for NodeIds.
 * Note that "PTR" is special - it uses the high bit only set to zero.
 * Note that "XSD_DOUBLE" is special - it sets the high bit (value/ptr)
 * and the next bit only, leaving 62 bits of value.
 * Otherwise a type is encoded as the high byte.
 *
 *  @see NodeIdInline
 */
public enum NodeIdType {
    PTR(T_PTR, null),

    XSD_INTEGER(T_INTEGER, "Integer"),
    XSD_DECIMAL(T_DECIMAL, "Decimal"),
    XSD_FLOAT(T_FLOAT, "Float"),
    XSD_DOUBLE(T_DOUBLE, "Double"),
    XSD_DATETIME(T_DATETIME, "DateTime"),
    XSD_DATETIMESTAMP(T_DATETIMESTAMP, "DateTimeStamp"),
    XSD_DATE(T_DATE, "Date"),
    XSD_BOOLEAN(T_BOOLEAN, "Boolean"),
    XSD_SHORTSTRING(T_SHORTSTRING, "ShortString"),
    // 1 and greater
    XSD_POSITIVE_INTEGER(T_POSITIVE_INTEGER, "Positive Integer"),
    // -1 and down
    XSD_NEGATIVE_INTEGER(T_NEGATIVE_INTEGER, "Negative Integer"),
    // postive and zero
    XSD_NON_NEGATIVE_INTEGER(T_NON_NEGATIVE_INTEGER, "Non-negative Integer"),
    // negative or zero
    XSD_NON_POSITIVE_INTEGER(T_NON_POSITIVE_INTEGER, "Non-positive Integer"),
    XSD_LONG(T_LONG, "Long"),
    XSD_INT(T_INT, "Int"),
    XSD_SHORT(T_SHORT, "Short"),
    XSD_BYTE(T_BYTE, "Byte"),
    XSD_UNSIGNEDLONG(T_UNSIGNEDLONG, "UnsignedLong"),
    XSD_UNSIGNEDINT(T_UNSIGNEDINT, "UnsignedInt"),
    XSD_UNSIGNEDSHORT(T_UNSIGNEDSHORT, "UnsignedShort"),
    XSD_UNSIGNEDBYTE(T_UNSIGNEDBYTE, "UnsignedByte"),

    // Never stored.
    SPECIAL(T_SPECIAL, "Special"),
    // Used here only.
    INVALID$(T_INVALID, "Invalid")

    //, EXTENSION(T_EXTENSION, "Extension")
   ;
    /** The type values - must be stable as many of these go on disk.
     * enum ordinals are not enough.
     *
     * Encode as:
     * <ul>
     * <li>PTR : high bit zero, everything else written with a high bit one (done in {@link NodeIdFactory#encode}
     * <li>T_DOUBLE : Special case: next bit one.  01?? ???? i.e. 11?? on disk (value bit, double bit).
     *    This leaves 62 bits for encoding a double (See {@link DoubleNode62}).
     * <li>Otherwise, a number in the low byte of the constant, high bits "10".
     * </ul>
     * The {@code T_*} constants do not include the high bit.
     */
    public static class TYPES {
        public static final int T_PTR = 0;

        public static final int T_DOUBLE = enc(0x40);

        // Value types : 1 to 100
        public static final int T_INTEGER = enc(1);
        public static final int T_DECIMAL = enc(2);
        public static final int T_FLOAT = enc(3);
        //private static final int T_DOUBLE_X = enc(4);

        public static final int T_DATETIME = enc(5);
        public static final int T_DATETIMESTAMP = enc(6);
        public static final int T_DATE = enc(7);
        public static final int T_BOOLEAN = enc(8);
        public static final int T_SHORTSTRING = enc(9);

        public static final int T_POSITIVE_INTEGER = enc(10);
        public static final int T_NEGATIVE_INTEGER = enc(11);
        public static final int T_NON_NEGATIVE_INTEGER = enc(12);
        public static final int T_NON_POSITIVE_INTEGER = enc(13);
        public static final int T_LONG = enc(14);
        public static final int T_INT = enc(15);
        public static final int T_SHORT = enc(16);
        public static final int T_BYTE = enc(17);
        public static final int T_UNSIGNEDLONG = enc(18);
        public static final int T_UNSIGNEDINT = enc(19);
        public static final int T_UNSIGNEDSHORT = enc(20);
        public static final int T_UNSIGNEDBYTE = enc(21);
        // 21 is 00010101

        // Never stored : bits 1011 0000 so as not to look like a double.
        public static final int T_SPECIAL = enc(0x30);
        public static final int T_INVALID = enc(0x31);

        public static final int T_EXTENSION = enc(0x3F);

        // Encode/decode of the type value.
        static int enc(int v) { return v; }
        static int dec(int v) { return v; }
    }

    // We provide lots of natural questions to ask of an NodeId type.
    // All are efficient. There is redundancy but limited to this file.

    static boolean isStorable(NodeIdType type) {
        return !isSpecial(type);
    }

    // For numbers, an out-of-range number maybe stored a PTR.

    static boolean isInteger(NodeIdType type) {
        switch(type) {
            case XSD_INTEGER:
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
                return true;
            default:
                return false;
        }
    }

    static boolean isDecimal(NodeIdType type) {
        return type == XSD_DECIMAL;
    }

    static boolean isDouble(NodeIdType type) {
        return type == XSD_DOUBLE;
    }

    static boolean isFloat(NodeIdType type) {
        return type == XSD_FLOAT;
    }

    static boolean isNumber(NodeIdType type) {
        return isInteger(type) || isDecimal(type) || isDouble(type) || isFloat(type);
    }

    static boolean isSpecial(NodeIdType type) {
        return type == SPECIAL;
    }

    static boolean isInline(NodeIdType type) {
        switch(type) {
            case XSD_INTEGER:
            case XSD_DECIMAL:
            case XSD_DOUBLE:
            case XSD_FLOAT:

            case XSD_DATETIME:
            case XSD_DATETIMESTAMP:
            case XSD_DATE:
            case XSD_BOOLEAN:
            case XSD_SHORTSTRING:

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
                return true;
            default:
                return false;
        }
    }

    private final int value;
    private final String displayName;

    public int type() { return TYPES.dec(value); }

    @Override
    public String toString() { return displayName != null ? displayName : name(); }

    private NodeIdType(int value, String displayName){
//        if ( value != 0 )
//            // Set high bit.
//            value = TYPES.enc(value);
        this.value = value;
        this.displayName = displayName;
    }

    public static NodeIdType intToEnum(int x) {
        if ( x >= 0x80 )
            throw new IllegalArgumentException("Value '"+x+"' not legal: too large");
        if ( x != 0 )
            x = TYPES.enc(x);
        NodeIdType t = intToEnum$(x);
        if ( t == INVALID$ )
            throw new IllegalArgumentException("Value '"+x+"' not legal for "+NodeIdType.class.getSimpleName());
        return t;
    }

    private static NodeIdType intToEnum$(int x) {
        //x = TYPES.enc(x);
        if (x == PTR.value )                        return PTR;
        // XSD_DOUBL is special encoded - handled elsewhere.
        if (x == XSD_DOUBLE.value )                 return XSD_DOUBLE;
        if (x == XSD_INTEGER.value )                return XSD_INTEGER;
        if (x == XSD_DECIMAL.value )                return XSD_DECIMAL;
        if (x == XSD_FLOAT.value )                  return XSD_FLOAT;

        if (x == XSD_DATETIME.value )               return XSD_DATETIME;
        if (x == XSD_DATETIMESTAMP.value )          return XSD_DATETIMESTAMP;
        if (x == XSD_DATE.value )                   return XSD_DATE;

        if (x == XSD_BOOLEAN.value )                return XSD_BOOLEAN;
        if (x == XSD_SHORTSTRING.value )            return XSD_SHORTSTRING;

        if (x == XSD_POSITIVE_INTEGER.value )       return XSD_POSITIVE_INTEGER;
        if (x == XSD_NEGATIVE_INTEGER.value )       return XSD_NEGATIVE_INTEGER;
        if (x == XSD_NON_NEGATIVE_INTEGER.value )   return XSD_NON_NEGATIVE_INTEGER;
        if (x == XSD_NON_POSITIVE_INTEGER.value )   return XSD_NON_POSITIVE_INTEGER;

        if (x == XSD_LONG.value )                   return XSD_LONG;
        if (x == XSD_INT.value )                    return XSD_INT;
        if (x == XSD_SHORT.value )                  return XSD_SHORT;
        if (x == XSD_BYTE.value )                   return XSD_BYTE;
        if (x == XSD_UNSIGNEDLONG.value )           return XSD_UNSIGNEDLONG;
        if (x == XSD_UNSIGNEDINT.value )            return XSD_UNSIGNEDINT;
        if (x == XSD_UNSIGNEDSHORT.value )          return XSD_UNSIGNEDSHORT;
        if (x == XSD_UNSIGNEDBYTE.value )           return XSD_UNSIGNEDBYTE;
        //if (x == EXTENSION.value )                  return EXTENSION;
        return INVALID$;
    }
}
