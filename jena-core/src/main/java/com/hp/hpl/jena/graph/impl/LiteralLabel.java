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
