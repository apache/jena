/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            26 Jan 2001
 * Filename           $RCSfile: IntLiteralAccessorImpl.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-05-21 16:45:18 $
 *               by   $Author: chris-dollin $
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
package com.hp.hpl.jena.ontology.daml.impl;


// Imports
///////////////

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Literal;

import com.hp.hpl.jena.ontology.daml.IntLiteralAccessor;
import com.hp.hpl.jena.ontology.daml.DAMLCommon;

import com.hp.hpl.jena.util.Log;

import com.hp.hpl.jena.shared.*;


/**
 * Encapsulates the standard methods of modifying a property on a DAML object, where
 * the value of the property is an RDF literal (as opposed to another DAML value,
 * see {@link com.hp.hpl.jena.ontology.daml.PropertyAccessor PropertyAccessor},
 * and the literal is known to encapsulate an integer value.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: IntLiteralAccessorImpl.java,v 1.3 2003-05-21 16:45:18 chris-dollin Exp $
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
     * Construct a new accessor for integer literal values of the given property.
     *
     * @param property The property that this accessor works on
     * @param val The DAML value that has this property
     */
    public IntLiteralAccessorImpl( Property property, DAMLCommon val ) {
        super( property, val );
    }



    // External signature methods
    //////////////////////////////////

    /**
     * Answer the a value of the encapsulated property. If it has no values, answer
     * null. If it has one value, answer that value. Otherwise, answer an undetermined
     * member of the set of values.
     *
     * @return A value for the encapsulated property in the model, as an integer.
     * @exception {@link java.lang.RuntimeException} if the property is not defined or cannot be
     *            interpreted as an integer.
     */
    public int getInt() {
        try {
            NodeIterator i = getValues();

            if (i == null  ||  !i.hasNext()) {
                throw new RuntimeException( "No value defined for property " + getProperty() );
            }
            else {
                return ((Literal) i.nextNode()).getInt();
            }
        }
        catch (JenaException e) {
            Log.severe( "RDF exception when getting literal values: " + e, e );
            throw new RuntimeException( "RDF exception when getting literal values: " + e );
        }
    }


    /**
     * Add a value to the encapsulated property.
     *
     * @param value The value to be added, as an int.
     */
    public void addInt( int value ) {
        addValue( Integer.toString( value ) );
    }


    /**
     * Remove an integer value from the encapsulated property.
     *
     * @param value The value to be removed, as an int.
     */
    public void removeInt( int value ) {
        removeValue( Integer.toString( value ) );
    }


    /**
     * Answer true if the encapsulated property has the given value as one of its
     * values.
     *
     * @param value An int value to test for
     * @return True if the RDF model contains a statement giving a value for
     *         the encapsulated property matching the given value.
     */
    public boolean hasIntValue( int value ) {
        return hasValue( Integer.toString( value ) );
    }


    // Internal implementation methods
    //////////////////////////////////




    //==============================================================================
    // Inner class definitions
    //==============================================================================


}
