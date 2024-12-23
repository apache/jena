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

package org.apache.jena.ext.xerces.impl.dv;

import org.apache.jena.ext.xerces.xs.XSSimpleTypeDefinition;

/**
 * This interface <code>XSSimpleType</code> represents the simple type
 * definition of schema component and defines methods to query the information
 * contained.
 * Any simple type (atomic, list or union) will implement this interface.
 * It inherits from <code>XSTypeDecl</code>.
 *
 * {@literal @xerces.internal} 
 *
 * @author Sandy Gao, IBM
 *
 * @version $Id: XSSimpleType.java 446751 2006-09-15 21:54:06Z mrglavas $
 */
public interface XSSimpleType extends XSSimpleTypeDefinition {

    /**
     * constants defined for the values of 'whitespace' facet.
     * see <a href='http://www.w3.org/TR/xmlschema-2/#dt-whiteSpace'> XML Schema
     * Part 2: Datatypes </a>
     */
    /** preserve the white spaces */
    public static final short WS_PRESERVE = 0;
    /** replace the white spaces */
    public static final short WS_REPLACE  = 1;
    /** collapse the white spaces */
    public static final short WS_COLLAPSE = 2;

    /**
     * Constant defined for the primitive built-in simple tpyes.
     * see <a href='http://www.w3.org/TR/xmlschema-2/#built-in-primitive-datatypes'>
     * XML Schema Part 2: Datatypes </a>
     */
    /** "anySimpleType" type */
    public static final short PRIMITIVE_ANYSIMPLETYPE = 0;
    /** "string" type */
    public static final short PRIMITIVE_STRING        = 1;
    /** "boolean" type */
    public static final short PRIMITIVE_BOOLEAN       = 2;
    /** "decimal" type */
    public static final short PRIMITIVE_DECIMAL       = 3;
    /** "float" type */
    public static final short PRIMITIVE_FLOAT         = 4;
    /** "double" type */
    public static final short PRIMITIVE_DOUBLE        = 5;
    /** "duration" type */
    public static final short PRIMITIVE_DURATION      = 6;
    /** "dataTime" type */
    public static final short PRIMITIVE_DATETIME      = 7;
    /** "time" type */
    public static final short PRIMITIVE_TIME          = 8;
    /** "date" type */
    public static final short PRIMITIVE_DATE          = 9;
    /** "gYearMonth" type */
    public static final short PRIMITIVE_GYEARMONTH    = 10;
    /** "gYear" type */
    public static final short PRIMITIVE_GYEAR         = 11;
    /** "gMonthDay" type */
    public static final short PRIMITIVE_GMONTHDAY     = 12;
    /** "gDay" type */
    public static final short PRIMITIVE_GDAY          = 13;
    /** "gMonth" type */
    public static final short PRIMITIVE_GMONTH        = 14;
    /** "hexBinary" type */
    public static final short PRIMITIVE_HEXBINARY     = 15;
    /** "base64Binary" type */
    public static final short PRIMITIVE_BASE64BINARY  = 16;
    /** "anyURI" type */
    public static final short PRIMITIVE_ANYURI        = 17;
    /** "precisionDecimal" type */
    public static final short PRIMITIVE_PRECISIONDECIMAL = 18;
    /** "NOTATION" type */
    public static final short PRIMITIVE_NOTATION      = 19;

    /**
     * validate a given string against this simple type.
     *
     * @param content       the string value that needs to be validated
     * @param validatedInfo used to store validation result
     *
     * @return              the actual value (QName, Boolean) of the string value
     */
    public Object validate(String content, ValidatedInfo validatedInfo)
        throws InvalidDatatypeValueException;

    /**
     * Validate an actual value against this simple type.
     *
     * @param validatedInfo used to provide the actual value and member types
     * @exception InvalidDatatypeValueException  exception for invalid values.
     */
    public void validate(ValidatedInfo validatedInfo)
        throws InvalidDatatypeValueException;

    /**
     * Check whether two actual values are equal.
     *
     * @param value1  the first value
     * @param value2  the second value
     * @return        true if the two value are equal
     */
    public boolean isEqual(Object value1, Object value2);
}
