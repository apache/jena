/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            4 Jan 2001
 * Filename           $RCSfile: DAMLObjectPropertyImpl.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-06-13 19:09:28 $
 *               by   $Author: ian_dickinson $
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
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.daml.*;
import com.hp.hpl.jena.vocabulary.*;



/**
 * Implementation for Java encapsulation of an object property in a DAML ontology. An object property
 * is a partition of the class of properties, in which the range of the property
 * is a DAML instance (rather than a datatype). Object properties may be transitive
 * and unambiguous, which are modelled in the specification by sub-classes of
 * <code>ObjectProperty</code> named <code>TransitiveProperty</code> and
 * <code>UnambiguousProperty</code>.  In this API, transitivity and uniqueness are
 * modelled as attributes of the DAMLObjectProperty object.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLObjectPropertyImpl.java,v 1.2 2003-06-13 19:09:28 ian_dickinson Exp $
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
     * A factory for generating DAMLDataInstance facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use 
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    public static Implementation factory = new Implementation() {
        public EnhNode wrap( Node n, EnhGraph eg ) { 
            if (canWrap( n, eg )) {
                return new DAMLClassImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n.toString() + " to DAMLOntology" );
            } 
        }
            
        public boolean canWrap( Node node, EnhGraph eg ) {
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            return (profile != null)  &&  profile.isSupported( node, eg, DAMLDatatypeProperty.class );
        }
    };


    // Instance variables
    //////////////////////////////////

    /** Property accessor for inverseOf */
    private PropertyAccessor m_propInverseOf = null;



    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a DAML list represented by the given node in the given graph.
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
     * Return a readable representation of the DAML value
     *
     * @return a string denoting this value
     */
    public String toString() {
        // get the public name for this value type (e.g. we change DAMLClassImpl -> DAMLClass)
        String cName = getClass().getName();
        int i = cName.indexOf( "Impl" );
        int j = cName.lastIndexOf( "." ) + 1;
        cName = (i > 0) ? cName.substring( j, i ) : cName.substring( j );

        // property attributes
        String attribs;
        attribs = isUnique() ? "unique " : "";
        attribs = attribs + (isTransitive() ? "transitive " : "");
        attribs = attribs + (isUnambiguous() ? "unambiguous " : "");

        // now format the return string
        return (getURI() == null) ?
                   ("<" + attribs + "Anonymous " + cName + "@" + Integer.toHexString( hashCode() ) + ">") :
                   ("<" + attribs + cName + " " + getURI() + ">");
    }


    /**
     * Set the flag to indicate that this property is to be considered
     * transitive - that is, it is defined by the DAML class TransitiveProperty.
     *
     * @param transitive True for a transitive property
     */
    public void setIsTransitive( boolean transitive ) {
        if (transitive) {
            // add the transitive type to this property
            setRDFType( getVocabulary().TransitiveProperty(), false );
        }
        else {
            // remove the transitive type from this property
            removeProperty( RDF.type, getVocabulary().TransitiveProperty() );
        }
    }


    /**
     * Answer true if this property is to be considered transitive, that is
     * it is characterised by the DAML class TransitiveProperty
     *
     * @return True if this property is transitive
     */
    public boolean isTransitive() {
        return hasRDFType( getVocabulary().TransitiveProperty() )  ||
               DAMLHierarchy.getInstance().isTransitiveProperty( this );
    }


    /**
     * Set the flag to indicate that this property is to be considered
     * unambiguous - that is, it is defined by the DAML class UnambiguousProperty.
     *
     * @param unambiguous True for a unambiguous property
     */
    public void setIsUnambiguous( boolean unambiguous ) {
        if (unambiguous) {
            // add the transitive type to this property
            setRDFType( getVocabulary().UnambiguousProperty(), false );
        }
        else {
            // remove the transitive type from this property
            removeProperty( RDF.type, getVocabulary().UnambiguousProperty() );
        }
    }


    /**
     * Answer true if this property is to be considered unabiguous, that is
     * it is characterised by the DAML class UnambiguousProperty
     *
     * @return True if this property is unambiguous
     */
    public boolean isUnambiguous() {
        return hasRDFType( getVocabulary().UnambiguousProperty() );
    }


    /**
     * Property accessor for the 'inverseOf' property of a DAML Property. This denotes
     * that the named property (say, P) is an inverse of this property (say, Q). Formally,
     * if (x, y) is an instance of P, then (y, x) is an instance of Q.
     *
     * @return Property accessor for 'inverseOf'
     */
    public PropertyAccessor prop_inverseOf() {
        if (m_propInverseOf == null) {
            m_propInverseOf = new PropertyAccessorImpl( getVocabulary().inverseOf(), this );
        }

        return m_propInverseOf;
    }


    /**
     * Answer a key that can be used to index collections of this DAML property for
     * easy access by iterators.  Package access only.
     *
     * @return a key object.
     */
    Object getKey() {
        return DAML_OIL.Property.getURI();
    }




    // Internal implementation methods
    //////////////////////////////////




    //==============================================================================
    // Inner class definitions
    //==============================================================================


}
