/******************************************************************
 * File:        Datatype.java
 * Created by:  Dave Reynolds
 * Created on:  07-Dec-02
 * 
 * (c) Copyright 2002, 2003, 2004, 2005, 2006 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: RDFDatatype.java,v 1.11 2006-03-22 13:52:23 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.datatypes;

import com.hp.hpl.jena.graph.impl.LiteralLabel;

/**
 * Interface on a datatype representation. An instance of this
 * interface is needed to convert typed literals between lexical
 * and value forms. 
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.11 $ on $Date: 2006-03-22 13:52:23 $
 */
public interface RDFDatatype {

    /**
     * Return the URI which is the label for this datatype
     */
    public String getURI();
    
    /**
     * Convert a value of this datatype out
     * to lexical form.
     */
    public String unparse(Object value);
    
    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    public Object parse(String lexicalForm) throws DatatypeFormatException;
    
    /**
     * Test whether the given string is a legal lexical form
     * of this datatype.
     */
    public boolean isValid(String lexicalForm);
    
    /**
     * Test whether the given object is a legal value form
     * of this datatype.
     */
    public boolean isValidValue(Object valueForm);
    
    /**
     * Test whether the given LiteralLabel is a valid instance
     * of this datatype. This takes into account typing information
     * as well as lexical form - for example an xsd:string is
     * never considered valid as an xsd:integer (even if it is
     * lexically legal like "1").
     */
    public boolean isValidLiteral(LiteralLabel lit);
    
    /**
     * Compares two instances of values of the given datatype.
     * This defaults to just testing equality of the java value
     * representation but datatypes can override this. We pass the
     * entire LiteralLabel to allow the equality function to take
     * the xml:lang tag and the datatype itself into account.
     */
    public boolean isEqual(LiteralLabel value1, LiteralLabel value2);
    
    /**
         Gets the hash code of a given value. This defaults to
         lit.getValue().hashCode(), but datatypes can overide this, and array types 
         must.
    */
    public int getHashCode( LiteralLabel lit );
    
    /**
     * If this datatype is used as the cannonical representation
     * for a particular java datatype then return that java type,
     * otherwise returns null.
     */
    public Class getJavaClass();
    
    /**
     * Cannonicalise a java Object value to a normal form.
     * Primarily used in cases such as xsd:integer to reduce
     * the Java object representation to the narrowest of the Number
     * subclasses to ensure that indexing of typed literals works. 
     */
    public Object cannonicalise( Object value );
    
    /**
     * Returns an object giving more details on the datatype.
     * This is type system dependent. In the case of XSD types
     * this will be an instance of 
     * <code>org.apache.xerces.impl.xs.dv.XSSimpleType</code>.
     */
    public Object extendedTypeDefinition();
    
    /**
     * Return a minimal datatype for this object. Used to handle
     * cases where a single java object can represent multiple
     * specific types and where we want narrow the type used.
     * For example, a BigDecimal may narrow to a simple xsd:int. 
     * Currently only used to narrow gener XSDDateTime objects
     * to the minimal XSD date/time type.
     */
    public RDFDatatype getNarrowedDatatype(Object value);
}

/*
    (c) Copyright 2002, 2003, 2004, 2005, 2006 Hewlett-Packard Development Company, LP
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

