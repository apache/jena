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

package org.apache.jena.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Defines Jena resources corresponding to the URIs for 
 * the XSD primitive datatypes which are known to Jena.
 * See <a href="http://www.w3.org/2001/XMLSchema">XSD</a>
 */
public class XSD {  
	/**
	 * The namespace of the vocabulary as a string
	 */
	public static final String NS = "http://www.w3.org/2001/XMLSchema#";
    /**
     * The RDF-friendly version of the XSD namespace
     * with trailing # character.
     */
    public static String getURI() { return NS; }

    protected static Resource resource(String local) {
        return ResourceFactory.createResource(NS + local);
    }

    protected static Property property(String local) {
        return ResourceFactory.createProperty(NS, local);
    }
    
    /** Resource URI for xsd:float */
    public static final Resource xfloat = resource("float");
    
    /** Resource URI for xsd:double */
    public static final Resource xdouble = resource("double");
    
    /** Resource URI for xsd:int */
    public static final Resource xint = resource("int");
    
    /** Resource URI for xsd:long */
    public static final Resource xlong = resource("long");
       
    /** Resource URI for xsd:short */
    public static final Resource xshort = resource("short");
       
    /** Resource URI for xsd:byte */
    public static final Resource xbyte = resource("byte");
       
    /** Resource URI for xsd:boolean */
    public static final Resource xboolean = resource("boolean");
    
    /** Resource URI for xsd:string */
    public static final Resource xstring = resource("string");
    
    /** Resource URI for xsd:unsignedByte */
    public static final Resource unsignedByte = resource("unsignedByte");
       
    /** Resource URI for xsd:unsignedShort */
    public static final Resource unsignedShort = resource("unsignedShort");
       
    /** Resource URI for xsd:unsignedInt */
    public static final Resource unsignedInt = resource("unsignedInt");
       
    /** Resource URI for xsd:unsignedLong */
    public static final Resource unsignedLong = resource("unsignedLong");
       
    /** Resource URI for xsd:decimal */
    public static final Resource decimal = resource("decimal");
       
    /** Resource URI for xsd:integer */
    public static final Resource integer = resource("integer");
       
    /** Resource URI for xsd:nonPositiveInteger */
    public static final Resource nonPositiveInteger = resource("nonPositiveInteger");
       
    /** Resource URI for xsd:nonNegativeInteger */
    public static final Resource nonNegativeInteger = resource("nonNegativeInteger");
       
    /** Resource URI for xsd:positiveInteger */
    public static final Resource positiveInteger = resource("positiveInteger");
       
    /** Resource URI for xsd:negativeInteger */
    public static final Resource negativeInteger = resource("negativeInteger");
       
    /** Resource URI for xsd:normalizedString */
    public static final Resource normalizedString = resource("normalizedString");
    
    /** Resource URI for xsd:anyURI */
    public static final Resource anyURI = resource("anyURI");
    
    /** Resource URI for xsd:token */
    public static final Resource token = resource("token");

    /** Resource URI for xsd:Name */
    public static final Resource Name = resource("Name");

    /** Resource URI for xsd:QName */
    public static final Resource QName = resource("QName");

    /** Resource URI for xsd:language */
    public static final Resource language = resource("language");

    /** Resource URI for xsd:NMTOKEN */
    public static final Resource NMTOKEN = resource("NMTOKEN");

    /** Resource URI for xsd:ENTITIES */
    public static final Resource ENTITIES = resource("ENTITIES");

    /** Resource URI for xsd:NMTOKENS */
    public static final Resource NMTOKENS = resource("NMTOKENS");

    /** Resource URI for xsd:ENTITY */
    public static final Resource ENTITY = resource("ENTITY");

    /** Resource URI for xsd:ID */
    public static final Resource ID = resource("ID");

    /** Resource URI for xsd:NCName */
    public static final Resource NCName = resource("NCName");

    /** Resource URI for xsd:IDREF */
    public static final Resource IDREF = resource("IDREF");

    /** Resource URI for xsd:IDREFS */
    public static final Resource IDREFS = resource("IDREFS");

    /** Resource URI for xsd:NOTATION */
    public static final Resource NOTATION = resource("NOTATION");

    /** Resource URI for xsd:hexBinary */
    public static final Resource hexBinary = resource("hexBinary");

    /** Resource URI for xsd:base64Binary */
    public static final Resource base64Binary = resource("base64Binary");

    /** Resource URI for xsd:date */
    public static final Resource date = resource("date");

    /** Resource URI for xsd:time */
    public static final Resource time = resource("time");

    /** Resource URI for xsd:dateTime */
    public static final Resource dateTime = resource("dateTime");

    /** Resource URI for xsd:dateTimeStamp */
    public static final Resource dateTimeStamp = resource("dateTimeStamp");

    /** Resource URI for xsd:duration */
    public static final Resource duration = resource("duration");

    /** Resource URI for xsd:yearMonthDuration */
    public static final Resource yearMonthDuration = resource("yearMonthDuration");

    /** Resource URI for xsd:dayTimeDuration */
    public static final Resource dayTimeDuration = resource("dayTimeDuration");

    /** Resource URI for xsd:gDay */
    public static final Resource gDay = resource("gDay");

    /** Resource URI for xsd:gMonth */
    public static final Resource gMonth = resource("gMonth");

    /** Resource URI for xsd:gYear */
    public static final Resource gYear = resource("gYear");

    /** Resource URI for xsd:gYearMonth */
    public static final Resource gYearMonth = resource("gYearMonth");

    /** Resource URI for xsd:gMonthDay */
    public static final Resource gMonthDay = resource("gMonthDay");

    /** Property URI for xsd:length */
    public static final Property length = property("length");

    /** Property URI for xsd:minLength */
    public static final Property minLength = property("minLength");

    /** Property URI for xsd:maxLength */
    public static final Property maxLength = property("maxLength");

    /** Property URI for xsd:pattern */
    public static final Property pattern = property("pattern");

    /** Property URI for xsd:minInclusive */
    public static final Property minInclusive = property("minInclusive");

    /** Property URI for xsd:minExclusive */
    public static final Property minExclusive = property("minExclusive");

    /** Property URI for xsd:maxInclusive */
    public static final Property maxInclusive = property("maxInclusive");

    /** Property URI for xsd:maxExclusive */
    public static final Property maxExclusive = property("maxExclusive");

    /** Property URI for xsd:totalDigits */
    public static final Property totalDigits = property("totalDigits");

    /** Property URI for xsd:fractionDigits */
    public static final Property fractionDigits = property("fractionDigits");

}
