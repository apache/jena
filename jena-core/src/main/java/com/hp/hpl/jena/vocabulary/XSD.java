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

package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

/**
 * Defines Jena resources corresponding to the URIs for 
 * the XSD primitive datatypes which are known to Jena. 
 */
public class XSD {    
    /**
     * The RDF-friendly version of the XSD namespace
     * with trailing # character.
     */
    public static String getURI() { return XSDDatatype.XSD + "#"; }
    
    /** Resource URI for xsd:float */
    public static Resource xfloat;
    
    /** Resource URI for xsd:double */
    public static Resource xdouble;
    
    /** Resource URI for xsd:int */
    public static Resource xint;
    
    /** Resource URI for xsd:long */
    public static Resource xlong;
       
    /** Resource URI for xsd:short */
    public static Resource xshort;
       
    /** Resource URI for xsd:byte */
    public static Resource xbyte;
       
    /** Resource URI for xsd:boolean */
    public static Resource xboolean;
    
    /** Resource URI for xsd:string */
    public static Resource xstring;
    
    /** Resource URI for xsd:unsignedByte */
    public static Resource unsignedByte;
       
    /** Resource URI for xsd:unsignedShort */
    public static Resource unsignedShort;
       
    /** Resource URI for xsd:unsignedInt */
    public static Resource unsignedInt;
       
    /** Resource URI for xsd:unsignedLong */
    public static Resource unsignedLong;
       
    /** Resource URI for xsd:decimal */
    public static Resource decimal;
       
    /** Resource URI for xsd:integer */
    public static Resource integer;
       
    /** Resource URI for xsd:nonPositiveInteger */
    public static Resource nonPositiveInteger;
       
    /** Resource URI for xsd:nonNegativeInteger */
    public static Resource nonNegativeInteger;
       
    /** Resource URI for xsd:positiveInteger */
    public static Resource positiveInteger;
       
    /** Resource URI for xsd:negativeInteger */
    public static Resource negativeInteger;
       
    /** Resource URI for xsd:normalizedString */
    public static Resource normalizedString;
    
    /** Resource URI for xsd:anyURI */
    public static Resource anyURI;
    
    /** Resource URI for xsd:token */
    public static Resource token;

    /** Resource URI for xsd:Name */
    public static Resource Name;

    /** Resource URI for xsd:QName */
    public static Resource QName;

    /** Resource URI for xsd:language */
    public static Resource language;

    /** Resource URI for xsd:NMTOKEN */
    public static Resource NMTOKEN;

    /** Resource URI for xsd:ENTITIES */
    public static Resource ENTITIES;

    /** Resource URI for xsd:NMTOKENS */
    public static Resource NMTOKENS;

    /** Resource URI for xsd:ENTITY */
    public static Resource ENTITY;

    /** Resource URI for xsd:ID */
    public static Resource ID;

    /** Resource URI for xsd:NCName */
    public static Resource NCName;

    /** Resource URI for xsd:IDREF */
    public static Resource IDREF;

    /** Resource URI for xsd:IDREFS */
    public static Resource IDREFS;

    /** Resource URI for xsd:NOTATION */
    public static Resource NOTATION;

    /** Resource URI for xsd:hexBinary */
    public static Resource hexBinary;

    /** Resource URI for xsd:base64Binary */
    public static Resource base64Binary;

    /** Resource URI for xsd:date */
    public static Resource date;

    /** Resource URI for xsd:time */
    public static Resource time;

    /** Resource URI for xsd:dateTime */
    public static Resource dateTime;

    /** Resource URI for xsd:duration */
    public static Resource duration;

    /** Resource URI for xsd:gDay */
    public static Resource gDay;

    /** Resource URI for xsd:gMonth */
    public static Resource gMonth;

    /** Resource URI for xsd:gYear */
    public static Resource gYear;

    /** Resource URI for xsd:gYearMonth */
    public static Resource gYearMonth;

    /** Resource URI for xsd:gMonthDay */
    public static Resource gMonthDay;

