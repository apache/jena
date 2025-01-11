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

import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.jena.ext.xerces.impl.dv.*;
import org.apache.jena.ext.xerces.xs.*;
import org.apache.jena.ext.xerces.util.XercesXMLChar;

/**
 * {@literal @xerces.internal}
 *
 * @author Sandy Gao, IBM
 * @author Neeraj Bajaj, Sun Microsystems, inc.
 *
 * @version $Id: XSSimpleTypeDecl.java 1026362 2010-10-22 15:15:18Z sandygao $
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class XSSimpleTypeDecl implements XSSimpleType {

    protected static final short DV_ANYSIMPLETYPE = PRIMITIVE_ANYSIMPLETYPE;
    protected static final short DV_STRING        = PRIMITIVE_STRING;
    protected static final short DV_BOOLEAN       = PRIMITIVE_BOOLEAN;
    protected static final short DV_DECIMAL       = PRIMITIVE_DECIMAL;
    protected static final short DV_FLOAT         = PRIMITIVE_FLOAT;
    protected static final short DV_DOUBLE        = PRIMITIVE_DOUBLE;
    protected static final short DV_DURATION      = PRIMITIVE_DURATION;
    protected static final short DV_DATETIME      = PRIMITIVE_DATETIME;
    protected static final short DV_TIME          = PRIMITIVE_TIME;
    protected static final short DV_DATE          = PRIMITIVE_DATE;
    protected static final short DV_GYEARMONTH    = PRIMITIVE_GYEARMONTH;
    protected static final short DV_GYEAR         = PRIMITIVE_GYEAR;
    protected static final short DV_GMONTHDAY     = PRIMITIVE_GMONTHDAY;
    protected static final short DV_GDAY          = PRIMITIVE_GDAY;
    protected static final short DV_GMONTH        = PRIMITIVE_GMONTH;
    protected static final short DV_HEXBINARY     = PRIMITIVE_HEXBINARY;
    protected static final short DV_BASE64BINARY  = PRIMITIVE_BASE64BINARY;
    protected static final short DV_ANYURI        = PRIMITIVE_ANYURI;
    protected static final short DV_PRECISIONDECIMAL = PRIMITIVE_PRECISIONDECIMAL;

    protected static final short DV_INTEGER       = DV_PRECISIONDECIMAL + 1;
    protected static final short DV_YEARMONTHDURATION = DV_PRECISIONDECIMAL + 2;
    protected static final short DV_DAYTIMEDURATION	= DV_PRECISIONDECIMAL + 3;
    protected static final short DV_ANYATOMICTYPE = DV_PRECISIONDECIMAL + 4;
    protected static final short DV_DATETIMESTAMP = DV_PRECISIONDECIMAL + 5;

    private static final TypeValidator[] gDVs = {
        new AnySimpleDV(),
        new StringDV(),
        new BooleanDV(),
        new DecimalDV(),
        new FloatDV(),
        new DoubleDV(),
        new DurationDV(),
        new DateTimeDV(),
        new TimeDV(),
        new DateDV(),
        new YearMonthDV(),
        new YearDV(),
        new MonthDayDV(),
        new DayDV(),
        new MonthDV(),
        new HexBinaryDV(),
        new Base64BinaryDV(),
        new AnyURIDV(),
        new PrecisionDecimalDV(),   // XML Schema 1.1 type
        new IntegerDV(),
        new YearMonthDurationDV(),  // XML Schema 1.1 type
        new DayTimeDurationDV(),    // XML Schema 1.1 type
        new AnyAtomicDV(),          // XML Schema 1.1 type
        new DateTimeStampDV()       // XML Schema 1.1 type
    };

    static final short NORMALIZE_NONE = 0;
    static final short NORMALIZE_TRIM = 1;
    static final short NORMALIZE_FULL = 2;
    static final short[] fDVNormalizeType = {
        NORMALIZE_NONE, //AnySimpleDV(),
        NORMALIZE_FULL, //StringDV(),
        NORMALIZE_TRIM, //BooleanDV(),
        NORMALIZE_TRIM, //DecimalDV(),
        NORMALIZE_TRIM, //FloatDV(),
        NORMALIZE_TRIM, //DoubleDV(),
        NORMALIZE_TRIM, //DurationDV(),
        NORMALIZE_TRIM, //DateTimeDV(),
        NORMALIZE_TRIM, //TimeDV(),
        NORMALIZE_TRIM, //DateDV(),
        NORMALIZE_TRIM, //YearMonthDV(),
        NORMALIZE_TRIM, //YearDV(),
        NORMALIZE_TRIM, //MonthDayDV(),
        NORMALIZE_TRIM, //DayDV(),
        NORMALIZE_TRIM, //MonthDV(),
        NORMALIZE_TRIM, //HexBinaryDV(),
        NORMALIZE_NONE, //Base64BinaryDV(),  // Base64 know how to deal with spaces
        NORMALIZE_TRIM, //AnyURIDV(),
        NORMALIZE_TRIM, //PrecisionDecimalDV() (Schema 1.1)
        NORMALIZE_TRIM, //IntegerDV(),
        NORMALIZE_TRIM, //YearMonthDurationDV() (Schema 1.1)
        NORMALIZE_TRIM, //DayTimeDurationDV() (Schema 1.1)
        NORMALIZE_NONE, //AnyAtomicDV() (Schema 1.1)
    };

    static final short SPECIAL_PATTERN_NONE     = 0;
    static final short SPECIAL_PATTERN_NMTOKEN  = 1;
    static final short SPECIAL_PATTERN_NAME     = 2;
    static final short SPECIAL_PATTERN_NCNAME   = 3;

    static final String[] SPECIAL_PATTERN_STRING   = {
        "NONE", "NMTOKEN", "Name", "NCName"
    };

    static final String[] WS_FACET_STRING = {
        "preserve", "replace", "collapse"
    };

    static final String URI_SCHEMAFORSCHEMA = "http://www.w3.org/2001/XMLSchema";

    private final TypeValidator[] fDVs = gDVs;

    // this will be true if this is a static XSSimpleTypeDecl
    // and hence must remain immutable (i.e., applyFacets
    // may not be permitted to have any effect).
    private boolean fIsImmutable = false;

    // The most specific built-in type kind.
    private short fBuiltInKind;

    private String fTypeName;
    private String fTargetNamespace;
    private XSSimpleTypeDecl fBase;
    private short fVariety = -1;
    private short fValidationDV = -1;

    private short fFacetsDefined = 0;
    private short fFixedFacet = 0;

    //for constraining facets
    private short fWhiteSpace = 0;
    private int fLength = -1;
    private int fMinLength = -1;
    private int fMaxLength = -1;
    private int fTotalDigits = -1;
    private int fFractionDigits = -1;
    private Vector fPattern;
    private Vector fPatternStr;
    private ValidatedInfo[] fEnumeration;
    private int fEnumerationSize;
    private Object fMaxInclusive;
    private Object fMaxExclusive;
    private Object fMinExclusive;
    private Object fMinInclusive;

    private short fPatternType = SPECIAL_PATTERN_NONE;

    // for fundamental facets
    private short fOrdered;
    private boolean fFinite;
    private boolean fNumeric;

    // default constructor
    public XSSimpleTypeDecl(){}

    //Create a new built-in primitive types (and integer/yearMonthDuration)
    protected XSSimpleTypeDecl(XSSimpleTypeDecl base, String name, short validateDV,
                               short ordered, boolean finite,
                               boolean numeric, boolean isImmutable, short builtInKind) {
        fIsImmutable = isImmutable;
        fBase = base;
        fTypeName = name;
        fTargetNamespace = URI_SCHEMAFORSCHEMA;
        // To simplify the code for anySimpleType, we treat it as an atomic type
        fVariety = VARIETY_ATOMIC;
        fValidationDV = validateDV;
        fFacetsDefined = FACET_WHITESPACE;
        if (validateDV == DV_ANYSIMPLETYPE ||
            validateDV == DV_ANYATOMICTYPE ||
            validateDV == DV_STRING) {
            fWhiteSpace = WS_PRESERVE;
        }
        else {
            fWhiteSpace = WS_COLLAPSE;
            fFixedFacet = FACET_WHITESPACE;
        }
        this.fOrdered = ordered;
        this.fFinite = finite;
        this.fNumeric = numeric;

        // Specify the build in kind for this primitive type
        fBuiltInKind = builtInKind;
    }

    //Create a new simple type for restriction for built-in types
    protected XSSimpleTypeDecl(XSSimpleTypeDecl base, String name, String uri, boolean isImmutable, short builtInKind) {
        this(base, name, uri, isImmutable);
        // Specify the build in kind for this built-in type
        fBuiltInKind = builtInKind;
    }

    //Create a new simple type for restriction.
    protected XSSimpleTypeDecl(XSSimpleTypeDecl base, String name, String uri, boolean isImmutable) {
        fBase = base;
        fTypeName = name;
        fTargetNamespace = uri;

        fVariety = fBase.fVariety;
        fValidationDV = fBase.fValidationDV;

        // always inherit facets from the base.
        // in case a type is created, but applyFacets is not called
        fLength = fBase.fLength;
        fMinLength = fBase.fMinLength;
        fMaxLength = fBase.fMaxLength;
        fPattern = fBase.fPattern;
        fPatternStr = fBase.fPatternStr;
        fEnumeration = fBase.fEnumeration;
        fEnumerationSize = fBase.fEnumerationSize;
        fWhiteSpace = fBase.fWhiteSpace;
        fMaxExclusive = fBase.fMaxExclusive;
        fMaxInclusive = fBase.fMaxInclusive;
        fMinExclusive = fBase.fMinExclusive;
        fMinInclusive = fBase.fMinInclusive;
        fTotalDigits = fBase.fTotalDigits;
        fFractionDigits = fBase.fFractionDigits;
        fPatternType = fBase.fPatternType;
        fFixedFacet = fBase.fFixedFacet;
        fFacetsDefined = fBase.fFacetsDefined;

        //we also set fundamental facets information in case applyFacets is not called.
        calcFundamentalFacets();
        fIsImmutable = isImmutable;

        // Inherit from the base type
        fBuiltInKind = base.fBuiltInKind;
    }

    @Override
    public String getName() {
        return fTypeName;
    }

    @Override
    public String getNamespace() {
        return fTargetNamespace;
    }

    @Override
    public XSTypeDefinition getBaseType(){
        return fBase;
    }

    /**
     * built-in derived types by restriction
     */
    void applyFacets1(XSFacets facets, short presentFacet) {

        try {
            applyFacets(facets, presentFacet, SPECIAL_PATTERN_NONE);
        } catch (InvalidDatatypeFacetException e) {
            // should never gets here, internel error
            throw new RuntimeException("internal error");
        }
        // we've now applied facets; so lock this object:
        fIsImmutable = true;
    }

    /**
     * built-in derived types by restriction
     */
    void applyFacets2(XSFacets facets, short patternType) {

        try {
            applyFacets(facets, XSSimpleTypeDefinition.FACET_WHITESPACE, patternType);
        } catch (InvalidDatatypeFacetException e) {
            // should never gets here, internal error
            throw new RuntimeException("internal error", e);
        }
        // we've now applied facets; so lock this object:
        fIsImmutable = true;
    }

    /**
     * If <restriction> is chosen, or built-in derived types by restriction
     */
    void applyFacets(XSFacets facets, short presentFacet, short patternType)
    throws InvalidDatatypeFacetException {

        // if the object is immutable, should not apply facets...
        if(fIsImmutable) return;
        ValidatedInfo tempInfo = new ValidatedInfo();

        // clear facets. because we always inherit facets in the constructor
        // REVISIT: in fact, we don't need to clear them.
        // we can convert 5 string values (4 bounds + 1 enum) to actual values,
        // store them somewhere, then do facet checking at once, instead of
        // going through the following steps. (lots of checking are redundant:
        // for example, ((presentFacet & FACET_XXX) != 0))

        fFacetsDefined = 0;
        fFixedFacet = 0;

        int result = 0 ;

        // step 1: parse present facets
        short allowedFacet = fDVs[fValidationDV].getAllowedFacets();

        // pattern
        if ((presentFacet & FACET_PATTERN) != 0) {
            if ((allowedFacet & FACET_PATTERN) == 0) {
                reportError("cos-applicable-facets", new Object[]{"pattern", fTypeName});
            } else {
                Pattern regex = null;
                try {
                    regex = Pattern.compile(facets.pattern);
                } catch (Exception e) {
                    reportError("InvalidRegex", new Object[]{facets.pattern, e.getLocalizedMessage()});
                }
                if (regex != null) {
                    fPattern = new Vector();
                    fPattern.addElement(regex);
                    fPatternStr = new Vector();
                    fPatternStr.addElement(facets.pattern);
                    fFacetsDefined |= FACET_PATTERN;
                }
            }
        }

        // whiteSpace
        if ((presentFacet & FACET_WHITESPACE) != 0) {
            if ((allowedFacet & FACET_WHITESPACE) == 0) {
                reportError("cos-applicable-facets", new Object[]{"whiteSpace", fTypeName});
            } else {
                fWhiteSpace = facets.whiteSpace;
                fFacetsDefined |= FACET_WHITESPACE;
            }
        }

        // maxInclusive
        if ((presentFacet & FACET_MAXINCLUSIVE) != 0) {
            if ((allowedFacet & FACET_MAXINCLUSIVE) == 0) {
                reportError("cos-applicable-facets", new Object[]{"maxInclusive", fTypeName});
            } else {
                try {
                    fMaxInclusive = fBase.getActualValue(facets.maxInclusive, tempInfo);
                    fFacetsDefined |= FACET_MAXINCLUSIVE;
                } catch (InvalidDatatypeValueException ide) {
                    reportError(ide.getKey(), ide.getArgs());
                    reportError("FacetValueFromBase", new Object[]{fTypeName, facets.maxInclusive,
                            "maxInclusive", fBase.getName()});
                }

                // check against fixed value in base
                if (((fBase.fFacetsDefined & FACET_MAXINCLUSIVE) != 0)) {
                    if ((fBase.fFixedFacet & FACET_MAXINCLUSIVE) != 0) {
                        if (fDVs[fValidationDV].compare(fMaxInclusive, fBase.fMaxInclusive) != 0)
                            reportError( "FixedFacetValue", new Object[]{"maxInclusive", fMaxInclusive, fBase.fMaxInclusive, fTypeName});
                    }
                }
                // maxInclusive from base
                try {
                    fBase.validate(tempInfo);
                } catch (InvalidDatatypeValueException ide) {
                    reportError(ide.getKey(), ide.getArgs());
                    reportError("FacetValueFromBase", new Object[]{fTypeName, facets.maxInclusive,
                            "maxInclusive", fBase.getName()});
                }
            }
        }

        // maxExclusive
        boolean needCheckBase = true;
        if ((presentFacet & FACET_MAXEXCLUSIVE) != 0) {
            if ((allowedFacet & FACET_MAXEXCLUSIVE) == 0) {
                reportError("cos-applicable-facets", new Object[]{"maxExclusive", fTypeName});
            } else {
                try {
                    fMaxExclusive = fBase.getActualValue(facets.maxExclusive, tempInfo);
                    fFacetsDefined |= FACET_MAXEXCLUSIVE;
                } catch (InvalidDatatypeValueException ide) {
                    reportError(ide.getKey(), ide.getArgs());
                    reportError("FacetValueFromBase", new Object[]{fTypeName, facets.maxExclusive,
                            "maxExclusive", fBase.getName()});
                }

                // check against fixed value in base
                if (((fBase.fFacetsDefined & FACET_MAXEXCLUSIVE) != 0)) {
                    result = fDVs[fValidationDV].compare(fMaxExclusive, fBase.fMaxExclusive);
                    if ((fBase.fFixedFacet & FACET_MAXEXCLUSIVE) != 0 && result != 0) {
                        reportError( "FixedFacetValue", new Object[]{"maxExclusive", facets.maxExclusive, fBase.fMaxExclusive, fTypeName});
                    }
                    if (result == 0) {
                        needCheckBase = false;
                    }
                }
                // maxExclusive from base
                if (needCheckBase) {
                    try {
                        fBase.validate(tempInfo);
                    } catch (InvalidDatatypeValueException ide) {
                        reportError(ide.getKey(), ide.getArgs());
                        reportError("FacetValueFromBase", new Object[]{fTypeName, facets.maxExclusive,
                                "maxExclusive", fBase.getName()});
                    }
                }
                // If maxExclusive == base.maxExclusive, then we only need to check
                // maxExclusive <= base.maxInclusive
                else if (((fBase.fFacetsDefined & FACET_MAXINCLUSIVE) != 0)) {
                    if (fDVs[fValidationDV].compare(fMaxExclusive, fBase.fMaxInclusive) > 0) {
                        reportError( "maxExclusive-valid-restriction.2", new Object[]{facets.maxExclusive, fBase.fMaxInclusive});
                    }
                }
            }
        }
        // minExclusive
        needCheckBase = true;
        if ((presentFacet & FACET_MINEXCLUSIVE) != 0) {
            if ((allowedFacet & FACET_MINEXCLUSIVE) == 0) {
                reportError("cos-applicable-facets", new Object[]{"minExclusive", fTypeName});
            } else {
                try {
                    fMinExclusive = fBase.getActualValue(facets.minExclusive, tempInfo);
                    fFacetsDefined |= FACET_MINEXCLUSIVE;
                } catch (InvalidDatatypeValueException ide) {
                    reportError(ide.getKey(), ide.getArgs());
                    reportError("FacetValueFromBase", new Object[]{fTypeName, facets.minExclusive,
                            "minExclusive", fBase.getName()});
                }

                // check against fixed value in base
                if (((fBase.fFacetsDefined & FACET_MINEXCLUSIVE) != 0)) {
                    result = fDVs[fValidationDV].compare(fMinExclusive, fBase.fMinExclusive);
                    if ((fBase.fFixedFacet & FACET_MINEXCLUSIVE) != 0 && result != 0) {
                        reportError( "FixedFacetValue", new Object[]{"minExclusive", facets.minExclusive, fBase.fMinExclusive, fTypeName});
                    }
                    if (result == 0) {
                        needCheckBase = false;
                    }
                }
                // minExclusive from base
                if (needCheckBase) {
                    try {
                        fBase.validate(tempInfo);
                    } catch (InvalidDatatypeValueException ide) {
                        reportError(ide.getKey(), ide.getArgs());
                        reportError("FacetValueFromBase", new Object[]{fTypeName, facets.minExclusive,
                                "minExclusive", fBase.getName()});
                    }
                }
                // If minExclusive == base.minExclusive, then we only need to check
                // minExclusive >= base.minInclusive
                else if (((fBase.fFacetsDefined & FACET_MININCLUSIVE) != 0)) {
                    if (fDVs[fValidationDV].compare(fMinExclusive, fBase.fMinInclusive) < 0) {
                        reportError( "minExclusive-valid-restriction.3", new Object[]{facets.minExclusive, fBase.fMinInclusive});
                    }
                }
            }
        }
        // minInclusive
        if ((presentFacet & FACET_MININCLUSIVE) != 0) {
            if ((allowedFacet & FACET_MININCLUSIVE) == 0) {
                reportError("cos-applicable-facets", new Object[]{"minInclusive", fTypeName});
            } else {
                try {
                    fMinInclusive = fBase.getActualValue(facets.minInclusive, tempInfo);
                    fFacetsDefined |= FACET_MININCLUSIVE;
                } catch (InvalidDatatypeValueException ide) {
                    reportError(ide.getKey(), ide.getArgs());
                    reportError("FacetValueFromBase", new Object[]{fTypeName, facets.minInclusive,
                            "minInclusive", fBase.getName()});
                }

                // check against fixed value in base
                if (((fBase.fFacetsDefined & FACET_MININCLUSIVE) != 0)) {
                    if ((fBase.fFixedFacet & FACET_MININCLUSIVE) != 0) {
                        if (fDVs[fValidationDV].compare(fMinInclusive, fBase.fMinInclusive) != 0)
                            reportError( "FixedFacetValue", new Object[]{"minInclusive", facets.minInclusive, fBase.fMinInclusive, fTypeName});
                    }
                }
                // minInclusive from base
                try {
                    fBase.validate(tempInfo);
                } catch (InvalidDatatypeValueException ide) {
                    reportError(ide.getKey(), ide.getArgs());
                    reportError("FacetValueFromBase", new Object[]{fTypeName, facets.minInclusive,
                            "minInclusive", fBase.getName()});
                }
            }
        }

        // token type: internal use, so do less checking
        if (patternType != SPECIAL_PATTERN_NONE) {
            fPatternType = patternType;
        }

        // step 2: check facets against each other: length, bounds
        if(fFacetsDefined != 0) {

            // check 4.3.8.c1 error: maxInclusive + maxExclusive
            if (((fFacetsDefined & FACET_MAXEXCLUSIVE) != 0) && ((fFacetsDefined & FACET_MAXINCLUSIVE) != 0)) {
                reportError( "maxInclusive-maxExclusive", new Object[]{fMaxInclusive, fMaxExclusive, fTypeName});
            }

            // check 4.3.9.c1 error: minInclusive + minExclusive
            if (((fFacetsDefined & FACET_MINEXCLUSIVE) != 0) && ((fFacetsDefined & FACET_MININCLUSIVE) != 0)) {
                reportError("minInclusive-minExclusive", new Object[]{fMinInclusive, fMinExclusive, fTypeName});
            }

            // check 4.3.7.c1 must: minInclusive <= maxInclusive
            if (((fFacetsDefined &  FACET_MAXINCLUSIVE) != 0) && ((fFacetsDefined & FACET_MININCLUSIVE) != 0)) {
                result = fDVs[fValidationDV].compare(fMinInclusive, fMaxInclusive);
                if (result != -1 && result != 0)
                    reportError("minInclusive-less-than-equal-to-maxInclusive", new Object[]{fMinInclusive, fMaxInclusive, fTypeName});
            }

            // check 4.3.8.c2 must: minExclusive <= maxExclusive ??? minExclusive < maxExclusive
            if (((fFacetsDefined & FACET_MAXEXCLUSIVE) != 0) && ((fFacetsDefined & FACET_MINEXCLUSIVE) != 0)) {
                result = fDVs[fValidationDV].compare(fMinExclusive, fMaxExclusive);
                if (result != -1 && result != 0)
                    reportError( "minExclusive-less-than-equal-to-maxExclusive", new Object[]{fMinExclusive, fMaxExclusive, fTypeName});
            }

            // check 4.3.9.c2 must: minExclusive < maxInclusive
            if (((fFacetsDefined & FACET_MAXINCLUSIVE) != 0) && ((fFacetsDefined & FACET_MINEXCLUSIVE) != 0)) {
                if (fDVs[fValidationDV].compare(fMinExclusive, fMaxInclusive) != -1)
                    reportError( "minExclusive-less-than-maxInclusive", new Object[]{fMinExclusive, fMaxInclusive, fTypeName});
            }

            // check 4.3.10.c1 must: minInclusive < maxExclusive
            if (((fFacetsDefined & FACET_MAXEXCLUSIVE) != 0) && ((fFacetsDefined & FACET_MININCLUSIVE) != 0)) {
                if (fDVs[fValidationDV].compare(fMinInclusive, fMaxExclusive) != -1)
                    reportError( "minInclusive-less-than-maxExclusive", new Object[]{fMinInclusive, fMaxExclusive, fTypeName});
            }

            // check 4.3.6.c1 error:
            // (whiteSpace = preserve || whiteSpace = replace) && fBase.whiteSpace = collapese or
            // whiteSpace = preserve && fBase.whiteSpace = replace

            if ( (fFacetsDefined & FACET_WHITESPACE) != 0 && (fBase.fFacetsDefined & FACET_WHITESPACE) != 0 ){
                if ( (fBase.fFixedFacet & FACET_WHITESPACE) != 0 &&  fWhiteSpace != fBase.fWhiteSpace ) {
                    reportError( "FixedFacetValue", new Object[]{"whiteSpace", whiteSpaceValue(fWhiteSpace), whiteSpaceValue(fBase.fWhiteSpace), fTypeName});
                }

                if ( fWhiteSpace == WS_PRESERVE &&  fBase.fWhiteSpace == WS_COLLAPSE ){
                    reportError( "whiteSpace-valid-restriction.1", new Object[]{fTypeName, "preserve"});
                }
                if ( fWhiteSpace == WS_REPLACE &&  fBase.fWhiteSpace == WS_COLLAPSE ){
                    reportError( "whiteSpace-valid-restriction.1", new Object[]{fTypeName, "replace"});
                }
                if ( fWhiteSpace == WS_PRESERVE &&  fBase.fWhiteSpace == WS_REPLACE ){
                    reportError( "whiteSpace-valid-restriction.2", new Object[]{fTypeName});
                }
            }
        }//fFacetsDefined != null

        // step 4: inherit other facets from base (including fTokeyType)

        // inherit pattern
        if ( (fBase.fFacetsDefined & FACET_PATTERN) != 0 ) {
            if ((fFacetsDefined & FACET_PATTERN) == 0) {
                fFacetsDefined |= FACET_PATTERN;
                fPattern = fBase.fPattern;
                fPatternStr = fBase.fPatternStr;
            }
            else {
                for (int i = fBase.fPattern.size()-1; i >= 0; --i) {
                    fPattern.addElement(fBase.fPattern.elementAt(i));
                    fPatternStr.addElement(fBase.fPatternStr.elementAt(i));
                }
            }
        }
        // inherit whiteSpace
        if ( (fFacetsDefined & FACET_WHITESPACE) == 0 &&  (fBase.fFacetsDefined & FACET_WHITESPACE) != 0 ) {
            fFacetsDefined |= FACET_WHITESPACE;
            fWhiteSpace = fBase.fWhiteSpace;
        }
        // inherit maxExclusive
        if ((( fBase.fFacetsDefined & FACET_MAXEXCLUSIVE) != 0) &&
                !((fFacetsDefined & FACET_MAXEXCLUSIVE) != 0) && !((fFacetsDefined & FACET_MAXINCLUSIVE) != 0)) {
            fFacetsDefined |= FACET_MAXEXCLUSIVE;
            fMaxExclusive = fBase.fMaxExclusive;
        }
        // inherit maxInclusive
        if ((( fBase.fFacetsDefined & FACET_MAXINCLUSIVE) != 0) &&
                !((fFacetsDefined & FACET_MAXEXCLUSIVE) != 0) && !((fFacetsDefined & FACET_MAXINCLUSIVE) != 0)) {
            fFacetsDefined |= FACET_MAXINCLUSIVE;
            fMaxInclusive = fBase.fMaxInclusive;
        }
        // inherit minExclusive
        if ((( fBase.fFacetsDefined & FACET_MINEXCLUSIVE) != 0) &&
                !((fFacetsDefined & FACET_MINEXCLUSIVE) != 0) && !((fFacetsDefined & FACET_MININCLUSIVE) != 0)) {
            fFacetsDefined |= FACET_MINEXCLUSIVE;
            fMinExclusive = fBase.fMinExclusive;
        }
        // inherit minExclusive
        if ((( fBase.fFacetsDefined & FACET_MININCLUSIVE) != 0) &&
                !((fFacetsDefined & FACET_MINEXCLUSIVE) != 0) && !((fFacetsDefined & FACET_MININCLUSIVE) != 0)) {
            fFacetsDefined |= FACET_MININCLUSIVE;
            fMinInclusive = fBase.fMinInclusive;
        }
        //inherit tokeytype
        if ((fPatternType == SPECIAL_PATTERN_NONE ) && (fBase.fPatternType != SPECIAL_PATTERN_NONE)) {
            fPatternType = fBase.fPatternType ;
        }

        // step 5: mark fixed values
        fFixedFacet |= fBase.fFixedFacet;

        //step 6: setting fundamental facets
        calcFundamentalFacets();

    } //applyFacets()

    /**
     * validate a value, and return the compiled form
     */
    @Override
    public Object validate(String content, ValidatedInfo validatedInfo) throws InvalidDatatypeValueException {
        if (validatedInfo == null)
            validatedInfo = new ValidatedInfo();

        // first normalize string value, and convert it to actual value
        Object ob = getActualValue(content, validatedInfo);

        validate(validatedInfo);

        return ob;
    }

    /**
     * validate an actual value against this DV
     *
     * @param validatedInfo used to provide the actual value and member types
     */
    @Override
    public void validate(ValidatedInfo validatedInfo)
        throws InvalidDatatypeValueException {

        // then validate the actual value against the facets
        if (fFacetsDefined != 0 && fFacetsDefined != FACET_WHITESPACE) {
            checkFacets(validatedInfo);
        }
    }

    private void checkFacets(ValidatedInfo validatedInfo) throws InvalidDatatypeValueException {

        Object ob = validatedInfo.actualValue;
        String content = validatedInfo.normalizedValue;

        int compare;

        //maxinclusive
        if ( (fFacetsDefined & FACET_MAXINCLUSIVE) != 0 ) {
            compare = fDVs[fValidationDV].compare(ob, fMaxInclusive);
            if (compare != -1 && compare != 0) {
                throw new InvalidDatatypeValueException("cvc-maxInclusive-valid",
                        new Object[] {content, fMaxInclusive, fTypeName});
            }
        }

        //maxExclusive
        if ( (fFacetsDefined & FACET_MAXEXCLUSIVE) != 0 ) {
            compare = fDVs[fValidationDV].compare(ob, fMaxExclusive );
            if (compare != -1) {
                throw new InvalidDatatypeValueException("cvc-maxExclusive-valid",
                        new Object[] {content, fMaxExclusive, fTypeName});
            }
        }

        //minInclusive
        if ( (fFacetsDefined & FACET_MININCLUSIVE) != 0 ) {
            compare = fDVs[fValidationDV].compare(ob, fMinInclusive);
            if (compare != 1 && compare != 0) {
                throw new InvalidDatatypeValueException("cvc-minInclusive-valid",
                        new Object[] {content, fMinInclusive, fTypeName});
            }
        }

        //minExclusive
        if ( (fFacetsDefined & FACET_MINEXCLUSIVE) != 0 ) {
            compare = fDVs[fValidationDV].compare(ob, fMinExclusive);
            if (compare != 1) {
                throw new InvalidDatatypeValueException("cvc-minExclusive-valid",
                        new Object[] {content, fMinExclusive, fTypeName});
            }
        }

    }

    //we can still return object for internal use.
    private Object getActualValue(Object content, ValidatedInfo validatedInfo)
    throws InvalidDatatypeValueException{

        String nvalue;
        nvalue = normalize(content, fWhiteSpace);
        if ( (fFacetsDefined & FACET_PATTERN ) != 0 ) {
            Pattern regex;
            for (int idx = fPattern.size()-1; idx >= 0; idx--) {
                regex = (Pattern)fPattern.elementAt(idx);
                if (!regex.matcher(nvalue).matches()) {
                    throw new InvalidDatatypeValueException("cvc-pattern-valid",
                            new Object[]{content,
                            fPatternStr.elementAt(idx),

                            fTypeName});
                }
            }
        }

        // validate special kinds of token, in place of old pattern matching
        if (fPatternType != SPECIAL_PATTERN_NONE) {

            boolean seenErr = false;
            if (fPatternType == SPECIAL_PATTERN_NMTOKEN) {
                // PATTERN "\\c+"
                seenErr = !XercesXMLChar.isValidNmtoken(nvalue);
            }
            else if (fPatternType == SPECIAL_PATTERN_NAME) {
                // PATTERN "\\i\\c*"
                seenErr = !XercesXMLChar.isValidName(nvalue);
            }
            else if (fPatternType == SPECIAL_PATTERN_NCNAME) {
                // PATTERN "[\\i-[:]][\\c-[:]]*"
                seenErr = !XercesXMLChar.isValidNCName(nvalue);
            }
            if (seenErr) {
                throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1",
                        new Object[]{nvalue, SPECIAL_PATTERN_STRING[fPatternType]});
            }
        }

        validatedInfo.normalizedValue = nvalue;
        Object avalue = fDVs[fValidationDV].getActualValue(nvalue);
        validatedInfo.actualValue = avalue;
        validatedInfo.actualValueType = fBuiltInKind;

        return avalue;
    }//getActualValue()

    @Override
    public boolean isEqual(Object value1, Object value2) {
        if (value1 == null) {
            return false;
        }
        return value1.equals(value2);
    }//isEqual()

    // normalize the string according to the whiteSpace facet
    public static String normalize(String content, short ws) {
        int len = content == null ? 0 : content.length();
        if (len == 0 || ws == WS_PRESERVE)
            return content;

        StringBuilder sb = new StringBuilder();
        if (ws == WS_REPLACE) {
            char ch;
            // when it's replace, just replace #x9, #xa, #xd by #x20
            for (int i = 0; i < len; i++) {
                ch = content.charAt(i);
                if (ch != 0x9 && ch != 0xa && ch != 0xd)
                    sb.append(ch);
                else
                    sb.append((char)0x20);
            }
        } else {
            char ch;
            int i;
            boolean isLeading = true;
            // when it's collapse
            for (i = 0; i < len; i++) {
                ch = content.charAt(i);
                // append real characters, so we passed leading ws
                if (ch != 0x9 && ch != 0xa && ch != 0xd && ch != 0x20) {
                    sb.append(ch);
                    isLeading = false;
                }
                else {
                    // for whitespaces, we skip all following ws
                    for (; i < len-1; i++) {
                        ch = content.charAt(i+1);
                        if (ch != 0x9 && ch != 0xa && ch != 0xd && ch != 0x20)
                            break;
                    }
                    // if it's not a leading or tailing ws, then append a space
                    if (i < len - 1 && !isLeading)
                        sb.append((char)0x20);
                }
            }
        }

        return sb.toString();
    }

    // normalize the string according to the whiteSpace facet
    protected String normalize(Object content, short ws) {
        if (content == null)
            return null;

        // If pattern is not defined, we can skip some of the normalization.
        // Otherwise we have to normalize the data for correct result of
        // pattern validation.
        if ( (fFacetsDefined & FACET_PATTERN ) == 0 ) {
            short norm_type = fDVNormalizeType[fValidationDV];
            if (norm_type == NORMALIZE_NONE) {
                return content.toString();
            }
            else if (norm_type == NORMALIZE_TRIM) {
                return XercesXMLChar.trim(content.toString());
            }
        }

        if (!(content instanceof StringBuilder)) {
            String strContent = content.toString();
            return normalize(strContent, ws);
        }

        StringBuilder sb = (StringBuilder)content;
        int len = sb.length();
        if (len == 0)
            return "";
        if (ws == WS_PRESERVE)
            return sb.toString();

        if (ws == WS_REPLACE) {
            char ch;
            // when it's replace, just replace #x9, #xa, #xd by #x20
            for (int i = 0; i < len; i++) {
                ch = sb.charAt(i);
                if (ch == 0x9 || ch == 0xa || ch == 0xd)
                    sb.setCharAt(i, (char)0x20);
            }
        } else {
            char ch;
            int i, j = 0;
            boolean isLeading = true;
            // when it's collapse
            for (i = 0; i < len; i++) {
                ch = sb.charAt(i);
                // append real characters, so we passed leading ws
                if (ch != 0x9 && ch != 0xa && ch != 0xd && ch != 0x20) {
                    sb.setCharAt(j++, ch);
                    isLeading = false;
                }
                else {
                    // for whitespaces, we skip all following ws
                    for (; i < len-1; i++) {
                        ch = sb.charAt(i+1);
                        if (ch != 0x9 && ch != 0xa && ch != 0xd && ch != 0x20)
                            break;
                    }
                    // if it's not a leading or tailing ws, then append a space
                    if (i < len - 1 && !isLeading)
                        sb.setCharAt(j++, (char)0x20);
                }
            }
            sb.setLength(j);
        }

        return sb.toString();
    }

    void reportError(String key, Object[] args) throws InvalidDatatypeFacetException {
        throw new InvalidDatatypeFacetException(key, args);
    }


    private String whiteSpaceValue(short ws){
        return WS_FACET_STRING[ws];
    }

    private void calcFundamentalFacets() {
        setOrdered();
        setNumeric();
        setCardinality();
    }

    private void setOrdered(){
        // When {variety} is atomic, {value} is inherited from {value} of {base type definition}. For all "primitive" types {value} is as specified in the table in Fundamental Facets (C.1).
        this.fOrdered = fBase.fOrdered;
    }//setOrdered

    private void setNumeric(){
        this.fNumeric = fBase.fNumeric;
    }//setNumeric

    private void setCardinality(){
        if(fBase.fFinite){
            this.fFinite = true;
        }
        else {// (!fBase.fFinite)
            if( (((this.fFacetsDefined & FACET_MININCLUSIVE) != 0 ) || ((this.fFacetsDefined & FACET_MINEXCLUSIVE) != 0 ))
                    && (((this.fFacetsDefined & FACET_MAXINCLUSIVE) != 0 ) || ((this.fFacetsDefined & FACET_MAXEXCLUSIVE) != 0 )) ){
                this.fFinite = false;
            }
            else{
                this.fFinite = false;
            }
        }
    }//setCardinality

    static final XSSimpleTypeDecl fAnySimpleType = new XSSimpleTypeDecl(null, "anySimpleType", DV_ANYSIMPLETYPE, ORDERED_FALSE, true, false, true, XSConstants.ANYSIMPLETYPE_DT);

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.fTargetNamespace+"," +this.fTypeName;
    }
} // class XSSimpleTypeDecl

