/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            26 Jan 2001
 * Filename           $RCSfile: PropertyAccessor.java,v $
 * Revision           $Revision: 1.1.1.1 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2002-12-19 19:15:03 $
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

import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * Encapsulates the standard methods of modifying a property on a DAML value.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: PropertyAccessor.java,v 1.1.1.1 2002-12-19 19:15:03 bwm Exp $
 */
public interface PropertyAccessor
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

    /**
     * Answer the property that this accessor works on
     *
     * @return A property
     */
    public Property getProperty();


    /**
     * Answer the number of values that the encapsulated property has in the
     * RDF model.
     *
     * @return The number statements for this property in the model.
     */
    public int count();


    /**
     * Answer an iteration over the DAML values that this property has in the
     * RDF model.
     *
     * @param closed If true, and the property is transitive, generate the closure
     *               of the property starting from the encapsulated resource.
     * @return An iteration over the values of the encapsulated property.
     */
    public Iterator getAll( boolean closed );


    /**
     * Answer a general value of the encapsulated property. If it has no values, answer
     * null. If it has one value, answer that value. Otherwise, answer an undetermined
     * member of the set of values. This version of the method makes no assumptions
     * about the property value, other than it is an RDF node. This is the safest, most
     * conservative, assumption.  If it is known that a value is certain to be a
     * DAML value, you can use {@link #getDAMLValue} instead.
     *
     * @return A value for the encapsulated property in the RDF model, or null
     *         if the property has no value.
     */
    public RDFNode get();


    /**
     * Answer a value of the encapsulated property, making the assumption that it is
     * a DAML value. If the property has no DAML value, answer
     * null. If it has one DAML value, answer that value. Otherwise, answer an undetermined
     * member of the set of values.  This method is optimised to select only DAML
     * values for the property - that is, values that extend DAMLCommon.  <b>This
     * method will therefore answer null if there is no DAML value for the property, even
     * if there is one or more vanilla-RDF values</b>. For
     * a more general version of this method, which returns all RDF values, see {@link
     * #get}.
     *
     * @return A DAML value for the encapsulated property in the RDF model, or null
     *         if the property has no DAML value.
     */
    public DAMLCommon getDAMLValue();


    /**
     * Add a value to the encapsulated property.
     *
     * @param value The value to be added.
     */
    public void add( Resource value );


    /**
     * Remove a value from the encapsulated property.
     *
     * @param value The value to be removed.
     */
    public void remove( DAMLCommon value );


    /**
     * Answer true if the encapsulated property has the given value as one of its
     * values.
     *
     * @param value A DAML value to test for
     * @return True if the RDF model contains a statement giving a value for
     *         the encapsulated property matching the given value.
     */
    public boolean hasValue( DAMLCommon value );

}