    // Initializer
    static {
        xfloat = ResourceFactory.createResource(XSDDatatype.XSDfloat.getURI());
        xdouble = ResourceFactory.createResource(XSDDatatype.XSDdouble.getURI());
        xint = ResourceFactory.createResource(XSDDatatype.XSDint.getURI());
        xlong = ResourceFactory.createResource(XSDDatatype.XSDlong.getURI());
        xshort = ResourceFactory.createResource(XSDDatatype.XSDshort.getURI());
        xbyte = ResourceFactory.createResource(XSDDatatype.XSDbyte.getURI());
        unsignedByte = ResourceFactory.createResource(XSDDatatype.XSDunsignedByte.getURI());
        unsignedShort = ResourceFactory.createResource(XSDDatatype.XSDunsignedShort.getURI());
        unsignedInt = ResourceFactory.createResource(XSDDatatype.XSDunsignedInt.getURI());
        unsignedLong = ResourceFactory.createResource(XSDDatatype.XSDunsignedLong.getURI());
        decimal = ResourceFactory.createResource(XSDDatatype.XSDdecimal.getURI());
        integer = ResourceFactory.createResource(XSDDatatype.XSDinteger.getURI());
        nonPositiveInteger = ResourceFactory.createResource(XSDDatatype.XSDnonPositiveInteger.getURI());
        nonNegativeInteger = ResourceFactory.createResource(XSDDatatype.XSDnonNegativeInteger.getURI());
        positiveInteger = ResourceFactory.createResource(XSDDatatype.XSDpositiveInteger.getURI());
        negativeInteger = ResourceFactory.createResource(XSDDatatype.XSDnegativeInteger.getURI());
        xboolean = ResourceFactory.createResource(XSDDatatype.XSDboolean.getURI());
        xstring = ResourceFactory.createResource(XSDDatatype.XSDstring.getURI());
        normalizedString = ResourceFactory.createResource(XSDDatatype.XSDnormalizedString.getURI());
        anyURI = ResourceFactory.createResource(XSDDatatype.XSDanyURI.getURI());
        token = ResourceFactory.createResource(XSDDatatype.XSDtoken.getURI());
        Name = ResourceFactory.createResource(XSDDatatype.XSDName.getURI());
        QName = ResourceFactory.createResource(XSDDatatype.XSDQName.getURI());
        language = ResourceFactory.createResource(XSDDatatype.XSDlanguage.getURI());
        NMTOKEN = ResourceFactory.createResource(XSDDatatype.XSDNMTOKEN.getURI());
        ENTITY = ResourceFactory.createResource(XSDDatatype.XSDENTITY.getURI());
        ID = ResourceFactory.createResource(XSDDatatype.XSDID.getURI());
        NCName = ResourceFactory.createResource(XSDDatatype.XSDNCName.getURI());
        IDREF = ResourceFactory.createResource(XSDDatatype.XSDIDREF.getURI());
        NOTATION = ResourceFactory.createResource(XSDDatatype.XSDNOTATION.getURI());
        hexBinary = ResourceFactory.createResource(XSDDatatype.XSDhexBinary.getURI());
        base64Binary = ResourceFactory.createResource(XSDDatatype.XSDbase64Binary.getURI());
        date = ResourceFactory.createResource(XSDDatatype.XSDdate.getURI());
        time = ResourceFactory.createResource(XSDDatatype.XSDtime.getURI());
        dateTime = ResourceFactory.createResource(XSDDatatype.XSDdateTime.getURI());
        duration = ResourceFactory.createResource(XSDDatatype.XSDduration.getURI());
        gDay = ResourceFactory.createResource(XSDDatatype.XSDgDay.getURI());
        gMonth = ResourceFactory.createResource(XSDDatatype.XSDgMonth.getURI());
        gYear = ResourceFactory.createResource(XSDDatatype.XSDgYear.getURI());
        gYearMonth = ResourceFactory.createResource(XSDDatatype.XSDgYearMonth.getURI());
        gMonthDay = ResourceFactory.createResource(XSDDatatype.XSDgMonthDay.getURI());
//        ENTITIES = ResourceFactory.createResource(XSDDatatype.XSDENTITIES.getURI());
//        NMTOKENS = ResourceFactory.createResource(XSDDatatype.XSDNMTOKENS.getURI());
//        IDREFS = ResourceFactory.createResource(XSDDatatype.XSDIDREFS.getURI());
    }
}
