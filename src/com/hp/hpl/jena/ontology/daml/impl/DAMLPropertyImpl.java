/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            4 Jan 2001
 * Filename           $RCSfile: DAMLPropertyImpl.java,v $
 * Revision           $Revision: 1.9 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2005-02-21 12:05:28 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml.impl;


// Imports
///////////////
import java.util.*;

import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.daml.*;
import com.hp.hpl.jena.ontology.impl.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.*;


/**
 * <p>Encapsulates a property in a DAML ontology.  According to the specification,
 * a daml:Property is an alias for rdf:Property.  It also acts as the super-class for
 * more semantically meaningful property classes: datatype properties and object properties.
 * The DAML spec also allows any property to be unique (that is, it defines UniqueProperty
 * as a sub-class of Property), so uniqueness is modelled here as an attribute of a DAMLProperty.</p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLPropertyImpl.java,v 1.9 2005-02-21 12:05:28 andy_seaborne Exp $
 */
public class DAMLPropertyImpl
    extends OntPropertyImpl
    implements DAMLProperty
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating DAMLProperty facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use 
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    public static Implementation factory = new Implementation() {
        public EnhNode wrap( Node n, EnhGraph eg ) { 
            if (canWrap( n, eg )) {
                return new DAMLPropertyImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n.toString() + " to DAMLProperty" );
            } 
        }
            
        public boolean canWrap( Node node, EnhGraph eg ) {
            return hasType( node, eg, DAML_OIL.Property ) ||
                   hasType( node, eg, DAML_OIL.DatatypeProperty ) ||
                   hasType( node, eg, DAML_OIL.ObjectProperty );
        }
    };


    // Instance variables
    //////////////////////////////////

    /** Vocabulary */
    private DAMLVocabulary m_vocabulary = VocabularyManager.getDefaultVocabulary();
    
    /** Property accessor for domain */
    private PropertyAccessor m_propDomain = new PropertyAccessorImpl( getVocabulary().domain(), this );

    /** Property accessor for range */
    private PropertyAccessor m_propRange = new PropertyAccessorImpl( getVocabulary().range(), this );

    /** Property accessor for subPropertyOf */
    private PropertyAccessor m_propSubPropertyOf = new PropertyAccessorImpl( getVocabulary().subPropertyOf(), this );

    /** Property accessor for samePropertyAs */
    private PropertyAccessor m_propSamePropertyAs = new PropertyAccessorImpl( getVocabulary().samePropertyAs(), this );

    /** DAMLCommon delegate */
    private DAMLCommon m_common = null;
    
    
    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a DAML property represented by the given node in the given graph.
     * </p>
     * 
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public DAMLPropertyImpl( Node n, EnhGraph g ) {
        super( n, g );
        m_common = new DAMLCommonImpl( n, g );
    }



    // External signature methods
    //////////////////////////////////

    // delegate to DAMLCommon what we can
    /** @deprecated */
    public void setRDFType( Resource rdfClass, boolean replace ) { m_common.setRDFType( rdfClass, replace ); }
    public DAMLModel getDAMLModel()                              { return m_common.getDAMLModel(); }
    public ExtendedIterator getRDFTypes( boolean complete )      { return m_common.getRDFTypes( complete ); }
    public DAMLVocabulary getVocabulary()                        { return m_vocabulary; }
    public LiteralAccessor prop_label()                          { return m_common.prop_label(); }
    public LiteralAccessor prop_comment()                        { return m_common.prop_comment(); }
    public PropertyAccessor prop_equivalentTo()                  { return m_common.prop_equivalentTo(); }
    public PropertyAccessor prop_type()                          { return m_common.prop_type(); }
    
    /**
     * <p>Answer an iterator over all of the DAML objects that are equivalent to this
     * class, which will be the union of <code>daml:equivalentTo</code> and
     * <code>daml:sameClassAs</code>.</p>
     *
     * @return an iterator ranging over every equivalent DAML class
     */
    public ExtendedIterator getEquivalentValues() {
        ConcatenatedIterator i = new ConcatenatedIterator(
                       // first the iterator over the equivalentTo values
                       m_common.getEquivalentValues(),
                       // followed by the samePropertyAs values
                       getSameProperties() );

        return UniqueExtendedIterator.create( i ).mapWith( new AsMapper( DAMLProperty.class ) );
    }


    /**
     * Answer the set of equivalent values to this value, but not including the
     * value itself.  The iterator will range over a set: each element occurs only
     * once.
     *
     * @return An iteration ranging over the set of values that are equivalent to this
     *         value, but not itself.
     */
    public ExtendedIterator getEquivalenceSet() {
        Set s = new HashSet();

        s.add( this );
        for (Iterator i = getEquivalentValues();  i.hasNext();  s.add( i.next() ) );
        s.remove( this );
        
        return WrappedIterator.create( s.iterator() );
    }




    /**
     * Set the flag to indicate that this property is to be considered
     * unique - that is, it is defined by the DAML class UniqueProperty.
     *
     * @param unique True for a unique property
     */
    public void setIsUnique( boolean unique ) {
        if (unique) {
            // add the unique type to this property
            addRDFType( getVocabulary().UniqueProperty() );
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
        return m_propDomain;
    }


    /**
     * Property accessor for the 'subPropertyOf' property of a property. This
     * denotes the property that is the super-property of this property
     *
     * @return Property accessor for 'subPropertyOf'.
     */
    public PropertyAccessor prop_subPropertyOf() {
        return m_propSubPropertyOf;
    }


    /**
     * Property accessor for the 'samePropertyAs' property of a property. This
     * denotes that two properties should be considered equivalent.
     *
     * @return Property accessor for 'samePropertyAs'.
     */
    public PropertyAccessor prop_samePropertyAs() {
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
        return m_propRange;
    }


    /**
     * <p>Answer an iterator over all of the DAML properties that are equivalent to this
     * value under the <code>daml:samePropertyAs</code> relation.  Note: only considers
     * <code>daml:samePropertyAs</code>, for general equivalence, see
     * {@link #getEquivalentValues}.  Note also that the first member of the iteration is
     * always the DAMLProperty on which the method is invoked: trivially, a property is
     * a member of the set of properties equivalent to itself.  If the caller wants
     * the set of properties equivalent to this one, not including itself, simply ignore
     * the first element of the iteration.</p>
     *
     * @return an iterator ranging over every equivalent DAML property.
     */
    public ExtendedIterator getSameProperties() {
        return WrappedIterator.create( super.listEquivalentProperties() ).mapWith( new AsMapper( DAMLProperty.class ) );
    }


    /**
     * Answer an iterator over all of the super-properties of this property, using the
     * <code>rdfs:subPropertyOf</code> relation (or one of its aliases).   The set of super-properties
     * is transitively closed over the subPropertyOf relation.
     *
     * @return An iterator over the super-properties of this property,
     *         whose values will be DAMLProperties.
     */
    public ExtendedIterator getSuperProperties() {
        return getSuperProperties( true );
    }


    /**
     * <p>Answer an iterator over all of the super-properties of this property.</p>
     * <p><strong>Note:</strong> In a change to the Jena 1 DAML API, whether
     * this iterator includes <em>inferred</em> super-properties is determined
     * not by a flag at the API level, but by the construction of the DAML
     * model itself.  See {@link ModelFactory} for details. The boolean parameter
     * <code>closed</code> is now re-interpreted to mean the inverse of <code>
     * direct</code>, see {@link OntClass#listSubClasses(boolean)} for more details.
     * </p>
     * 
     * @param closed If true, return all available values; otherwise, return
     * only local (direct) super-properties. See note for details.
     * @return An iterator over this property's super-properties.
     */
    public ExtendedIterator getSuperProperties( boolean closed ) {
        return WrappedIterator.create( listSuperProperties( !closed ) ).mapWith( new AsMapper( DAMLProperty.class ) );
    }


    /**
     * <p>Answer an iterator over all of the sub-properties of this property.</p>
     *
     * @return An iterator over the sub-properties of this property.
     */
    public ExtendedIterator getSubProperties() {
        return getSubProperties( true );
    }


    /**
     * <p>Answer an iterator over all of the sub-properties of this property.</p>
     * <p><strong>Note:</strong> In a change to the Jena 1 DAML API, whether
     * this iterator includes <em>inferred</em> sub-properties is determined
     * not by a flag at the API level, but by the construction of the DAML
     * model itself.  See {@link ModelFactory} for details. The boolean parameter
     * <code>closed</code> is now re-interpreted to mean the inverse of <code>
     * direct</code>, see {@link OntClass#listSubClasses(boolean)} for more details.
     * </p>
     * 
     * @param closed If true, return all available values; otherwise, return
     * only local (direct) sub-properties. See note for details.
     * @return An iterator over this property's sub-properties.
     */
    public ExtendedIterator getSubProperties( boolean closed ) {
        return WrappedIterator.create( listSubProperties( !closed ) ).mapWith( new AsMapper( DAMLProperty.class ) );
    }


    /**
     * <p>Answer an iterator over all of the DAML classes that form the domain of this
     * property.  The actual domain of the relation denoted by this property is the
     * conjunction of all of the classes mention by the RDFS:domain property of this
     * DAML property and all of its super-properties.</p>
     *
     * @return an iterator whose values will be the DAML classes that define the domain
     *         of the relation
     */
    public ExtendedIterator getDomainClasses() {
        return WrappedIterator.create( listPropertyValues( getProfile().DOMAIN() ) ).mapWith( new AsMapper( DAMLClass.class ) );
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
    public ExtendedIterator getRangeClasses() {
        return WrappedIterator.create( listPropertyValues( getProfile().RANGE() ) ).mapWith( new AsMapper( DAMLClass.class ) );
    }

     


    // Internal implementation methods
    //////////////////////////////////



    //==============================================================================
    // Inner class definitions
    //==============================================================================


}

/*
    (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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

