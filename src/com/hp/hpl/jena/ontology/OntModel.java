/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            10 Feb 2003
 * Filename           $RCSfile: OntModel.java,v $
 * Revision           $Revision: 1.10 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-04-30 09:58:34 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved. 
 * (see footer for full conditions)
 * ****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology;


// Imports
///////////////
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphFactory;
import com.hp.hpl.jena.rdf.model.*;

import java.util.*;



/**
 * <p>
 * An enhanced view of a Jena model that is known to contain ontology
 * data, under a given ontology {@link Profile vocabulary} (such as OWL).
 * This class does not by itself compute the deductive extension of the graph
 * under the semantic rules of the language.  Instead, we wrap an underlying
 * graph with this interface that presents a convenience syntax for accessing
 * the language elements. Depending on the capability of the inference graph,
 * the OntModel will appear to contain more or less triples. For example, if
 * this class is used to wrap a plain memory or database model, only the
 * relationships asserted by the document will be reported through this
 * convenience API. Alternatively, if the OntModel wraps an inferencing graph,
 * the inferred triples from the extension will be reported as well. For
 * example, assume the following ontology fragment: <code><pre>     :a rdf:type
 * owl:Class .     :b rdf:type owl:Class ; rdfs:subClassOf :a .     :widget rdf:
 * type :b .
 * </pre></code>
 * In a non-inferencing graph, the <code>rdf:type</code> of the widget will be
 * reported as class <code>:b</code> only.  In a graph that can process the OWL
 * semantics, the widget's types will include <code>:b</code>, <code>:a</code>,
 * and <code>owl:Thing</code>.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OntModel.java,v 1.10 2003-04-30 09:58:34 ian_dickinson Exp $
 */
