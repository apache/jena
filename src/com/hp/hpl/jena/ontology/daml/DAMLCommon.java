/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            5 Jan 2001
 * Filename           $RCSfile: DAMLCommon.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-01-23 15:14:22 $
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
package com.hp.hpl.jena.ontology.daml;


// Imports
///////////////
import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.NodeIterator;

import com.hp.hpl.jena.vocabulary.DAMLVocabulary;


/**
 * Abstract super-class for all DAML resources (including properties).  Defines shared
 * implementations and common services, such as property manipulation, vocabulary
 * management and <code>rdf:type</code> management.  Also defines accessors for common
 * properties, such as comment, label, and equivalentTo.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLCommon.java,v 1.2 2003-01-23 15:14:22 ian_dickinson Exp $
 */
public interface DAMLCommon
    extends Resource
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

    /**
     * Answer the DAML model wherein this value is stored.
     *
     * @return a DAMLModel reference.
     */
    public DAMLModel getDAMLModel();


    /**
     * Set the RDF type property for this node in the underlying model, replacing any
     * existing type property.  To add a second or subsequent type statement to a resource,
     * use {@link #setRDFType( com.hp.hpl.jena.rdf.model.Resource, boolean )
     * setRDFType( Resource, false ) }.
     *
     * @param rdfClass The RDF resource denoting the new value for the rdf:type property,
     *                 which will replace any existing type property.
     */
    public void setRDFType( Resource rdfClass );


    /**
     * Add an RDF type property for this node in the underlying model. If the replace flag
     * is true, this type will replace any current type property for the node. Otherwise,
     * the type will be in addition to any existing type property.  Note that for most normal
     * uses, a DAML resource should have at most one rdf:type property.  One exception to this,
     * in the March 2001 release, is when DatatypeProperties are marked as unique, unambiguous
     * or transitive.  This is achieved by the use of two rdf type properties.
     *
     * @param rdfClass The RDF resource denoting the class that will be new value for the rdf:type property.
     * @param replace  If true, the given class will replace any existing type property for this
     *                 value, otherwise it will be added as an extra type statement.
     */
    public void setRDFType( Resource rdfClass, boolean replace );


    /**
     * Answer true if this DAML value is a member of the class denoted by the given URI.
     *
     * @param classURI String denoting the URI of the class to test against
     * @return true if it can be shown that this DAML value is a member of the class, via
     *         <code>rdf:type</code>.
     */
    public boolean hasRDFType( String classURI );


    /**
     * Answer true if this DAML value is a member of the class denoted by the
     * given DAML class object.  This will traverse the class hierarchy, until
     * every class and super-class for this
     * DAML value has been examined.  Depending on the depth of the hierarchy,
     * this may be an expensive operation. Cycles are detected, however, so it
     * is guaranteed to terminate.
     *
     * @param damlClass Denotes a class to which this value may belong
     * @return true if the value is a member of the class (or one of its sub-classes)
     *         via <code>rdf:type</code>.
     */
    public boolean hasRDFType( Resource damlClass );


    /**
     * Answer an iterator over all of the types to which this class belongs. Optionally,
     * generate a closure by considering the closure of the set of classes over the
     * class hierarchy (e.g. if 'fido' is the resource, the non-closed set of fido's
     * classes might be
     * <code>{Dog, Vaccinated}</code>,
     * i.e. the set of classes for which rdf:type statements exist for fido,
     * while the closed set might be
     * <code>{Dog, Vaccinated, Mammal, Pet, Vertebrate, Thing, MedicallyCertified}</code>
     *
     * @param closed If true, generate the closed set by considering the super-classes of
     *               the known classes of this value.
     * @return an iterator over the set of this value's classes
     */
    public Iterator getRDFTypes( boolean closed );


    /**
     * Answer the value of a given RDF property for this DAML value, or null
     * if it doesn't have one.  The value is returned as an RDFNode, from which
     * the value can be extracted for literals.  If there is more than one RDF
     * statement with the given property for the current value, it is not defined
     * which of the values will be returned.
     *
     * @param property An RDF property
     * @return An RDFNode whose value is the value, or one of the values, of the
     *         given property. If the property is not defined, or an error occurs,
     *         returns null.
     */
    public RDFNode getPropertyValue( Property property );


    /**
     * Answer an iterator over the set of all values for a given RDF property. Each
     * value in the iterator will be an RDFNode, representing the value (object) of
     * each statement in the underlying model.
     *
     * @param property The property whose values are sought
     * @return An Iterator over the values of the property
     */
    public NodeIterator getPropertyValues( Property property );


    /**
     * Set the value of the given property of this DAML value to the given
     * value, encoded as an RDFNode.  Maintains the invariant that there is
     * at most one value of the property for a given DAML object, so existing
     * property values are first removed.  To add multiple properties to a
     * given DAML object, use
     * {@link com.hp.hpl.jena.rdf.model.Resource#addProperty( com.hp.hpl.jena.rdf.model.Property, com.hp.hpl.jena.rdf.model.RDFNode ) addProperty}.
     *
     * @param property The property to update
     * @param value The new value of the property as an RDFNode, or null to
     *              effectively remove this property.
     */
    public void setPropertyValue( Property property, RDFNode value );


    /**
     * Remove the specific property-value pair from this DAML resource.
     *
     * @param property The property to be removed
     * @param value The specific value of the property to be removed
     */
    public void removeProperty( Property property, RDFNode value );


    /**
     * Remove all the values for a given property on the principal resource.
     *
     * @param prop The RDF resource that defines the property to be removed
     */
    public void removeAll( Property prop );


    /**
     * Replace the value of the named property with the given value.  All existing
     * values, if any, for the property are first removed.
     *
     * @param prop The RDF property to be updated
     * @param value The new value.
     */
    public void replaceProperty( Property prop, RDFNode value );


    /**
     * Answer the number of values a given property has with this value as subject.
     *
     * @param property The property to be tested
     * @return The number of statements with this value as subject and the given
     *         property as relation.
     */
    public int getNumPropertyValues( Property property );


    /**
     * Answer an iterator over a set of resources that are the objects of statements
     * with subject this DAML object and predicate the given property. Respects DAML
     * semantics of equivalence, transitivity and the property hierarchy.
     *
     * @param property The property whose values are sought
     * @param closed If true, and the given property is transitive, generate the
     *               closure over the given property from this value.
     * @return An iterator of resources that are the objects of statements whose
     *         subject is this value (or one of its equivalents) and whose predicate
     *         is <code>property</code> or one of its equivalents
     */
    public Iterator getAll( Property property, boolean closed );


    /**
     * Answer the DAML+OIL vocabulary that corresponds to the namespace that this value
     * was declared in.
     *
     * @return a vocabulary object
     */
    public DAMLVocabulary getVocabulary();


    /**
     * Answer an iterator over all of the DAML objects that are equivalent to this
     * value under the <code>daml:equivalentTo</code> relation.
     * Note that the first member of the iteration is
     * always the DAML value on which the method is invoked: trivially, a value is
     * a member of the set of values equivalent to itself.  If the caller wants
     * the set of values equivalent to this one, not including itself, simply ignore
     * the first element of the iteration.
     *
     * @return an iterator ranging over every equivalent DAML value - each value of
     *         the iteration will be a damlCommon object.
     */
    public Iterator getEquivalentValues();


    /**
     * Answer the set of equivalent values to this value, but not including the
     * value itself.  The iterator will range over a set: each element occurs only
     * once.
     *
     * @return An iteration ranging over the set of values that are equivalent to this
     *         value, but not itself.
     */
    public Iterator getEquivalenceSet();


    /**
     * Remove the DAML object from the model.  All of the RDF statements with this
     * DAML value as its subject will be removed from the model, and this object will
     * be removed from the indexes.  It will be the responsibility of client code to
     * ensure that references to this object are removed so that the object itself
     * can be garbage collected.
     */
    public void remove();



    // Properties
    /////////////

    /**
     * Accessor for the property of the label on the value, whose value
     * is a literal (string).
     *
     * @return Literal accessor for the label property
     */
    public LiteralAccessor prop_label();

    /**
     * Accessor for the property of the comment on the value, whose value
     * is a literal (string).
     *
     * @return Literal accessor for the comment property
     */
    public LiteralAccessor prop_comment();

    /**
     * Property accessor for the 'equivalentTo' property of a class. This
     * denotes that two terms have the same meaning. The spec helpfully
     * says: <i>for equivalentTo(X, Y), read X is an equivalent term to Y</i>.
     *
     * @return Property accessor for 'equivalentTo'.
     */
    public PropertyAccessor prop_equivalentTo();


    /**
     * Property accessor for the 'rdf:type' property of a DAML value.
     *
     * @return Property accessor for 'rdf:type'.
     */
    public PropertyAccessor prop_type();


}
