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

package org.apache.jena.datatypes.xsd;

import java.math.BigDecimal ;
import java.math.BigInteger ;
import java.net.URI ;

import org.apache.jena.datatypes.BaseDatatype ;
import org.apache.jena.datatypes.DatatypeFormatException ;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.TypeMapper ;
import org.apache.jena.datatypes.xsd.impl.* ;
import org.apache.jena.ext.xerces.impl.dv.*;
import org.apache.jena.ext.xerces.impl.dv.util.Base64;
import org.apache.jena.ext.xerces.impl.dv.util.HexBin;
import org.apache.jena.ext.xerces.impl.dv.xs.DecimalDV;
import org.apache.jena.ext.xerces.xs.XSConstants;
import org.apache.jena.ext.xerces.xs.XSTypeDefinition;
import org.apache.jena.graph.impl.LiteralLabel ;

/**
 * Representation of an XSD datatype based on the Xerces-2
 * XSD implementation.
 */
public class XSDDatatype extends BaseDatatype {

//=======================================================================
// Global statics - define single instance for each import XSD type

    /** The xsd namespace */
    public static final String XSD = "http://www.w3.org/2001/XMLSchema";

    /** Datatype representing xsd:float */
    public static final XSDDatatype XSDfloat = new XSDFloat("float", Float.class);

    /** Datatype representing xsd:double */
    public static final XSDDatatype XSDdouble = new XSDDouble("double", Double.class);

    /** Datatype representing xsd:int */
    public static final XSDDatatype XSDint = new XSDBaseNumericType("int", Integer.class);

    /** Datatype representing xsd:long */
    public static final XSDDatatype XSDlong = new XSDBaseNumericType("long", Long.class);

    /** Datatype representing xsd:short */
    public static final XSDDatatype XSDshort = new XSDBaseNumericType("short", Short.class);

    /** Datatype representing xsd:byte */
    public static final XSDDatatype XSDbyte = new XSDByteType("byte", Byte.class);

    /** Datatype representing xsd:unsignedByte */
    public static final XSDDatatype XSDunsignedByte = new XSDBaseNumericType("unsignedByte");

    /** Datatype representing xsd:unsignedShort */
    public static final XSDDatatype XSDunsignedShort = new XSDBaseNumericType("unsignedShort");

    /** Datatype representing xsd:unsignedInt */
    public static final XSDDatatype XSDunsignedInt = new XSDBaseNumericType("unsignedInt");

    /** Datatype representing xsd:unsignedLong */
    public static final XSDDatatype XSDunsignedLong = new XSDBaseNumericType("unsignedLong");

    /** Datatype representing xsd:decimal */
    public static final XSDDatatype XSDdecimal = new XSDBaseNumericType("decimal", BigDecimal.class);

    /* * Datatype representing xsd:precisionDecimal https://www.w3.org/TR/xsd-precisionDecimal/ */
    // Not a derived type of xsd:decimal.
    //public static final XSDDatatype XSDprecisionDecimal = new XSDPRecisionDecimal("precisionDecimal", BigDecimal.class);

    /** Datatype representing xsd:integer */
    public static final XSDDatatype XSDinteger = new XSDBaseNumericType("integer", BigInteger.class);

    /** Datatype representing xsd:nonPositiveInteger */
    public static final XSDDatatype XSDnonPositiveInteger = new XSDBaseNumericType("nonPositiveInteger");

    /** Datatype representing xsd:nonNegativeInteger */
    public static final XSDDatatype XSDnonNegativeInteger = new XSDBaseNumericType("nonNegativeInteger");

    /** Datatype representing xsd:positiveInteger */
    public static final XSDDatatype XSDpositiveInteger = new XSDBaseNumericType("positiveInteger");

    /** Datatype representing xsd:negativeInteger */
    public static final XSDDatatype XSDnegativeInteger = new XSDBaseNumericType("negativeInteger");

    /** Datatype representing xsd:boolean */
    public static final XSDDatatype XSDboolean = new XSDDatatype("boolean", Boolean.class);

    /** Datatype representing xsd:string */
    public static final XSDDatatype XSDstring = new XSDBaseStringType("string", String.class);

    /** Datatype representing xsd:anyURI */
    public static final XSDDatatype XSDanyURI = new XSDPlainType("anyURI", URI.class);

    /** Datatype representing xsd:language */
    public static final XSDDatatype XSDlanguage = new XSDBaseStringType("language");

    /** Datatype representing xsd:normalizedString */
    public static final XSDDatatype XSDnormalizedString = new XSDBaseStringType("normalizedString", String.class);

