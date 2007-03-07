/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: LiteralLabel.java,v 1.31 2007-03-07 15:54:28 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.datatypes.xsd.*;
import com.hp.hpl.jena.datatypes.xsd.impl.*;
import com.hp.hpl.jena.shared.impl.JenaParameters;

/**
 * Represents the "contents" of a Node_Literal.
 * These contents comprise a lexical form, an optional language tag,
 * and optional datatype structure and a value.
 * 
 * @author Jeremy Carroll and Dave Reynolds
 */
final public class LiteralLabel {

	//=======================================================================
	// Variables

	public static LiteralLabel createLiteralLabel
        ( String lex, String lang, RDFDatatype dtype ) 
    throws DatatypeFormatException
        { return new LiteralLabel( lex, lang, dtype ); }

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
	final private String lang;

	/**
	 * Indicates whether this is a legal literal. The working groups requires
	 * ill-formed literals to be treated as syntactically correct so instead
	 * of only storing well-formed literals we hack around it this way.
	 * N.B. This applies to any literal, not just XML well-formed literals.
	 */
	private boolean wellformed = true;

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
	private LiteralLabel(String lex, String lang, RDFDatatype dtype)
		throws DatatypeFormatException {
        lexicalForm = lex;
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
	 * Build a plain literal label from its lexical form. 
	 * @param lex the lexical form of the literal
	 * @param lang the optional language tag, only relevant for plain literals
	 */
	public LiteralLabel(String lex, String lang) {
		this(lex, lang, null);
	}

	/**
	 * Build a typed literal label from its value form. If the value is a string we
     * assume this is inteded to be a lexical form after all.
	 * 
	 * @param value the value of the literal
	 * @param lang the optional language tag, only relevant for plain literals
	 * @param dtype the type of the literal, null for old style "plain" literals
	 */
	public LiteralLabel(Object value, String lang, RDFDatatype dtype) throws DatatypeFormatException {
		this.dtype = dtype;
		this.lang = (lang == null ? "" : lang);
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
	 * Build a typed literal label supplying both value and lexical form.
	 * The caller guarantees that the lexical form is legal, 
	 * and the value corresponds. 
	 * 
	 * @param lex the lexical form of the literal
	 * @param value the value of the literal
	 * @param lang the optional language tag, only relevant for plain literals
	 * @param dtype the type of the literal, null for old style "plain" literals
	 */
	public LiteralLabel(
		String lex,
		Object value,
		String lang,
		RDFDatatype dtype) {
		this(value, lang, dtype);
		this.lexicalForm = lex;
	}

	/**
	 * Build a typed literal label from its value form using
	 * whatever datatype is currently registered as the the default
	 * representation for this java class. No language tag is supplied.
	 * @param value the literal value to encapsulate
	 */
	public LiteralLabel(Object value) {
		this(value, "", TypeMapper.getInstance().getTypeByValue(value));
	}

	/**
	 * Old style constructor. Creates either a plain literal or an
	 * XMLLiteral.
	 *       @param xml If true then s is exclusive canonical XML of type rdf:XMLLiteral, and no checking will be invoked.
	
	 */
	public LiteralLabel(String s, String lg, boolean xml) {
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
			}
		}
	}
    
    /**
     * Normalize the literal. In the future this may normalize
     * the lexical value. At present this is used to 
     * reduce datatypes to a minimal enclosing form when desired.
     */
    protected void normalize() {
        if (dtype != null && value != null) {
            dtype = dtype.getNarrowedDatatype(value);
        }
    }

	//=======================================================================
	// Methods

	/** 
        Answer true iff this is a well-formed XML literal.
    */
	public boolean isXML() {
		return dtype == XMLLiteralType.theXMLLiteralType && this.wellformed;
	}
    
	/** 
     	Answer truee iff this is a well-formed literal.
    */
	public boolean isWellFormed() {
		return dtype != null && this.wellformed;
	}
    
