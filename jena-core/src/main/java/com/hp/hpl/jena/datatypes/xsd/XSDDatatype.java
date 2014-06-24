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

package com.hp.hpl.jena.datatypes.xsd;

import java.io.Reader ;
import java.math.BigDecimal ;
import java.math.BigInteger ;
import java.net.URI;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.xerces.impl.dv.* ;
import org.apache.xerces.impl.dv.util.Base64 ;
import org.apache.xerces.impl.dv.util.HexBin ;
import org.apache.xerces.impl.dv.xs.DecimalDV ;
import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl ;
import org.apache.xerces.impl.validation.ValidationState ;
import org.apache.xerces.parsers.XMLGrammarPreparser ;
import org.apache.xerces.util.SymbolHash ;
import org.apache.xerces.xni.grammars.XMLGrammarDescription ;
import org.apache.xerces.xni.grammars.XSGrammar ;
import org.apache.xerces.xni.parser.XMLInputSource ;
import org.apache.xerces.xs.XSConstants ;
import org.apache.xerces.xs.XSNamedMap ;
import org.apache.xerces.xs.XSTypeDefinition ;

import com.hp.hpl.jena.datatypes.BaseDatatype ;
import com.hp.hpl.jena.datatypes.DatatypeFormatException ;
import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.TypeMapper ;
import com.hp.hpl.jena.datatypes.xsd.impl.* ;
import com.hp.hpl.jena.graph.impl.LiteralLabel ;

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

    /** Datatype representing xsd:normalizedString */
    public static final XSDDatatype XSDnormalizedString = new XSDBaseStringType("normalizedString", String.class);

    /** Datatype representing xsd:anyURI */
    // If you see this, remove commented lines.
    // Merely temporary during switch over and testing.
    //public static final XSDDatatype XSDanyURI = new XSDDatatype("anyURI", URI.class);
    public static final XSDDatatype XSDanyURI = new XSDPlainType("anyURI", URI.class);

    /** Datatype representing xsd:token */
    public static final XSDDatatype XSDtoken = new XSDBaseStringType("token");

    /** Datatype representing xsd:Name */
    public static final XSDDatatype XSDName = new XSDBaseStringType("Name");

    /** Datatype representing xsd:QName */
    // If you see this, remove commented lines.
    // Merely temporary during switch over and testing.
    // public static final XSDDatatype XSDQName = new XSDDatatype("QName");
    public static final XSDDatatype XSDQName = new XSDPlainType("QName");

    /** Datatype representing xsd:language */
    public static final XSDDatatype XSDlanguage = new XSDBaseStringType("language");

    /** Datatype representing xsd:NMTOKEN */
    public static final XSDDatatype XSDNMTOKEN = new XSDBaseStringType("NMTOKEN");

    /** Datatype representing xsd:ENTITY */
    public static final XSDDatatype XSDENTITY = new XSDBaseStringType("ENTITY");

    /** Datatype representing xsd:ID */
    public static final XSDDatatype XSDID = new XSDBaseStringType("ID");

    /** Datatype representing xsd:NCName */
    public static final XSDDatatype XSDNCName = new XSDBaseStringType("NCName");

    /** Datatype representing xsd:IDREF */
    // If you see this, remove commented lines.
    // Merely temporary during switch over and testing.
    //public static final XSDDatatype XSDIDREF = new XSDDatatype("IDREF");
    public static final XSDDatatype XSDIDREF = new XSDPlainType("IDREF");

    /** Datatype representing xsd:NOTATION */
    // If you see this, remove commented lines.
    // Merely temporary during switch over and testing.
    //public static final XSDDatatype XSDNOTATION = new XSDDatatype("NOTATION");
    public static final XSDDatatype XSDNOTATION = new XSDPlainType("NOTATION");

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

    /** Datatype representing xsd:duration */
    public static final XSDDatatype XSDduration = new XSDDurationType();

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

    // The following are list rather than simple types and are omitted for now

//    /** Datatype representing xsd:ENTITIES */
//    public static final XSDDatatype XSDENTITIES = new XSDBaseStringType("ENTITIES");
//
//    /** Datatype representing xsd:NMTOKENS */
//    public static final XSDDatatype XSDNMTOKENS = new XSDBaseStringType("NMTOKENS");
//
//    /** Datatype representing xsd:IDREFS */
//    public static final XSDDatatype XSDIDREFS = new XSDBaseStringType("IDREFS");

