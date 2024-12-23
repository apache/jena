/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.ext.xerces.xs;

/**
 *  This interface defines constants used by this specification.
 */
public interface XSConstants {
    // Built-in types: primitive and derived
    /**
     * anySimpleType
     */
    public static final short ANYSIMPLETYPE_DT          = 1;
    /**
     * string
     */
    public static final short STRING_DT                 = 2;
    /**
     * boolean
     */
    public static final short BOOLEAN_DT                = 3;
    /**
     * decimal
     */
    public static final short DECIMAL_DT                = 4;
    /**
     * float
     */
    public static final short FLOAT_DT                  = 5;
    /**
     * double
     */
    public static final short DOUBLE_DT                 = 6;
    /**
     * duration
     */
    public static final short DURATION_DT               = 7;
    /**
     * dateTime
     */
    public static final short DATETIME_DT               = 8;
    /**
     * time
     */
    public static final short TIME_DT                   = 9;
    /**
     * date
     */
    public static final short DATE_DT                   = 10;
    /**
     * gYearMonth
     */
    public static final short GYEARMONTH_DT             = 11;
    /**
     * gYear
     */
    public static final short GYEAR_DT                  = 12;
    /**
     * gMonthDay
     */
    public static final short GMONTHDAY_DT              = 13;
    /**
     * gDay
     */
    public static final short GDAY_DT                   = 14;
    /**
     * gMonth
     */
    public static final short GMONTH_DT                 = 15;
    /**
     * hexBinary
     */
    public static final short HEXBINARY_DT              = 16;
    /**
     * base64Binary
     */
    public static final short BASE64BINARY_DT           = 17;
    /**
     * anyURI
     */
    public static final short ANYURI_DT                 = 18;
    /**
     * NOTATION
     */
    public static final short NOTATION_DT               = 20;
    /**
     * normalizedString
     */
    public static final short NORMALIZEDSTRING_DT       = 21;
    /**
     * token
     */
    public static final short TOKEN_DT                  = 22;
    /**
     * language
     */
    public static final short LANGUAGE_DT               = 23;
    /**
     * NMTOKEN
     */
    public static final short NMTOKEN_DT                = 24;
    /**
     * Name
     */
    public static final short NAME_DT                   = 25;
    /**
     * NCName
     */
    public static final short NCNAME_DT                 = 26;
    /**
     * ENTITY
     */
    public static final short ENTITY_DT                 = 29;
    /**
     * integer
     */
    public static final short INTEGER_DT                = 30;
    /**
     * nonPositiveInteger
     */
    public static final short NONPOSITIVEINTEGER_DT     = 31;
    /**
     * negativeInteger
     */
    public static final short NEGATIVEINTEGER_DT        = 32;
    /**
     * long
     */
    public static final short LONG_DT                   = 33;
    /**
     * int
     */
    public static final short INT_DT                    = 34;
    /**
     * short
     */
    public static final short SHORT_DT                  = 35;
    /**
     * byte
     */
    public static final short BYTE_DT                   = 36;
    /**
     * nonNegativeInteger
     */
    public static final short NONNEGATIVEINTEGER_DT     = 37;
    /**
     * unsignedLong
     */
    public static final short UNSIGNEDLONG_DT           = 38;
    /**
     * unsignedInt
     */
    public static final short UNSIGNEDINT_DT            = 39;
    /**
     * unsignedShort
     */
    public static final short UNSIGNEDSHORT_DT          = 40;
    /**
     * unsignedByte
     */
    public static final short UNSIGNEDBYTE_DT           = 41;
    /**
     * positiveInteger
     */
    public static final short POSITIVEINTEGER_DT        = 42;
    /**
     * The built-in type category is not available.
     */
    public static final short UNAVAILABLE_DT            = 45;
    
}
