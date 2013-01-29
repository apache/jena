/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Package
///////////////
package com.hp.hpl.jena.ontology;




// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


/**
 * <p>
 * Interface that encapsulates an <i>individual</i> in an ontology, sometimes
 * referred to as a fact or assertion, or a member of the <i>a-box</i>. In order
 * to be recognised as an individual, rather than a generic resource,
 * at least one <code>rdf:type</code> statement, referring to a known class,
 * must  be present in the model.
 * </p>
 */
public interface Individual
    extends OntResource
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////


    /**
     * <p>Set the ontology class for this individual, replacing any
     * existing class membership. Class membership is encoded using the
     * <code>rdf:type</code> property. Any existing statements for the RDF type
     * will first be removed.</p>
     *
     * @param cls The RDF resource denoting the new class to which this individual belongs,
     *                 which will replace any existing <code>rdf:type</code> property.
     */
    public void setOntClass( Resource cls );

    /**
     * <p>Add the given ontology class as one of the classes to which
     * this individual belongs. Class membership is encoded using the
     * <code>rdf:type</code> property. </p>
     *
     * @param cls An RDF resource denoting an additional class to which this individual
     * belongs.
     */
    public void addOntClass( Resource cls );

    /**
     * <p>
     * Answer an ontology class to which this individual belongs. If the individual
     * belongs to more than one class, which is common in ontology models using
     * a reasoner, then the return value will be one of
     * the possible values but <strong>it is not specified which one</strong>.
     * In the case of multiple classes, callers <strong>should not</strong> rely on
     * the return value being consistent, e.g. across runs, since it may
     * depend on the underlying hash indexes in the model. </p>
     * <p>This method considers any ontology class for the individual, not just
     * <em>direct</em> classes. It is equivalent to <code>getOntClass(false)</code>.
     * </p>
     *
     * @return A resource denoting the ontology class for this individual, or one of them if
     * more than one is defined.
     * @exception ConversionException if the return value is known to be an
     * ontology class, assuming strict type checking is turned on for the underlying
     * <code>OntModel</code>. See {@link OntModel#setStrictMode(boolean)}
     */
    public OntClass getOntClass();

    /**
     * <p>
     * Answer an ontology class to which this individual belongs. If the resource
     * belongs to more than one class, which is common in ontology models using
     * a reasoner, then the return value will be one of
     * the possible values but <strong>it is not specified which one</strong>.
     * In the case of multiple classes, callers <strong>should not</strong> rely on
     * the return value being consistent, e.g. across runs, since it may
     * depend on the underlying hash indexes in the model. </p>
     *
     * @param direct If <code>true</code>, only <em>direct</em> classes are considered.
     * A class is a direct class of this <code>Individual</code> if and only if
     * there is no other resource is both an <code>rdf:type</code> of this
     * individual and a sub-class of the candidate class.
     *
     * @return A resource denoting the ontology class for this individual, or one of them if
     * more than one is defined.
     * @exception ConversionException if the return value is known to be an
     * ontology class, assuming strict type checking is turned on for the underlying
     * <code>OntModel</code>. See {@link OntModel#setStrictMode(boolean)}
     */
    public OntClass getOntClass( boolean direct );

    /**
     * <p>
     * Answer an iterator over the ontology classes to which this individual belongs.
     * The members of the iterator will be {@link OntClass} objects.
     * </p>
     *
     * @param direct If true, only answer those resources that are direct types
     * of this individual, not the super-classes of the class etc.
     * @return An iterator over the set of this individual's classes. Each member
     * of the iteration will be an {@link OntClass}.
     */
    public <T extends OntClass> ExtendedIterator<T> listOntClasses( boolean direct );

    /**
     * <p>
     * Answer true if this individual is a member of the class denoted by the
     * given class resource.
     * </p>
     *
     * @param ontClass Denotes an ontology class to which this individual may belong
     * @param direct If true, only consider the direct types of this individual, ignoring
     * the super-classes of the stated types.
     * @return True if this individual is a member of the given class, possibly taking the
     * directness constraint into account.
     */
    public boolean hasOntClass( Resource ontClass, boolean direct );

    /**
     * <p>
     * Answer true if this individual is a member of the class denoted by the
     * given ontology class resource.  Not limited to only direct class relationships,
     * so this is equivalent to:
     * <code><pre>
     * hasOntClass( ontClass, false );
     * </pre></code>
     * </p>
     *
     * @param ontClass Denotes a class to which this individual may belong
     * @return True if this individual has the given class as one of its <code>rdf:type</code>'s.
     */
    public boolean hasOntClass( Resource ontClass );

    /**
     * <p>
     * Answer true if this individual is a member of the class denoted by the
     * given URI.</p>
     *
     * @param uri Denotes the URI of a class to which this value may belong
     * @return True if this individual has the given class as one of its <code>rdf:type</code>'s.
     */
    public boolean hasOntClass( String uri );

    /**
     * <p>Attempt to remove this <code>individual</code> as a member of the
     * given ontology class. This relationship is represented by a <code>rdf:type</code>
     * statement in the underlying model. If this relationship was originally
     * asserted, then removal will always succeed. However, if the <code>rdf:type</code>
     * relationship is entailed by the action of an attached reasoner, it may not be
     * possible to directly remove it. Callers should instead update the assertions
     * and axioms that entail the class membership relationship, and ensure the
     * reasoner gets chance to update the entailments.</p>
     * <p>If this individual is not a member of the given class, the
     * operation has no effect.</p>
     *
     * @param ontClass A resource denoting a class that that is to be removed from
     * the set of classes to which this individual belongs
     */
    public void removeOntClass( Resource ontClass );




}
