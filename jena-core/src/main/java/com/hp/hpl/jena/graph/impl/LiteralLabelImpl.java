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

package com.hp.hpl.jena.graph.impl;

import java.util.Locale ;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.datatypes.xsd.*;
import com.hp.hpl.jena.datatypes.xsd.impl.*;
import com.hp.hpl.jena.shared.impl.JenaParameters;

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
     * assume this is inteded to be a lexical form after all.
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
	 * whatever datatype is currently registered as the the default
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
	LiteralLabelImpl(String s, String lg, boolean xml) {
	    setLiteralLabel_3(s, lg, xml) ;
	}

	private void setLiteralLabel_3(String s, String lg, boolean xml) {
	    // Constructor extraction: Preparation for moving into Node_Literal.
        this.lexicalForm = s;
        this.lang = (lg == null ? "" : lg);
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
		StringBuilder b = new StringBuilder();
		if (quoting) b.append('"');
		b.append(getLexicalForm());
		if (quoting) b.append('"');
		if (lang != null && !lang.equals( "" )) b.append( "@" ).append(lang);
		if (dtype != null) b.append( "^^" ).append(dtype.getURI());
		return b.toString();
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
     	Answer the value used to index this literal
        TODO Consider pushing indexing decisions down to the datatype
    */
    @Override
    public Object getIndexingValue() {
        return
            isXML() ? this
            : !lang.equals( "" ) ? getLexicalForm() + "@" + lang.toLowerCase(Locale.ENGLISH)
            : wellformed ? getValue()
            : getLexicalForm() 
            ;
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
	    boolean typeEqual =
	        (dtype == null
	            ? otherLiteral.getDatatype() == null
	            : dtype.equals(otherLiteral.getDatatype()));
	    boolean langEqual =
	        (dtype == null ? lang.equals(otherLiteral.language()) : true);
	    return typeEqual
	        && langEqual
	        && getLexicalForm().equals(otherLiteral.getLexicalForm());
	}

	/** 
     	Answer true iff this literal represents the same (abstract) value as
        the other one.
    */
	@Override
    public boolean sameValueAs( LiteralLabel other ) {
		if (other == null)
			return false;
		if (!wellformed || !other.isWellFormedRaw()) 
			return areIllFormedLiteralsSameValueAs( other );
		return dtype == null 
		    ? isPlainLiteralSameValueAsOther( other ) 
		    : isTypedLiteralSameValueAsOther( other );
	}

	/**
	    Need to support comparison of ill-formed literals in order for the WG 
	    tests on ill formed literals to be testable using isIsomorphic to. "same
	    value as" on ill-formed literals is defined to mean "same lexical form
	    and same [ignoring case] language code".
	<p>
	    precondition: at least one of <i>this</i> and <i>other</i> is
	    ill-formed.
	*/
    private boolean areIllFormedLiteralsSameValueAs( LiteralLabel other )
        { return  !other.isWellFormedRaw() && sameByFormAndLanguage( other ); }

    /**
        Answer true iff <i>this</i> and <i>other</i> have the same lexical
        form and [ignoring case] language code. [Note: both typed literals
        and plain literals have the empty string as language code.]
    */
    private boolean sameByFormAndLanguage( LiteralLabel other )
        {
        String lex1 = getLexicalForm() ;
        String lex2 = other.getLexicalForm() ;
        return 
            lex1.equals( lex2 ) 
            && lang.equalsIgnoreCase( other.language() );
        }

    private boolean isTypedLiteralSameValueAsOther( LiteralLabel other )
        {
        return other.getDatatype() == null
            ? looksLikePlainString( this ) && sameByFormAndLanguage( other )
            : dtype.isEqual( this, other );
        }

    private boolean isPlainLiteralSameValueAsOther( LiteralLabel other )
        {
        return other.getDatatype() == null || looksLikePlainString( other )
            ? sameByFormAndLanguage( other )
            : false;
        }

    /**
        Answer true iff <i>L</i> has type XSD string and we're treating plain
        literals and xsd string literals as "the same".
    */
    private boolean looksLikePlainString( LiteralLabel L )
        {
        return 
            L.getDatatype().equals( XSDDatatype.XSDstring )
            && JenaParameters.enablePlainLiteralSameAsString;
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
