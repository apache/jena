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

package org.apache.jena.ext.xerces.impl.dv.xs;

import org.apache.jena.ext.xerces.impl.dv.SchemaDVFactory;
import org.apache.jena.ext.xerces.impl.dv.XSFacets;
import org.apache.jena.ext.xerces.impl.dv.XSSimpleType;
import org.apache.jena.ext.xerces.util.SymbolHash;
import org.apache.jena.ext.xerces.xs.XSConstants;

/**
 * the base factory to create/return built-in schema DVs and create user-defined DVs
 * 
 * {@literal @xerces.internal} 
 *
 * @author Neeraj Bajaj, Sun Microsystems, inc.
 * @author Sandy Gao, IBM
 * @author Khaled Noaman, IBM 
 *
 * @version $Id: BaseSchemaDVFactory.java 805582 2009-08-18 21:13:20Z sandygao $
 */
@SuppressWarnings("all")
public abstract class BaseSchemaDVFactory extends SchemaDVFactory {

    static final String URI_SCHEMAFORSCHEMA = "http://www.w3.org/2001/XMLSchema";
    static final String DATETIME = "dateTime";
    static final String DURATION = "duration";

    // create common built-in types
    protected static void createBuiltInTypes(SymbolHash builtInTypes, XSSimpleTypeDecl baseAtomicType) {
        // all schema simple type names
        final String ANYSIMPLETYPE     = "anySimpleType";
        final String ANYURI            = "anyURI";
        final String BASE64BINARY      = "base64Binary";
        final String BOOLEAN           = "boolean";
        final String BYTE              = "byte";
        final String DATE              = "date";
        final String DAY               = "gDay";
        final String DECIMAL           = "decimal";
        final String DOUBLE            = "double";
        final String FLOAT             = "float";
        final String HEXBINARY         = "hexBinary";
        final String INT               = "int";
        final String INTEGER           = "integer";
        final String LONG              = "long";
        final String NAME              = "Name";
        final String NEGATIVEINTEGER   = "negativeInteger";
        final String MONTH             = "gMonth";
        final String MONTHDAY          = "gMonthDay";
        final String NCNAME            = "NCName";
        final String NMTOKEN           = "NMTOKEN";
        final String LANGUAGE          = "language";
        final String NONNEGATIVEINTEGER= "nonNegativeInteger";
        final String NONPOSITIVEINTEGER= "nonPositiveInteger";
        final String NORMALIZEDSTRING  = "normalizedString";
        final String POSITIVEINTEGER   = "positiveInteger";
        final String SHORT             = "short";
        final String STRING            = "string";
        final String TIME              = "time";
        final String TOKEN             = "token";
        final String UNSIGNEDBYTE      = "unsignedByte";
        final String UNSIGNEDINT       = "unsignedInt";
        final String UNSIGNEDLONG      = "unsignedLong";
        final String UNSIGNEDSHORT     = "unsignedShort";
        final String YEAR              = "gYear";
        final String YEARMONTH         = "gYearMonth";

        // These SHOULD NOT be used in RDF 1.1
        // Only kept here because this is used by the respective datatype registrations in XSDDatatype.
        // TODO: remove this in Jena 6 together with the datatype registrations.
        final String ENTITY            = "ENTITY";
        final String ID                = "ID";
        final String IDREF             = "IDREF";
        final String NOTATION          = "NOTATION";
        final String QNAME             = "QName";

        final XSFacets facets = new XSFacets();
        
        builtInTypes.put(ANYSIMPLETYPE, XSSimpleTypeDecl.fAnySimpleType);

        XSSimpleTypeDecl stringDV = new XSSimpleTypeDecl(baseAtomicType, STRING, XSSimpleTypeDecl.DV_STRING, XSSimpleType.ORDERED_FALSE, false, false , true, XSConstants.STRING_DT);
        builtInTypes.put(STRING, stringDV);
        builtInTypes.put(BOOLEAN, new XSSimpleTypeDecl(baseAtomicType, BOOLEAN, XSSimpleTypeDecl.DV_BOOLEAN, XSSimpleType.ORDERED_FALSE, true, false, true, XSConstants.BOOLEAN_DT));
        XSSimpleTypeDecl decimalDV = new XSSimpleTypeDecl(baseAtomicType, DECIMAL, XSSimpleTypeDecl.DV_DECIMAL, XSSimpleType.ORDERED_TOTAL, false, true, true, XSConstants.DECIMAL_DT);
        builtInTypes.put(DECIMAL, decimalDV);

        builtInTypes.put(ANYURI, new XSSimpleTypeDecl(baseAtomicType, ANYURI, XSSimpleTypeDecl.DV_ANYURI, XSSimpleType.ORDERED_FALSE, false, false, true, XSConstants.ANYURI_DT));
        builtInTypes.put(BASE64BINARY, new XSSimpleTypeDecl(baseAtomicType, BASE64BINARY, XSSimpleTypeDecl.DV_BASE64BINARY, XSSimpleType.ORDERED_FALSE, false, false, true, XSConstants.BASE64BINARY_DT));

        XSSimpleTypeDecl durationDV = new XSSimpleTypeDecl(baseAtomicType, DURATION, XSSimpleTypeDecl.DV_DURATION, XSSimpleType.ORDERED_PARTIAL, false, false, true, XSConstants.DURATION_DT);
        builtInTypes.put(DURATION, durationDV);

        builtInTypes.put(DATETIME, new XSSimpleTypeDecl(baseAtomicType, DATETIME, XSSimpleTypeDecl.DV_DATETIME, XSSimpleType.ORDERED_PARTIAL, false, false, true, XSConstants.DATETIME_DT));
        builtInTypes.put(TIME, new XSSimpleTypeDecl(baseAtomicType, TIME, XSSimpleTypeDecl.DV_TIME, XSSimpleType.ORDERED_PARTIAL, false, false, true, XSConstants.TIME_DT));
        builtInTypes.put(DATE, new XSSimpleTypeDecl(baseAtomicType, DATE, XSSimpleTypeDecl.DV_DATE, XSSimpleType.ORDERED_PARTIAL, false, false, true, XSConstants.DATE_DT));
        builtInTypes.put(YEARMONTH, new XSSimpleTypeDecl(baseAtomicType, YEARMONTH, XSSimpleTypeDecl.DV_GYEARMONTH, XSSimpleType.ORDERED_PARTIAL, false, false, true, XSConstants.GYEARMONTH_DT));
        builtInTypes.put(YEAR, new XSSimpleTypeDecl(baseAtomicType, YEAR, XSSimpleTypeDecl.DV_GYEAR, XSSimpleType.ORDERED_PARTIAL, false, false, true, XSConstants.GYEAR_DT));
        builtInTypes.put(MONTHDAY, new XSSimpleTypeDecl(baseAtomicType, MONTHDAY, XSSimpleTypeDecl.DV_GMONTHDAY, XSSimpleType.ORDERED_PARTIAL, false, false, true, XSConstants.GMONTHDAY_DT));
        builtInTypes.put(DAY, new XSSimpleTypeDecl(baseAtomicType, DAY, XSSimpleTypeDecl.DV_GDAY, XSSimpleType.ORDERED_PARTIAL, false, false, true, XSConstants.GDAY_DT));
        builtInTypes.put(MONTH, new XSSimpleTypeDecl(baseAtomicType, MONTH, XSSimpleTypeDecl.DV_GMONTH, XSSimpleType.ORDERED_PARTIAL, false, false, true, XSConstants.GMONTH_DT));

        XSSimpleTypeDecl integerDV = new XSSimpleTypeDecl(decimalDV, INTEGER, XSSimpleTypeDecl.DV_INTEGER, XSSimpleType.ORDERED_TOTAL, false, true, true, XSConstants.INTEGER_DT);
        builtInTypes.put(INTEGER, integerDV);

        facets.maxInclusive = "0";
        XSSimpleTypeDecl nonPositiveDV = new XSSimpleTypeDecl(integerDV, NONPOSITIVEINTEGER, URI_SCHEMAFORSCHEMA, false, XSConstants.NONPOSITIVEINTEGER_DT);
        nonPositiveDV.applyFacets1(facets , XSSimpleType.FACET_MAXINCLUSIVE);
        builtInTypes.put(NONPOSITIVEINTEGER, nonPositiveDV);

        facets.maxInclusive = "-1";
        XSSimpleTypeDecl negativeDV = new XSSimpleTypeDecl(nonPositiveDV, NEGATIVEINTEGER, URI_SCHEMAFORSCHEMA, false, XSConstants.NEGATIVEINTEGER_DT);
        negativeDV.applyFacets1(facets , XSSimpleType.FACET_MAXINCLUSIVE);
        builtInTypes.put(NEGATIVEINTEGER, negativeDV);

        facets.maxInclusive = "9223372036854775807";
        facets.minInclusive = "-9223372036854775808";
        XSSimpleTypeDecl longDV = new XSSimpleTypeDecl(integerDV, LONG, URI_SCHEMAFORSCHEMA, false, XSConstants.LONG_DT);
        longDV.applyFacets1(facets , (short)(XSSimpleType.FACET_MAXINCLUSIVE | XSSimpleType.FACET_MININCLUSIVE));
        builtInTypes.put(LONG, longDV);

        facets.maxInclusive = "2147483647";
        facets.minInclusive =  "-2147483648";
        XSSimpleTypeDecl intDV = new XSSimpleTypeDecl(longDV, INT, URI_SCHEMAFORSCHEMA, false, XSConstants.INT_DT);
        intDV.applyFacets1(facets, (short)(XSSimpleType.FACET_MAXINCLUSIVE | XSSimpleType.FACET_MININCLUSIVE));
        builtInTypes.put(INT, intDV);

        facets.maxInclusive = "32767";
        facets.minInclusive = "-32768";
        XSSimpleTypeDecl shortDV = new XSSimpleTypeDecl(intDV, SHORT , URI_SCHEMAFORSCHEMA, false, XSConstants.SHORT_DT);
        shortDV.applyFacets1(facets, (short)(XSSimpleType.FACET_MAXINCLUSIVE | XSSimpleType.FACET_MININCLUSIVE));
        builtInTypes.put(SHORT, shortDV);

        facets.maxInclusive = "127";
        facets.minInclusive = "-128";
        XSSimpleTypeDecl byteDV = new XSSimpleTypeDecl(shortDV, BYTE , URI_SCHEMAFORSCHEMA, false, XSConstants.BYTE_DT);
        byteDV.applyFacets1(facets, (short)(XSSimpleType.FACET_MAXINCLUSIVE | XSSimpleType.FACET_MININCLUSIVE));
        builtInTypes.put(BYTE, byteDV);

        facets.minInclusive =  "0" ;
        XSSimpleTypeDecl nonNegativeDV = new XSSimpleTypeDecl(integerDV, NONNEGATIVEINTEGER , URI_SCHEMAFORSCHEMA, false, XSConstants.NONNEGATIVEINTEGER_DT);
        nonNegativeDV.applyFacets1(facets, XSSimpleType.FACET_MININCLUSIVE);
        builtInTypes.put(NONNEGATIVEINTEGER, nonNegativeDV);

        facets.maxInclusive = "18446744073709551615" ;
        XSSimpleTypeDecl unsignedLongDV = new XSSimpleTypeDecl(nonNegativeDV, UNSIGNEDLONG , URI_SCHEMAFORSCHEMA, false, XSConstants.UNSIGNEDLONG_DT);
        unsignedLongDV.applyFacets1(facets, XSSimpleType.FACET_MAXINCLUSIVE);
        builtInTypes.put(UNSIGNEDLONG, unsignedLongDV);

        facets.maxInclusive = "4294967295" ;
        XSSimpleTypeDecl unsignedIntDV = new XSSimpleTypeDecl(unsignedLongDV, UNSIGNEDINT , URI_SCHEMAFORSCHEMA, false, XSConstants.UNSIGNEDINT_DT);
        unsignedIntDV.applyFacets1(facets, XSSimpleType.FACET_MAXINCLUSIVE);
        builtInTypes.put(UNSIGNEDINT, unsignedIntDV);

        facets.maxInclusive = "65535" ;
        XSSimpleTypeDecl unsignedShortDV = new XSSimpleTypeDecl(unsignedIntDV, UNSIGNEDSHORT , URI_SCHEMAFORSCHEMA, false, XSConstants.UNSIGNEDSHORT_DT);
        unsignedShortDV.applyFacets1(facets, XSSimpleType.FACET_MAXINCLUSIVE);
        builtInTypes.put(UNSIGNEDSHORT, unsignedShortDV);

        facets.maxInclusive = "255" ;
        XSSimpleTypeDecl unsignedByteDV = new XSSimpleTypeDecl(unsignedShortDV, UNSIGNEDBYTE , URI_SCHEMAFORSCHEMA, false, XSConstants.UNSIGNEDBYTE_DT);
        unsignedByteDV.applyFacets1(facets, XSSimpleType.FACET_MAXINCLUSIVE);
        builtInTypes.put(UNSIGNEDBYTE, unsignedByteDV);

        facets.minInclusive = "1" ;
        XSSimpleTypeDecl positiveIntegerDV = new XSSimpleTypeDecl(nonNegativeDV, POSITIVEINTEGER , URI_SCHEMAFORSCHEMA, false, XSConstants.POSITIVEINTEGER_DT);
        positiveIntegerDV.applyFacets1(facets, XSSimpleType.FACET_MININCLUSIVE);
        builtInTypes.put(POSITIVEINTEGER, positiveIntegerDV);

        builtInTypes.put(FLOAT, new XSSimpleTypeDecl(baseAtomicType, FLOAT, XSSimpleTypeDecl.DV_FLOAT, XSSimpleType.ORDERED_PARTIAL, true, true, true, XSConstants.FLOAT_DT));
        builtInTypes.put(DOUBLE, new XSSimpleTypeDecl(baseAtomicType, DOUBLE, XSSimpleTypeDecl.DV_DOUBLE, XSSimpleType.ORDERED_PARTIAL, true, true, true, XSConstants.DOUBLE_DT));
        builtInTypes.put(HEXBINARY, new XSSimpleTypeDecl(baseAtomicType, HEXBINARY, XSSimpleTypeDecl.DV_HEXBINARY, XSSimpleType.ORDERED_FALSE, false, false, true, XSConstants.HEXBINARY_DT));

        facets.whiteSpace =  XSSimpleType.WS_REPLACE;
        XSSimpleTypeDecl normalizedDV = new XSSimpleTypeDecl(stringDV, NORMALIZEDSTRING , URI_SCHEMAFORSCHEMA, false, XSConstants.NORMALIZEDSTRING_DT);
        normalizedDV.applyFacets1(facets, XSSimpleType.FACET_WHITESPACE);
        builtInTypes.put(NORMALIZEDSTRING, normalizedDV);

        facets.whiteSpace = XSSimpleType.WS_COLLAPSE;
        XSSimpleTypeDecl tokenDV = new XSSimpleTypeDecl(normalizedDV, TOKEN , URI_SCHEMAFORSCHEMA, false, XSConstants.TOKEN_DT);
        tokenDV.applyFacets1(facets, XSSimpleType.FACET_WHITESPACE);
        builtInTypes.put(TOKEN, tokenDV);

        facets.whiteSpace = XSSimpleType.WS_COLLAPSE;
        facets.pattern  = "([a-zA-Z]{1,8})(-[a-zA-Z0-9]{1,8})*";
        XSSimpleTypeDecl languageDV = new XSSimpleTypeDecl(tokenDV, LANGUAGE , URI_SCHEMAFORSCHEMA, false, XSConstants.LANGUAGE_DT);
        languageDV.applyFacets1(facets, (short)(XSSimpleType.FACET_WHITESPACE | XSSimpleType.FACET_PATTERN));
        builtInTypes.put(LANGUAGE, languageDV);

        facets.whiteSpace =  XSSimpleType.WS_COLLAPSE;
        XSSimpleTypeDecl nameDV = new XSSimpleTypeDecl(tokenDV, NAME , URI_SCHEMAFORSCHEMA, false, XSConstants.NAME_DT);
        nameDV.applyFacets2(facets, XSSimpleTypeDecl.SPECIAL_PATTERN_NAME);
        builtInTypes.put(NAME, nameDV);

        facets.whiteSpace = XSSimpleType.WS_COLLAPSE;
        XSSimpleTypeDecl ncnameDV = new XSSimpleTypeDecl(nameDV, NCNAME , URI_SCHEMAFORSCHEMA, false, XSConstants.NCNAME_DT) ;
        ncnameDV.applyFacets2(facets, XSSimpleTypeDecl.SPECIAL_PATTERN_NCNAME);
        builtInTypes.put(NCNAME, ncnameDV);

        facets.whiteSpace  = XSSimpleType.WS_COLLAPSE;
        XSSimpleTypeDecl nmtokenDV = new XSSimpleTypeDecl(tokenDV, NMTOKEN, URI_SCHEMAFORSCHEMA, false, XSConstants.NMTOKEN_DT);
        nmtokenDV.applyFacets2(facets, XSSimpleTypeDecl.SPECIAL_PATTERN_NMTOKEN);
        builtInTypes.put(NMTOKEN, nmtokenDV);

        // Dummy registrations for XML datatypes that SHOULD NOT be used in RDF 1.1.
        // We don't do any validation on them.
        // Only kept here because this is used by the respective datatype registrations in XSDDatatype.
        // TODO: remove this in Jena 6 together with the datatype registrations.
        builtInTypes.put(ENTITY, XSSimpleTypeDecl.fAnySimpleType);
        builtInTypes.put(ID, XSSimpleTypeDecl.fAnySimpleType);
        builtInTypes.put(IDREF, XSSimpleTypeDecl.fAnySimpleType);
        builtInTypes.put(NOTATION, XSSimpleTypeDecl.fAnySimpleType);
        builtInTypes.put(QNAME, XSSimpleTypeDecl.fAnySimpleType);
    } //createBuiltInTypes()

    /** Implementation internal **/
    public XSSimpleTypeDecl newXSSimpleTypeDecl() {
        return new XSSimpleTypeDecl();
    }
} //BaseSchemaDVFactory