    /** Datatype representing xsd:token */
    public static final XSDDatatype XSDtoken = new XSDBaseStringType("token");

    /** Datatype representing xsd:NMTOKEN */
    public static final XSDDatatype XSDNMTOKEN = new XSDBaseStringType("NMTOKEN");

    /** Datatype representing xsd:Name */
    public static final XSDDatatype XSDName = new XSDBaseStringType("Name");

    /** Datatype representing xsd:NCName */
    public static final XSDDatatype XSDNCName = new XSDBaseStringType("NCName");

    /** Datatype representing xsd:hexBinary */
    public static final XSDDatatype XSDhexBinary = new XSDhexBinary("hexBinary");

    /** Datatype representing xsd:base64Binary */
    public static final XSDDatatype XSDbase64Binary = new XSDbase64Binary("base64Binary");

    /** Datatype representing xsd:date */
    public static final XSDDatatype XSDdate = new XSDDateType("date");

    /** Datatype representing xsd:time */
    public static final XSDDatatype XSDtime = new XSDTimeType("time");

    /** Datatype representing xsd:dateTime */
    public static final XSDDatatype XSDdateTime = new XSDDateTimeType("dateTime");

    /** Datatype representing xsd:dateTimeStamp */
    public static final XSDDatatype XSDdateTimeStamp = new XSDDateTimeStampType("dateTimeStamp");

    /** Datatype representing xsd:duration */
    public static final XSDDatatype XSDduration = new XSDDurationType();

    /** Datatype representing xsd:dayTimeDration */
    public static final XSDDatatype XSDdayTimeDuration = new XSDDayTimeDurationType();

    /** Datatype representing xsd:yearMonthDuration */
    public static final XSDDatatype XSDyearMonthDuration = new XSDYearMonthDurationType();

    /** Datatype representing xsd:gDay */
    public static final XSDDatatype XSDgDay = new XSDDayType("gDay");

    /** Datatype representing xsd:gMonth */
    public static final XSDDatatype XSDgMonth = new XSDMonthType("gMonth");

    /** Datatype representing xsd:gYear */
    public static final XSDDatatype XSDgYear = new XSDYearType("gYear");

    /** Datatype representing xsd:gYearMonth */
    public static final XSDDatatype XSDgYearMonth = new XSDYearMonthType("gYearMonth");

    /** Datatype representing xsd:gMonthDay */
    public static final XSDDatatype XSDgMonthDay = new XSDMonthDayType("gMonthDay");

//=======================================================================
// local variables

    /** the Xerces internal type declaration */
    XSSimpleType typeDeclaration;

    /** the corresponding java primitive class, if any */
    protected Class<?> javaClass = null;

    /** Used to access the values and facets of any of the decimal numeric types */
    static final DecimalDV decimalDV = new DecimalDV();

//=======================================================================
// Methods

    /**
     * Constructor.
     * @param typeName the name of the XSD type to be instantiated, this is
     * used to lookup a type definition from the Xerces schema factory.
     */
    public XSDDatatype(String typeName) {
        super("");
        typeDeclaration = SchemaDVFactory.getInstance().getBuiltInType(typeName);
        uri = typeDeclaration.getNamespace() + "#" + typeDeclaration.getName();
    }

    /**
     * Constructor.
     * @param typeName the name of the XSD type to be instantiated, this is
     * used to lookup a type definition from the Xerces schema factory.
     * @param javaClass the java class for which this xsd type is to be
     * treated as the canonical representation
     */
    public XSDDatatype(String typeName, Class<?> javaClass) {
        this(typeName);
        this.javaClass = javaClass;
    }