public interface OntModel
    extends Model
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Answer an iterator that ranges over the ontology resources in this model, i&#046;e&#046; 
     * the resources with <code>rdf:type Ontology</code> or equivalent. These resources
     * typically contain metadata about the ontology document that contains them. 
     * </p>
     * <p>
     * Specifically, the resources in this iterator will those whose type corresponds 
     * to the value given in the ontology vocabulary associated with this model, see
     * {@link Profile#ONTOLOGY}.
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over ontology resources. 
     */
    public Iterator listOntologies();
    

    /**
     * <p>
     * Answer an iterator that ranges over the property resources in this model, i&#046;e&#046; 
     * the resources with <code>rdf:type Property</code> or equivalent.  An <code>OntProperty</code>
     * is equivalent to an <code>rdfs:Property</code> in a normal RDF graph; this type is
     * provided as a common super-type for the more specific {@link ObjectProperty} and
     * {@link DatatypeProperty} property types.
     * </p>
     * <p>
     * Specifically, the resources in this iterator will those whose type corresponds 
     * to the value given in the ontology vocabulary associated with this model.
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over property resources. 
     */
    public Iterator listOntProperties();
    

    /**
     * <p>
     * Answer an iterator that ranges over the object property resources in this model, i&#046;e&#046; 
     * the resources with <code>rdf:type ObjectProperty</code> or equivalent.  An object
     * property is a property that is defined in the ontology language semantics as a 
     * one whose range comprises individuals (rather than datatyped literals).
     * </p>
     * <p>
     * Specifically, the resources in this iterator will those whose type corresponds 
     * to the value given in the ontology vocabulary associated with this model: see
     * {@link Profile#OBJECT_PROPERTY}.
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over object property resources. 
     */
    public Iterator listObjectProperties();
    

    /**
     * <p>
     * Answer an iterator that ranges over the datatype property resources in this model, i&#046;e&#046; 
     * the resources with <code>rdf:type DatatypeProperty</code> or equivalent.  An datatype
     * property is a property that is defined in the ontology language semantics as a 
     * one whose range comprises datatyped literals (rather than individuals).
     * </p>
     * <p>
     * Specifically, the resources in this iterator will those whose type corresponds 
     * to the value given in the ontology vocabulary associated with this model: see
     * {@link Profile#DATATYPE_PROPERTY}.
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over datatype property resources. 
     */
    public Iterator listDatatypeProperties();
    

    /**
     * <p>
     * Answer an iterator that ranges over the individual resources in this model, i&#046;e&#046; 
     * the resources with <code>rdf:type</code> corresponding to a class defined
     * in the ontology.
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over individual resources. 
     */
    public Iterator listIndividuals();
    

    /**
     * <p>
     * Answer an iterator that ranges over all of the various forms of class description resource 
     * in this model.  Class descriptions include {@link #listEnumeratedClasses enumerated}
     * classes, {@link #listUnionClasses union} classes, {@link #listComplementClasses complement}
     * classes, {@link #listIntersectionClasses intersection} classes, {@link #listClasses named}
     * classes and {@link #listRestrictions property restrictions}.
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over class description resources. 
     */
    public Iterator listClasses();
    

    /**
     * <p>
     * Answer an iterator that ranges over the enumerated class class-descriptions
     * in this model, i&#046;e&#046; the class resources specified to have a property
     * <code>oneOf</code> (or equivalent) and a list of values. 
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over enumerated class resources. 
     * @see Profile#ONE_OF
     */
    public Iterator listEnumeratedClasses();
    

    /**
     * <p>
     * Answer an iterator that ranges over the union class-descriptions
     * in this model, i&#046;e&#046; the class resources specified to have a property
     * <code>unionOf</code> (or equivalent) and a list of values. 
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over union class resources. 
     * @see Profile#UNION_OF
     */
    public Iterator listUnionClasses();
    

    /**
     * <p>
     * Answer an iterator that ranges over the complement class-descriptions
     * in this model, i&#046;e&#046; the class resources specified to have a property
     * <code>complementOf</code> (or equivalent) and a list of values. 
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over complement class resources. 
     * @see Profile#COMPLEMENT_OF
     */
    public Iterator listComplementClasses();
    

    /**
     * <p>
     * Answer an iterator that ranges over the intersection class-descriptions
     * in this model, i&#046;e&#046; the class resources specified to have a property
     * <code>intersectionOf</code> (or equivalent) and a list of values. 
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over complement class resources. 
     * @see Profile#INTERSECTION_OF
     */
    public Iterator listIntersectionClasses();
    

    /**
     * <p>
     * Answer an iterator that ranges over the named class-descriptions
     * in this model, i&#046;e&#046; resources with <code>rdf:type
     * Class</code> (or equivalent) and a node URI. 
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over named class resources. 
     */
    public Iterator listNamedClasses();
    

    /**
     * <p>
     * Answer an iterator that ranges over the property restriction class-descriptions
     * in this model, i&#046;e&#046; resources with <code>rdf:type
     * Restriction</code> (or equivalent). 
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over restriction class resources. 
     * @see Profile#RESTRICTION
     */
    public Iterator listRestrictions();
    
    
    /**
     * <p>
     * Answer an iterator that ranges over the properties in this model that are declared
     * to be annotation properties. Not all supported languages define annotation properties
     * (the category of annotation properties is chiefly an OWL innovation).
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over annotation properties. 
     * @see Profile#getAnnotationProperties()
     */
    public Iterator listAnnotationProperties();
    
   
    /**
     * <p>
     * Answer an iterator that ranges over the nodes that denote pair-wise disjointness between
     * sets of classes.
     * </p>
     * <p>
     * <strong>Note:</strong> the number of nodes returned by this iterator will vary according to
     * the completeness of the deductive extension of the underlying graph.  See class
     * overview for more details.
     * </p>
     * 
     * @return An iterator over AllDifferent nodes. 
     */
    public Iterator listAllDifferent();
    
   
    /**
     * <p>
     * Answer a resource that represents an ontology description node in this model. If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-graph of the ontology model. 
     * </p>
     * 
     * @param uri The uri for the ontology node. Conventionally, this corresponds to the base URI
     * of the document itself.
     * @return An Ontology resource.
     */
    public Ontology createOntology( String uri );
    
   
    /**
     * <p>
     * Answer a resource that represents an Indvidual node in this model. A new anonymous resource
     * will be created in the updateable sub-graph of the ontology model. 
     * </p>
     * 
     * @param cls Resource representing the ontology class to which the individual belongs
     * @return A new anoymous Individual of the given class.
     */
    public Individual createIndividual( Resource cls );
    
   
    /**
     * <p>
     * Answer a resource that represents an Individual node in this model. If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-graph of the ontology model. 
     * </p>
     * 
     * @param cls Resource representing the ontology class to which the individual belongs
     * @param uri The uri for the individual, or null for an anonymous individual.
     * @return An Individual resource.
     */
    public Individual createIndividual( Resource cls, String uri );
    
   
    /**
     * <p>
     * Answer a resource that represents an object property in this model.  An object property
     * is defined to have a range of individuals, rather than datatypes. 
     * If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-graph of the ontology model. 
     * </p>
     * 
     * @param uri The uri for the object property. May not be null.
     * @return An ObjectProperty resource.
     */
    public ObjectProperty createObjectProperty( String uri );
    
   
    /**
     * <p>
     * Answer a resource that represents datatype property in this model. A datattype property
     * is defined to have a range that is a concrete datatype, rather than an individual. 
     * If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-graph of the ontology model. 
     * </p>
     * 
     * @param uri The uri for the datatype property. May not be null.
     * @return A DatatypeProperty resource.
     */
    public DatatypeProperty createDatatypeProperty( String uri );
    
   
    /**
     * <p>
     * Answer a resource that represents an annotation property in this model. If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-graph of the ontology model. 
     * </p>
     * 
     * @param uri The uri for the annotation property.
     * @return An AnnotationProperty resource.
     */
    public AnnotationProperty createAnnotationProperty( String uri );
    
   
    /**
     * <p>
     * Answer a resource that represents an anonymous class description in this model. A new
     * anonymous resource of <code>rdf:type C</code>, where C is the class type from the
     * language profile.
     * </p>
     * 
     * @return An anonymous Class resource.
     */
    public OntClass createClass();
    
   
    /**
     * <p>
     * Answer a resource that represents a class description node in this model. If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-graph of the ontology model. 
     * </p>
     * 
     * @param uri The uri for the class node, or null for an anonymous class.
     * @return A Class resource.
     */
    public OntClass createClass( String uri );
    
   
    /**
     * <p>
     * Answer a resource that represents an anonymous property restriction in this model. A new
     * anonymous resource of <code>rdf:type R</code>, where R is the restriction type from the
     * language profile.
     * </p>
     * 
     * @return An anonymous Restriction resource.
     */
    public Restriction createRestriction();
    
   
    /**
     * <p>
     * Answer a resource that represents a property restriction in this model. If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-graph of the ontology model. 
     * </p>
     * 
     * @param uri The uri for the restriction node, or null for an anonymous restriction.
     * @return A Restriction resource.
     */
    public Restriction createRestriction( String uri );
    
   
    /**
     * <p>
     * Answer a new, anonymous node representing the fact that a given set of classes are all
     * pair-wise distinct.  <code>AllDifferent</code> is a feature of OWL only, and is something
     * of an anomoly in that it exists only to give a place to anchor the <code>distinctMembers</code>
     * property, which is the actual expression of the fact. 
     * </p>
     * 
     * @return A new AllDifferent resource
     */
    public AllDifferent createAllDifferent();
    
    
    /**
     * <p>
     * Answer a resource that represents a generic ontology node in this model. If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-graph of the ontology model. 
     * </p>
     * <p>
     * This is a generic method for creating any known ontology value.  The selector that determines
     * which resource to create is the same as as the argument to the {@link RDFNode#as as()} 
     * method: the Java class object of the desired abstraction.  For example, to create an
     * ontology class via this mechanism, use:
     * <code><pre>
     *     OntClass c = (OntClass) myModel.createOntResource( OntClass.class, null,
     *                                                        "http://example.org/ex#Parrot" );
     * </pre></code>
     * </p>
     * 
     * @param javaClass The Java class object that represents the ontology abstraction to create
     * @param rdfType Optional resource denoting the ontology class to which an individual or 
     * axiom belongs, if that is the type of resource being created.
     * @param uri The uri for the ontology resource, or null for an anonymous resource.
     * @return An ontology resource, of the type specified by the <code>javaClass</code>
     */
    public OntResource createOntResource( Class javaClass, Resource rdfType, String uri );

    
    /**
     * <p>
     * Answer a list of the imported URI's in this ontology model. Detection of <code>imports</code>
     * statments will be according to the local language profile
     * </p>
     * 
     * @return The imported ontology URI's as a list. Note that since the underlying graph is
     * not ordered, the order of values in the list in successive calls to this method is
     * not guaranteed to be preserved.
     */
    public List listImportedOntologyURIs();
    
    
    /**
     * <p>
     * Answer true if this model has had the given URI document imported into it. This is
     * important to know since an import only occurs once, and we also want to be able to
     * detect cycles of imports.
     * </p> 
     *
     * @param uri An ontology URI 
     * @return True if the document corresponding to the URI has been successfully loaded
     * into this model
     */
    public boolean hasLoadedImport( String uri );
    
    
    /**
     * <p>
     * Record that this model has now imported the document with the given
     * URI, so that it will not be re-imported in the future.
     * </p>
     * 
     * @param uri A document URI that has now been imported into the model.
     */
    public void addLoadedImport( String uri );
    
    
    /**
     * <p>
     * Answer the language profile (for example, OWL or DAML+OIL) that this model is 
     * working to.
     * </p>
     *  
     * @return A language profile
     */
    public Profile getProfile();
    
    
    /**
     * <p>
     * Answer the graph factory associated with this model (used for constructing the
     * constituent graphs of the imports closure).
     * </p>
     * 
     * @return The local graph factory
     */
    public GraphFactory getGraphFactory();
    
    
    /**
     * <p>
     * Read operation that is invoked while loading the imports closure of an
     * ontology model.  Not normally invoked by user code.
     * </p>
     *
     * @param uri The URI to load from 
     * @param readState The read-state for this operation
     */
    public void read( String uri, OntReadState readState );
    
    
    /**
     * <p>
     * Answer the sub-graphs of this model. A sub-graph is defined as a graph that
     * is used to contain the triples from an imported document.
     * </p>
     * 
     * @return A list of sub graphs for this ontology model
     */
    public List getSubGraphs();
    
    
    /**
     * <p>
     * Answer the base-graph of this model. The base-graph is the graph that
     * contains the triples read from the source document for this ontology.
     * </p>
     * 
     * @return The base-graph for this ontology model
     */
    public Graph getBaseGraph();
    
    
    /**
     * <p>
     * Answer the base model of this model. The base model is the model wrapping
     * the graph that contains the triples read from the source document for this 
     * ontology.  It is therefore the model that will be updated if statements are
     * added to a model that is built from a union of documents (via the 
     * <code>imports</code> statements in the source document).
     * </p>
     * 
     * @return The base model for this ontology model
     */
    public Model getBaseModel();
    
    
    /**
     * <p>
     * Add the given graph as one of the sub-graphs of this ontology union graph
     * </p>
     *
     * @param graph A sub-graph to add 
     */
    public void addSubGraph( Graph graph );
    
    
    /**
     * <p>
     * Answer true if this model is currently in <i>strict checking mode</i>. Strict
     * mode means
     * that converting a common resource to a particular language element, such as
     * an ontology class, will be subject to some simple syntactic-level checks for
     * appropriateness. 
     * </p>
     * 
     * @return True if in strict checking mode
     */
    public boolean strictMode();
    
    
    /**
     * <p>
     * Set the checking mode to strict or non-strict.
     * </p>
     * 
     * @param strict
     * @see #strictMode()
     */
    public void setStrictMode( boolean strict );
    
    
    /**
     * <p>
     * Answer a reference to the document manager that this model is using to manage
     * ontology &lt;-&gt; mappings, and to load the imports closure. <strong>Note</strong>
     * by default, an ontology model is constructed with a reference to the shared,
     * global document manager.  Thus changing the settings via this model's document
     * manager may affect other models also using the same instance.
     * </p>
     * @return A reference to this model's document manager
     */
    public OntDocumentManager getDocumentManager();
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

