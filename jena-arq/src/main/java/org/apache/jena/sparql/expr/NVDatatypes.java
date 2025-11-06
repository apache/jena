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

    public static final RDFDatatype XSDdecimal            = datatype(XSDDatatype.XSDdecimal);
    public static final RDFDatatype XSDfloat              = datatype(XSDDatatype.XSDfloat);
    public static final RDFDatatype XSDdouble             = datatype(XSDDatatype.XSDdouble);

    public static final RDFDatatype XSDinteger            = datatype(XSDDatatype.XSDinteger);
    public static final RDFDatatype XSDpositiveInteger    = datatype(XSDDatatype.XSDpositiveInteger);
    public static final RDFDatatype XSDnegativeInteger    = datatype(XSDDatatype.XSDnegativeInteger);
    public static final RDFDatatype XSDnonPositiveInteger = datatype(XSDDatatype.XSDnonPositiveInteger);
    public static final RDFDatatype XSDnonNegativeInteger = datatype(XSDDatatype.XSDnonNegativeInteger);

    public static final RDFDatatype XSDlong               = datatype(XSDDatatype.XSDlong);
    public static final RDFDatatype XSDint                = datatype(XSDDatatype.XSDint);
    public static final RDFDatatype XSDshort              = datatype(XSDDatatype.XSDshort);
    public static final RDFDatatype XSDbyte               = datatype(XSDDatatype.XSDbyte);

    public static final RDFDatatype XSDunsignedLong       = datatype(XSDDatatype.XSDunsignedLong);
    public static final RDFDatatype XSDunsignedInt        = datatype(XSDDatatype.XSDunsignedInt);
    public static final RDFDatatype XSDunsignedShort      = datatype(XSDDatatype.XSDunsignedShort);
    public static final RDFDatatype XSDunsignedByte       = datatype(XSDDatatype.XSDunsignedByte);

    public static final RDFDatatype XSDboolean            = datatype(XSDDatatype.XSDboolean);

    public static final RDFDatatype XSDstring             = datatype(XSDDatatype.XSDstring);
    public static final RDFDatatype langString            = datatype(RDF.dtLangString);
    public static final RDFDatatype dirLangString         = datatype(RDF.dtDirLangString);

    public static final RDFDatatype XSDnormalizedString   = datatype(XSDDatatype.XSDnormalizedString);
//    public static final RDFDatatype XSDtoken              = datatype(XSDDatatype.XSDtoken);
//    public static final RDFDatatype XSDlanguage           = datatype(XSDDatatype.XSDlanguage);

//    public static final RDFDatatype XSDhexBinary          = datatype(XSDDatatype.XSDhexBinary);
//    public static final RDFDatatype XSDbase64Binary       = datatype(XSDDatatype.XSDbase64Binary);

    public static final RDFDatatype XSDdateTime           = datatype(XSDDatatype.XSDdateTime);
    public static final RDFDatatype XSDdateTimeStamp      = datatype(XSDDatatype.XSDdateTimeStamp);
    public static final RDFDatatype XSDdate               = datatype(XSDDatatype.XSDdate);
    public static final RDFDatatype XSDtime               = datatype(XSDDatatype.XSDtime);

    public static final RDFDatatype XSDduration           = datatype(XSDDatatype.XSDduration);
    public static final RDFDatatype XSDdayTimeDuration    = datatype(XSDDatatype.XSDdayTimeDuration);
    public static final RDFDatatype XSDyearMonthDuration  = datatype(XSDDatatype.XSDyearMonthDuration);

    public static final RDFDatatype XSDgYear              = datatype(XSDDatatype.XSDgYear);
    public static final RDFDatatype XSDgMonth             = datatype(XSDDatatype.XSDgMonth);
    public static final RDFDatatype XSDgDay               = datatype(XSDDatatype.XSDgDay);
    public static final RDFDatatype XSDgYearMonth         = datatype(XSDDatatype.XSDgYearMonth);
    public static final RDFDatatype XSDgMonthDay          = datatype(XSDDatatype.XSDgMonthDay);

    public static final Set<RDFDatatype> numerics = Set.of(XSDdecimal, XSDfloat, XSDdouble, XSDinteger,
                                                           XSDpositiveInteger, XSDnegativeInteger, XSDnonPositiveInteger, XSDnonNegativeInteger,
                                                           XSDlong, XSDint, XSDshort, XSDbyte,
                                                           XSDunsignedLong, XSDunsignedInt, XSDunsignedShort, XSDunsignedByte);

    public static final Set<RDFDatatype> durations = Set.of(XSDduration, XSDdayTimeDuration, XSDyearMonthDuration);

    public static final Set<RDFDatatype> temporal = Set.of(XSDdateTime, XSDdateTimeStamp,XSDdate, XSDtime,
                                                           XSDgYear, XSDgMonth, XSDgDay,
                                                           XSDgYearMonth, XSDgMonthDay);

    private static RDFDatatype datatype(RDFDatatype rdfDatatype) {
        // Option to register in a set.
        return rdfDatatype;
    }
}