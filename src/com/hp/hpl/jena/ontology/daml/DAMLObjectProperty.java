/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            4 Jan 2001
 * Filename           $RCSfile: DAMLObjectProperty.java,v $
 * Revision           $Revision: 1.1.1.1 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2002-12-19 19:14:58 $
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
import com.hp.hpl.jena.rdf.model.Property;



/**
 * Java encapsulation of an object property in a DAML ontology. An object property
 * is a partition of the class of properties, in which the range of the property
 * is a DAML instance (rather than a datatype). Object properties may be transitive
 * and unambiguous, which are modelled in the specification by sub-classes of
 * <code>ObjectProperty</code> named <code>TransitiveProperty</code> and
 * <code>UnambiguousProperty</code>.  In this API, transitivity and uniqueness are
 * modelled as attributes of the DAMLObjectProperty object.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLObjectProperty.java,v 1.1.1.1 2002-12-19 19:14:58 bwm Exp $
 */
public interface DAMLObjectProperty
    extends DAMLProperty
{
    // Constants
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////


    /**
     * Set the flag to indicate that this property is to be considered
     * transitive - that is, it is defined by the DAML class TransitiveProperty.
     *
     * @param transitive True for a transitive property
     */
    public void setIsTransitive( boolean transitive );


    /**
     * Answer true if this property is to be considered transitive, that is
     * it is characterised by the DAML class TransitiveProperty
     *
     * @return True if this property is transitive
     */
    public boolean isTransitive();


    /**
     * Set the flag to indicate that this property is to be considered
     * unabiguous - that is, it is defined by the DAML class UnambiguousProperty.
     *
     * @param unabiguous True for a unabiguous property
     */
    public void setIsUnambiguous( boolean unabiguous );


    /**
     * Answer true if this property is to be considered unabiguous, that is
     * it is characterised by the DAML class UnambiguousProperty
     *
     * @return True if this property is unabiguous
     */
    public boolean isUnambiguous();


    /**
     * Property accessor for the 'inverseOf' property of a DAML Property. This denotes
     * that the named property (say, P) is an inverse of this property (say, Q). Formally,
     * if (x, y) is an instance of P, then (y, x) is an instance of Q. According to the
     * DAML specification, inverseOf is only defined for object properties (i.e. not
     * datatype properties).
     *
     * @return Property accessor for 'inverseOf'
     */
    public PropertyAccessor prop_inverseOf();



}
