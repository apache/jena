/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            4 Jan 2001
 * Filename           $RCSfile: DAMLPropertyImpl.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-01-23 15:14:21 $
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
package com.hp.hpl.jena.ontology.daml.common;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFException;
import com.hp.hpl.jena.rdf.model.Resource;

import java.util.Iterator;

import com.hp.hpl.jena.ontology.daml.DAMLModel;
import com.hp.hpl.jena.ontology.daml.DAMLProperty;
import com.hp.hpl.jena.ontology.daml.PropertyAccessor;
import com.hp.hpl.jena.ontology.daml.PropertyIterator;

import com.hp.hpl.jena.util.iterator.ConcatenatedIterator;

import com.hp.hpl.jena.vocabulary.DAML_OIL;
import com.hp.hpl.jena.vocabulary.DAMLVocabulary;
import com.hp.hpl.jena.vocabulary.RDF;


/**
 * Java encapsulation of a property in a DAML ontology.  In DAML, properties are
 * first-class values in their own right, and not just aspects of classes.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLPropertyImpl.java,v 1.2 2003-01-23 15:14:21 ian_dickinson Exp $
 */
public class DAMLPropertyImpl
    extends DAMLCommonImpl
    implements DAMLProperty
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////

    /** Property accessor for domain */
    private PropertyAccessor m_propDomain = null;

    /** Property accessor for range */
    private PropertyAccessor m_propRange = null;

    /** Property accessor for subPropertyOf */
    private PropertyAccessor m_propSubPropertyOf = null;

    /** Property accessor for samePropertyAs */
    private PropertyAccessor m_propSamePropertyAs = null;



    // Constructors
    //////////////////////////////////

    /**
     * Constructor, takes the name and namespace for this property, and the underlying
     * model it will be attached to.
     *
     * @param namespace The namespace the property inhabits, or null
     * @param name The name of the property
     * @param store The RDF store that contains the RDF statements defining the properties of the property
     * @param vocabulary Reference to the DAML vocabulary used by this property.
     */
    public DAMLPropertyImpl( String namespace, String name, DAMLModel store, DAMLVocabulary vocabulary ) {
        super( namespace, name, store, vocabulary );
        setRDFType( getVocabulary().Property() );
    }


    /**
     * Constructor, takes the URI for this property, and the underlying
     * model it will be attached to.
     *
     * @param uri The URI of the property
     * @param store The RDF store that contains the RDF statements defining the properties of the property
     * @param vocabulary Reference to the DAML vocabulary used by this property.
     */
    public DAMLPropertyImpl( String uri, DAMLModel store, DAMLVocabulary vocabulary ) {
        super( uri, store, vocabulary );
        setRDFType( getVocabulary().Property() );
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

        // now format the return string
        return (getURI() == null) ?
                   ("<" + attribs + "Anonymous " + cName + "@" + Integer.toHexString( hashCode() ) + ">") :
                   ("<" + attribs + cName + " " + getURI() + ">");
    }


    /**
     * Set the flag to indicate that this property is to be considered
     * unique - that is, it is defined by the DAML class UniqueProperty.
     *
     * @param unique True for a unique property
     */
    public void setIsUnique( boolean unique ) {
        if (unique) {
            // add the transitive type to this property
            setRDFType( getVocabulary().UniqueProperty(), false );
        }
        else {
            // remove the transitive type from this property
            removeProperty( RDF.type, getVocabulary().UniqueProperty() );
        }
    }


    /**
     * Answer true if this property is to be considered unique, that is
     * it is characterised by the DAML class UniqueProperty
     *
     * @return True if this property is unique
     */
    public boolean isUnique() {
        return hasRDFType( getVocabulary().UniqueProperty() );
    }


    /**
     * Property accessor for the 'domain' property of a property. This
     * denotes the class that is the domain of the relation denoted by
     * the property.
     *
     * @return Property accessor for 'domain'.
     */
    public PropertyAccessor prop_domain() {
        if (m_propDomain == null) {
            m_propDomain = new PropertyAccessorImpl( getVocabulary().domain(), this );
        }

        return m_propDomain;
    }


    /**
     * Property accessor for the 'subPropertyOf' property of a property. This
     * denotes the property that is the super-property of this property
     *
     * @return Property accessor for 'subPropertyOf'.
     */
    public PropertyAccessor prop_subPropertyOf() {
        if (m_propSubPropertyOf == null) {
            m_propSubPropertyOf = new PropertyAccessorImpl( getVocabulary().subPropertyOf(), this );
        }

        return m_propSubPropertyOf;
    }


    /**
     * Property accessor for the 'samePropertyAs' property of a property. This
     * denotes that two properties should be considered equivalent.
     *
     * @return Property accessor for 'samePropertyAs'.
     */
    public PropertyAccessor prop_samePropertyAs() {
        if (m_propSamePropertyAs == null) {
            m_propSamePropertyAs = new PropertyAccessorImpl( getVocabulary().samePropertyAs(), this );
        }

        return m_propSamePropertyAs;
    }


    /**
     * Property accessor for the 'range' property of a property. This
     * denotes the class that is the range of the relation denoted by
     * the property.
     *
     * @return Property accessor for 'range'.
     */
    public PropertyAccessor prop_range() {
        if (m_propRange == null) {
            m_propRange = new PropertyAccessorImpl( getVocabulary().range(), this );
        }

        return m_propRange;
    }


    /**
     * Answer an iterator over all of the DAML properties that are equivalent to this
     * value under the <code>daml:samePropertyAs</code> relation.  Note: only considers
     * <code>daml:samePropertyAs</code>, for general equivalence, see
     * {@link #getEquivalentValues}.  Note that the first member of the iteration is
     * always the DAMLProperty on which the method is invoked: trivially, a property is
     * a member of the set of properties equivalent to itself.  If the caller wants
     * the set of properties equivalent to this one, not including itself, simply ignore
     * the first element of the iteration.
     *
     * @return an iterator ranging over every equivalent DAML property - each value of
     *         the iteration will be a DAMLProperty object.
     */
    public Iterator getSameProperties() {
        return new PropertyIterator( this, getVocabulary().samePropertyAs(), getVocabulary().samePropertyAs(), true, true );
    }


    /**
     * Answer an iterator over all of the DAML objects that are equivalent to this
     * property, which will be the union of <code>daml:equivalentTo</code> and
     * <code>daml:samePropertyAs</code>.
     *
     * @return an iterator ranging over every equivalent DAML class - each value of
     *         the iteration should be a DAMLProperty object.
     */
    public Iterator getEquivalentValues() {
        ConcatenatedIterator i = new ConcatenatedIterator(
                       // first the iterator over the equivalentTo values
                       super.getEquivalentValues(),
                       // followed by the samePropertyAs values
                       new PropertyIterator( this, getVocabulary().samePropertyAs(), getVocabulary().samePropertyAs(), true, false, false ) );

        // ensure that the iteration includes self
        i.setDefaultValue( this );

        return i;
    }


    /**
     * Answer an iterator over all of the super-properties of this property, using the
     * <code>rdfs:subPropertyOf</code> relation (or one of its aliases).   The set of super-properties
     * is transitively closed over the subPropertyOf relation.
     *
     * @return An iterator over the super-properties of this property,
     *         whose values will be DAMLProperties.
     */
    public Iterator getSuperProperties() {
        return getSuperProperties( true );
    }


    /**
     * Answer an iterator over all of the super-properties of this property, using the
     * <code>rdfs:subPropertyOf</code> relation (or one of its aliases).   The set of super-properties
     * is optionally transitively closed over the subPropertyOf relation.
     *
     * @param closed If true, iterate over the super-properties of my super-properties, etc.
     * @return An iterator over the super-properties of this property,
     *         whose values will be DAMLProperties.
     */
    public Iterator getSuperProperties( boolean closed ) {
        return new PropertyIterator( this, getVocabulary().subPropertyOf(), null, closed, false, true );
    }


    /**
     * Answer an iterator over all of the sub-properties of this property, using the
     * <code>rdfs:subPropertyOf</code> relation (or one of its aliases).   The set of sub-properties
     * is transitively closed over the subPropertyOf relation.
     *
     * @return An iterator over the sub-properties of this property,
     *         whose values will be DAMLProperties.
     */
    public Iterator getSubProperties() {
        return getSubProperties( true );
    }


    /**
     * Answer an iterator over all of the sub-properties of this property, using the
     * <code>rdfs:subPropertyOf</code> relation (or one of its aliases).   The set of sub-properties
     * is optionally transitively closed over the subPropertyOf relation.
     *
     * @param closed If true, iterate over the sub-properties of my sub-properties, etc.
     * @return An iterator over the sub-properties of this property,
     *         whose values will be DAMLProperties.
     */
    public Iterator getSubProperties( boolean closed ) {
        return new PropertyIterator( this, null, getVocabulary().subPropertyOf(), closed, false, true );
    }


    /**
     * Answer an iterator over all of the DAML classes that form the domain of this
     * property.  The actual domain of the relation denoted by this property is the
     * conjunction of all of the classes mention by the RDFS:domain property of this
     * DAML property and all of its super-properties.
     *
     * @return an iterator whose values will be the DAML classes that define the domain
     *         of the relation
     */
    public Iterator getDomainClasses() {
        // first we want an iteration of this property and all of its super-properties
        Iterator i = new ConcatenatedIterator(
                             getSelfIterator(),
                             getSuperProperties() );

        // from this starting point, get all domain values
        return new PropertyIterator( i, getVocabulary().domain(), null, false, false );
    }


    /**
     * Answer an iterator over all of the DAML classes that form the range of this
     * property.  The actual range of the relation denoted by this property is the
     * conjunction of all of the classes mention by the RDFS:range property of this
     * DAML property and all of its super-properties.
     *
     * @return an iterator whose values will be the DAML classes that define the range
     *         of the relation
     */
    public Iterator getRangeClasses() {
        // first we want an iteration of this property and all of its super-properties
        Iterator i = new ConcatenatedIterator(
                             getSelfIterator(),
                             getSuperProperties() );

        // from this starting point, get all domain values
        return new PropertyIterator( i, getVocabulary().range(), null, false, false );
    }


     public boolean isProperty() {
     	return true;
     }
    /**
     * Answers the ordinal value of a containment property.  Since DAML properties
     * are not used as containment properties, this method always returns 0. See
     * {@link com.hp.hpl.jena.rdf.model.Property#getOrdinal getOrdinal} for
     * more details.
     *
     * @return zero.
     */
    public int getOrdinal() {
        return 0;
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



    /**
     * This is a Jena internal method.  I need to override the one in ResourceImpl
     * to prevent a ClassCastException in some circumstances.
     *
     * @param m The model to port to
     * @return The ported resource
     * @throws RDFException if an RDF processing error ocurrs.
     */
    public Resource port( Model m )
        throws RDFException
    {
        if ( getModel() == m ) {
            return this;
        }
        else {
            if (m instanceof DAMLModel) {
                return new DAMLPropertyImpl( getNameSpace(), getLocalName(), ((DAMLModel) m), getVocabulary() );
            }
            else {
                throw new RDFException( 0, "Cannot port DAML object to non-DAML model." );
            }
        }
    }


    // Internal implementation methods
    //////////////////////////////////



    //==============================================================================
    // Inner class definitions
    //==============================================================================


}
