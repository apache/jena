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

package org.apache.jena.graph.impl;

import java.util.Arrays;
import java.util.Locale ;
import java.util.Objects ;

import org.apache.jena.atlas.lib.EscapeStr;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.RDFjson;
import org.apache.jena.datatypes.xsd.impl.XMLLiteralType;
import org.apache.jena.shared.JenaException ;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.JenaParameters ;
import org.apache.jena.vocabulary.RDF ;

/**
 * Represents the "contents" of a Node_Literal.
 * These contents comprise a lexical form, an optional language tag,
 * and optional datatype structure and a value.
 */
final public class LiteralLabel {

    /**
     * The lexical form of the literal.
     */
    private String lexicalForm;

    /**
     * The language tag. The empty string is not valid; {@code ""} is used to ensure this field is set.
     */
    private String lang;

    /**
     * The type of the literal. A null type indicates a classic "plain" literal.
     * The type of a literal is fixed when it is created.
     */
    private RDFDatatype dtype;

    /**
     * The value form of the literal. It will be null only if the value
     * has not been parsed or if it is an illegal value.
     */
    private Object value;

    private enum ValueMode { EAGER , LAZY }
    // LAZY does not completely pass the test suite - the point where bad literals
    // cause exceptions has changed.
    // Whether this is the fact the tests are over sensitive or there is going to be
    // unexpected behaviour needs investigation.
    private static ValueMode valueMode = ValueMode.EAGER;

    /**
     * Indicates whether this is literal has a valid lexical form for the datatype.
     */
    private boolean wellformed = true;

    private Exception exception = null;

    private final int hash;

    //=======================================================================
    // Constructors

    // -- LiteralLabel by RDF term.
    // The value is not checked as being valid until it is asked for (getValue).

    /**
     * Build a LiteralLabel with lexical form and datatype.
     * The validity of the lexical form as a value is not checked.
     *
     * @param lex the lexical form of the literal
     * @param dtype the type of the literal
     */
    LiteralLabel(String lex, RDFDatatype dtype) {
        this(lex, "", dtype) ;
    }

    /**
     * Build a LiteralLabel with lexical form, lang tag and datatype.
     * The validity of the lexical form a a value is not checked.
     *
     * @param lex the lexical form of the literal
     * @param lang the optional language tag, only relevant for rdf:rdfLangString
     * @param dtype the type of the literal
     */
    /*package*/ LiteralLabel(String lex, String lang, RDFDatatype dtype) {
        this.lexicalForm = lex;
        this.dtype = Objects.requireNonNull(dtype);
        this.lang = (lang == null ? "" : lang);
        hash = calcHashCode();
        if ( valueMode == ValueMode.EAGER ) {
            this.wellformed = setValue(lex, dtype);
            dtype = normalize(value, dtype);
        } else
            // Lazy value calculation.
            value = null;
    }

    /**
     * Build a typed literal label from its value form using
     * whatever datatype is currently registered as the default
     * representation for this java class.
     * @param value the literal value to encapsulate
     */
    /*package*/ LiteralLabel( Object value ) {
        this(value, LiteralValue.datatypeForValueAny(value));
    }

    /**
     * Build a typed literal label from its value form.
     * If the value is a string, assume it is the intended lexical form to
     * align with (see {@link #LiteralLabel(String, RDFDatatype)}).
     *
     * @param value the value of the literal
     * @param dtype the type of the literal
     */
    /*package*/ LiteralLabel(Object value, RDFDatatype dtype) throws DatatypeFormatException {
        this.dtype = dtype;
        this.lang = "";
        if (value instanceof String) {
            // Treat as "lex ^^ datatype"
            String lex = (String)value;
            this.lexicalForm = lex;
            this.wellformed = setValue(lex, dtype);
            dtype = normalize(value, dtype);
            hash = calcHashCode();
            return;
        }
        // No lexical form yet.
        this.value = (dtype == null) ? value : dtype.cannonicalise( value );
        // This can change the datatype
        this.dtype = normalize(value, dtype);
        this.wellformed = this.dtype.isValidValue( value );

        // Eager
        if (JenaParameters.enableEagerLiteralValidation && !wellformed)
            throw new DatatypeFormatException(value.toString(),  dtype, "in literal creation");

        this.lexicalForm = (dtype == null ? value.toString() : dtype.unparse(value));
        hash = calcHashCode();
    }

    /**
     * Internal function to set the object value from the lexical form.
     * Requires datatype to be set. Return true if it succeeded else false.
     * @throws DatatypeFormatException if the value is ill-formed and
     * eager checking is on.
     */
    private boolean setValue(String lex, RDFDatatype dtype) throws DatatypeFormatException {
        try {
            value = dtype.parse(lex);
            return true;
        } catch (DatatypeFormatException e) {
            // Normally this parameter is false.
            if (JenaParameters.enableEagerLiteralValidation) {
                e.fillInStackTrace();
                throw e;
            }
            exception  = e;
            return false;
        }
    }

    // -- Thread safe delayed initialization at the cost of "volatile" incurred in getValueLazy()
    // Used by set-by-term.
    // set-by-value is always eager.

