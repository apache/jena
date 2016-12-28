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

import static org.seaborne.tdb2.store.NodeIdTypes.TYPES.*;

/** Constants for NodeIds.
 * Note that "PTR" is special - it uses the high bit only set to zero.
 * The rest of the bytes are the node reference. 
 */
public enum NodeIdTypes {
    // For PTR, only the top bit is used.
    PTR(T_PTR, null),

    // We provide lots of natural questions to ask of an NodeId type.
    // All are efficient. There is redundancy butliited to this file. 

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
    public static class TYPES {
        // The type values - must be stable as many of these go on disk.
        // enum ordinals are not enough.
        // Encode as:
        //   PTR : high bit zero, everythigj esle high bit one.
        //   7 bits of type value : 1 to 100
        // Low byte of ...
        public static final int T_PTR = 0 ;
        // Value types : 1 to 100  
        public static final int T_INTEGER = enc(1);
        public static final int T_DECIMAL = enc(2);
        public static final int T_FLOAT = enc(3);
        public static final int T_DOUBLE = enc(4);
        
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
        
        // Never stored : >= 0xF0 == enc(0x70)
        public static final int T_SPECIAL = enc(0x70);
        public static final int T_INVALID = enc(0x71);
        public static final int T_EXTENSION = enc(0x7F);
        
        // Encode/decode of the type value.
        static int enc(int v) { return v; }
        static int dec(int v) { return v; }
    }
    
    static boolean isStorable(NodeIdTypes type) {
        return !isSpecial(type); 
    }
    
    static boolean isInteger(NodeIdTypes type) {
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
    
    static boolean isSpecial(NodeIdTypes type) {
        return type == SPECIAL;
    }
    
    static boolean isInline(NodeIdTypes type) {
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
    
    static boolean isDecimal(NodeIdTypes type) {
        return type == XSD_DECIMAL;
    }
    
    static boolean isDouble(NodeIdTypes type) {
        return type == XSD_DOUBLE;
    }
    
    static boolean isFloat(NodeIdTypes type) {
        return type == XSD_FLOAT;
    }

    static boolean isNumber(NodeIdTypes type) {
        return isInteger(type) || isDecimal(type) || isDouble(type) || isFloat(type);
    }
    
    private final int value;
    private final String displayName;
    
    public int type() { return TYPES.dec(value); } 

    @Override
    public String toString() { return displayName != null ? displayName : name(); }

    private NodeIdTypes(int value, String displayName){
//        if ( value != 0 )
//            // Set high bit.
//            value = TYPES.enc(value);
        this.value = value;
        this.displayName = displayName;
    }

    public static NodeIdTypes intToEnum(int x) {
        if ( x >= 0x80 )
            throw new IllegalArgumentException("Value '"+x+"' not legal: too large");
        if ( x != 0 )
            x = TYPES.enc(x);
        NodeIdTypes t = intToEnum$(x);
        if ( t == INVALID$ )
            throw new IllegalArgumentException("Value '"+x+"' not legal for "+NodeIdTypes.class.getSimpleName());
        return t ;
    }
    
    private static NodeIdTypes intToEnum$(int x) {
        //x = TYPES.enc(x);
        if (x == PTR.value )                        return PTR;
        if (x == XSD_INTEGER.value )                return XSD_INTEGER ;
        if (x == XSD_DECIMAL.value )                return XSD_DECIMAL ;
        if (x == XSD_FLOAT.value )                  return XSD_FLOAT ;
        if (x == XSD_DOUBLE.value )                 return XSD_DOUBLE ;
        
        if (x == XSD_DATETIME.value )               return XSD_DATETIME ;
        if (x == XSD_DATETIMESTAMP.value )          return XSD_DATETIMESTAMP ;
        if (x == XSD_DATE.value )                   return XSD_DATE ;

        if (x == XSD_BOOLEAN.value )                return XSD_BOOLEAN ;
        if (x == XSD_SHORTSTRING.value )            return XSD_SHORTSTRING ;
        
        if (x == XSD_POSITIVE_INTEGER.value )       return XSD_POSITIVE_INTEGER;
        if (x == XSD_NEGATIVE_INTEGER.value )       return XSD_NEGATIVE_INTEGER ;
        if (x == XSD_NON_NEGATIVE_INTEGER.value )   return XSD_NON_NEGATIVE_INTEGER ;
        if (x == XSD_NON_POSITIVE_INTEGER.value )   return XSD_NON_POSITIVE_INTEGER ;
        
        if (x == XSD_LONG.value )                   return XSD_LONG ;
        if (x == XSD_INT.value )                    return XSD_INT ;
        if (x == XSD_SHORT.value )                  return XSD_SHORT ;
        if (x == XSD_BYTE.value )                   return XSD_BYTE ;
        if (x == XSD_UNSIGNEDLONG.value )           return XSD_UNSIGNEDLONG ;
        if (x == XSD_UNSIGNEDINT.value )            return XSD_UNSIGNEDINT ;
        if (x == XSD_UNSIGNEDSHORT.value )          return XSD_UNSIGNEDSHORT ;
        if (x == XSD_UNSIGNEDBYTE.value )           return XSD_UNSIGNEDBYTE ;
        //if (x == EXTENSION.value )                  return EXTENSION ;
        return INVALID$;
    }
}