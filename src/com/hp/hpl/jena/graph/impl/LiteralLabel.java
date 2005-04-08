/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: LiteralLabel.java,v 1.16 2005-04-08 13:49:50 der Exp $
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
	final private RDFDatatype dtype;

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
	public LiteralLabel(String lex, String lang, RDFDatatype dtype)
		throws DatatypeFormatException {
        lexicalForm = lex;
		this.dtype = dtype;
		this.lang = (lang == null ? "" : lang);
		if (dtype == null) {
			value = lex;
		} else {
			setValue(lex);
		}
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
                value = lex;
            } else {
                setValue(lex);
            }
        } else {
		    this.value = value;
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

	//=======================================================================
	// Methods

	/**
	 * Return true if the literal is a well-formed XML literal (for example as
	 * created via parsetype="literal")
	 */
	public boolean isXML() {
		return dtype == XMLLiteralType.theXMLLiteralType && this.wellformed;
	}
    
	/**
	* Return true if the literal is a typed literal and the
        * lexicalForm is in the lexical space.
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
	 *  Returns the string component of the LiteralLabel.
	 *  Note that different LiteralLabels may have the
	 *  same string component.
	 *  A canonical form is returned using the
	 *  asNTriple() method.
	 */
	public String getLexicalForm() {
		if (lexicalForm == null)
			lexicalForm =
				(dtype == null ? value.toString() : dtype.unparse(value));
		return lexicalForm;
	}

	/** An RFC 3066 lang tag or "".
	 *  These are case insensitive,
	 *  mixed case is returned from this method.
	 *  Where possible the case should be preserved.
	 *  e.g. "en-US" is usually written in mixed case
	 *  but is logically equivalent to "en-us" and "EN-US".
	 */
	public String language() {
		return lang;
	}

	/**
	 * Return the value of the literal.
	 * @throws DatatypeFormatException if the eager validation mode is
	 * switched off and this literal is ill-formed.
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
	 * Return the datatype of the Literal. This returns the java object
	 * (an instance of {@link RDFDatatype RDFDatatype}) that represents
	 * the datatype. It returns null for "plain" literals.
	 */
	public RDFDatatype getDatatype() {
		return dtype;
	}

	/**
	 * Return the uri of the datatype of the Literal. 
	 * It returns null for "plain" literals.
	 */
	public String getDatatypeURI() {
		if (dtype == null)
			return null;
		return dtype.getURI();
	}

	/**
	 * Structural comparision operator. Takes lang, datatype and value into
	 * account. This is not the same as RDF semantic equality which is
	 * dealt with by {@link #sameValueAs}.
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
            	(dtype == null ? lang.equalsIgnoreCase(otherLiteral.lang) : true);
            return typeEqual
                && langEqual
                && getLexicalForm().equals(otherLiteral.getLexicalForm());
//            if (wellformed) {
//            	return typeEqual && langEqual && value.equals(otherLiteral.value);
//            } else {
//            	return typeEqual
//            		&& langEqual
//            		&& lexicalForm.equals(otherLiteral.lexicalForm);
//            }
	}

	/**
	 * Test that two nodes are semantically equivalent.
	 * In some cases this may be the sames as equals, in others
	 * equals is stricter. For example, two xsd:int literals with
	 * the same value but different language tag are semantically
	 * equivalent but distinguished by the java equality function
	 * in order to support round tripping.
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
	 * Hash operator. Hashes only on lexical space.
	 * If two instances differ by datatype or significant lang tag then
	 * that is simply a hash collision, does not invalidate the invariants.
	 */
	public int hashCode() {
		return (wellformed ? value : getLexicalForm()).hashCode();
	}

}

/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