    private volatile Object value1 = null ;
    private static Object invalidValue = new Object();

    /** Does not return null - returns "invalidValue" */
    private Object getValueLazy() {
        // Eager value processing.
        if ( value != null )
            return value;
        if ( value1 != null ) {
            // value1 only goes from null to Object, and not back to null.
            return value1;
        }
        synchronized(this) {
            if ( value1 == null )
                value1 = calcValue(lexicalForm);
        }
        // Object assignment is atomic.
        // Synchronized ensured the value object is properly constructed.
        value = (value1 != invalidValue ) ? value1 : null;
        return value1;
    }

    private Object calcValue(String lex) {
        try {
            Object v = dtype.parse(lex);
            wellformed = true;
            dtype = dtype.normalizeSubType(v, dtype);
            return v;
        } catch (DatatypeFormatException e) {
            wellformed = false;
            return invalidValue;
        }
    }

    /**
     * Normalize the literal. If the value is narrower than the current data type
     * (e.g. value is xsd:date but the time is xsd:datetime) it will narrow
     * the type. If the type is narrower than the value then it may normalize
     * the value (e.g. set the mask of an XSDDateTime)
     */
    protected static RDFDatatype normalize(Object value, RDFDatatype datatype) {
        if (datatype != null && value != null) {
            return datatype.normalizeSubType(value, datatype);
        }
        return datatype;
    }

    //=======================================================================
    // Methods

    /**
     * Answer true iff this is a well-formed literal.
     */
    public boolean isWellFormed() {
        return dtype != null && isWellFormedRaw();
    }

    public boolean isWellFormedRaw() {
        if ( ! wellformed )
            return false;
        // Force initialization.
        getValueInternal();
        return wellformed;
    }

    public String toString(boolean quoting) {
        return toString(PrefixMapping.Standard, quoting);
    }

    public String toString(PrefixMapping pmap, boolean quoting) {
        StringBuilder b = new StringBuilder() ;
        if ( ! quoting && simpleLiteral() )
            return getLexicalForm();

        quoting = true;
        // Always quoted for language strings and datatypes (not xsd:string).
        if ( quoting )
            b.append('"') ;
        String elex = EscapeStr.stringEsc(getLexicalForm());
        b.append(elex) ;
        if ( quoting )
            b.append('"') ;

        if ( lang != null && !lang.equals("") )
            b.append("@").append(lang) ;
        else if ( ! dtype.equals(XSDDatatype.XSDstring) ) {
                String dtStr = (pmap != null)
                        ? PrefixMapping.Standard.shortForm(dtype.getURI())
                        : dtype.getURI();
                b.append("^^").append(dtStr);
        }
        return b.toString() ;
    }