	/**
	    Answer a human-acceptable representation of this literal value.
	    This is NOT intended for a machine-processed result. 
	*/
	public String toString(boolean quoting) {
		StringBuffer b = new StringBuffer();
		if (quoting) b.append('"');
		b.append(getLexicalForm());
		if (quoting) b.append('"');
		if (lang != null && !lang.equals( "" )) b.append( "@" ).append(lang);
		if (dtype != null) b.append( "^^" ).append(dtype.getURI());
		return b.toString();
	}

	public String toString() {
		return toString(false);
	}

	/** 
     	Answer the lexical form of this literal, constructing it on-the-fly
        (and remembering it) if necessary.
    */
	public String getLexicalForm() {
		if (lexicalForm == null)
			lexicalForm = (dtype == null ? value.toString() : dtype.unparse(value));
		return lexicalForm;
	}
    
    /** 
     	Answer the value used to index this literal
        TODO Consider pushing indexing decisions down to the datatype
    */
    public Object getIndexingValue() {
        return
            isXML() ? this
            : !lang.equals( "" ) ? getLexicalForm() + "@" + lang.toLowerCase()
            : wellformed ? getValue()
            : getLexicalForm() 
            ;
    }

	/** 
     	Answer the language associated with this literal (the empty string if
        there's no language).
    */
	public String language() {
		return lang;
	}

	/** 
     	Answer a suitable instance of a Java class representing this literal's
        value. May throw an exception if the literal is ill-formed.
    */
	public Object getValue() throws DatatypeFormatException {
		if (wellformed) {
			return value;
		} else {
			throw new DatatypeFormatException(
				lexicalForm,
				dtype,
				" in getValue()");
		}
	}

	/** 
     	Answer the datatype of this literal, null if it is untyped.
    */
	public RDFDatatype getDatatype() {
		return dtype;
	}

	/** 
     	Answer the datatype URI of this literal, null if it untyped.
    */
	public String getDatatypeURI() {
		if (dtype == null)
			return null;
		return dtype.getURI();
	}

	/** 
     	Answer true iff this literal is syntactically equal to <code>other</code>.
        Note: this is <i>not</i> <code>sameValueAs</code>.
    */
	public boolean equals(Object other) {
            if (other == null || !(other instanceof LiteralLabel)) {
            	return false;
            }
            LiteralLabel otherLiteral = (LiteralLabel) other;
            boolean typeEqual =
            	(dtype == null
            		? otherLiteral.dtype == null
            		: dtype.equals(otherLiteral.dtype));
            boolean langEqual =
                (dtype == null ? lang.equals(otherLiteral.lang) : true);
            return typeEqual
                && langEqual
                && getLexicalForm().equals(otherLiteral.getLexicalForm());
	}

	/** 
     	Answer true iff this literal represents the same (abstract) value as
        the other one.
    */
	public boolean sameValueAs(LiteralLabel other) {
		if (other == null)
			return false;
		if (!wellformed || !other.wellformed) {
			if (!other.wellformed) {
				// Need to support this comparison in order for the WG tests on ill formed
				// literals to be testable using isIsomorphic to
				return lexicalForm.equals(other.lexicalForm)
					&& lang.equalsIgnoreCase(other.lang);
			} else {
				return false;
			}
		}
		if (dtype == null) {
			// Plain literal
			if (other.dtype == null
				|| (JenaParameters.enablePlainLiteralSameAsString
					&& other.dtype.equals(XSDDatatype.XSDstring))) {
				return lexicalForm.equals(other.lexicalForm)
					&& lang.equalsIgnoreCase(other.lang);
			} else {
				return false;
			}
		} else {
			// Typed literal
			return dtype.isEqual(this, other);
		}
	}

	/** 
     	Answer the hashcode of this literal, derived from its value if it's
        well-formed and otherwise its lexical form.
    */
	public int hashCode() {
		return dtype == null ? getDefaultHashcode() : dtype.getHashCode( this );
	}

    /**
        Answer the default hash value, suitable for datatypes which have values
        which support hashCode() naturally: it is derived from its value if it is 
        well-formed and otherwise from its lexical form.
    */
    public int getDefaultHashcode()
        { return (wellformed ? value : getLexicalForm()).hashCode(); }

    }

/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
