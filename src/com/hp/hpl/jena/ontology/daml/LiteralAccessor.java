/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            26 Jan 2001
 * Filename           $RCSfile: LiteralAccessor.java,v $
 * Revision           $Revision: 1.1.1.1 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2002-12-19 19:15:02 $
 *               by   $Author: bwm $
 *
 * (c) Copyright Hewlett-Packard Company 2001
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
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml;


// Imports
///////////////

import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Literal;



/**
 * Encapsulates the standard methods of modifying a property on a DAML object, where
 * the value of the property is an RDF literal (as opposed to another DAML value,
 * see {@link PropertyAccessor}.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: LiteralAccessor.java,v 1.1.1.1 2002-12-19 19:15:02 bwm Exp $
 */
public interface LiteralAccessor
    extends PropertyAccessor
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

    /**
     * Answer an iteration over the literal values that this property has in the
     * RDF model.
     *
     * @return An iteration over the values of the encapsulated property. Each
     *         member of the iteration will be an RDF literal.
     */
    public NodeIterator getValues();


    /**
     * Answer the a value of the encapsulated property. If it has no values, answer
     * null. If it has one value, answer that value. Otherwise, answer an undetermined
     * member of the set of values.
     *
     * @return A value for the encapsulated property, or null
     *         if the property has no value.
     */
    public Literal getValue();


    /**
     * Add a value to the encapsulated property.
     *
     * @param value The value to be added, as a string.
     */
    public void addValue( String value );


    /**
     * Add a value to the encapsulated property.
     *
     * @param value The value to be added, as an RDF literal.
     */
    public void addValue( Literal value );


    /**
     * Remove a value from the encapsulated property.
     *
     * @param value The value to be removed, as a string.
     */
    public void removeValue( String value );


    /**
     * Remove a value from the encapsulated property.
     *
     * @param value The value to be removed, as a literal.
     */
    public void removeValue( Literal value );


    /**
     * Answer true if the encapsulated property has the given value as one of its
     * values.
     *
     * @param value A DAML value to test for
     * @return True if the RDF model contains a statement giving a value for
     *         the encapsulated property matching the given value.
     */
    public boolean hasValue( String value );


    /**
     * Answer true if the encapsulated property has the given value as one of its
     * values.
     *
     * @param value A DAML value to test for
     * @return True if the RDF model contains a statement giving a value for
     *         the encapsulated property matching the given value.
     */
    public boolean hasValue( Literal value );


}