    /**
     * Constructor used when loading in external user defined XSD types via
     *
     * @param xstype the XSSimpleType definition to be wrapped
     * @param namespace the namespace for the type (used because the grammar loading doesn't seem to keep that)
     */
    protected XSDDatatype(XSSimpleType xstype, String namespace) {
        super("");
        typeDeclaration = xstype;
        this.uri = namespace + "#" + typeDeclaration.getName();
    }

    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    @Override
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        try {
            ValidatedInfo resultInfo = new ValidatedInfo();
            typeDeclaration.validate(lexicalForm, resultInfo);
            return convertValidatedDataValue(resultInfo);
        } catch (InvalidDatatypeValueException e) {
            throw new DatatypeFormatException(lexicalForm, this, "during parse -" + e);
        }
    }

    /**
     * Convert a value of this datatype out
     * to lexical form.
     */
    @Override
    public String unparse(Object value) {
        return value.toString();
    }

    /**
     * Compares two instances of values of the given datatype.
     */
    @Override
    public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
       return typeDeclaration.isEqual(value1.getValue(), value2.getValue());
    }

    /**
     * If this datatype is used as the cannonical representation
     * for a particular java datatype then return that java type,
     * otherwise returns null.
     */
    @Override
    public Class< ? > getJavaClass() {
        return javaClass;
    }

    /**
     * Returns the Xerces datatype representation for this type, this
     * is an XSSimpleType, in fact an XSSimpleTypeDecl.
     */
    @Override
    public Object extendedTypeDefinition() {
        return typeDeclaration;
    }

    /**
     * Convert a validated xerces data value into the corresponding java data value.
     * This function is currently the most blatently xerces-version dependent part
     * of this subsystem. In many cases it also involves reparsing data which has
     * already been parsed as part of the validation.
     *
     * @param validatedInfo a fully populated Xerces data validation context
     * @return the appropriate java wrapper type
     */
    Object convertValidatedDataValue(ValidatedInfo validatedInfo) throws DatatypeFormatException {
        switch (validatedInfo.actualValueType) {
            case XSConstants.BASE64BINARY_DT:
                byte[] decoded = Base64.decode(validatedInfo.normalizedValue);
                return (decoded);

            case XSConstants.BOOLEAN_DT:
                return validatedInfo.actualValue;

            case XSConstants.HEXBINARY_DT:
                decoded = HexBin.decode(validatedInfo.normalizedValue);
                return (decoded);

            case XSConstants.UNSIGNEDSHORT_DT:
            case XSConstants.INT_DT:
                return Integer.valueOf(trimPlus(validatedInfo.normalizedValue));

            case XSConstants.UNSIGNEDINT_DT:
            case XSConstants.LONG_DT:
                return suitableInteger( trimPlus(validatedInfo.normalizedValue) );

            case XSConstants.UNSIGNEDBYTE_DT:
            case XSConstants.SHORT_DT:
            case XSConstants.BYTE_DT:
                return Integer.valueOf(trimPlus(validatedInfo.normalizedValue));

            case XSConstants.UNSIGNEDLONG_DT:
            case XSConstants.INTEGER_DT:
            case XSConstants.NONNEGATIVEINTEGER_DT:
            case XSConstants.NONPOSITIVEINTEGER_DT:
            case XSConstants.POSITIVEINTEGER_DT:
            case XSConstants.NEGATIVEINTEGER_DT:
            case XSConstants.DECIMAL_DT:
                Object xsdValue = validatedInfo.actualValue;
                if (decimalDV.getTotalDigits(xsdValue) == 0) {
                    return Integer.valueOf(0);
                }
                if (decimalDV.getFractionDigits(xsdValue) >= 1) {
                    BigDecimal value =  new BigDecimal(trimPlus(validatedInfo.normalizedValue));
                    return XSDdecimal.cannonicalise( value );
                }
                // Can have 0 fractionDigits but still have a trailing .000
                String lexical = trimPlus(validatedInfo.normalizedValue);
                int dotx = lexical.indexOf('.');
                if (dotx != -1) {
                    lexical = lexical.substring(0, dotx);
                }
                if (decimalDV.getTotalDigits(xsdValue) > 18) {
                    return new BigInteger(lexical);
                } else {
                    return suitableInteger( lexical );
                }

            default:
                return parseValidated(validatedInfo.normalizedValue);
        }
    }

    /**
     	@param lexical
     	@return Number
    */
    protected Number suitableInteger( String lexical )
        {
        long number = Long.parseLong( lexical );
        return suitableInteger( number );
        }

    /**
     	@param number
     	@return Number
    */
    protected static Number suitableInteger( long number )
        {
        if (number > Integer.MAX_VALUE || number < Integer.MIN_VALUE)
            return Long.valueOf( number );
        else
            return Integer.valueOf( (int) number );
        }

    /**
     * Parse a validated lexical form. Subclasses which use the default
     * parse implementation and are not convered by the explicit convertValidatedData
     * cases should override this.
     */
    public Object parseValidated(String lexical) {
        return lexical;
    }

    /**
     * Test whether the given LiteralLabel is a valid instance
     * of this datatype. This takes into account typing information
     * as well as lexical form - for example an xsd:string is
     * never considered valid as an xsd:integer (even if it is
     * lexically legal like "1").
     */
    @Override
    public boolean isValidLiteral(LiteralLabel lit) {
        return isBaseTypeCompatible(lit) && isValid(lit.getLexicalForm());
    }

    /**
     * Test if the given typed value is in the right partition of the XSD type space.
     * If this test passes then if the typed value has a legal lexical form for
     * this type then it is a legal instance.
     */
    public boolean isBaseTypeCompatible(LiteralLabel lit) {
        XSTypeDefinition base = getFoundingType();
        RDFDatatype litDT = lit.getDatatype();
        if (litDT instanceof XSDDatatype) {
            XSTypeDefinition litBase = ((XSDDatatype)litDT).getFoundingType();
            return base.equals(litBase);

        } else if (litDT == null && lit.language().equals("")) {
            // Special RDF case, a plain literal is type compatible with and xsd:string-based type
            return base.equals(XSDstring.typeDeclaration);
        } else {
            return false;
        }
    }

    /**
     * Return the most specific type below xsd:anySimpleType that this type is derived from.
     */
    private XSTypeDefinition getFoundingType() {
        XSTypeDefinition founding = typeDeclaration;
        XSTypeDefinition parent = founding.getBaseType();
        if ( parent == null )
            // it is xsd:anySimpleType
            return founding;
        while (parent.getBaseType() != null) {
            founding = parent;
            parent = parent.getBaseType();
        }
        return founding;
    }

    /**
     * Helper function to return the substring of a validated number string
     * omitting any leading + sign.
     */
    public static String trimPlus(String str) {
        int i = str.indexOf('+');
        if (i == -1) {
            return str;
        } else {
            return str.substring(i+1);
        }
    }

    /**
     * Add all of the XSD pre-defined simple types to the given
     * type mapper registry.
     */
    public static void loadXSDSimpleTypes(TypeMapper tm) {

        // There are 39 XSD atomic datatypes (RDF 1.2)
        //   excluding xsd:precisionDecimal -- not part of XSD 1.1 datatypes, defined in a separate document
        //   excluding xsd:anySimpleType and xsd:anyAtomicType -- "Special types"

        tm.registerDatatype(XSDdecimal);
        tm.registerDatatype(XSDinteger);
        tm.registerDatatype(XSDnonPositiveInteger);
        tm.registerDatatype(XSDnonNegativeInteger);
        tm.registerDatatype(XSDpositiveInteger);
        tm.registerDatatype(XSDnegativeInteger);

        tm.registerDatatype(XSDbyte);
        tm.registerDatatype(XSDunsignedByte);
        tm.registerDatatype(XSDdouble);
        tm.registerDatatype(XSDfloat);
        tm.registerDatatype(XSDlong);
        tm.registerDatatype(XSDunsignedInt);
        tm.registerDatatype(XSDunsignedShort);
        tm.registerDatatype(XSDunsignedLong);
        tm.registerDatatype(XSDint);
        tm.registerDatatype(XSDshort);

        tm.registerDatatype(XSDboolean);
        tm.registerDatatype(XSDbase64Binary);
        tm.registerDatatype(XSDhexBinary);

        tm.registerDatatype(XSDdate);
        tm.registerDatatype(XSDtime);
        tm.registerDatatype(XSDdateTime);
        tm.registerDatatype(XSDdateTimeStamp);
        tm.registerDatatype(XSDduration);
        tm.registerDatatype(XSDyearMonthDuration);
        tm.registerDatatype(XSDdayTimeDuration) ;
        tm.registerDatatype(XSDgYearMonth);
        tm.registerDatatype(XSDgMonthDay);
        tm.registerDatatype(XSDgMonth);
        tm.registerDatatype(XSDgDay);
        tm.registerDatatype(XSDgYear);

        tm.registerDatatype(XSDnormalizedString);
        tm.registerDatatype(XSDstring);
        tm.registerDatatype(XSDanyURI);

        tm.registerDatatype(XSDtoken);
        tm.registerDatatype(XSDName);
        tm.registerDatatype(XSDlanguage);

        tm.registerDatatype(XSDNMTOKEN);
        tm.registerDatatype(XSDNCName);
        }

    /**
     * Generic XML Schema datatype (outside the xsd: namespace)
     * <p>
     * Datatype template that adapts any response back from Xerces type parsing
     * to an appropriate java representation. This is primarily used in creating
     * user defined types - the built in types have a fixed mapping.
     */
    public static class XSDGenericType extends XSDDatatype {

        /**
         * Hidden constructor used when loading in external user defined XSD
         * types
         *
         * @param xstype
         *            the XSSimpleType definition to be wrapped
         * @param namespace
         *            the namespace for the type (used because the grammar
         *            loading doesn't seem to keep that)
         */
        XSDGenericType(XSSimpleType xstype, String namespace) {
            super(xstype, namespace);
        }
    }
}
