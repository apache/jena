/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            4 Jan 2001
 * Filename           $RCSfile: DAMLObjectProperty.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-06-18 21:56:08 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved. 
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml;


// Imports
///////////////



/**
 * <p>Java encapsulation of an object property in a DAML ontology. An object property
 * is a partition of the class of properties, in which the range of the property
 * is a DAML instance (rather than a datatype). Object properties may be transitive
 * and unambiguous, which are modelled in the specification by sub-classes of
 * <code>ObjectProperty</code> named <code>TransitiveProperty</code> and
 * <code>UnambiguousProperty</code>.  In this API, transitivity and uniqueness are
 * modelled as attributes of the DAMLObjectProperty object.</p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLObjectProperty.java,v 1.4 2003-06-18 21:56:08 ian_dickinson Exp $
 */
public interface DAMLObjectProperty
    extends DAMLProperty
{
    // Constants
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////


    /**
     * <p>Set the flag to indicate that this property is to be considered
     * transitive - that is, it is defined by the DAML class <code>TransitiveProperty</code>.</p>
     *
     * @param transitive True for a transitive property
     */
    public void setIsTransitive( boolean transitive );


    /**
     * <p>Answer true if this property is transitive.</p>
     *
     * @return True if this property is transitive
     */
    public boolean isTransitive();


    /**
     * <p>Set the flag to indicate that this property is to be considered
     * unabiguous - that is, it is defined by the DAML class <code>UnambiguousProperty</code>.</p>
     *
     * @param unambiguous True for a unabiguous property
     */
    public void setIsUnambiguous( boolean unambiguous );


    /**
     * <p>Answer true if this property is an unambiguous property.</p>
     *
     * @return True if this property is unambiguous
     */
    public boolean isUnambiguous();


    /**
     * <p>Property accessor for the <code>inverseOf</code> property of a DAML Property. This denotes
     * that the named property (say, P) is an inverse of this property (say, Q). Formally,
     * if (x, y) is an instance of P, then (y, x) is an instance of Q. According to the
     * DAML specification, inverseOf is only defined for object properties (i.e. not
     * datatype properties).</p>
     *
     * @return Property accessor for <code>inverseOf</code>
     */
    public PropertyAccessor prop_inverseOf();



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