    private boolean simpleLiteral() {
        return dtype.equals(XSDDatatype.XSDstring);
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * Answer the lexical form of this literal.
     */
    public String getLexicalForm() {
        return lexicalForm;
    }

    /**
     * Answer an object used to index this literal. This object must provide
     * {@link Object#equals} and {@link Object#hashCode} based on values, not object
     * instance identity.
     */
    public Object getIndexingValue() {
        if ( indexingValueIsSelf() )
            return this;
        if ( !lang.equals("") )
            return getLexicalForm() + "@" + lang.toLowerCase(Locale.ROOT);
        if ( wellformed ) {
            Object value = getValue();
            // JENA-1936
            // byte[] does not provide hashCode/equals based on the contents of the array.
            if ( value instanceof byte[] )
                return new ByteArray((byte[])value);
            return value;
        }
        return getLexicalForm();
    }

    /**
     * Return true for datatype with large values (XML, JSON) and
     * the value is the lexical form, the indexing value is this object.
     * Therefore getValueHashCode is the same as hashCode();
     */
    private boolean indexingValueIsSelf() {
        return dtype == XMLLiteralType.theXMLLiteralType ||
               dtype == RDFjson.rdfJSON ;
    }

    /**
     * {@code byte[]} wrapper that provides {@code hashCode} and {@code equals} based
     * on the value of the array. This assumes the {@code byte[]} is not changed
     * (which is the case for literals with binary value).
     */
    static class ByteArray {
        private int hashCode = 0 ;

        private final byte[] bytes;
        /*package*/ ByteArray(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public int hashCode() {
            if ( hashCode == 0 ) {
                final int prime = 31;
                int result = 1;
                hashCode = prime * result + Arrays.hashCode(bytes);
            }
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            ByteArray other = (ByteArray)obj;
            return Arrays.equals(bytes, other.bytes);
        }
    }

    /**
     * Answer the language associated with this literal (the empty string if there's
     * no language).
     */
    public String language() {
        return lang;
    }

    /**
     * Answer a suitable instance of a Java class representing this literal's value.
     * May throw an exception if the literal is ill-formed.
     */
    public Object getValue() throws DatatypeFormatException {
        Object val = getValueInternal();
        if (! wellformed )
            throw new DatatypeFormatException(lexicalForm, dtype, (Throwable)null);
        if ( val != null )
            // Value is good.
            return val;
        if ( ! JenaParameters.enableEagerLiteralValidation )
            throw new DatatypeFormatException();
        return null;
    }

    private Object getValueInternal() {
        Object v = getValueLazy();
        return (v == invalidValue ) ? null : v ;
    }

    /**
     * Answer the datatype of this literal, null if it is untyped.
     */
    public RDFDatatype getDatatype() {
		return dtype;
	}

    /**
     * Answer the datatype URI of this literal, null if it untyped.
     */
    public String getDatatypeURI() {
        if (dtype == null)
            return null;
        return dtype.getURI();
    }

    /**
     * Answer true iff this literal is syntactically equal to <code>other</code>.
     * Note: this is <i>not</i> <code>sameValueAs</code>.
     */
    @Override
    public boolean equals(Object other) {
        if ( this == other ) return true ;
        if (other == null || !(other instanceof LiteralLabel)) {
            return false;
        }
        LiteralLabel otherLiteral = (LiteralLabel) other;

        boolean typeEquals = Objects.equals(dtype, otherLiteral.getDatatype()) ;
        if ( !typeEquals )
            return false ;

        // Don't just use this.lexcialForm -- need to force delayed calculation from values.
        boolean lexEquals = Objects.equals(getLexicalForm(), otherLiteral.getLexicalForm());
        if ( ! lexEquals )
            return false ;

        boolean langEquals = Objects.equals(lang, otherLiteral.language()) ;
        if ( ! langEquals )
            return false ;
        // Ignore xml flag as it is calculated from the lexical form + datatype
        // Ignore value as lexical form + datatype -> value is a function.
        return true ;
    }

    /**
     * Answer true iff this literal represents the same (abstract) value as the other
     * one.
     */
    public boolean sameValueAs( LiteralLabel other ) {
        return sameValueAs(this, other) ;
    }
    /**
     * Two literal labels are the "same value" if they are the same string,
     * or same language string or same value-by-datatype or .equals (= Same RDF Term)
     * @param lit1
     * @param lit2
     * @return
     */
    private static boolean sameValueAs(LiteralLabel lit1, LiteralLabel lit2) {
        //return  lit1.sameValueAs(lit2) ;
        if ( lit1 == null )
            throw new NullPointerException() ;
        if ( lit2 == null )
            throw new NullPointerException() ;
        // Strings.
        if ( isStringValue(lit1) && isStringValue(lit2) )
            return lit1.getLexicalForm().equals(lit2.getLexicalForm()) ;

        if ( isStringValue(lit1) ) return false ;
        if ( isStringValue(lit2) ) return false ;

        // Language tag strings
        if ( isLangString(lit1) && isLangString(lit2) ) {
            String lex1 = lit1.getLexicalForm() ;
            String lex2 = lit2.getLexicalForm() ;
            return lex1.equals(lex2) && lit1.language().equalsIgnoreCase(lit2.language()) ;
        }
        if ( isLangString(lit1) ) return false ;
        if ( isLangString(lit2) ) return false ;

        // Both not strings, not lang strings.
        // Datatype set.
        if ( lit1.isWellFormedRaw() && lit2.isWellFormedRaw() )
            // Both well-formed.
            return lit1.getDatatype().isEqual(lit1, lit2) ;
        if ( ! lit1.isWellFormedRaw() && ! lit2.isWellFormedRaw() )
            return lit1.equals(lit2) ;
        // One is well formed, the other is not.
        return false ;
    }

    /** Return true if the literal label is a string value (RDF 1.0 and RDF 1.1) */
    private static boolean isStringValue(LiteralLabel lit) {
        if ( lit.getDatatype() == null )
            // RDF 1.0
            return ! isLangString(lit) ;
        if ( lit.getDatatype().equals(XSDDatatype.XSDstring)  )
            return true;
        return false ;
    }

    /** Return true if the literal label is a language string. */
    private static boolean isLangString(LiteralLabel lit) {
        // Duplicate of Util.isLangString except for the additional consistency check.
        String lang = lit.language() ;
        if ( lang == null )
            return false ;
        // Check.
        if ( lang.equals("") )
            return false ;
        // This is an additional check.
        if ( ! Objects.equals(lit.getDatatype(), RDF.dtLangString) )
            throw new JenaException("Literal with language string which is not rdf:langString: "+lit) ;
        return true ;
    }

    private int calcHashCode() {
        return Objects.hash(lexicalForm, lang, dtype);
    }

    /**
     * Answer the hashcode of this literal, derived from its value if it's
     * well-formed and otherwise its lexical form.
     */
    @Override
    public int hashCode() {
        return hash ;
    }

    /**
     * Answer the default hash value, suitable for datatypes which have values which
     * support hashCode() naturally: it is derived from its value if it is
     * well-formed and otherwise from its lexical form.
     */
    public int getValueHashCode() {
        if ( indexingValueIsSelf() )
            return hashCode();
        Object v = getValueInternal();
        if ( ! wellformed )
            return hashCode();
        if ( ! wellformed )
            return hashCode();
        return v.hashCode();
    }
}
