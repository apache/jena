/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            26 Jan 2001
 * Filename           $RCSfile: IntLiteralAccessorImpl.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-06-18 21:56:07 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved. 
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml.impl;


// Imports
///////////////

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.daml.*;


/**
 * <p>Encapsulates the standard methods of modifying a property on a DAML object, where
 * the value of the property is an RDF literal (as opposed to another DAML value,
 * see {@link PropertyAccessor}, and the literal is known to encapsulate an integer value.</p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: IntLiteralAccessorImpl.java,v 1.5 2003-06-18 21:56:07 ian_dickinson Exp $
 */
public class IntLiteralAccessorImpl
    extends LiteralAccessorImpl
    implements IntLiteralAccessor
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////


    // Constructors
    //////////////////////////////////

    /**
     * <p>Construct a new accessor for integer literal values of the given property.</p>
     *
     * @param property The property that this accessor works on
     * @param val The DAML value that has this property
     */
    public IntLiteralAccessorImpl( Property property, OntResource val ) {
        super( property, val );
    }



    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer the integer value of the encapsulated property.</p>
     *
     * @return A value for the encapsulated property in the model, as an integer.
     */
    public int getInt() {
        return getValue().getInt();
    }


    /**
     * <p>Add a value to the encapsulated property.</p>
     *
     * @param value The value to be added, as an int.
     */
    public void addInt( int value ) {
        add( m_val.getModel().createTypedLiteral( value ) );
    }


    /**
     * <p>Remove an integer value from the encapsulated property.</p>
     *
     * @param value The value to be removed, as an int.
     */
    public void removeInt( int value ) {
        remove( m_val.getModel().createTypedLiteral( value ) );
    }


    /**
     * <p>Answer true if the encapsulated property has the given value as one of its
     * values.</p>
     * 
     * @param value An int value to test for
     * @return True if the RDF model contains a statement giving a value for
     *         the encapsulated property matching the given value.
     */
    public boolean hasIntValue( int value ) {
        return hasValue( m_val.getModel().createTypedLiteral( value ) );
    }


    // Internal implementation methods
    //////////////////////////////////




    //==============================================================================
    // Inner class definitions
    //==============================================================================


}

/*
    (c) Copyright Hewlett-Packard Company 2001-2003
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

