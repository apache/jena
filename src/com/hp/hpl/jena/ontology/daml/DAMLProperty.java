/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            4 Jan 2001
 * Filename           $RCSfile: DAMLProperty.java,v $
 * Revision           $Revision: 1.7 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2004-12-06 13:50:18 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml;


// Imports
///////////////
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


/**
 * <p>Encapsulates a property in a DAML ontology.  According to the specification,
 * a daml:Property is an alias for rdf:Property.  It also acts as the super-class for
 * more semantically meaningful property classes: datatype properties and object properties.
 * The DAML spec also allows any property to be unique (that is, it defines UniqueProperty
 * as a sub-class of Property), so uniqueness is modelled here as an attribute of a DAMLProperty.</p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLProperty.java,v 1.7 2004-12-06 13:50:18 andy_seaborne Exp $
 */
public interface DAMLProperty
    extends DAMLCommon, OntProperty
{
    // Constants
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////


    /**
     * <p>Set the flag to indicate that this property is to be considered
     * unique - that is, it is defined by the DAML class UniqueProperty.</p>
     *
     * @param unique True for a unique property
     */
    public void setIsUnique( boolean unique );


    /**
     * <p>Answer true if this property is to be considered unique, that is
     * it is characterised by the DAML class UniqueProperty</p>
     *
     * @return True if this property is unique
     */
    public boolean isUnique();


    /**
     * <p>Property accessor for the <code>domain</code> of a property. This
     * denotes the class that is the domain of the relation denoted by
     * the property.
     *
     * @return Property accessor for 'domain'.
     */
    public PropertyAccessor prop_domain();


    /**
     * <p>Property accessor for the <code>subPropertyOf</code> property of a property. This
     * denotes the property that is the super-property of this property.</p>
     *
     * @return Property accessor for <code>daml:subPropertyOf</code>
     */
    public PropertyAccessor prop_subPropertyOf();


    /**
     * <p>Property accessor for the <code>samePropertyAs</code> property of a DAML Property. This
     * denotes that the named property and this one have the same elements.</p>
     *
     * @return PropertyAccessor for <code>samePropertyAs</code>
     */
    public PropertyAccessor prop_samePropertyAs();


    /**
     * Property accessor for the <code>range</code> of a property. This
     * denotes the class that is the range of the relation denoted by
     * the property.
     *
     * @return Property accessor for <code>range</code>.
     */
    public PropertyAccessor prop_range();


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
    public ExtendedIterator getSameProperties();


    /**
     * <p>Answer an iterator over all of the DAML objects that are equivalent to this
     * property, which will be the union of <code>daml:equivalentTo</code> and
     * <code>daml:samePropertyAs</code>.</p>
     *
     * @return an iterator ranging over every equivalent DAML property.
     */
    public ExtendedIterator getEquivalentValues();


    /**
     * <p>Answer an iterator over all of the DAML classes that form the domain of this
     * property.  The actual domain of the relation denoted by this property is the
     * conjunction of all of the classes mention by the RDFS:domain property of this
     * DAML property and all of its super-properties.</p>
     *
     * @return an iterator whose values will be the DAML classes that define the domain
     *         of the relation
     */
    public ExtendedIterator getDomainClasses();


    /**
     * <p>Answer an iterator over all of the DAML classes that form the range of this
     * property.  The actual range of the relation denoted by this property is the
     * conjunction of all of the classes mention by the RDFS:range property of this
     * DAML property and all of its super-properties.</p>
     *
     * @return an iterator whose values will be the DAML classes that define the range
     *         of the relation
     */
    public ExtendedIterator getRangeClasses();


    /**
     * <p>Answer an iterator over all of the super-properties of this property, using the
     * <code>rdfs:subPropertyOf</code> relation (or one of its aliases).   The set of super-properties
     * is transitively closed over the subPropertyOf relation.</p>
     *
     * @return An iterator over the super-properties of this property,
     *         whose values will be DAMLProperties.
     */
    public ExtendedIterator getSuperProperties();


    /**
     * <p>Answer an iterator over all of the super-properties of this property.</p>
     * <p><strong>Note:</strong> In a change to the Jena 1 DAML API, whether
     * this iterator includes <em>inferred</em> super-properties is determined
     * not by a flag at the API level, but by the construction of the DAML
     * model itself.  See {@linkplain com.hp.hpl.jena.rdf.model.ModelFactory the model factory} 
     * for details. The boolean parameter
     * <code>closed</code> is now re-interpreted to mean the inverse of <code>
     * direct</code>, see {@link OntClass#listSubClasses(boolean)} for more details.
     * </p>
     * 
     * @param closed If true, return all available values; otherwise, return
     * only local (direct) super-properties. See note for details.
     * @return An iterator over this property's super-properties.
     */
    public ExtendedIterator getSuperProperties( boolean closed );


    /**
     * <p>Answer an iterator over all of the sub-properties of this property.</p>
     *
     * @return An iterator over the sub-properties of this property.
     */
    public ExtendedIterator getSubProperties();


    /**
     * <p>Answer an iterator over all of the sub-properties of this property.</p>
     * <p><strong>Note:</strong> In a change to the Jena 1 DAML API, whether
     * this iterator includes <em>inferred</em> sub-properties is determined
     * not by a flag at the API level, but by the construction of the DAML
     * model itself.  See {@linkplain com.hp.hpl.jena.rdf.model.ModelFactory the model factory} 
     * for details. The boolean parameter
     * <code>closed</code> is now re-interpreted to mean the inverse of <code>
     * direct</code>, see {@link OntClass#listSubClasses(boolean)} for more details.
     * </p>
     * 
     * @param closed If true, return all available values; otherwise, return
     * only local (direct) sub-properties. See note for details.
     * @return An iterator over this property's sub-properties.
     */
    public ExtendedIterator getSubProperties( boolean closed );
}


/*
    (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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

