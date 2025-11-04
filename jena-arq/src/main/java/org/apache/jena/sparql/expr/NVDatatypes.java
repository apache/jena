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

package org.apache.jena.sparql.expr;

import java.util.Set;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.vocabulary.RDF;

/** Constants related to datatypes with values support in ARQ/SPARQL. */
class NVDatatypes {

    /*
     * Datatype representing xsd:precisionDecimal https://www.w3.org/TR/xsd-precisionDecimal/
     */
    // Not a derived type of xsd:decimal.
    //public static final RDFDatatype XSDprecisionDecimal = new XSDPRecisionDecimal("precisionDecimal", BigDecimal.class);

    public static final RDFDatatype XSDdecimal = XSDDatatype.XSDdecimal;
    public static final RDFDatatype XSDfloat = XSDDatatype.XSDfloat;
    public static final RDFDatatype XSDdouble = XSDDatatype.XSDdouble;

    public static final RDFDatatype XSDinteger = XSDDatatype.XSDinteger;
    public static final RDFDatatype XSDpositiveInteger = XSDDatatype.XSDpositiveInteger;
    public static final RDFDatatype XSDnegativeInteger = XSDDatatype.XSDnegativeInteger;
    public static final RDFDatatype XSDnonPositiveInteger = XSDDatatype.XSDnonPositiveInteger;
    public static final RDFDatatype XSDnonNegativeInteger = XSDDatatype.XSDnonNegativeInteger;

    public static final RDFDatatype XSDlong = XSDDatatype.XSDlong;
    public static final RDFDatatype XSDint = XSDDatatype.XSDint;
    public static final RDFDatatype XSDshort = XSDDatatype.XSDshort;
    public static final RDFDatatype XSDbyte = XSDDatatype.XSDbyte;

    public static final RDFDatatype XSDunsignedLong = XSDDatatype.XSDunsignedLong;
    public static final RDFDatatype XSDunsignedInt = XSDDatatype.XSDunsignedInt;
    public static final RDFDatatype XSDunsignedShort = XSDDatatype.XSDunsignedShort;
    public static final RDFDatatype XSDunsignedByte = XSDDatatype.XSDunsignedByte;

    public static final RDFDatatype XSDboolean = XSDDatatype.XSDboolean;

    public static final RDFDatatype XSDstring = XSDDatatype.XSDstring;
    public static final RDFDatatype langString = RDF.dtLangString;
    public static final RDFDatatype dirLangString = RDF.dtDirLangString;


    public static final RDFDatatype XSDnormalizedString = XSDDatatype.XSDnormalizedString;
//    public static final RDFDatatype XSDtoken = XSDDatatype.XSDtoken;
//    public static final RDFDatatype XSDlanguage = XSDDatatype.XSDlanguage;

//    public static final RDFDatatype XSDhexBinary = XSDDatatype.XSDhexBinary;
//    public static final RDFDatatype XSDbase64Binary = XSDDatatype.XSDbase64Binary;

    public static final RDFDatatype XSDdateTime = XSDDatatype.XSDdateTime;
    public static final RDFDatatype XSDdateTimeStamp = XSDDatatype.XSDdateTimeStamp;
    public static final RDFDatatype XSDdate = XSDDatatype.XSDdate;
    public static final RDFDatatype XSDtime = XSDDatatype.XSDtime;

    public static final RDFDatatype XSDduration = XSDDatatype.XSDduration;
    public static final RDFDatatype XSDdayTimeDuration = XSDDatatype.XSDdayTimeDuration;
    public static final RDFDatatype XSDyearMonthDuration = XSDDatatype.XSDyearMonthDuration;

    public static final RDFDatatype XSDgYear = XSDDatatype.XSDgYear;
    public static final RDFDatatype XSDgMonth = XSDDatatype.XSDgMonth;
    public static final RDFDatatype XSDgDay = XSDDatatype.XSDgDay;
    public static final RDFDatatype XSDgYearMonth = XSDDatatype.XSDgYearMonth;
    public static final RDFDatatype XSDgMonthDay = XSDDatatype.XSDgMonthDay;

    public static final Set<RDFDatatype> numerics = Set.of(XSDdecimal, XSDfloat, XSDdouble, XSDinteger,
                                                           XSDpositiveInteger, XSDnegativeInteger, XSDnonPositiveInteger, XSDnonNegativeInteger,
                                                           XSDlong, XSDint, XSDshort, XSDbyte,
                                                           XSDunsignedLong, XSDunsignedInt, XSDunsignedShort, XSDunsignedByte);

    public static final Set<RDFDatatype> durations = Set.of(XSDduration, XSDdayTimeDuration, XSDyearMonthDuration);

    public static final Set<RDFDatatype> temporal = Set.of(XSDdateTime, XSDdateTimeStamp,XSDdate, XSDtime,
                                                           XSDgYear, XSDgMonth, XSDgDay,
                                                           XSDgYearMonth, XSDgMonthDay);
}
