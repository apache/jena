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

import org.apache.jena.JenaRuntime ;
import org.apache.jena.datatypes.* ;
import org.apache.jena.datatypes.xsd.* ;
import org.apache.jena.datatypes.xsd.impl.* ;
import org.apache.jena.rdf.model.impl.Util ;
import org.apache.jena.shared.JenaException ;
import org.apache.jena.shared.impl.JenaParameters ;
import org.apache.jena.vocabulary.RDF ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the "contents" of a Node_Literal.
 * These contents comprise a lexical form, an optional language tag,
 * and optional datatype structure and a value.
 */
final /*public*/ class LiteralLabelImpl implements LiteralLabel {

	static private Logger log = LoggerFactory.getLogger( LiteralLabelImpl.class );

    /** 
	 * The lexical form of the literal, may be null if the literal was 
	 * created programatically and has not yet been serialized 
	 */
	private String lexicalForm;

	/**
	 * The value form of the literal. It will be null only if the value
	 * has not been parsed or if it is an illegal value.
	 * For plain literals and xsd:string literals
	 * the value is the same as the lexicalForm.
	 */
	private Object value;

	/**
	 * The type of the literal. A null type indicates a classic "plain" literal.
	 * The type of a literal is fixed when it is created.
	 */
	private RDFDatatype dtype;

	/**
	 * The xml:lang tag. For xsd literals this is ignored and not part of
	 * equality. For plain literals it is not ignored. The lang of a
	 * literal is fixed when it is created.
	 */
	/*final*/ private String lang;

	/**
	 * Indicates whether this is a legal literal. The working groups requires
	 * ill-formed literals to be treated as syntactically correct so instead
	 * of only storing well-formed literals we hack around it this way.
	 * N.B. This applies to any literal, not just XML well-formed literals.
	 */
	private boolean wellformed = true;
	
	/**
	 * keeps the message provided by the DatatypeFormatException
	 * if parsing failed for delayed exception thrown in getValue()
	 */
	private String exceptionMsg = null; // Suggested by Andreas Langegger
	
	//=======================================================================
	// Constructors

	/**
	 * Build a typed literal label from its lexical form. The
	 * lexical form will be parsed now and the value stored. If
	 * the form is not legal this will throw an exception.
	 * 
	 * @param lex the lexical form of the literal
	 * @param lang the optional language tag, only relevant for plain literals
	 * @param dtype the type of the literal, null for old style "plain" literals
	 * @throws DatatypeFormatException if lex is not a legal form of dtype
	 */
	LiteralLabelImpl(String lex, String lang, RDFDatatype dtype) throws DatatypeFormatException
	{
	    setLiteralLabel_1(lex, lang, dtype) ;
	}

	private void setLiteralLabel_1(String lex, String lang, RDFDatatype dtype)
        throws DatatypeFormatException {
        this.lexicalForm = lex;
        this.dtype = dtype;
        this.lang = (lang == null ? "" : lang);
        if (dtype == null) {
            value = lex;
        } else {
            setValue(lex);
        }
        normalize();
    }
	
	/**
	 * Build a typed literal label from its value form. If the value is a string we
     * assume this is intended to be a lexical form after all.
	 * 
	 * @param value the value of the literal
	 * @param lang the optional language tag, only relevant for plain literals
	 * @param dtype the type of the literal, null for old style "plain" literals
	 */
	LiteralLabelImpl(Object value, String lang, RDFDatatype dtype) throws DatatypeFormatException {
	    setLiteralLabel_2(value, lang, dtype) ;
	}
	
	/**
	 * Build a typed literal label from its value form using
	 * whatever datatype is currently registered as the default
	 * representation for this java class. No language tag is supplied.
	 * @param value the literal value to encapsulate
	 */
	LiteralLabelImpl( Object value ) {
		RDFDatatype dt = TypeMapper.getInstance().getTypeByValue( value );
		if (dt == null) {
			setWithNewDatatypeForValueClass(value);
		} else {
			setLiteralLabel_2( value, "", dt );
		}
	}

	private void setWithNewDatatypeForValueClass( Object value ) {
		Class<?> c = value.getClass();
		log.warn( "inventing a datatype for " + c );
		RDFDatatype dt = new AdhocDatatype( c );
		TypeMapper.getInstance().registerDatatype( dt );
		this.lang = "";
		this.dtype = dt;
		this.value = value;		
		this.lexicalForm = value.toString();
	}
	
	private void setLiteralLabel_2(Object value, String language, RDFDatatype dtype) throws DatatypeFormatException
    {
        // Constructor extraction: Preparation for moving into Node_Literal.
        this.dtype = dtype;
        this.lang = (language == null ? "" : language);
        if (value instanceof String) {
            String lex = (String)value;
            lexicalForm = lex;
            if (dtype == null) {
                this.value = lex;
            } else {
                setValue(lex);
            }
        } else {
            this.value = (dtype == null) ? value : dtype.cannonicalise( value );
        }
        
        normalize();
        
        if (dtype != null && lexicalForm == null) {
            // We are creating a literal from a java object, check the lexical form of the object is acceptable
            // Done here and uses this.dtype so it can use the normalized type
            wellformed = this.dtype.isValidValue( value );
            if (JenaParameters.enableEagerLiteralValidation && !wellformed) {
                throw new DatatypeFormatException(value.toString(),  dtype, "in literal creation");
            }
        } 
    }

    /**
	 * Old style constructor. Creates either a plain literal or an
	 * XMLLiteral.
	 *       @param xml If true then s is exclusive canonical XML of type rdf:XMLLiteral, and no checking will be invoked.
	
	 */
	LiteralLabelImpl(String s, String lang, boolean xml) {
	    setLiteralLabel_3(s, lang, xml) ;
	}

	private void setLiteralLabel_3(String s, String lang, boolean xml) {
	    // Constructor extraction: Preparation for moving into Node_Literal.
        this.lexicalForm = s;
        this.lang = (lang == null ? "" : lang);
        if (xml) {
            // XML Literal
            this.dtype = XMLLiteralType.theXMLLiteralType;
            value = s;
            wellformed = true;
        } else {
            // Plain literal
            this.value = s;
            this.dtype = null;
        }
    }
	
	/**
	 * Internal function to set the object value from the lexical form.
	 * Requires datatype to be set.
	 * @throws DatatypeFormatException if the value is ill-formed and
	 * eager checking is on.
	 */
	private void setValue(String lex) throws DatatypeFormatException {
		try {
			value = dtype.parse(lex);
			wellformed = true;
		} catch (DatatypeFormatException e) {
			if (JenaParameters.enableEagerLiteralValidation) {
				e.fillInStackTrace();
				throw e;
			} else {
				wellformed = false;
				exceptionMsg  = e.getMessage();
			}
		}
	}
    
    /**
     * Normalize the literal. If the value is narrower than the current data type
     * (e.g. value is xsd:date but the time is xsd:datetime) it will narrow
     * the type. If the type is narrower than the value then it may normalize
     * the value (e.g. set the mask of an XSDDateTime)
     */
    protected void normalize() {
        if (dtype != null && value != null) {
            dtype = dtype.normalizeSubType(value, dtype);
        }
    }

	//=======================================================================
	// Methods

	/** 
        Answer true iff this is a well-formed XML literal.
    */
	@Override
    public boolean isXML() {
		return dtype == XMLLiteralType.theXMLLiteralType && this.wellformed;
	}
    
	/** 
     	Answer true iff this is a well-formed literal.
    */
	@Override
    public boolean isWellFormed() {
		return dtype != null && this.wellformed;
	}
    
    @Override
    public boolean isWellFormedRaw() {
        return wellformed;
    }

	
	/**
	    Answer a human-acceptable representation of this literal value.
	    This is NOT intended for a machine-processed result. 
	*/
	@Override
    public String toString(boolean quoting) {
        StringBuilder b = new StringBuilder() ;
        if ( quoting )
            b.append('"') ;
        String lex = getLexicalForm() ;
        lex = Util.replace(lex, "\"", "\\\"") ;
        b.append(lex) ;
        if ( quoting )
            b.append('"') ;
        if ( lang != null && !lang.equals("") )
            b.append("@").append(lang) ;
        else if ( dtype != null ) {
            if ( ! ( JenaRuntime.isRDF11 && dtype.equals(XSDDatatype.XSDstring) ) )  
                b.append("^^").append(dtype.getURI()) ;
        }
        return b.toString() ;
	}

	@Override
    public String toString() {
		return toString(false);
	}

	/** 
     	Answer the lexical form of this literal, constructing it on-the-fly
        (and remembering it) if necessary.
    */
	@Override
    public String getLexicalForm() {
		if (lexicalForm == null)
			lexicalForm = (dtype == null ? value.toString() : dtype.unparse(value));
		return lexicalForm;
	}
    
    /**
     * Answer an object used to index this literal. This object must provide
     * {@link Object#equals} and {@link Object#hashCode} based on values, not object
     * instance identity.
     */
    @Override
    public Object getIndexingValue() {
        if ( isXML() )
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
     	Answer the language associated with this literal (the empty string if
        there's no language).
    */
	@Override
    public String language() {
		return lang;
	}

	/** 
     	Answer a suitable instance of a Java class representing this literal's
        value. May throw an exception if the literal is ill-formed.
    */
	@Override
    public Object getValue() throws DatatypeFormatException {
		if (wellformed) {
			return value;
		} else {
			throw new DatatypeFormatException(
				lexicalForm,
				dtype,
				exceptionMsg);
		}
	}

	/** 
     	Answer the datatype of this literal, null if it is untyped.
    */
	@Override
    public RDFDatatype getDatatype() {
		return dtype;
	}

	/** 
     	Answer the datatype URI of this literal, null if it untyped.
    */
	@Override
    public String getDatatypeURI() {
		if (dtype == null)
			return null;
		return dtype.getURI();
	}

	/** 
     	Answer true iff this literal is syntactically equal to <code>other</code>.
        Note: this is <i>not</i> <code>sameValueAs</code>.
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
     	Answer true iff this literal represents the same (abstract) value as
        the other one.
    */
	@Override
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
        if ( isStringValue(lit1) && isStringValue(lit2) ) {
            // Complete compatibility mode.
            if ( JenaParameters.enablePlainLiteralSameAsString )
                return lit1.getLexicalForm().equals(lit2.getLexicalForm()) ;
            else
                return lit1.getLexicalForm().equals(lit2.getLexicalForm()) &&
                    Objects.equals(lit1.getDatatype(), lit2.getDatatype()) ;
        }
        
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
    
	/** Return true if the literal lable is a string value (RDF 1.0 and RDF 1.1) */ 
    private static boolean isStringValue(LiteralLabel lit) {
        if ( lit.getDatatype() == null )
            // RDF 1.0
            return ! isLangString(lit) ;
        if ( lit.getDatatype().equals(XSDDatatype.XSDstring)  )
            return true;
        return false ;
    }
    
    /** Return true if the literal label is a language string. (RDF 1.0 and RDF 1.1) */
    public static boolean isLangString(LiteralLabel lit) {
        // Duplicated by Util.isLangString except for the consistency check.
        String lang = lit.language() ;
        if ( lang == null )
            return false ;
        // Check.
        if ( lang.equals("") )
            return false ;
        // This is an additional check.
        if ( JenaRuntime.isRDF11 ) {
            if ( ! Objects.equals(lit.getDatatype(), RDF.dtLangString) )
                throw new JenaException("Literal with language string which is not rdf:langString: "+lit) ;
        }
        return true ;
    }

    private int hash = 0 ;
	/** 
     	Answer the hashcode of this literal, derived from its value if it's
        well-formed and otherwise its lexical form.
    */
	@Override
    public int hashCode() {
	    // Literal labels are immutable.
	    if ( hash == 0 )
	        hash = (dtype == null ? getDefaultHashcode() : dtype.getHashCode( this ));
	    return hash ;
	}

    /**
        Answer the default hash value, suitable for datatypes which have values
        which support hashCode() naturally: it is derived from its value if it is 
        well-formed and otherwise from its lexical form.
    */
    @Override
    public int getDefaultHashcode()
        { return (wellformed ? value : getLexicalForm()).hashCode(); }

    }
