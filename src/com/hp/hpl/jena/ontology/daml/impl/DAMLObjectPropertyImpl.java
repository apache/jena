/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            4 Jan 2001
 * Filename           $RCSfile: DAMLObjectPropertyImpl.java,v $
 * Revision           $Revision: 1.7 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2005-02-21 12:05:26 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml.impl;


// Imports
///////////////
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.daml.*;
import com.hp.hpl.jena.vocabulary.*;



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
 * @version CVS info: $Id: DAMLObjectPropertyImpl.java,v 1.7 2005-02-21 12:05:26 andy_seaborne Exp $
 */
public class DAMLObjectPropertyImpl 
    extends DAMLPropertyImpl
    implements DAMLObjectProperty
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating DAMLObjectProperty facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use 
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    public static Implementation factory = new Implementation() {
        public EnhNode wrap( Node n, EnhGraph eg ) { 
            if (canWrap( n, eg )) {
                return new DAMLObjectPropertyImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n.toString() + " to DAMLObjectProperty" );
            } 
        }
            
        public boolean canWrap( Node n, EnhGraph g ) {
            return hasType( n, g, DAML_OIL.ObjectProperty );
        }
    };


    // Instance variables
    //////////////////////////////////

    /** Property accessor for inverseOf */
    private PropertyAccessor m_propInverseOf = new PropertyAccessorImpl( getVocabulary().inverseOf(), this );



    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a DAML object property represented by the given node in the given graph.
     * </p>
     * 
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public DAMLObjectPropertyImpl( Node n, EnhGraph g ) {
        super( n, g );
    }


    // External signature methods
    //////////////////////////////////


    /**
     * <p>Set the flag to indicate that this property is to be considered
     * transitive - that is, it is defined by the DAML class <code>TransitiveProperty</code>.</p>
     *
     * @param transitive True for a transitive property
     */
    public void setIsTransitive( boolean transitive ) {
        if (transitive) {
            addRDFType( getVocabulary().TransitiveProperty() );
        }
        else {
            removeRDFType( getVocabulary().TransitiveProperty() );
        }
    }


    /**
     * <p>Answer true if this property is transitive.</p>
     *
     * @return True if this property is transitive
     */
    public boolean isTransitive() {
        return hasRDFType( getVocabulary().TransitiveProperty() )  ||
               DAMLHierarchy.getInstance().isTransitiveProperty( this );
    }


    /**
     * <p>Set the flag to indicate that this property is to be considered
     * unabiguous - that is, it is defined by the DAML class <code>UnambiguousProperty</code>.</p>
     *
     * @param unambiguous True for a unabiguous property
     */
    public void setIsUnambiguous( boolean unambiguous ) {
        if (unambiguous) {
            addRDFType( getVocabulary().UnambiguousProperty() );
        }
        else {
            removeRDFType( getVocabulary().UnambiguousProperty() );
        }
    }


    /**
     * <p>Answer true if this property is an unambiguous property.</p>
     *
     * @return True if this property is unambiguous
     */
    public boolean isUnambiguous() {
        return hasRDFType( getVocabulary().UnambiguousProperty() );
    }


    /**
     * <p>Property accessor for the <code>inverseOf</code> property of a DAML Property. This denotes
     * that the named property (say, P) is an inverse of this property (say, Q). Formally,
     * if (x, y) is an instance of P, then (y, x) is an instance of Q. According to the
     * DAML specification, inverseOf is only defined for object properties (i.e. not
     * datatype properties).</p>
     *
     * @return Property accessor for <code>inverseOf</code>
     */
    public PropertyAccessor prop_inverseOf() {
        return m_propInverseOf;
    }




    // Internal implementation methods
    //////////////////////////////////




    //==============================================================================
    // Inner class definitions
    //==============================================================================


}

/*
 * (c) Copyright 2001, 2002, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
