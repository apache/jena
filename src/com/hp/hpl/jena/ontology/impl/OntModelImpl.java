/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            22 Feb 2003
 * Filename           $RCSfile: OntModelImpl.java,v $
 * Revision           $Revision: 1.44 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-08-25 08:31:30 $
 *               by   $Author: der $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.enhanced.*;

import java.io.*;
import java.net.*;
import java.util.*;



/**
 * <p>
 * Implementation of a model that can process general ontologies in OWL,
 * DAML and similar languages.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OntModelImpl.java,v 1.44 2003-08-25 08:31:30 der Exp $
 */
public class OntModelImpl
    extends ModelCom
    implements OntModel
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////

    /** The model specification this model is using to define its structure */
    protected OntModelSpec m_spec;
    
    /** List of URI strings of documents that have been imported into this one */
    protected Set m_imported = new HashSet();
    
    /** Query that will access nodes with types whose type is Class */
    protected BindingQueryPlan m_individualsQuery;
    
    /** Mode switch for strict checking mode */
    protected boolean m_strictMode = true;
    
    /** The union graph that contains the imports closure - there is always one of these, which may also be _the_ graph for the model */
    protected MultiUnion m_union = new MultiUnion();
    
    
    // Constructors
    //////////////////////////////////


    /**
     * <p>
     * Construct a new ontology model, using the given model as a base.  The document manager
     * given in the specification object
     * will be used to build the imports closure of the model if its policy permits.
     * </p>
     * 
     * @param model The base model that may contain existing statements for the ontology
     * @param spec A specification object that allows us to specify parameters and structure for the
     *              ontology model to be constructed.
     */
    public OntModelImpl( OntModelSpec spec, Model model ) {
        // we haven't built the full graph yet, so we pass a vestigial form up to the super constructor
        super( generateGraph( spec ), BuiltinPersonalities.model );
        m_spec = spec;
        
        // extract the union graph from whatever generateGraph() created
        m_union = (getGraph() instanceof MultiUnion) ?
                        ((MultiUnion) getGraph()) :
                        (MultiUnion) ((InfGraph) getGraph()).getRawGraph();
        
        // add the base model to the union, if we have one
        m_union.addGraph( (model != null) ? model.getGraph() : ModelFactory.createDefaultModel().getGraph() );

        // cache the query plan for individuals
        Profile lang = getProfile();
        m_individualsQuery = queryXTypeOfType( lang.CLASS() );
        
        // add the global prefixes, if required
        if (getDocumentManager().useDeclaredPrefixes()) {
            setNsPrefixes( getDocumentManager().getDeclaredPrefixMapping() );
        }
        
        // load the imports closure, according to the policies in my document manager
        getDocumentManager().loadImports( this );
        
        // force the inference engine, if we have one, to see the new graph data
        rebind();
    }
    
    
    
    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Answer a reference to the document manager that this model is using to manage
     * ontology &lt;-&gt; mappings, and to load the imports closure. <strong>Note</strong>
     * the default ontology model {@linkplain OntModelSpec specifications} each have 
     * a contained default document manager. Changing the document managers specified by 
     * these default specification may (in fact, probably will)
     * affect other models built with the same specification
     * policy. This may or may not be as desired by the programmer!
     * </p>
     * @return A reference to this model's document manager, obtained from the specification object
     */
    public OntDocumentManager getDocumentManager() {
        return m_spec.getDocumentManager();
    }
    
    
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
    public ExtendedIterator listOntologies() {
        checkProfileEntry( getProfile().ONTOLOGY(), "ONTOLOGY" );
        return findByTypeAs( getProfile().ONTOLOGY(), Ontology.class );
    }
    

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
    public ExtendedIterator listOntProperties() {
        return findByTypeAs( RDF.Property, OntProperty.class );
    }
    

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
    public ExtendedIterator listObjectProperties() {
        checkProfileEntry( getProfile().OBJECT_PROPERTY(), "OBJECT_PROPERTY" );
        return findByTypeAs( getProfile().OBJECT_PROPERTY(), ObjectProperty.class );
    }
    

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
    public ExtendedIterator listDatatypeProperties() {
        checkProfileEntry( getProfile().DATATYPE_PROPERTY(), "DATATYPE_PROPERTY" );
        return findByTypeAs( getProfile().DATATYPE_PROPERTY(), DatatypeProperty.class );
    }
    

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
     * @return An iterator over Individuals. 
     */
    public ExtendedIterator listIndividuals() {
        return queryFor( m_individualsQuery, null, Individual.class );
    }
    

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
    public ExtendedIterator listClasses() {
        return findByTypeAs( getProfile().getClassDescriptionTypes(), OntClass.class );
    }
    

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
    public ExtendedIterator listEnumeratedClasses()  {
        checkProfileEntry( getProfile().ONE_OF(), "ONE_OF" );
        return findByDefiningPropertyAs( getProfile().ONE_OF(), EnumeratedClass.class );
    }
    

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
    public ExtendedIterator listUnionClasses() {
        checkProfileEntry( getProfile().UNION_OF(), "UNION_OF" );
        return findByDefiningPropertyAs( getProfile().UNION_OF(), UnionClass.class );
    }
    

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
    public ExtendedIterator listComplementClasses() {
        checkProfileEntry( getProfile().COMPLEMENT_OF(), "COMPLEMENT_OF" );
        return findByDefiningPropertyAs( getProfile().COMPLEMENT_OF(), ComplementClass.class );
    }
    

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
    public ExtendedIterator listIntersectionClasses() {
        checkProfileEntry( getProfile().INTERSECTION_OF(), "INTERSECTION_OF" );
        return findByDefiningPropertyAs( getProfile().INTERSECTION_OF(), IntersectionClass.class );
    }
    

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
    public ExtendedIterator listNamedClasses() {
        return listClasses().filterDrop(
            new Filter() {
                public boolean accept( Object x ) {
                    return ((Resource) x).isAnon();
                }
            }
        );
    }
    

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
    public ExtendedIterator listRestrictions() {
        checkProfileEntry( getProfile().RESTRICTION(), "RESTRICTION" );
        return findByTypeAs( getProfile().RESTRICTION(), Restriction.class );
    }


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
    public ExtendedIterator listAllDifferent() {
        checkProfileEntry( getProfile().ALL_DIFFERENT(), "ALL_DIFFERENT" );
        return findByTypeAs( getProfile().ALL_DIFFERENT(), AllDifferent.class );
    }
    
   
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
    public ExtendedIterator listAnnotationProperties() {
        checkProfileEntry( getProfile().ANNOTATION_PROPERTY(), "ANNOTATION_PROPERTY" );
        Resource r = getProfile().ANNOTATION_PROPERTY();
        
        if (r == null) {
            return WrappedIterator.create( new ArrayList().iterator() ); 
        }
        else {
            return findByType( r )
                   .andThen( WrappedIterator.create( getProfile().getAnnotationProperties() ) )
                   .mapWith( new SubjectNodeAs( AnnotationProperty.class ) );
        }
    }
    
   
    /**
     * <p>
     * Answer a resource that represents an ontology description node in this model. If a resource
     * with the given uri exists in the model, and can be viewed as an Ontology, return the
     * Ontology facet, otherwise return null. 
     * </p>
     * 
     * @param uri The uri for the ontology node. Conventionally, this corresponds to the base URI
     * of the document itself.
     * @return An Ontology resource or null.
     */
    public Ontology getOntology( String uri ) {
        return (Ontology) findByURIAs( uri, Ontology.class );
    }
    
   
    /**
     * <p>
     * Answer a resource that represents an Individual node in this model. If a resource
     * with the given uri exists in the model, and can be viewed as an Individual, return the
     * Individual facet, otherwise return null. 
     * </p>
     * 
     * @param uri The URI for the requried individual 
     * @return An Individual resource or null.
     */
    public Individual getIndividual( String uri ) {
        return (Individual) findByURIAs( uri, Individual.class );
    }
    
   
    /**
     * <p>
     * Answer a resource representing an generic property in this model. If a property
     * with the given uri exists in the model, return the
     * OntProperty facet, otherwise return null. 
     * </p>
     * 
     * @param uri The uri for the property.
     * @return An OntProperty resource or null.
     */
    public OntProperty getOntProperty( String uri ) {
        return (OntProperty) findByURIAs( uri, OntProperty.class );
    }
    
   
    /**
     * <p>
     * Answer a resource representing an object property in this model. If a resource
     * with the given uri exists in the model, and can be viewed as an ObjectProperty, return the
     * ObjectProperty facet, otherwise return null. 
     * </p>
     * 
     * @param uri The uri for the object property. May not be null.
     * @return An ObjectProperty resource or null.
     */
    public ObjectProperty getObjectProperty( String uri ) {
        return (ObjectProperty) findByURIAs( uri, ObjectProperty.class );
    }
    
   
    /**
     * <p>Answer a resource representing a transitive property. If a resource
     * with the given uri exists in the model, and can be viewed as a TransitiveProperty, return the
     * TransitiveProperty facet, otherwise return null. </p>
     * @param uri The uri for the property. May not be null.
     * @return A TransitiveProperty resource or null
     */
    public TransitiveProperty getTransitiveProperty( String uri ) {
        return (TransitiveProperty) findByURIAs( uri, TransitiveProperty.class );
    }
    
    
    /**
     * <p>Answer a resource representing a symmetric property. If a resource
     * with the given uri exists in the model, and can be viewed as a SymmetricProperty, return the
     * SymmetricProperty facet, otherwise return null. </p>
     * @param uri The uri for the property. May not be null.
     * @return A SymmetricProperty resource or null
     */
    public SymmetricProperty getSymmetricProperty( String uri ) {
        return (SymmetricProperty) findByURIAs( uri, SymmetricProperty.class );
    }
    
    
    /**
     * <p>Answer a resource representing an inverse functional property. If a resource
     * with the given uri exists in the model, and can be viewed as a InverseFunctionalProperty, return the
     * InverseFunctionalProperty facet, otherwise return null. </p>
     * @param uri The uri for the property. May not be null.
     * @return An InverseFunctionalProperty resource or null
     */
    public InverseFunctionalProperty getInverseFunctionalProperty( String uri ) {
        return (InverseFunctionalProperty) findByURIAs( uri, InverseFunctionalProperty.class );
    }
    
    
    /**
     * <p>
     * Answer a resource that represents datatype property in this model. . If a resource
     * with the given uri exists in the model, and can be viewed as a DatatypeProperty, return the
     * DatatypeProperty facet, otherwise return null. 
     * </p>
     * 
     * @param uri The uri for the datatype property. May not be null.
     * @return A DatatypeProperty resource or null
     */
    public DatatypeProperty getDatatypeProperty( String uri ) {
        return (DatatypeProperty) findByURIAs( uri, DatatypeProperty.class );
    }
    
   
    /**
     * <p>
     * Answer a resource that represents an annotation property in this model. If a resource
     * with the given uri exists in the model, and can be viewed as an AnnotationProperty, return the
     * AnnotationProperty facet, otherwise return null. 
     * </p>
     * 
     * @param uri The uri for the annotation property. May not be null.
     * @return An AnnotationProperty resource or null
     */
    public AnnotationProperty getAnnotationProperty( String uri ) {
        return (AnnotationProperty) findByURIAs( uri, AnnotationProperty.class );
    }
    
   
    /**
     * <p>
     * Answer a resource that represents a class description node in this model. If a resource
     * with the given uri exists in the model, and can be viewed as an OntClass, return the
     * OntClass facet, otherwise return null. 
     * </p>
     * 
     * @param uri The uri for the class node, or null for an anonymous class.
     * @return An OntClass resource or null.
     */
    public OntClass getOntClass( String uri ) {
        return (OntClass) findByURIAs( uri, OntClass.class );
    }
    
   
    /**
     * <p>Answer a resource representing the class that is the complement of another class. If a resource
     * with the given uri exists in the model, and can be viewed as a ComplementClass, return the
     * ComplementClass facet, otherwise return null. </p>
     * @param uri The URI of the new complement class.
     * @return A complement class or null
     */
    public ComplementClass getComplementClass( String uri ) {
        return (ComplementClass) findByURIAs( uri, ComplementClass.class );
    }
    
   
    /**
     * <p>Answer a resource representing the class that is the enumeration of a list of individuals. If a resource
     * with the given uri exists in the model, and can be viewed as an EnumeratedClass, return the
     * EnumeratedClass facet, otherwise return null. </p>
     * @param uri The URI of the new enumeration class.
     * @return An enumeration class or null
     */
    public EnumeratedClass getEnumeratedClass( String uri ) {
        return (EnumeratedClass) findByURIAs( uri, EnumeratedClass.class );
    }
    
   
    /**
     * <p>Answer a resource representing the class that is the union of a list of class desctiptions. If a resource
     * with the given uri exists in the model, and can be viewed as a UnionClass, return the
     * UnionClass facet, otherwise return null. </p>
     * @param uri The URI of the new union class.
     * @return A union class description or null
     */
    public UnionClass getUnionClass( String uri ) {
        return (UnionClass) findByURIAs( uri, UnionClass.class );
    }
    
   
    /**
     * <p>Answer a resource representing the class that is the intersection of a list of class descriptions. If a resource
     * with the given uri exists in the model, and can be viewed as a IntersectionClass, return the
     * IntersectionClass facet, otherwise return null. </p>
     * @param uri The URI of the new intersection class.
     * @return An intersection class description or null
     */
    public IntersectionClass getIntersectionClass( String uri ) {
        return (IntersectionClass) findByURIAs( uri, IntersectionClass.class );
    }


    /**
     * <p>
     * Answer a resource that represents a property restriction in this model. If a resource
     * with the given uri exists in the model, and can be viewed as a Restriction, return the
     * Restriction facet, otherwise return null. 
     * </p>
     * 
     * @param uri The uri for the restriction node.
     * @return A Restriction resource or null
     */
    public Restriction getRestriction( String uri ) {
        return (Restriction) findByURIAs( uri, Restriction.class );
    }
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals that have the given
     * resource as the value of the given property. If a resource
     * with the given uri exists in the model, and can be viewed as a HasValueRestriction, return the
     * HasValueRestriction facet, otherwise return null. </p>
     * 
     * @param uri The URI for the restriction
     * @return A resource representing a has-value restriction or null
     */
    public HasValueRestriction getHasValueRestriction( String uri ) {
        return (HasValueRestriction) findByURIAs( uri, HasValueRestriction.class );
    }
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals that have at least
     * one property with a value belonging to the given class. If a resource
     * with the given uri exists in the model, and can be viewed as a SomeValuesFromRestriction, return the
     * SomeValuesFromRestriction facet, otherwise return null. </p>
     * 
     * @param uri The URI for the restriction
     * @return A resource representing a some-values-from restriction, or null
     */
    public SomeValuesFromRestriction getSomeValuesFromRestriction( String uri ) {
        return (SomeValuesFromRestriction) findByURIAs( uri, SomeValuesFromRestriction.class );
    }
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals for which all values
     * of the given property belong to the given class. If a resource
     * with the given uri exists in the model, and can be viewed as an AllValuesFromResriction, return the
     * AllValuesFromRestriction facet, otherwise return null. </p>
     * 
     * @param uri The URI for the restriction
     * @return A resource representing an all-values-from restriction or null
     */
    public AllValuesFromRestriction getAllValuesFromRestriction( String uri ) {
        return (AllValuesFromRestriction) findByURIAs( uri, AllValuesFromRestriction.class );
    }
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals that have exactly
     * the given number of values for the given property. If a resource
     * with the given uri exists in the model, and can be viewed as a CardinalityRestriction, return the
     * CardinalityRestriction facet, otherwise return null. </p>
     * 
     * @param uri The URI for the restriction
     * @return A resource representing a has-value restriction, or null
     */
    public CardinalityRestriction getCardinalityRestriction( String uri ) {
        return (CardinalityRestriction) findByURIAs( uri, CardinalityRestriction.class );
    }
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals that have at least
     * the given number of values for the given property. If a resource
     * with the given uri exists in the model, and can be viewed as a MinCardinalityRestriction, return the
     * MinCardinalityRestriction facet, otherwise return null. </p>
     * 
     * @param uri The URI for the restriction
     * @return A resource representing a min-cardinality restriction, or null
     */
    public MinCardinalityRestriction getMinCardinalityRestriction( String uri ) {
        return (MinCardinalityRestriction) findByURIAs( uri, MinCardinalityRestriction.class );
    }
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals that have at most
     * the given number of values for the given property. If a resource
     * with the given uri exists in the model, and can be viewed as a MaxCardinalityRestriction, return the
     * MaxCardinalityRestriction facet, otherwise return null.</p>
     * 
     * @param uri The URI for the restriction
     * @return A resource representing a mas-cardinality restriction, or null
     */
    public MaxCardinalityRestriction getMaxCardinalityRestriction( String uri ) {
        return (MaxCardinalityRestriction) findByURIAs( uri, MaxCardinalityRestriction.class );
    }


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
    public Ontology createOntology( String uri ) {
        checkProfileEntry( getProfile().ONTOLOGY(), "ONTOLOGY" );
        return (Ontology) createOntResource( Ontology.class, getProfile().ONTOLOGY(), uri );
    }
    
   
    /**
     * <p>
     * Answer a resource that represents an Indvidual node in this model. A new anonymous resource
     * will be created in the updateable sub-graph of the ontology model. 
     * </p>
     * 
     * @param cls Resource representing the ontology class to which the individual belongs
     * @return A new anoymous Individual of the given class.
     */
    public Individual createIndividual( Resource cls ) {
        return (Individual) createOntResource( Individual.class, cls, null );
    }
    
   
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
    public Individual createIndividual( String uri, Resource cls ) {
        return (Individual) createOntResource( Individual.class, cls, uri );
    }
    
   
    /**
     * <p>
     * Answer a resource representing an generic property in this model.  Effectively
     * this method is an alias for {@link #createProperty( String )}, except that
     * the return type is {@link OntProperty}, which allow more convenient access to
     * a property's position in the property hierarchy, domain, range, etc.
     * </p>
     * 
     * @param uri The uri for the property. May not be null.
     * @return An OntProperty resource.
     */
    public OntProperty createOntProperty( String uri ) {
        Property p = createProperty( uri );
        p.addProperty( RDF.type, getProfile().PROPERTY() );
        return (OntProperty) p.as( OntProperty.class );
    }
    
   
    /**
     * <p>
     * Answer a resource representing an object property in this model, 
     * and that is not a functional property. 
     * </p>
     * 
     * @param uri The uri for the object property. May not be null.
     * @return An ObjectProperty resource.
     * @see #createObjectProperty( String, boolean )
     */
    public ObjectProperty createObjectProperty( String uri ) {
        return createObjectProperty( uri, false );
    }
    
   
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
     * @param functional If true, the resource will also be typed as a {@link FunctionalProperty},
     * that is, a property that has a unique range value for any given domain value.
     * @return An ObjectProperty resource, optionally also functional.
     */
    public ObjectProperty createObjectProperty( String uri, boolean functional ) {
        checkProfileEntry( getProfile().OBJECT_PROPERTY(), "OBJECT_PROPERTY" );
        ObjectProperty p = (ObjectProperty) createOntResource( ObjectProperty.class, getProfile().OBJECT_PROPERTY(), uri );

        if (functional) {
            checkProfileEntry( getProfile().FUNCTIONAL_PROPERTY(), "FUNCTIONAL_PROPERTY" );
            p.addProperty( RDF.type, getProfile().FUNCTIONAL_PROPERTY() );
        }
        
        return p;
    }
    
    
    /**
     * <p>Answer a resource representing a transitive property</p>
     * @param uri The uri for the property. May not be null.
     * @return An TransitiveProperty resource
     * @see #createTransitiveProperty( String, boolean )
     */
    public TransitiveProperty createTransitiveProperty( String uri ) {
        return createTransitiveProperty( uri, false );
    }
    
    
    /**
     * <p>Answer a resource representing a transitive property, which is optionally
     * also functional. <strong>Note:</strong> although it is permitted in OWL full
     * to have functional transitive properties, it makes the language undecideable.
     * Functional transitive properties are not permitted in OWL Lite or OWL DL.</p>
     * @param uri The uri for the property. May not be null.
     * @param functional If true, the property is also functional
     * @return An TransitiveProperty resource, optionally also functional.
     */
    public TransitiveProperty createTransitiveProperty( String uri, boolean functional ) {
        checkProfileEntry( getProfile().TRANSITIVE_PROPERTY(), "TRANSITIVE_PROPERTY" );
        TransitiveProperty p = (TransitiveProperty) createOntResource( TransitiveProperty.class, getProfile().TRANSITIVE_PROPERTY(), uri );

        if (functional) {
            checkProfileEntry( getProfile().FUNCTIONAL_PROPERTY(), "FUNCTIONAL_PROPERTY" );
            p.addProperty( RDF.type, getProfile().FUNCTIONAL_PROPERTY() );
        }
        
        return p;
    }

    
    /**
     * <p>Answer a resource representing a symmetric property</p>
     * @param uri The uri for the property. May not be null.
     * @return An SymmetricProperty resource
     * @see #createSymmetricProperty( String, boolean )
     */
    public SymmetricProperty createSymmetricProperty( String uri ) {
        return createSymmetricProperty( uri, false );  
    }
    
    
    /**
     * <p>Answer a resource representing a symmetric property, which is optionally
     * also functional.</p>
     * @param uri The uri for the property. May not be null.
     * @param functional If true, the property is also functional
     * @return An SymmetricProperty resource, optionally also functional.
     */
    public SymmetricProperty createSymmetricProperty( String uri, boolean functional ) {
        checkProfileEntry( getProfile().SYMMETRIC_PROPERTY(), "SYMMETRIC_PROPERTY" );
        SymmetricProperty p = (SymmetricProperty) createOntResource( SymmetricProperty.class, getProfile().SYMMETRIC_PROPERTY(), uri );

        if (functional) {
            checkProfileEntry( getProfile().FUNCTIONAL_PROPERTY(), "FUNCTIONAL_PROPERTY" );
            p.addProperty( RDF.type, getProfile().FUNCTIONAL_PROPERTY() );
        }
        
        return p;
    }

    
    /**
     * <p>Answer a resource representing an inverse functional property</p>
     * @param uri The uri for the property. May not be null.
     * @return An InverseFunctionalProperty resource
     * @see #createInverseFunctionalProperty( String, boolean )
     */
    public InverseFunctionalProperty createInverseFunctionalProperty( String uri ) {
        return createInverseFunctionalProperty( uri, false );
    }
    
    
    /**
     * <p>Answer a resource representing an inverse functional property, which is optionally
     * also functional.</p>
     * @param uri The uri for the property. May not be null.
     * @param functional If true, the property is also functional
     * @return An InverseFunctionalProperty resource, optionally also functional.
     */
    public InverseFunctionalProperty createInverseFunctionalProperty( String uri, boolean functional ) {
        checkProfileEntry( getProfile().INVERSE_FUNCTIONAL_PROPERTY(), "INVERSE_FUNCTIONAL_PROPERTY" );
        InverseFunctionalProperty p = (InverseFunctionalProperty) createOntResource( InverseFunctionalProperty.class, getProfile().INVERSE_FUNCTIONAL_PROPERTY(), uri );

        if (functional) {
            checkProfileEntry( getProfile().FUNCTIONAL_PROPERTY(), "FUNCTIONAL_PROPERTY" );
            p.addProperty( RDF.type, getProfile().FUNCTIONAL_PROPERTY() );
        }
        
        return p;
    }
    
   
    /**
     * <p>
     * Answer a resource that represents datatype property in this model, and that is
     * not a functional property.
     * </p>
     * 
     * @param uri The uri for the datatype property. May not be null.
     * @return A DatatypeProperty resource.
     * @see #createDatatypeProperty( String, boolean )
     */
    public DatatypeProperty createDatatypeProperty( String uri ) {
        return createDatatypeProperty( uri, false );
    }
    
   
    /**
     * <p>
     * Answer a resource that represents datatype property in this model. A datatype property
     * is defined to have a range that is a concrete datatype, rather than an individual. 
     * If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-graph of the ontology model. 
     * </p>
     * 
     * @param uri The uri for the datatype property. May not be null.
     * @param functional If true, the resource will also be typed as a {@link FunctionalProperty},
     * that is, a property that has a unique range value for any given domain value.
     * @return A DatatypeProperty resource.
     */
    public DatatypeProperty createDatatypeProperty( String uri, boolean functional ) {
        checkProfileEntry( getProfile().DATATYPE_PROPERTY(), "DATATYPE_PROPERTY" );
        DatatypeProperty p = (DatatypeProperty) createOntResource( DatatypeProperty.class, getProfile().DATATYPE_PROPERTY(), uri );

        if (functional) {
            checkProfileEntry( getProfile().FUNCTIONAL_PROPERTY(), "FUNCTIONAL_PROPERTY" );
            p.addProperty( RDF.type, getProfile().FUNCTIONAL_PROPERTY() );
        }
     
        return p;   
    }
    
   
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
    public AnnotationProperty createAnnotationProperty( String uri ) {
        checkProfileEntry( getProfile().ANNOTATION_PROPERTY(), "ANNOTATION_PROPERTY" );
        return (AnnotationProperty) createOntResource( AnnotationProperty.class, getProfile().ANNOTATION_PROPERTY(), uri );
    }
    
   
    /**
     * <p>
     * Answer a resource that represents an anonymous class description in this model. A new
     * anonymous resource of <code>rdf:type C</code>, where C is the class type from the
     * language profile.
     * </p>
     * 
     * @return An anonymous Class resource.
     */
    public OntClass createClass() {
        checkProfileEntry( getProfile().CLASS(), "CLASS" );
        return (OntClass) createOntResource( OntClass.class, getProfile().CLASS(), null );
    }
    
   
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
    public OntClass createClass( String uri ) {
        checkProfileEntry( getProfile().CLASS(), "CLASS" );
        return (OntClass) createOntResource( OntClass.class, getProfile().CLASS(), uri );
    }
    

    /**
     * <p>Answer a resource representing the class that is the complement of the given argument class</p>
     * @param uri The URI of the new complement class, or null for an anonymous class description.
     * @param cls Resource denoting the class that the new class is a complement of
     * @return A complement class
     */
    public ComplementClass createComplementClass( String uri, Resource cls ) {
        checkProfileEntry( getProfile().CLASS(), "CLASS" );
        OntClass c = (OntClass) createOntResource( OntClass.class, getProfile().CLASS(), uri );
        
        checkProfileEntry( getProfile().COMPLEMENT_OF(), "COMPLEMENT_OF" );
        // if the class that this class is a complement of is not specified, use owl:nothing or daml:nothing
        c.addProperty( getProfile().COMPLEMENT_OF(), (cls == null) ? getProfile().NOTHING() : cls );
        
        return (ComplementClass) c.as( ComplementClass.class );
    }
    
   
    /**
     * <p>Answer a resource representing the class that is the enumeration of the given list of individuals</p>
     * @param uri The URI of the new enumeration class, or null for an anonymous class description.
     * @param members An optional list of resources denoting the individuals in the enumeration
     * @return An enumeration class
     */
    public EnumeratedClass createEnumeratedClass( String uri, RDFList members ) {
        checkProfileEntry( getProfile().CLASS(), "CLASS" );
        OntClass c = (OntClass) createOntResource( OntClass.class, getProfile().CLASS(), uri );
        
        checkProfileEntry( getProfile().ONE_OF(), "ONE_OF" );
        c.addProperty( getProfile().ONE_OF(), (members == null) ? createList() : members );
        
        return (EnumeratedClass) c.as( EnumeratedClass.class );
    }
    
   
    /**
     * <p>Answer a resource representing the class that is the union of the given list of class desctiptions</p>
     * @param uri The URI of the new union class, or null for an anonymous class description.
     * @param members A list of resources denoting the classes that comprise the union
     * @return A union class description
     */
    public UnionClass createUnionClass( String uri, RDFList members ) {
        checkProfileEntry( getProfile().CLASS(), "CLASS" );
        OntClass c = (OntClass) createOntResource( OntClass.class, getProfile().CLASS(), uri );
        
        checkProfileEntry( getProfile().UNION_OF(), "UNION_OF" );
        c.addProperty( getProfile().UNION_OF(), (members == null) ? createList() : members );
        
        return (UnionClass) c.as( UnionClass.class );
    }
    
   
    /**
     * <p>Answer a resource representing the class that is the intersection of the given list of class descriptions.</p>
     * @param uri The URI of the new intersection class, or null for an anonymous class description.
     * @param members A list of resources denoting the classes that comprise the intersection
     * @return An intersection class description
     */
    public IntersectionClass createIntersectionClass( String uri, RDFList members ) {
        checkProfileEntry( getProfile().CLASS(), "CLASS" );
        OntClass c = (OntClass) createOntResource( OntClass.class, getProfile().CLASS(), uri );
        
        checkProfileEntry( getProfile().INTERSECTION_OF(), "INTERSECTION_OF" );
        c.addProperty( getProfile().INTERSECTION_OF(), (members == null) ? createList() : members );
        
        return (IntersectionClass) c.as( IntersectionClass.class );
    }


    /**
     * <p>
     * Answer a resource that represents an anonymous property restriction in this model. A new
     * anonymous resource of <code>rdf:type R</code>, where R is the restriction type from the
     * language profile.
     * </p>
     * 
     * @param p The property that is restricted by this restriction
     * @return An anonymous Restriction resource.
     */
    public Restriction createRestriction( Property p ) {
        checkProfileEntry( getProfile().RESTRICTION(), "RESTRICTION" );
        Restriction r = (Restriction) createOntResource( Restriction.class, getProfile().RESTRICTION(), null );
        if (p != null) {
            r.setOnProperty( p );
        } 
        
        return r;
    }
    
   
    /**
     * <p>
     * Answer a resource that represents a property restriction in this model. If a resource
     * with the given uri exists in the model, it will be re-used.  If not, a new one is created in
     * the updateable sub-graph of the ontology model. 
     * </p>
     * 
     * @param uri The uri for the restriction node, or null for an anonymous restriction.
     * @param p The property that is restricted by this restriction
     * @return A Restriction resource.
     */
    public Restriction createRestriction( String uri, Property p ) {
        checkProfileEntry( getProfile().RESTRICTION(), "RESTRICTION" );
        Restriction r = (Restriction) createOntResource( Restriction.class, getProfile().RESTRICTION(), uri );
        if (p != null) {
            r.setOnProperty( p );
        } 
        
        return r;
    }
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals that have the given
     * resource as the value of the given property</p>
     * 
     * @param uri The optional URI for the restriction, or null for an anonymous restriction (which 
     * should be the normal case)
     * @param prop The property the restriction applies to
     * @param value The value of the property, as a resource or RDF literal
     * @return A new resource representing a has-value restriction
     */
    public HasValueRestriction createHasValueRestriction( String uri, Property prop, RDFNode value ) {
        checkProfileEntry( getProfile().RESTRICTION(), "RESTRICTION" );
        Restriction r = (Restriction) createOntResource( Restriction.class, getProfile().RESTRICTION(), uri );
        
        if (prop == null || value == null) {
            throw new IllegalArgumentException( "Cannot create hasValueRestriction with a null property or value" );
        }
        
        checkProfileEntry( getProfile().HAS_VALUE(), "HAS_VALUE" );
        r.addProperty( getProfile().ON_PROPERTY(), prop );
        r.addProperty( getProfile().HAS_VALUE(), value );
        
        return (HasValueRestriction) r.as( HasValueRestriction.class );
    }
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals that have at least
     * one property with a value belonging to the given class</p>
     * 
     * @param uri The optional URI for the restriction, or null for an anonymous restriction (which 
     * should be the normal case)
     * @param prop The property the restriction applies to
     * @param cls The class to which at least one value of the property belongs
     * @return A new resource representing a some-values-from restriction
     */
    public SomeValuesFromRestriction createSomeValuesFromRestriction( String uri, Property prop, Resource cls ) {
        checkProfileEntry( getProfile().RESTRICTION(), "RESTRICTION" );
        Restriction r = (Restriction) createOntResource( Restriction.class, getProfile().RESTRICTION(), uri );
            
        if (prop == null || cls == null) {
            throw new IllegalArgumentException( "Cannot create someValuesFromRestriction with a null property or class" );
        }
            
        checkProfileEntry( getProfile().SOME_VALUES_FROM(), "SOME_VALUES_FROM" );
        r.addProperty( getProfile().ON_PROPERTY(), prop );
        r.addProperty( getProfile().SOME_VALUES_FROM(), cls );
            
        return (SomeValuesFromRestriction) r.as( SomeValuesFromRestriction.class );
    }
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals for which all values
     * of the given property belong to the given class</p>
     * 
     * @param uri The optional URI for the restriction, or null for an anonymous restriction (which 
     * should be the normal case)
     * @param prop The property the restriction applies to
     * @param cls The class to which any value of the property belongs
     * @return A new resource representing an all-values-from restriction
     */
    public AllValuesFromRestriction createAllValuesFromRestriction( String uri, Property prop, Resource cls ) {
        checkProfileEntry( getProfile().RESTRICTION(), "RESTRICTION" );
        Restriction r = (Restriction) createOntResource( Restriction.class, getProfile().RESTRICTION(), uri );
                
        if (prop == null || cls == null) {
            throw new IllegalArgumentException( "Cannot create allValuesFromRestriction with a null property or class" );
        }
                
        checkProfileEntry( getProfile().ALL_VALUES_FROM(), "ALL_VALUES_FROM" );
        r.addProperty( getProfile().ON_PROPERTY(), prop );
        r.addProperty( getProfile().ALL_VALUES_FROM(), cls );
                
        return (AllValuesFromRestriction) r.as( AllValuesFromRestriction.class );
    }
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals that have exactly
     * the given number of values for the given property.</p>
     * 
     * @param uri The optional URI for the restriction, or null for an anonymous restriction (which 
     * should be the normal case)
     * @param prop The property the restriction applies to
     * @param cardinality The exact cardinality of the property
     * @return A new resource representing a has-value restriction
     */
    public CardinalityRestriction createCardinalityRestriction( String uri, Property prop, int cardinality ) {
        checkProfileEntry( getProfile().RESTRICTION(), "RESTRICTION" );
        Restriction r = (Restriction) createOntResource( Restriction.class, getProfile().RESTRICTION(), uri );
                
        if (prop == null) {
            throw new IllegalArgumentException( "Cannot create cardinalityRestriction with a null property" );
        }
                
        checkProfileEntry( getProfile().CARDINALITY(), "CARDINALITY" );
        r.addProperty( getProfile().ON_PROPERTY(), prop );
        r.addProperty( getProfile().CARDINALITY(), createTypedLiteral( cardinality ) );
                
        return (CardinalityRestriction) r.as( CardinalityRestriction.class );
    }
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals that have at least
     * the given number of values for the given property.</p>
     * 
     * @param uri The optional URI for the restriction, or null for an anonymous restriction (which 
     * should be the normal case)
     * @param prop The property the restriction applies to
     * @param cardinality The minimum cardinality of the property
     * @return A new resource representing a min-cardinality restriction
     */
    public MinCardinalityRestriction createMinCardinalityRestriction( String uri, Property prop, int cardinality ) {
        checkProfileEntry( getProfile().RESTRICTION(), "RESTRICTION" );
        Restriction r = (Restriction) createOntResource( Restriction.class, getProfile().RESTRICTION(), uri );
                    
        if (prop == null) {
            throw new IllegalArgumentException( "Cannot create minCardinalityRestriction with a null property" );
        }
                    
        checkProfileEntry( getProfile().MIN_CARDINALITY(), "MIN_CARDINALITY" );
        r.addProperty( getProfile().ON_PROPERTY(), prop );
        r.addProperty( getProfile().MIN_CARDINALITY(), createTypedLiteral( cardinality ) );
                    
        return (MinCardinalityRestriction) r.as( MinCardinalityRestriction.class );
    }
    
    
    /**
     * <p>Answer a class description defined as the class of those individuals that have at most
     * the given number of values for the given property.</p>
     * 
     * @param uri The optional URI for the restriction, or null for an anonymous restriction (which 
     * should be the normal case)
     * @param prop The property the restriction applies to
     * @param cardinality The maximum cardinality of the property
     * @return A new resource representing a mas-cardinality restriction
     */
    public MaxCardinalityRestriction createMaxCardinalityRestriction( String uri, Property prop, int cardinality ) {
        checkProfileEntry( getProfile().RESTRICTION(), "RESTRICTION" );
        Restriction r = (Restriction) createOntResource( Restriction.class, getProfile().RESTRICTION(), uri );
                        
        if (prop == null) {
            throw new IllegalArgumentException( "Cannot create maxCardinalityRestriction with a null property" );
        }
                        
        checkProfileEntry( getProfile().MAX_CARDINALITY(), "MAX_CARDINALITY" );
        r.addProperty( getProfile().ON_PROPERTY(), prop );
        r.addProperty( getProfile().MAX_CARDINALITY(), createTypedLiteral( cardinality ) );
                        
        return (MaxCardinalityRestriction) r.as( MaxCardinalityRestriction.class );
    }
    
   
    /**
     * <p>Answer a data range defined as the given set of concrete data values.  DataRange resources
     * are necessarily bNodes.</p>
     * 
     * @param literals An iterator over a set of literals that will be the members of the data range, 
     *                 or null to define an empty data range
     * @return A new data range containing the given literals as permissible values
     */
    public DataRange createDataRange( RDFList literals ) {
        checkProfileEntry( getProfile().DATARANGE(), "DATARANGE" );
        DataRange d = (DataRange) createOntResource( DataRange.class, getProfile().DATARANGE(), null );
        
        checkProfileEntry( getProfile().ONE_OF(), "ONE_OF" );
        d.addProperty( getProfile().ONE_OF(), (literals == null) ? createList() : literals );
        
        return d;
    }


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
    public AllDifferent createAllDifferent() {
        return createAllDifferent( null );
    }
    
    
    /**
     * <p>
     * Answer a new, anonymous node representing the fact that a given set of classes are all
     * pair-wise distinct.  <code>AllDifferent</code> is a feature of OWL only, and is something
     * of an anomoly in that it exists only to give a place to anchor the <code>distinctMembers</code>
     * property, which is the actual expression of the fact. 
     * </p>
     * @param differentMembers A list of the class expressions that denote a set of mutually disjoint classes
     * @return A new AllDifferent resource
     */
    public AllDifferent createAllDifferent( RDFList differentMembers ) {
        checkProfileEntry( getProfile().ALL_DIFFERENT(), "ALL_DIFFERENT" );
        AllDifferent ad = (AllDifferent) createOntResource( AllDifferent.class, getProfile().ALL_DIFFERENT(), null );
        
        ad.setDistinctMembers( (differentMembers == null) ? createList() : differentMembers );
        
        return ad;
    }
    
    
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
    public OntResource createOntResource( Class javaClass, Resource rdfType, String uri ) {
        return (OntResource) getResourceWithType( uri, rdfType ).as( javaClass );
    }
    
    
    /**
     * <p>Answer a new empty list.  This method overrides the list create method in ModelCom,
     * to allow both DAML and RDFS lists to be created.</p>
     * @return An RDF-encoded list of no elements, using the current language profile
     */
    public RDFList createList() {
        Resource list = getResource( getProfile().NIL().getURI() );
        list.addProperty( RDF.type, getProfile().LIST() );
        
        return (RDFList) list.as( RDFList.class );
    }
    
    
    /**
     * <p>
     * Answer the language profile (for example, OWL or DAML+OIL) that this model is 
     * working to.
     * </p>
     *  
     * @return A language profile
     */
    public Profile getProfile() {
        return m_spec.getProfile();
    }


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
    public boolean hasLoadedImport( String uri ) {
        return m_imported.contains( uri );
    }
    
    
    /**
     * <p>
     * Record that this model has now imported the document with the given
     * URI, so that it will not be re-imported in the future.
     * </p>
     * 
     * @param uri A document URI that has now been imported into the model.
     */
    public void addLoadedImport( String uri ) {
        m_imported.add( uri );
    }
    
    
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
    public List listImportedOntologyURIs() {
        List imports = new ArrayList();
        
        // list the ontology nodes
        if (getProfile().ONTOLOGY() != null  &&  getProfile().IMPORTS() != null) {
            // for efficiency (specifically, avoiding the reasoner), we do the query directly on the base graph
            for (StmtIterator i = getBaseModel().listStatements( null, RDF.type, getProfile().ONTOLOGY() );  i.hasNext(); ) {
                Resource ontology = i.nextStatement().getSubject();
                
                for (StmtIterator j = ontology.listProperties( getProfile().IMPORTS() ); j.hasNext();  ) {
                    // add the imported URI to the list
                    imports.add( j.nextStatement().getResource().getURI() );
                }
            }
        }
                
        return imports;
    }
    
    
    /**
     * <p>
     * Answer the model maker associated with this model (used for constructing the
     * constituent models of the imports closure).
     * </p>
     * 
     * @return The local graph factory
     */
    public ModelMaker getModelMaker() {
        return m_spec.getModelMaker();
    }
    
    
    /**
     * <p>Read statements into the model from the given source, and then load
     * imported ontologies (according to the document manager policy).</p>
     * @param uri URI to read from, may be mapped to a local source by the document manager
     */
    public Model read( String uri ) {
        return read( uri, null );
    }
    
    /**
     * <p>Read statements into the model from the given source, and then load
     * imported ontologies (according to the document manager policy).</p>
     * @param reader An input reader
     * @param base The base URI
     */
    public Model read( Reader reader, String base ) {
        super.read( reader, base );
        
        getDocumentManager().loadImports( this );
        rebind();
        return this;
    }
    
    /**
     * <p>Read statements into the model from the given source, and then load
     * imported ontologies (according to the document manager policy).</p>
     * @param reader An input stream
     * @param base The base URI
     */
    public Model read(InputStream reader, String base) {
        super.read( reader, base );
        
        getDocumentManager().loadImports( this );
        rebind();
        return this;
    } 
    
    /**
     * <p>Read statements into the model from the given source, and then load
     * imported ontologies (according to the document manager policy).</p>
     * @param uri URI to read from, may be mapped to a local source by the document manager
     * @param lang The source syntax
     */
    public Model read( String uri, String syntax ) {
        String sourceURL = getDocumentManager().doAltURLMapping( uri );
        
        // we have to duplicate the encoding translation here, since there's no method on Model
        // to read from a URL with a separate baseURI
        try {
            URLConnection conn = new URL( sourceURL ).openConnection();
            String encoding = conn.getContentEncoding();
            
            if (encoding == null) {
                super.read( conn.getInputStream(), uri, syntax );
            }
            else {
                super.read( new InputStreamReader(conn.getInputStream(), encoding), uri, syntax );
            }
        } 
        catch (IOException e) {
            throw new JenaException( e);
        }

        // cache this model against the public uri (if caching enabled)
        getDocumentManager().addModel( uri, this );

        // now load the imported documents
        getDocumentManager().loadImports( this );
        rebind();
        return this;
    }
    
    /**
     * <p>Read statements into the model from the given source, and then load
     * imported ontologies (according to the document manager policy).</p>
     * @param reader An input reader
     * @param base The base URI
     * @param lang The source syntax
     */
    public Model read(Reader reader, String base, String syntax) {
        super.read( reader, base, syntax );
        
        getDocumentManager().loadImports( this );
        rebind();
        return this;
    }
    
    /**
     * <p>Read statements into the model from the given source, and then load
     * imported ontologies (according to the document manager policy).</p>
     * @param reader An input stream
     * @param base The base URI
     * @param lang The source syntax
     */
    public Model read(InputStream reader, String base, String syntax) {
        super.read( reader, base, syntax );
        
        getDocumentManager().loadImports( this );
        rebind();
        return this;
    }
    
    
    /**
     * <p>
     * Answer the sub-graphs of this model. A sub-graph is defined as a graph that
     * is used to contain the triples from an imported document.
     * </p>
     * 
     * @return A list of sub graphs for this ontology model
     */
    public List getSubGraphs() {
        return getUnionGraph().getSubGraphs();
    }
    
    
    /**
     * <p>
     * Answer the base-graph of this model. The base-graph is the graph that
     * contains the triples read from the source document for this ontology.
     * </p>
     * 
     * @return The base-graph for this ontology model
     */
    public Graph getBaseGraph() {
        return getUnionGraph().getBaseGraph();
    }
    
    
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
    public Model getBaseModel() {
        Model result = ModelFactory.createModelForGraph( getBaseGraph() );
        result.setNsPrefixes( this );
        return result;
    }
    
    
    /**
     * <p>
     * Add the given model as one of the sub-models of this ontology union.   Will 
     * cause the associated infererence engine (if any) to update, so this may be
     * an expensive operation in some cases. 
     * </p>
     *
     * @param model A sub-model to add 
     */
    public void addSubModel( Model model) {
        addSubModel( model, true );
    }
    
    
    /**
     * <p>
     * Add the given model as one of the sub-models of the enclosed ontology union model.
     * </p>
     *
     * @param model A sub-model to add
     * @param rebind If true, rebind any associated inferencing engine to the new data (which
     * may be an expensive operation) 
     */
    public void addSubModel( Model model, boolean rebind ) {
        getUnionGraph().addGraph( model.getGraph() );
        if (rebind) {
            rebind();
        } 
    }
    
    
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
    public boolean strictMode() {
        return m_strictMode;
    }
    
    
    /**
     * <p>
     * Set the checking mode to strict or non-strict.
     * </p>
     * 
     * @param strict
     * @see #strictMode()
     */
    public void setStrictMode( boolean strict ) {
        m_strictMode = strict;
    }
    
    
    /**
     * <p>Answer the ontology model specification that was used to construct this model</p>
     * @return An ont model spec instance.
     */
    public OntModelSpec getSpecification() {
        return m_spec;
    }


    /**
     * <p>
     * Answer the iterator over the resources from the graph that satisfy the given
     * query, followed by the answers to the alternative queries (if specified). A
     * typical scenario is that the main query gets resources of a given class (say,
     * <code>rdfs:Class</code>), while the altQueries query for aliases for that
     * type (such as <code>daml:Class</code>).
     * </p>
     * 
     * @param query A query to run against the model
     * @param altQueries An optional list of subsidiary queries to chain on to the first 
     * @return ExtendedIterator An iterator over the (assumed single) results of 
     * executing the queries.
     */
    public ExtendedIterator queryFor( BindingQueryPlan query, List altQueries, Class asKey ) {
        GetBinding firstBinding  = new GetBinding( 0 );
        
        // get the results from the main query
        ExtendedIterator mainQuery = query.executeBindings().mapWith( firstBinding );
        
        // now add the alternate queries, if defined
        if (altQueries != null) {
            for (Iterator i = altQueries.iterator();  i.hasNext();  ) {
                ExtendedIterator altQuery = ((BindingQueryPlan) i.next()).executeBindings().mapWith( firstBinding );
                mainQuery = mainQuery.andThen( altQuery );
            }
        }
        
        // map each answer value to the appropriate ehnanced node
        return mainQuery.mapWith( new SubjectNodeAs( asKey ) );
    }
    
    // output operations - delegate to base model
    
    public Model write( Writer writer )                             { return getBaseModel().write( writer ); }
    public Model write( Writer writer, String lang )                { return getBaseModel().write( writer, lang ); }
    public Model write( Writer writer, String lang, String base )   { return getBaseModel().write( writer, lang, base ); }
    public Model write( OutputStream out )                          { return getBaseModel().write( out ); }
    public Model write( OutputStream out, String lang )             { return getBaseModel().write( out, lang ); }
    public Model write( OutputStream out, String lang, String base) { return getBaseModel().write( out, lang, base ); }

    public Model writeAll( Writer writer, String lang, String base ) {
        return super.write( writer, lang, base );
    }
    
    public Model writeAll( OutputStream out, String lang, String base ) {
        return super.write( out, lang, base );
    }
    

    // Implementation of inf model interface methods
    
    /**
     * Return the raw RDF model being processed (i.e. the argument
     * to the Reasonder.bind call that created this InfModel).
     */
    public Model getRawModel() {
        return getBaseModel();
    }
    
    /**
     * Return the Reasoner which is being used to answer queries to this graph.
     */
    public Reasoner getReasoner() {
        return (getGraph() instanceof InfGraph) ? ((InfGraph) getGraph()).getReasoner() : null;
    }

    /**
     * Cause the inference model  to reconsult the underlying data to take
     * into account changes. Normally changes are made through the InfModel's add and
     * remove calls are will be handled appropriately. However, in some cases changes
     * are made "behind the InfModels's back" and this forces a full reconsult of
     * the changed data. 
     */
    public void rebind() {
        if (getGraph() instanceof InfGraph) {
            ((InfGraph) getGraph()).rebind();
        }
    }
    
    /**
     * Perform any initial processing and caching. This call is optional. Most
     * engines either have negligable set up work or will perform an implicit
     * "prepare" if necessary. The call is provided for those occasions where
     * substantial preparation work is possible (e.g. running a forward chaining
     * rule system) and where an application might wish greater control over when
     * this prepration is done rather than just leaving to be done at first query time.
     */
    public void prepare() {
        if (getGraph() instanceof InfGraph) {
            ((InfGraph) getGraph()).prepare();
        }
    }
    
    /**
     * Reset any internal caches. Some systems, such as the tabled backchainer, 
     * retain information after each query. A reset will wipe this information preventing
     * unbounded memory use at the expense of more expensive future queries. A reset
     * does not cause the raw data to be reconsulted and so is less expensive than a rebind.
     */
    public void reset() {
        if (getGraph() instanceof InfGraph) {
            ((InfGraph) getGraph()).reset();
        }
    }
    
    /**
     * Returns a derivations model. The rule reasoners typically create a 
     * graph containing those triples added to the base graph due to rule firings.
     * In some applications it can useful to be able to access those deductions
     * directly, without seeing the raw data which triggered them. In particular,
     * this allows the forward rules to be used as if they were rewrite transformation
     * rules.
     * @return null always because this functionality is rarely relevant to ontology models
     */
    public Model getDeductionsModel() {
        return null;
    }
    
    /**
     * Test the consistency of the underlying data. This normally tests
     * the validity of the bound instance data against the bound
     * schema data. 
     * @return a ValidityReport structure
     */
    public ValidityReport validate() {
        return (getGraph() instanceof InfGraph) ? ((InfGraph) getGraph()).validate() : null;
    }
    
    /** Find all the statements matching a pattern.
     * <p>Return an iterator over all the statements in a model
     *  that match a pattern.  The statements selected are those
     *  whose subject matches the <code>subject</code> argument,
     *  whose predicate matches the <code>predicate</code> argument
     *  and whose object matchesthe <code>object</code> argument.
     *  If an argument is <code>null</code> it matches anything.</p>
     * <p>
     * The s/p/o terms may refer to resources which are temporarily defined in the "posit" model.
     * This allows one, for example, to query what resources are of type CE where CE is a
     * class expression rather than a named class - put CE in the posit arg.</p>
     * 
     * @return an iterator over the subjects
     * @param subject   The subject sought
     * @param predicate The predicate sought
     * @param object    The value sought
     */ 
    public StmtIterator listStatements( Resource subject, Property predicate, RDFNode object, Model posit ) {
        if (getGraph() instanceof InfGraph) {
            Iterator iter = ((InfGraph) getGraph()).find(subject.asNode(), predicate.asNode(), object.asNode(), posit.getGraph());
            return IteratorFactory.asStmtIterator(iter,this);
        }
        else {
            return null;
        }
    }
    
    /**
     * Switch on/off drivation logging. If this is switched on then every time an inference
     * is a made that fact is recorded and the resulting record can be access through a later
     * getDerivation call. This may consume a lot of space!
     */
    public void setDerivationLogging(boolean logOn) {
        if (getGraph() instanceof InfGraph) {
            ((InfGraph) getGraph()).setDerivationLogging( logOn );
        }
    }
   
    /**
     * Return the derivation of the given statement (which should be the result of
     * some previous list operation).
     * Not all reasoneers will support derivations.
     * @return an iterator over Derivation records or null if there is no derivation information
     * available for this triple.
     */
    public Iterator getDerivation(Statement statement) {
        return (getGraph() instanceof InfGraph) ? ((InfGraph) getGraph()).getDerivation( statement.asTriple() ) : null;
    }

    
    
    // Internal implementation methods
    //////////////////////////////////

    /**
     * <p>Helper method to the constructor, which interprets the spec and generates an appropriate
     * graph for this model</p>
     * @param spec The model spec to interpret
     * @param base The base model, or null
     */
    private static Graph generateGraph( OntModelSpec spec ) {
        // create a empty union graph
        MultiUnion u = new MultiUnion();
        
        // if we have a reasoner in the spec, bind to the union graph and return
        return (spec.getReasoner() == null) ? (Graph) u : (Graph) spec.getReasoner().bind( u );
    }


    /**
     * <p>Answer the union graph that contains the imports closure for this ontology</p>
     * @return The union graph
     */
    protected MultiUnion getUnionGraph() {
        return m_union;
    }
    
    
    /** Answer the resource with the given URI, if present, as the given facet */
    protected Resource findByURIAs( String uri, Class asKey ) {
        if (uri == null) {
            throw new IllegalArgumentException( "Cannot get() ontology value with a null URI" );
        }
        
        Node n = Node.createURI( uri );
        
        if (getGraph().contains( n, Node.ANY, Node.ANY )) {
            // this resource is a subject in the graph
            try {
                return (Resource) getNodeAs( n, asKey );
            }
            catch (ConversionException ignore) {}
        }
        
        // not present, or cannot be as'ed to the desired facet
        return null;
    }
    
    /**
     * <p>
     * Answer an iterator over all of the resources that have 
     * <code>rdf:type</code> type.
     * </p>
     * 
     * @param type The resource that is the value of <code>rdf:type</code> we
     * want to match
     * @return An iterator over all triples <code>_x rdf:type type</code>
     */
    protected ExtendedIterator findByType( Resource type ) {
        return getGraph().find( null, RDF.type.asNode(), type.asNode() );
    }
    

    /**
     * <p>
     * Answer an iterator over all of the resources that have 
     * <code>rdf:type type</code>, or optionally, one of the alternative types.
     * </p>
     * 
     * @param type The resource that is the value of <code>rdf:type</code> we
     * want to match
     * @param alternates An iterator over alternative types to search for, or null
     * @return An iterator over all triples <code>_x rdf:type t</code> where t
     * is <code>type</code> or one of the values from <code>types</code>.
     */
    protected ExtendedIterator findByType( Resource type, Iterator alternates ) {
        ExtendedIterator i = findByType( type );
        
        // compose onto i the find iterators for the alternate types
        if (alternates != null) {
            while (alternates.hasNext()) {
                i = i.andThen( findByType( (Resource) alternates.next() ) );
            }
        }
        
        return i;
    }
    

    /**
     * <p>
     * Answer an iterator over all of the resources that have 
     * <code>rdf:type type</code>, or optionally, one of the alternative types, 
     * and present the results <code>as()</code> the given class.
     * </p>
     * 
     * @param type The resource that is the value of <code>rdf:type</code> we
     * want to match
     * @param types An iterator over alternative types to search for, or null
     * @param asKey The value to use to present the polymorphic results
     * @return An iterator over all triples <code>_x rdf:type type</code>
     */
    protected ExtendedIterator findByTypeAs( Resource type, Iterator types, Class asKey ) {
        return findByType( type, types ).mapWith( new SubjectNodeAs( asKey ) );
    }
    
    
    /**
     * <p>
     * Answer an iterator over all of the resources that has an 
     * <code>rdf:type</code> from the types iterator, 
     * and present the results <code>as()</code> the given class.
     * </p>
     * 
     * @param types An iterator over types to search for.  An exception will
     * be raised if this iterator does not have at least one next() element.
     * @param asKey The value to use to present the polymorphic results
     * @return An iterator over all triples <code>_x rdf:type type</code>
     */
    protected ExtendedIterator findByTypeAs( Iterator types, Class asKey ) {
        return findByTypeAs( (Resource) types.next(), types, asKey );
    }
    
    
    /**
     * <p>
     * Answer an iterator over resources with the given rdf:type; for each value
     * in the iterator, ensure that is is presented <code>as()</code> the
     * polymorphic object denoted by the given class key.
     * </p>
     * 
     * @param type The rdf:type to search for
     * @param asKey The key to pass to as() on the subject nodes
     * @return An iterator over subjects with the given type, presenting as
     * the given polymorphic class.
     */
    protected ExtendedIterator findByTypeAs( Resource type, Class asKey ) {
        return findByType( type ).mapWith( new SubjectNodeAs( asKey ) );
    }
    
    
    /**
     * <p>
     * Answer a binding query that will search for 'an X that has an 
     * rdf:type whose rdf:type is C' for some given resource C.
     * </p>
     * 
     * @param type The type of the type of the resources we're searching for
     * @return BindingQueryPlan A binding query for the X resources.
     */
    protected BindingQueryPlan queryXTypeOfType( Resource type ) {
        Query q = new Query();
        q.addMatch( Query.X, RDF.type.asNode(), Query.Y );
        q.addMatch( Query.Y, RDF.type.asNode(), type.asNode() );
        
        return queryHandler().prepareBindings( q, new Node[] {Query.X} );
    }
    
    
    /**
     * <p>
     * Answer an iterator over nodes that have p as a subject
     * </p>
     * 
     * @param p A property
     * @return ExtendedIterator over subjects of p.
     */
    protected ExtendedIterator findByDefiningProperty( Property p ) {
        return getGraph().find( null, p.getNode(), null );
    }
    
    
    /**
     * <p>
     * Answer an iterator over nodes that have p as a subject, presented as 
     * polymorphic enh resources of the given facet.
     * </p>
     * 
     * @param p A property
     * @param asKey A facet type
     * @return ExtendedIterator over subjects of p, presented as the facet.
     */
    protected ExtendedIterator findByDefiningPropertyAs( Property p, Class asKey ) {
        return findByDefiningProperty( p ).mapWith( new SubjectNodeAs( asKey ) );
    }
    
    
    /**
     * <p>
     * Answer the resource with the given uri and that has the given rdf:type -
     * creating the resource if necessary.
     * </p>
     * 
     * @param uri The uri to use, or null
     * @param rdfType The resource to assert as the rdf:type
     * @return A new or existing Resource
     */
    protected Resource getResourceWithType( String uri, Resource rdfType ) {
        return getResource( uri ).addProperty( RDF.type, rdfType );
    }
    
    
    /**
     * <p>Throw an OntologyException if the term is not in language profile</p>
     * 
     * @param profileTerm The entry from the profile
     * @param desc A label for the profile term
     * @exception OntologyException if profileTerm is null.
     */
    protected void checkProfileEntry( Object profileTerm, String desc ) {
        if (profileTerm == null) {
            // not in the profile
            throw new ProfileException( desc, getProfile() );
        }
    }
    
    
    /**
     * <p>Check that every member of the given list has the given rdf:type, and throw an exception if not.</p>
     * @param list The list to be checked
     * @param rdfType The rdf:type value to check for
     * @exception LanguageConsistencyException if any member of the list does not have <code>rdf:type <i>rdfType</i></code>
     */
    protected void checkListMembersRdfType( RDFList list, Resource rdfType ) {
        if (strictMode() && ! ((Boolean) list.reduce( new RdfTypeTestFn( rdfType), Boolean.TRUE )).booleanValue()) {
            // not all of the members of the list are of the given type
            throw new LanguageConsistencyException( "The members of the given list are expected to be of rdf:type " + rdfType.toString() );
        }
    }

    
    //==============================================================================
    // Inner class definitions
    //==============================================================================

    /** Map triple subjects or single nodes to subject enh nodes, presented as() the given class */
    protected class SubjectNodeAs implements Map1 
    {
        protected Class m_asKey;
        
        protected SubjectNodeAs( Class asKey ) { m_asKey = asKey; }
        
        public Object map1( Object x ) {
            Node n = (x instanceof Triple) 
                         ? ((Triple) x).getSubject() 
                         : ((x instanceof EnhNode) ? ((EnhNode) x).asNode() :  (Node) x);
            return getNodeAs( n, m_asKey );
        }
        
    }
    
    
    
    /** Project out the first element of a list of bindings */
    protected class GetBinding implements Map1
    {
        protected int m_index;
        protected GetBinding( int index ) { m_index = index; }
        public Object map1( Object x )    { return ((List) x).get( m_index );  }
    }
    
    /** Function to test the rdf type of a list */
    protected class RdfTypeTestFn implements RDFList.ReduceFn
    {
        protected Resource m_type;
        protected RdfTypeTestFn( Resource type ) { m_type = type; }
        public Object reduce( RDFNode node, Object accumulator ) {
            Boolean acc = (Boolean) accumulator;
            if (acc.booleanValue()) {
                // true so far
                Resource r = (Resource) node;
                return new Boolean( r.hasProperty( RDF.type, m_type ) );
            }
            else {
                return acc;
            }
        }
    }
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