//=======================================================================
// local variables

    /** the Xerces internal type declaration */
    protected XSSimpleType typeDeclaration;

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
     * treated as the cannonical representation
     */
    public XSDDatatype(String typeName, Class<?> javaClass) {
        this(typeName);
        this.javaClass = javaClass;
    }

    /**
     * Constructor used when loading in external user defined XSD types -
     * should only be used by the internals but public scope because
     * the internals spread across multiple packages.
     *
     * @param xstype the XSSimpleType definition to be wrapped
     * @param namespace the namespace for the type (used because the grammar loading doesn't seem to keep that)
     */
    public XSDDatatype(XSSimpleType xstype, String namespace) {
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
            ValidationContext context = new ValidationState();
            ValidatedInfo resultInfo = new ValidatedInfo();
            Object result = typeDeclaration.validate(lexicalForm, context, resultInfo);
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
     * This ignores lang tags and defers to the equality function
     * defined by the Xerces package - to be checked.
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
     * Create and register a set of types specified in a user schema file.
     * We use the (illegal) DAML+OIL approach that the uriref of the type
     * is the url of the schema file with fragment ID corresponding the
     * the name of the type.
     *
     * @param uri the absolute uri of the schema file to be loaded
     * @param reader the Reader stream onto the file (useful if you wish to load a cached copy of the schema file)
     * @param encoding the encoding of the source file (can be null)
     * @param tm the type mapper into which to load the definitions
     * @return a List of strings giving the uri's of the newly defined datatypes
     * @throws DatatypeFormatException if there is a problem during load (not that we use Xerces
     * in default mode for load which may provide diagnostic output direct to stderr)
     */
    public static List<String> loadUserDefined(String uri, Reader reader, String encoding, TypeMapper tm) throws DatatypeFormatException {
        return loadUserDefined(new XMLInputSource(null, uri, uri, reader, encoding), tm);
    }

    /**
     * Create and register a set of types specified in a user schema file.
     * We use the (illegal) DAML+OIL approach that the uriref of the type
     * is the url of the schema file with fragment ID corresponding the
     * the name of the type.
     *
     * @param uri the absolute uri of the schema file to be loaded, this should be a resolvable URL
     * @param encoding the encoding of the source file (can be null)
     * @param tm the type mapper into which to load the definitions
     * @return a List of strings giving the uri's of the newly defined datatypes
     * @throws DatatypeFormatException if there is a problem during load (not that we use Xerces
     * in default mode for load which may provide diagnostic output direct to stderr)
     */
    public static List<String> loadUserDefined(String uri, String encoding, TypeMapper tm) throws DatatypeFormatException {
        return loadUserDefined(new XMLInputSource(null, uri, uri), tm);
    }

    /**
     * Internal implementation of loadUserDefined
     *
     * @param uri the absolute uri of the schema file to be loaded
     * @param reader the Reader stream onto the file (useful if you wish to load a cached copy of the schema file)
     * @param encoding the encoding of the source file (can be null)
     * @param tm the type mapper into which to load the definitions
     * @return a List of strings giving the uri's of the newly defined datatypes
     * @throws DatatypeFormatException if there is a problem during load (not that we use Xerces
     * in default mode for load which may provide diagnostic output direct to stderr)
     */
    private static List<String> loadUserDefined(XMLInputSource source, TypeMapper tm) throws DatatypeFormatException {
        XMLGrammarPreparser parser = new XMLGrammarPreparser();
        parser.registerPreparser(XMLGrammarDescription.XML_SCHEMA, null);
        try {
            XSGrammar xsg = (XSGrammar) parser.preparseGrammar(XMLGrammarDescription.XML_SCHEMA, source);
            org.apache.xerces.xs.XSModel xsm = xsg.toXSModel();
            XSNamedMap map = xsm.getComponents(XSTypeDefinition.SIMPLE_TYPE);
            int numDefs = map.getLength();
            ArrayList<String> names = new ArrayList<>(numDefs);
            for (int i = 0; i < numDefs; i++) {
                XSSimpleType xstype = (XSSimpleType) map.item(i);
                // Filter built in types - only needed for 2.6.0
                if ( ! XSD.equals(xstype.getNamespace()) ) {
                    //xstype.derivedFrom()
                    XSDDatatype definedType = new XSDGenericType(xstype, source.getSystemId());
                    tm.registerDatatype(definedType);
                    names.add(definedType.getURI());
                }
            }
            return names;
        } catch (Exception e) {
            e.printStackTrace();    // Temp
            throw new DatatypeFormatException(e.toString());
        }
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
    public Object convertValidatedDataValue(ValidatedInfo validatedInfo) throws DatatypeFormatException {
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
                    return new Integer(0);
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
            return new Long( number );
        else
            return new Integer( (int) number );
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
     * of this datatype. This takes into accound typing information
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
        tm.registerDatatype(new XSDDatatype("anySimpleType"));

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
        tm.registerDatatype(XSDduration);
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
        tm.registerDatatype(XSDQName);
        tm.registerDatatype(XSDNMTOKEN);
        tm.registerDatatype(XSDID);
        tm.registerDatatype(XSDENTITY);
        tm.registerDatatype(XSDNCName);
        tm.registerDatatype(XSDNOTATION);
        tm.registerDatatype(XSDIDREF);

//        tm.registerDatatype(XSDIDREFS);
//        tm.registerDatatype(XSDENTITIES);
//        tm.registerDatatype(XSDNMTOKENS);
    }

    // Temporary - used bootstrap the above initialization code
    public static void main(String[] args) {
        SymbolHash types = SchemaDVFactory.getInstance().getBuiltInTypes();
        int len = types.getLength();
        Object[] values = new Object[len];
        types.getValues(values, 0);
        for ( Object value : values )
        {
            if ( value instanceof XSSimpleTypeDecl )
            {
                XSSimpleTypeDecl decl = (XSSimpleTypeDecl) value;
                System.out.println( "tm.registerDatatype(new XSDDatatype(\"" + decl.getName() + "\"));" );
            }
            else
            {
                System.out.println( " - " + value );
            }
        }
    }

    public int getHashCode( byte [] bytes )
        {
        int length = bytes.length;
        return length == 0
            ? 0
            : (bytes[0] << 12) ^ (bytes[length / 2] << 6) ^ (bytes[length - 1]) ^ length
            ;
        }

}
