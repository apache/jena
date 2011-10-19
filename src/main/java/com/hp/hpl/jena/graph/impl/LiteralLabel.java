/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.graph.impl ;

import com.hp.hpl.jena.datatypes.DatatypeFormatException ;
import com.hp.hpl.jena.datatypes.RDFDatatype ;

public interface LiteralLabel
{

    /** 
        Answer true iff this is a well-formed XML literal.
     */
    public boolean isXML() ;

    /** 
     	Answer true iff this is a well-formed literal.
     */
    public boolean isWellFormed() ;
    
    /** 
        Answer true iff the wellformed flag is true. Does not test for datatype. 
     */
    public boolean isWellFormedRaw() ;

    /**
        Answer a human-acceptable representation of this literal value.
        This is NOT intended for a machine-processed result. 
     */
    public String toString(boolean quoting) ;

    @Override
    public String toString() ;

    /** 
     	Answer the lexical form of this literal, constructing it on-the-fly
        (and remembering it) if necessary.
     */
    public String getLexicalForm() ;

    /** 
     	Answer the value used to index this literal
        TODO Consider pushing indexing decisions down to the datatype
     */
    public Object getIndexingValue() ;

    /** 
     	Answer the language associated with this literal (the empty string if
        there's no language).
     */
    public String language() ;

    /** 
     	Answer a suitable instance of a Java class representing this literal's
        value. May throw an exception if the literal is ill-formed.
     */
    public Object getValue() throws DatatypeFormatException ;

    /** 
     	Answer the datatype of this literal, null if it is untyped.
     */
    public RDFDatatype getDatatype() ;

    /** 
     	Answer the datatype URI of this literal, null if it untyped.
     */
    public String getDatatypeURI() ;

    /** 
     	Answer true iff this literal is syntactically equal to <code>other</code>.
        Note: this is <i>not</i> <code>sameValueAs</code>.
     */
    @Override
    public boolean equals(Object other) ;

    /** 
     	Answer true iff this literal represents the same (abstract) value as
        the other one.
     */
    public boolean sameValueAs(LiteralLabel other) ;

    /** 
     	Answer the hashcode of this literal, derived from its value if it's
        well-formed and otherwise its lexical form.
     */
    @Override
    public int hashCode() ;

    /**
        Answer the default hash value, suitable for datatypes which have values
        which support hashCode() naturally: it is derived from its value if it is 
        well-formed and otherwise from its lexical form.
     */
    public int getDefaultHashcode() ;

}
/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */