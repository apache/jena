/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            5 Jan 2001
 * Filename           $RCSfile: DAMLCommon.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-06-10 12:24:10 $
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
import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.ontology.*;

import com.hp.hpl.jena.vocabulary.*;


/**
 * <p>Abstract super-class for all DAML resources (including properties).  Defines shared
 * implementations and common services, such as property manipulation, vocabulary
 * management and <code>rdf:type</code> management.  Also defines accessors for common
 * properties, including <code>comment</code>, <code>label</code>, and <code>equivalentTo</code>.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLCommon.java,v 1.3 2003-06-10 12:24:10 ian_dickinson Exp $
 */
public interface DAMLCommon
    extends OntResource
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer the DAML model wherein this value is stored.</p>
     *
     * @return a DAMLModel reference.
     */
    public DAMLModel getDAMLModel();


    /**
     * <p>Add an RDF type property for this node in the underlying model. If the replace flag
     * is true, this type will replace any current type property for the node. Otherwise,
     * the type will be in addition to any existing type property.</p>
     * <p>Deprecated in favour of {@link OntResource#addRDFType} for add, or 
     * {@link OntResource#setRDFType} for replace.</p>
     *
     * @param rdfClass The RDF resource denoting the class that will be new value for the rdf:type property.
     * @param replace  If true, the given class will replace any existing type property for this
     *                 value, otherwise it will be added as an extra type statement.
     * @deprecated Use {@link OntResource#addRDFType} or {@link OntResource#setRDFType}.
     */
    public void setRDFType( Resource rdfClass, boolean replace );


    /**
     * <p>Answer an iterator over all of the types to which this resource belongs. Optionally,
     * restrict the results to the most specific types, so that any class that is subsumed by
     * another class in this resource's set of types is not reported.</p>
     * <p><strong>Note:</strong> that the interpretation of the <code>complete</code> flag has
     * changed since Jena 1.x. Previously, the boolean flag was to generated the transitive 
     * closure of the class hierarchy; this is now handled by the underlyin inference graph
     * (if specified). Now the flag is used to restrict the returned values to the most-specific
     * types for this resource.</p>
     *
     * @param complete If true, return all known types; if false, return only the most-specific
     * types.
     * @return an iterator over the set of this value's classes
     */
    public Iterator getRDFTypes( boolean complete );


    /**
     * <p>Answer the value of a given RDF property for this DAML value, or null
     * if it doesn't have one.  The value is returned as an RDFNode, from which
     * the value can be extracted for literals.  If there is more than one RDF
     * statement with the given property for the current value, it is not defined
     * which of the values will be returned.</p>
     *
     * @param property An RDF property
     * @return An RDFNode whose value is the value, or one of the values, of the
     *         given property. If the property is not defined, or an error occurs,
     *         returns null.
     */
    public RDFNode getPropertyValue( Property property );


    /**
     * <p>Answer an iterator over the set of all values for a given RDF property. Each
     * value in the iterator will be an RDFNode, representing the value (object) of
     * each statement in the underlying model.</p>
     *
     * @param property The property whose values are sought
     * @return An Iterator over the values of the property
     */
    public NodeIterator getPropertyValues( Property property );


    /**
     * <p>Set the value of the given property of this DAML value to the given
     * value, encoded as an RDFNode.  Maintains the invariant that there is
     * at most one value of the property for a given DAML object, so existing
     * property values are first removed.  To add multiple properties to a
     * given DAML object, use
     * {@link Resource#addProperty( Property, RDFNode ) addProperty}.</p>
     *
     * @param property The property to update
     * @param value The new value of the property as an RDFNode, or null to
     *              effectively remove this property.
     */
    public void setPropertyValue( Property property, RDFNode value );


    /**
     * <p>Remove the specific property-value pair from this DAML resource.</p>
     *
     * @param property The property to be removed
     * @param value The specific value of the property to be removed
     */
    public void removeProperty( Property property, RDFNode value );


    /**
     * <p>Remove all the values for a given property on the principal resource.</p>
     *
     * @param prop The RDF resource that defines the property to be removed
     */
    public void removeAll( Property prop );


    /**
     * <p>Replace the value of the named property with the given value.  Any existing
     * values, if any, for the property are first removed.</p>
     *
     * @param prop The RDF property to be updated
     * @param value The new value.
     */
    public void replaceProperty( Property prop, RDFNode value );


    /**
     * <p>Answer the number of values a given property has with this value as subject.</p>
     *
     * @param property The property to be tested
     * @return The number of statements with this value as subject and the given
     *         property as relation.
     * @deprecated Use {@link OntResource#getCardinality} instead.
     */
    public int getNumPropertyValues( Property property );


    /**
     * <p>Answer an iterator over a set of resources that are the objects of statements
     * with subject this DAML object and predicate the given property. This method is
     * deprecated, since the deductive closure is now (i.e. from Jena 2 onwards) 
     * handled by the underlying inference graph
     *
     * @param property The property whose values are sought
     * @param closed Ignored.
     * @return An iterator of resources that are the objects of statements whose
     *         subject is this value and whose predicate
     *         is <code>property</code>
     * @deprecated Use {@link #getAll()} instead.
     */
    public Iterator getAll( Property property, boolean closed );


    /**
     * <p>Answer an iterator over a set of RDF nodes that are the objects of statements
     * with subject this DAML object and predicate the given property.</p>
     *
     * @param property The property whose values are sought
     * @return An iterator of resources that are the objects of statements whose
     *         subject is this value and whose predicate
     *         is <code>property</code>
     */
    public Iterator getAll( Property property );


    /**
     * <p>Answer the DAML+OIL vocabulary that corresponds to the namespace that this value
     * was declared in.</p>
     *
     * @return A vocabulary object
     */
    public DAMLVocabulary getVocabulary();


    /**
     * <p>Answer an iterator over all of the DAML objects that are equivalent to this
     * value under the <code>daml:equivalentTo</code> relation.
     * Note that the first member of the iteration is
     * always the DAML value on which the method is invoked: trivially, a value is
     * a member of the set of values equivalent to itself.  If the caller wants
     * the set of values equivalent to this one, not including itself, simply ignore
     * the first element of the iteration.</p>
     *
     * @return An iterator ranging over every equivalent DAML value
     */
    public Iterator getEquivalentValues();


    /**
     * <p>Answer the set of equivalent values to this value, but not including the
     * value itself.  The iterator will range over a set: each element occurs only
     * once.</p>
     *
     * @return An iteration ranging over the set of values that are equivalent to this
     *         value, but not itself.
     */
    public Iterator getEquivalenceSet();


    /**
     * <p>Remove the DAML object from the model.  All of the RDF statements with this
     * DAML value as its subject will be removed from the model, and this object will
     * be removed from the indexes.  It will be the responsibility of client code to
     * ensure that references to this object are removed so that the object itself
     * can be garbage collected.</p>
     */
    public void remove();


    /**
     * <p>
     * Answer true if this resource is a member of the class denoted by the
     * given class resource.  Includes all available types, so is equivalent to
     * <code><pre>
     * hasRDF( ontClass, false );
     * </pre></code>
     * </p>
     * 
     * @param ontClass Denotes a class to which this value may belong
     * @return True if this resource has the given class as one of its <code>rdf:type</code>'s.
     */
    public boolean hasRDFType( String uri );
    

    // Properties
    /////////////

    /**
     * <p>Accessor for the property of the label on the value, whose value
     * is a literal (string).</p>
     *
     * @return Literal accessor for the label property
     */
    public LiteralAccessor prop_label();

    /**
     * <p>Accessor for the property of the comment on the value, whose value
     * is a literal (string).</p>
     *
     * @return Literal accessor for the comment property
     */
    public LiteralAccessor prop_comment();

    /**
     * <p>Property accessor for the <code>equivalentTo</code> property. This
     * denotes that two terms have the same meaning. The DAML spec helpfully
     * says: <i>for equivalentTo(X, Y), read X is an equivalent term to Y</i>.
     *
     * @return Property accessor for <code>equivalentTo</code>.
     */
    public PropertyAccessor prop_equivalentTo();


    /**
     * <p>Property accessor for the <code>rdf:type</code> property of a DAML value.
     *
     * @return Property accessor for <code>rdf:type</code>
     */
    public PropertyAccessor prop_type();


}



/*
    (c) Copyright Hewlett-Packard Company 2002-2003
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

