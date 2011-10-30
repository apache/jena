/******************************************************************
 * File:        BaseDatatype.java
 * Created by:  Dave Reynolds
 * Created on:  08-Dec-02
 * 
 * (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: BaseDatatype.java,v 1.1 2009-06-29 08:55:50 castagna Exp $
 *****************************************************************/

package com.hp.hpl.jena.datatypes;

import com.hp.hpl.jena.graph.impl.LiteralLabel;

/**
 * Base level implementation of datatype from which real implementations
 * can inherit.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2009-06-29 08:55:50 $
 */
public class BaseDatatype implements RDFDatatype {
    
    /** The URI label for this data type */
    protected String uri;
    
    /**
     * Constructor.
     * @param uri the URI label to use for this datatype
     */
    public BaseDatatype(String uri) {
        this.uri = uri;
    }
    
    /**
     * Return the URI which is the label for this datatype
     */
    @Override
    public String getURI() {
        return uri;
    }
    
    /**
     * Pair object used to encode both lexical form 
     * and datatype for a typed literal with unknown
     * datatype.
     */
    public static class TypedValue {
        public final String lexicalValue;
        public final String datatypeURI;
        
        public TypedValue(String lexicalValue, String datatypeURI) {
            this.lexicalValue = lexicalValue;
            this.datatypeURI = datatypeURI;
        }
        
        @Override
        public boolean equals(Object other) {
            if (other instanceof TypedValue) {
                return lexicalValue.equals(((TypedValue)other).lexicalValue) 
                         && datatypeURI.equals(((TypedValue)other).datatypeURI);
            } else {
                return false;
            }
        }
        
        @Override
        public int hashCode() {
            return lexicalValue.hashCode() ^ datatypeURI.hashCode();
        }
        
    }
    
    /**
     * Convert a value of this datatype out
     * to lexical form.
     */
    @Override
    public String unparse(Object value) {
        // Default implementation expects a parsed TypedValue but will 
        // accept a pure lexical form
        if (value instanceof TypedValue) {
            return ((TypedValue)value).lexicalValue;
        } 
        return value.toString();
    }
    
    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    @Override
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        return new TypedValue(lexicalForm, getURI());
    }
    
    /**
     * Test whether the given string is a legal lexical form
     * of this datatype.
     */
    @Override
    public boolean isValid(String lexicalForm) {
        try {
            parse(lexicalForm);
            return true;
        } catch (DatatypeFormatException e) {
            return false;
        }
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
        // default is that only literals with the same type are valid
        return equals(lit.getDatatype());
    }
     
    /**
     * Test whether the given object is a legal value form
     * of this datatype.
     */
    @Override
    public boolean isValidValue(Object valueForm) {
        // Default to brute force
        return isValid(unparse(valueForm));
    }
    
    /**
     * Compares two instances of values of the given datatype.
     * This default requires value and datatype equality.
     */
    @Override
    public boolean isEqual(LiteralLabel litLabel1, LiteralLabel litLabel2) {
        return isEqualPlain(litLabel1, litLabel2) ;
    }
    
    /** The default for equality - same datatype, same value */ 
    protected static boolean isEqualPlain(LiteralLabel litLabel1, LiteralLabel litLabel2) {
        return litLabel1.getDatatype() == litLabel2.getDatatype()
        && litLabel1.getValue().equals(litLabel2.getValue());
}
    
    /**
         Default implementation of getHashCode() delegates to the default from
         the literal label.
    */
    @Override
    public int getHashCode( LiteralLabel lit ) {
        return lit.getDefaultHashcode();
        }
    
    /**
     * Helper function to compare language tag values
     */
    public boolean langTagCompatible(LiteralLabel value1, LiteralLabel value2) {
        if (value1.language() == null) {
            return (value2.language() == null || value2.language().equals(""));
        } else {
            return value1.language().equalsIgnoreCase(value2.language());
        }
    }
    
    /**
     * Returns the java class which is used to represent value
     * instances of this datatype.
     */
    @Override
    public Class<?> getJavaClass() {
        return null;
    }
    
    /**
     * Cannonicalise a java Object value to a normal form.
     * Primarily used in cases such as xsd:integer to reduce
     * the Java object representation to the narrowest of the Number
     * subclasses to ensure that indexing of typed literals works. 
     */
    @Override
    public Object cannonicalise( Object value ) {
        return value;
    }
    
    /**
     * Returns an object giving more details on the datatype.
     * This is type system dependent. In the case of XSD types
     * this will be an instance of 
     * <code>org.apache.xerces.impl.xs.psvi.XSTypeDefinition</code>.
     */
    @Override
    public Object extendedTypeDefinition() {
        return null;
    }
    
    /**
     * Normalization. If the value is narrower than the current data type
     * (e.g. value is xsd:date but the time is xsd:datetime) returns
     * the narrower type for the literal. 
     * If the type is narrower than the value then it may normalize
     * the value (e.g. set the mask of an XSDDateTime)
     * Currently only used to narrow gener XSDDateTime objects
     * to the minimal XSD date/time type.
     * @param value the current object value
     * @param dt the currently set data type
     * @return a narrower version of the datatype based on the actual value range
     */
    @Override
    public RDFDatatype normalizeSubType(Object value, RDFDatatype dt) {
        return this; // default is no narrowing
    }
    
    /**
     * Display format
     */
    @Override
    public String toString() {
        return "Datatype[" + uri
              + (getJavaClass() == null ? "" : " -> " + getJavaClass())
              + "]";
    }
}

/*
    (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
