/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            4 Jan 2001
 * Filename           $RCSfile: DAMLProperty.java,v $
 * Revision           $Revision: 1.1.1.1 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2002-12-19 19:14:59 $
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

import java.util.Iterator;


/**
 * Encapsulates a property in a DAML ontology.  According to the specification,
 * a daml:Property is an alias for rdf:Property.  It also acts as the super-class for
 * more semantically meaningful property classes: datatype properties and object properties.
 * The DAML spec also allows any property to be unique (that is, it defines UniqueProperty
 * as a sub-class of Property), so uniqueness is modelled here as an attribute of a DAMLProperty.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLProperty.java,v 1.1.1.1 2002-12-19 19:14:59 bwm Exp $
 */
public interface DAMLProperty
    extends DAMLCommon, Property
{
    // Constants
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////


    /**
     * Set the flag to indicate that this property is to be considered
     * unique - that is, it is defined by the DAML class UniqueProperty.
     *
     * @param unique True for a unique property
     */
    public void setIsUnique( boolean unique );


    /**
     * Answer true if this property is to be considered unique, that is
     * it is characterised by the DAML class UniqueProperty
     *
     * @return True if this property is unique
     */
    public boolean isUnique();


    /**
     * Property accessor for the 'domain' property of a property. This
     * denotes the class that is the domain of the relation denoted by
     * the property.
     *
     * @return Property accessor for 'domain'.
     */
    public PropertyAccessor prop_domain();


    /**
     * Property accessor for the 'subPropertyOf' property of a property. This
     * denotes the property that is the super-property of this property
     *
     * @return Property accessor for 'subPropertyOf'.
     */
    public PropertyAccessor prop_subPropertyOf();


    /**
     * Property accessor for the 'samePropertyAs' property of a DAML Property. This
     * denotes that the named property and this one have the same elements.
     *
     * @return PropertyAccessor for 'samePropertyAs'
     */
    public PropertyAccessor prop_samePropertyAs();


    /**
     * Property accessor for the 'range' property of a property. This
     * denotes the class that is the range of the relation denoted by
     * the property.
     *
     * @return Property accessor for 'range'.
     */
    public PropertyAccessor prop_range();


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
    public Iterator getSameProperties();


    /**
     * Answer an iterator over all of the DAML objects that are equivalent to this
     * property, which will be the union of <code>daml:equivalentTo</code> and
     * <code>daml:samePropertyAs</code>.
     *
     * @return an iterator ranging over every equivalent DAML property - each value of
     *         the iteration should be a DAMLProperty object or one of its sub-classes.
     */
    public Iterator getEquivalentValues();


    /**
     * Answer an iterator over all of the DAML classes that form the domain of this
     * property.  The actual domain of the relation denoted by this property is the
     * conjunction of all of the classes mention by the RDFS:domain property of this
     * DAML property and all of its super-properties.
     *
     * @return an iterator whose values will be the DAML classes that define the domain
     *         of the relation
     */
    public Iterator getDomainClasses();


    /**
     * Answer an iterator over all of the DAML classes that form the range of this
     * property.  The actual range of the relation denoted by this property is the
     * conjunction of all of the classes mention by the RDFS:range property of this
     * DAML property and all of its super-properties.
     *
     * @return an iterator whose values will be the DAML classes that define the range
     *         of the relation
     */
    public Iterator getRangeClasses();


    /**
     * Answer an iterator over all of the super-properties of this property, using the
     * <code>rdfs:subPropertyOf</code> relation (or one of its aliases).   The set of super-properties
     * is transitively closed over the subPropertyOf relation.
     *
     * @return An iterator over the super-properties of this property,
     *         whose values will be DAMLProperties.
     */
    public Iterator getSuperProperties();


    /**
     * Answer an iterator over all of the super-properties of this property, using the
     * <code>rdfs:subPropertyOf</code> relation (or one of its aliases).   The set of super-properties
     * is optionally transitively closed over the subPropertyOf relation.
     *
     * @param closed If true, iterate over the super-properties of my super-properties, etc.
     * @return An iterator over the super-properties of this property,
     *         whose values will be DAMLProperties.
     */
    public Iterator getSuperProperties( boolean closed );


    /**
     * Answer an iterator over all of the sub-properties of this property, using the
     * <code>rdfs:subPropertyOf</code> relation (or one of its aliases).   The set of sub-properties
     * is transitively closed over the subPropertyOf relation.
     *
     * @return An iterator over the sub-properties of this property,
     *         whose values will be DAMLProperties.
     */
    public Iterator getSubProperties();


    /**
     * Answer an iterator over all of the sub-properties of this property, using the
     * <code>rdfs:subPropertyOf</code> relation (or one of its aliases).   The set of sub-properties
     * is optionally transitively closed over the subPropertyOf relation.
     *
     * @param closed If true, iterate over the sub-properties of my sub-properties, etc.
     * @return An iterator over the sub-properties of this property,
     *         whose values will be DAMLProperties.
     */
    public Iterator getSubProperties( boolean closed );
}
