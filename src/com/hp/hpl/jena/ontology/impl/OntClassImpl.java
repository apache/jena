/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            27-Mar-2003
 * Filename           $RCSfile: OntClassImpl.java,v $
 * Revision           $Revision: 1.21 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-06-19 14:37:17 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;



// Imports
///////////////
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.*;

import java.util.*;


/**
 * <p>
 * Implementation for the ontology abstraction representing ontology classes.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OntClassImpl.java,v 1.21 2003-06-19 14:37:17 ian_dickinson Exp $
 */
public class OntClassImpl
    extends OntResourceImpl
    implements OntClass 
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating OntClass facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use 
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    public static Implementation factory = new Implementation() {
        public EnhNode wrap( Node n, EnhGraph eg ) { 
            if (canWrap( n, eg )) {
                return new OntClassImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n.toString() + " to OntClass");
            } 
        }
            
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being an OntClass facet if it has rdf:type owl:Class or equivalent
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            return (profile != null)  &&  profile.isSupported( node, eg, OntClass.class );
        }
    };


    // Instance variables
    //////////////////////////////////

    /** Query for properties with this class as domain */
    protected BindingQueryPlan m_domainQuery;
    
    /** Query for properties restricted by this class */
    protected BindingQueryPlan m_restrictionPropQuery = null;
    

    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct an ontology class node represented by the given node in the given graph.
     * </p>
     * 
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public OntClassImpl( Node n, EnhGraph g ) {
        super( n, g );
        
        // pre-built queries
        // ?x a rdf:Property ; rdfs:domain this.
        Query q = new Query();
        q.addMatch( Query.X, getProfile().DOMAIN().asNode(), asNode() );
        
        m_domainQuery = getModel().queryHandler().prepareBindings( q, new Node[] {Query.X} );
        
        // this rdfs:subClassOf ?x. ?x owl:onProperty ?y.
        if (getProfile().ON_PROPERTY() != null) {
            q = new Query();
            q.addMatch( asNode(), getProfile().SUB_CLASS_OF().asNode(), Query.X );
            q.addMatch( Query.X, getProfile().ON_PROPERTY().asNode(), Query.Y );
            
            m_restrictionPropQuery = getModel().queryHandler().prepareBindings( q, new Node[] {Query.Y} );
        }
    }


    // External signature methods
    //////////////////////////////////

    // subClassOf
    
    /**
     * <p>Assert that this class is sub-class of the given class. Any existing 
     * statements for <code>subClassOf</code> will be removed.</p>
     * @param cls The class that this class is a sub-class of
     * @exception OntProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.   
     */ 
    public void setSuperClass( Resource cls ) {
        setPropertyValue( getProfile().SUB_CLASS_OF(), "SUB_CLASS_OF", cls );
    }

    /**
     * <p>Add a super-class of this class.</p>
     * @param cls A class that is a super-class of this class.
     * @exception OntProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.   
     */ 
    public void addSuperClass( Resource cls ) {
        addPropertyValue( getProfile().SUB_CLASS_OF(), "SUB_CLASS_OF", cls );
    }

    /**
     * <p>Answer a class that is the super-class of this class. If there is
     * more than one such class, an arbitrary selection is made.</p>
     * @return A super-class of this class
     * @exception OntProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.   
     */ 
    public OntClass getSuperClass() {
        return (OntClass) objectAs( getProfile().SUB_CLASS_OF(), "SUB_CLASS_OF", OntClass.class );
    }

    /**
     * <p>Answer an iterator over all of the classes that are declared to be super-classes of
     * this class. Each element of the iterator will be an {@link OntClass}.</p>
     * @return An iterator over the super-classes of this class.
     * @exception OntProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listSuperClasses() {
        return listSuperClasses( false );
    }
     
    /**
     * <p>Answer an iterator over all of the classes that are declared to be super-classes of
     * this class. Each element of the iterator will be an {@link OntClass}.
     * See {@link #listSubClasses( boolean )} for a full explanation of the <em>direct</em>
     * parameter.
     * </p>
     * 
     * @param direct If true, only answer the direcly adjacent classes in the
     * super-class relation: i&#046;e&#046; eliminate any class for which there is a longer route
     * to reach that child under the super-class relation.
     * @return an iterator over the resources representing this class's sub-classes.
     * @exception OntProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.   
     */
    public ExtendedIterator listSuperClasses( boolean direct ) {
        return listDirectPropertyValues( getProfile().SUB_CLASS_OF(), "SUB_CLASS_OF", OntClass.class, getProfile().SUB_CLASS_OF(), direct, false );
    }

    /**
     * <p>Answer true if the given class is a super-class of this class.</p>
     * @param cls A class to test.
     * @return True if the given class is a super-class of this class.
     * @exception OntProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.   
     */
    public boolean hasSuperClass( Resource cls ) {
        return hasSuperClass( cls, false );
    }
    
    /**
     * <p>Answer true if the given class is a super-class of this class.
     * See {@link #listSubClasses( boolean )} for a full explanation of the <em>direct</em>
     * parameter.
     * </p>
     * @param cls A class to test.
     * @param direct If true, only search the classes that are directly adjacent to this 
     * class in the class hierarchy.
     * @return True if the given class is a super-class of this class.
     * @exception OntProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.   
     */
    public boolean hasSuperClass( Resource cls, boolean direct ) {
        if (!direct) {
            // don't need any special case, we just get the property
            return hasPropertyValue( getProfile().SUB_CLASS_OF(), "SUB_CLASS_OF", cls );
        }
        else {
            // we want the direct, not general relationship
            if (getGraph() instanceof InfGraph) {
                InfGraph ig = (InfGraph) getGraph();
                if (ig.getReasoner().supportsProperty( ReasonerVocabulary.directSubClassOf )) {
                    // we can look this up directly
                    return hasPropertyValue( ReasonerVocabulary.directSubClassOf, "direct sub-class", cls );
                }
            }
            
            // otherwise, not an inf-graph or the given inf-graph does not support direct directly (:-)
            // we manually compute the maximal lower elements - this could be expensive in general
            return ResourceUtils.maximalLowerElements( listSuperClasses(), getProfile().SUB_CLASS_OF(), false ).contains( cls );
        }
    }
    
    /**
     * <p>Remove the given class from the super-classes of this class.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls A class to be removed from the super-classes of this class
     * @exception OntProfileException If the {@link Profile#SUB_CLASS_OF()} class is not supported in the current language profile.   
     */
    public void removeSuperClass( Resource cls ) {
        removePropertyValue( getProfile().SUB_CLASS_OF(), "SUB_CLASS_OF", cls );
    }
    
    /**
     * <p>Assert that this class is super-class of the given class. Any existing 
     * statements for <code>subClassOf</code> on <code>prop</code> will be removed.</p>
     * @param cls The class that is a sub-class of this class
     * @exception OntProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.   
     */ 
    public void setSubClass( Resource cls ) {
        // first we have to remove all of the inverse sub-class links
        checkProfile( getProfile().SUB_CLASS_OF(), "SUB_CLASS_OF" );
        for (StmtIterator i = getModel().listStatements( null, getProfile().SUB_CLASS_OF(), this );  i.hasNext(); ) {
            i.nextStatement().remove();
        }
        
        ((OntClass) cls.as( OntClass.class )).addSuperClass( this );
    }

    /**
     * <p>Add a sub-class of this class.</p>
     * @param cls A class that is a sub-class of this class.
     * @exception OntProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.   
     */ 
    public void addSubClass( Resource cls ) {
        ((OntClass) cls.as( OntClass.class )).addSuperClass( this );
    }

    /**
     * <p>Answer a class that is the sub-class of this class. If there is
     * more than one such class, an arbitrary selection is made.</p>
     * @return A sub-class of this class
     * @exception OntProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.   
     */ 
    public OntClass getSubClass() {
        checkProfile( getProfile().SUB_CLASS_OF(), "SUB_CLASS_OF" );
        return (OntClass) getModel().listStatements( null, getProfile().SUB_CLASS_OF(), this )
                          .nextStatement()
                          .getSubject()
                          .as( OntClass.class );                  
    }

    /**
     * <p>Answer an iterator over all of the classes that are declared to be sub-classes of
     * this class. Each element of the iterator will be an {@link OntClass}.</p>
     * @return An iterator over the sub-classes of this class.
     * @exception OntProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listSubClasses() {
        return listSubClasses( false );
    }

    /**
     * <p>
     * Answer an iterator over the classes that are declared to be sub-classes of
     * this class. Each element of the iterator will be an {@link OntClass}. The
     * distinguishing extra parameter for this method is the flag <code>direct<code>
     * that allows some selectivity over the classes that appear in the iterator.
     * Consider the following scenario:
     * <code><pre>
     *   :B rdfs:subClassOf :A.
     *   :C rdfs:subClassOf :A.
     *   :D rdfs:subClassof :C.
     * </pre></code>
     * (so A has two sub-classes, B and C, and C has sub-class D).  In a raw model, with
     * no inference support, listing the sub-classes of A will answer B and C.  In an 
     * inferencing model, <code>rdfs:subClassOf</code> is known to be transitive, so
     * the sub-classes iterator will include D.  The <code>direct</code> sub-classes 
     * are those members of the closure of the subClassOf relation, restricted to classes that
     * cannot be reached by a longer route, i.e. the ones that are <em>directly</em> adjacent
     * to the given root.  Thus, the direct sub-classes of A are B and C only, and not D -
     * even in an inferencing graph.  Note that this is not the same as the entailments
     * from the raw graph. Suppose we add to this example: 
     * <code><pre>
     *   :D rdfs:subClassof :A.
     * </pre></code>
     * Now, in the raw graph, A has sub-class C.  But the direct sub-classes of A remain
     * B and C, since there is a longer path A-C-D that means that D is not a direct sub-class
     * of A.  The assertion in the raw graph that A has sub-class D is essentially redundant,
     * since this can be inferred from the closure of the graph.
     * </p>
     * <p>
     * <strong>Note:</strong> This is is a change from the behaviour of Jena 1, which took a 
     * parameter <code>closed</code> to compute the closure over transitivity and equivalence
     * of sub-classes.  The closure capability in Jena2 is determined by the inference engine
     * that is wrapped with the ontology model.  The direct parameter is provided to allow,
     * for exmaple, a level-by-level traversal of the class hierarchy, starting at some given
     * root.
     * </p>
     * 
     * @param direct If true, only answer the direcly adjacent classes in the
     * sub-class relation: i&#046;e&#046; eliminate any class for which there is a longer route
     * to reach that child under the sub-class relation.
     * @return an iterator over the resources representing this class's sub-classes
     * @exception OntProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.   
     */
    public ExtendedIterator listSubClasses( boolean direct ) {
        return listDirectPropertyValues( getProfile().SUB_CLASS_OF(), "SUB_CLASS_OF", OntClass.class, getProfile().SUB_CLASS_OF(), direct, true );
    }


    /**
     * <p>Answer true if the given class is a sub-class of this class.</p>
     * @param cls A class to test.
     * @return True if the given class is a sub-class of this class.
     * @exception OntProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.   
     */
    public boolean hasSubClass( Resource cls ) {
        return hasSubClass( cls, false );
    }
    
    /**
     * <p>Answer true if the given class is a sub-class of this class.
     * See {@link #listSubClasses( boolean )} for a full explanation of the <em>direct</em>
     * parameter.
     * </p>
     * @param cls A class to test.
     * @param direct If true, only search the classes that are directly adjacent to this 
     * class in the class hierarchy.
     * @return True if the given class is a sub-class of this class.
     * @exception OntProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.   
     */
    public boolean hasSubClass( Resource cls, boolean direct ) {
        return ((OntClass) cls.as( OntClass.class )).hasSuperClass( this, direct );
    }
    
    /**
     * <p>Remove the given class from the sub-classes of this class.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls A class to be removed from the sub-classes of this class
     * @exception OntProfileException If the {@link Profile#SUB_CLASS_OF()} class is not supported in the current language profile.   
     */
    public void removeSubClass( Resource cls ) {
        ((OntClass) cls.as( OntClass.class)).removeSuperClass( this );
    }
    

    // equivalentClass
    
    /**
     * <p>Assert that the given class is equivalent to this class. Any existing 
     * statements for <code>equivalentClass</code> will be removed.</p>
     * @param cls The class that this class is a equivalent to.
     * @exception OntProfileException If the {@link Profile#EQUIVALENT_CLASS()} property is not supported in the current language profile.   
     */ 
    public void setEquivalentClass( Resource cls ) {
        setPropertyValue( getProfile().EQUIVALENT_CLASS(), "EQUIVALENT_CLASS", cls );
    }

    /**
     * <p>Add a class that is equivalent to this class.</p>
     * @param cls A class that is equivalent to this class.
     * @exception OntProfileException If the {@link Profile#EQUIVALENT_CLASS()} property is not supported in the current language profile.   
     */ 
    public void addEquivalentClass( Resource cls ) {
        addPropertyValue( getProfile().EQUIVALENT_CLASS(), "EQUIVALENT_CLASS", cls );
    }

    /**
     * <p>Answer a class that is equivalent to this class. If there is
     * more than one such class, an arbitrary selection is made.</p>
     * @return A class equivalent to this class
     * @exception OntProfileException If the {@link Profile#EQUIVALENT_CLASS()} property is not supported in the current language profile.   
     */ 
    public OntClass getEquivalentClass() {
        return (OntClass) objectAs( getProfile().EQUIVALENT_CLASS(), "EQUIVALENT_CLASS", OntClass.class );
    }

    /**
     * <p>Answer an iterator over all of the classes that are declared to be equivalent classes to
     * this class. Each element of the iterator will be an {@link OntClass}.</p>
     * @return An iterator over the classes equivalent to this class.
     * @exception OntProfileException If the {@link Profile#EQUIVALENT_CLASS()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listEquivalentClasses() {
        return listAs( getProfile().EQUIVALENT_CLASS(), "EQUIVALENT_CLASS", OntClass.class );
    }

    /**
     * <p>Answer true if the given class is equivalent to this class.</p>
     * @param class A class to test for
     * @return True if the given property is equivalent to this class.
     * @exception OntProfileException If the {@link Profile#EQUIVALENT_CLASS()} property is not supported in the current language profile.   
     */
    public boolean hasEquivalentClass( Resource cls ) {
        return hasPropertyValue( getProfile().EQUIVALENT_CLASS(), "EQUIVALENT_CLASS", cls );
    }
    
    /**
     * <p>Remove the statement that this class and the given class are
     * equivalent.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls A class that may be declared to be equivalent to this class, and which is no longer equivalent 
     * @exception OntProfileException If the {@link Profile#EQUIVALENT_CLASS()()} property is not supported in the current language profile.   
     */
    public void removeEquivalentClass( Resource cls ) {
        removePropertyValue( getProfile().EQUIVALENT_CLASS(), "EQUIVALENT_CLASS", cls );
    }
    
    // disjointWith
    
    /**
     * <p>Assert that this class is disjoint with the given class. Any existing 
     * statements for <code>disjointWith</code> will be removed.</p>
     * @param cls The property that this class is disjoint with.
     * @exception OntProfileException If the {@link Profile#DISJOINT_WITH()} property is not supported in the current language profile.   
     */ 
    public void setDisjointWith( Resource cls ) {
        setPropertyValue( getProfile().DISJOINT_WITH(), "DISJOINT_WITH", cls );
    }

    /**
     * <p>Add a class that this class is disjoint with.</p>
     * @param cls A class that has no instances in common with this class.
     * @exception OntProfileException If the {@link Profile#DISJOINT_WITH()} property is not supported in the current language profile.   
     */ 
    public void addDisjointWith( Resource cls ) {
        addPropertyValue( getProfile().DISJOINT_WITH(), "DISJOINT_WITH", cls );
    }

    /**
     * <p>Answer a class with which this class is disjoint. If there is
     * more than one such class, an arbitrary selection is made.</p>
     * @return A class disjoint with this class
     * @exception OntProfileException If the {@link Profile#DISJOINT_WITH()} property is not supported in the current language profile.   
     */ 
    public OntClass getDisjointWith() {
        return (OntClass) objectAs( getProfile().DISJOINT_WITH(), "DISJOINT_WITH", OntClass.class );
    }

    /**
     * <p>Answer an iterator over all of the classes that this class is declared to be disjoint with.
     * Each element of the iterator will be an {@link OntClass}.</p>
     * @return An iterator over the classes disjoint with this class.
     * @exception OntProfileException If the {@link Profile#DISJOINT_WITH()} property is not supported in the current language profile.   
     */ 
    public ExtendedIterator listDisjointWith() {
        return listAs( getProfile().DISJOINT_WITH(), "DISJOINT_WITH", OntClass.class );
    }

    /**
     * <p>Answer true if this class is disjoint with the given class.</p>
     * @param cls A class to test
     * @return True if the this class is disjoint with the the given class.
     * @exception OntProfileException If the {@link Profile#DISJOINT_WITH()} property is not supported in the current language profile.   
     */
    public boolean isDisjointWith( Resource cls ) {
        return hasPropertyValue( getProfile().DISJOINT_WITH(), "DISJOINT_WITH", cls );
    }
    
    /**
     * <p>Remove the statement that this class and the given class are
     * disjoint.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls A class that may be declared to be disjoint with this class, and which is no longer disjoint 
     * @exception OntProfileException If the {@link Profile#DISJOINT_WITH()()()} property is not supported in the current language profile.   
     */
    public void removeDisjointWith( Resource cls ) {
        removePropertyValue( getProfile().DISJOINT_WITH(), "DISJOINT_WITH", cls );
    }
    

    // other utility methods
    
    /**
     * <p>Answer an iteration of the properties that may be used for
     * instances of this class: i&#046;e&#046; the properties that have this class,
     * or one of its super-classes, as domain.<p>
     *
     * @return An iteration of the properties that have this class in the domain
     */
    public ExtendedIterator listDeclaredProperties() {
        return listDeclaredProperties( true );
    }


    /**
     * <p>Answer an iteration of the properties that may be used for
     * instances of this class: i&#046;e&#046; the properties that have this class,
     * or optionally one of its super-classes, as domain.</p>
     *
     * @param all If true, use all available information from the class hierarchy;
     * if false, only use properties defined for this class alone.
     * @return An iteration of the properties that have this class as domain
     */
    public ExtendedIterator listDeclaredProperties( boolean all ) {
        // decide which model to use, based on whether we want entailments
        Model m = all ? getModel() : ((OntModel) getModel()).getBaseModel();

        // list properties that have this class as domain
        ExtendedIterator results0 = m.listStatements( null, getProfile().DOMAIN(), this )
                                    .mapWith( new SubjectMapper() );
        
        // if defined, add the restrictions on this property
        if (getProfile().ON_PROPERTY() != null) {
            // a filter for restrictions with an upper cardinality of zero
            Filter zeroCard = new Filter() {
                public boolean accept( Object x ) {
                    Resource subj = nodeAsResource( (Node) ((List) x).get( 0 ) );
                    return subj.hasProperty( getProfile().MAX_CARDINALITY(), 0 ) || 
                           subj.hasProperty( getProfile().CARDINALITY(), 0 );
                }
            };
            
            // partial modality
            Query q0 = new Query();
            q0.addMatch( asNode(), getProfile().SUB_CLASS_OF().asNode(), Query.X );
            q0.addMatch( Query.X, getProfile().ON_PROPERTY().asNode(), Query.Y );
            
            // complete modality
            Query q1 = new Query();
            q1.addMatch( asNode(), getProfile().EQUIVALENT_CLASS().asNode(), Query.X );
            q1.addMatch( Query.X, getProfile().ON_PROPERTY().asNode(), Query.Y );
            
            // extract the results
            ExtendedIterator qr0 = m.queryHandler().prepareBindings( q0, new Node[] {Query.X, Query.Y} ).executeBindings();
            ExtendedIterator qr1 = m.queryHandler().prepareBindings( q1, new Node[] {Query.Y, Query.Y} ).executeBindings();
            
            // we rely on the reasoner for sub-class and equivalence reasoning
            ExtendedIterator results1 = qr0.andThen( qr1 ).filterDrop( zeroCard );
            
            Map1 bind1 = new Map1() {public Object map1( Object x ) { return nodeAsResource( (Node) ((List) x).get( 1 ) );} };
            results0 = results0.andThen( results1.mapWith( bind1 ) );
        }
        
        // map each answer value to the appropriate ehnanced node
        return new UniqueExtendedIterator( results0 );
    }

    // TODO: replace this with the new method Chris is going to add to Model
    private Resource nodeAsResource( Node n ) { return (Resource) ResourceImpl.rdfNodeFactory.wrap( n, (EnhGraph) getModel() ); }
    
    
    /**
     * <p>Answer an iterator over the individuals in the model that have this
     * class among their types.<p>
     *
     * @return An iterator over those instances that have this class as one of
     *         the classes to which they belong
     */
    public ExtendedIterator listInstances() {
        return getModel().listStatements( null, RDF.type, this ).mapWith( new SubjectAsMapper( null ) );
    }


    // access to facets
    /** 
     * <p>Answer a view of this class as an enumerated class</p>
     * @return This class, but viewed as an EnumeratedClass node
     * @exception ConversionException if the class cannot be converted to an enumerated class
     * given the lanuage profile and the current state of the underlying model.
     */
    public EnumeratedClass asEnumeratedClass() {
        return (EnumeratedClass) as( EnumeratedClass.class );
    }
         
    /** 
     * <p>Answer a view of this class as a union class</p>
     * @return This class, but viewed as a UnionClass node
     * @exception ConversionException if the class cannot be converted to a union class
     * given the lanuage profile and the current state of the underlying model.
     */
    public UnionClass asUnionClass()  {
        return (UnionClass) as( UnionClass.class );
    }
         
    /** 
     * <p>Answer a view of this class as an intersection class</p>
     * @return This class, but viewed as an IntersectionClass node
     * @exception ConversionException if the class cannot be converted to an intersection class
     * given the lanuage profile and the current state of the underlying model.
     */
    public IntersectionClass asIntersectionClass()  {
        return (IntersectionClass) as( IntersectionClass.class );
    }
         
    /** 
     * <p>Answer a view of this class as a complement class</p>
     * @return This class, but viewed as a ComplementClass node
     * @exception ConversionException if the class cannot be converted to a complement class
     * given the lanuage profile and the current state of the underlying model.
     */
    public ComplementClass asComplementClass() {
        return (ComplementClass) as( ComplementClass.class );
    }
         
    /** 
     * <p>Answer a view of this class as a restriction class expression</p>
     * @return This class, but viewed as a Restriction node
     * @exception ConversionException if the class cannot be converted to a restriction
     * given the lanuage profile and the current state of the underlying model.
     */
    public Restriction asRestriction() {
        return (Restriction) as( Restriction.class );
    }
         
     
    // sub-type testing

    /** 
     * <p>Answer true if this class is an enumerated class expression</p>
     * @return True if this is an enumerated class expression
     */
    public boolean isEnumeratedClass() {
        checkProfile( getProfile().ONE_OF(), "ONE_OF" );
        return canAs( EnumeratedClass.class );
    }
         
    /** 
     * <p>Answer true if this class is a union class expression</p>
     * @return True if this is a union class expression
     */
    public boolean isUnionClass() {
        checkProfile( getProfile().UNION_OF(), "UNION_OF" );
        return canAs( UnionClass.class );
    }
         
    /** 
     * <p>Answer true if this class is an intersection class expression</p>
     * @return True if this is an intersection class expression
     */
    public boolean isIntersectionClass() {
        checkProfile( getProfile().INTERSECTION_OF(), "INTERSECTION_OF" );
        return canAs( IntersectionClass.class );
    }
         
    /** 
     * <p>Answer true if this class is a complement class expression</p>
     * @return True if this is a complement class expression
     */
    public boolean isComplementClass() {
        checkProfile( getProfile().COMPLEMENT_OF(), "COMPLEMENT_OF" );
        return canAs( ComplementClass.class );
    }
         
    /** 
     * <p>Answer true if this class is a property restriction</p>
     * @return True if this is a restriction
     */
    public boolean isRestriction() {
        checkProfile( getProfile().RESTRICTION(), "RESTRICTION" );
        return canAs( Restriction.class );
    }
         
     
    // conversion operations
    
    /** 
     * <p>Answer a view of this class as an enumeration of the given individuals.</p>
     * @param individuals A list of the individuals that will comprise the permitted values of this
     * class converted to an enumeration
     * @return This ontology class, converted to an enumeration of the given individuals 
     */
    public EnumeratedClass convertToEnumeratedClass( RDFList individuals ) {
        setPropertyValue( getProfile().ONE_OF(), "ONE_OF", individuals );
        return (EnumeratedClass) as( EnumeratedClass.class );
    }

    /** 
     * <p>Answer a view of this class as an intersection of the given classes.</p>
     * @param classes A list of the classes that will comprise the operands of the intersection
     * @return This ontology class, converted to an intersection of the given classes 
     */
    public IntersectionClass convertToIntersectionClass( RDFList classes ) {
        setPropertyValue( getProfile().INTERSECTION_OF(), "INTERSECTION_OF", classes );
        return (IntersectionClass) as( IntersectionClass.class );
    }

    /** 
     * <p>Answer a view of this class as a union of the given classes.</p>
     * @param classes A list of the classes that will comprise the operands of the union
     * @return This ontology class, converted to an union of the given classes 
     */
    public UnionClass convertToUnionClass( RDFList classes ) {
        setPropertyValue( getProfile().UNION_OF(), "UNION_OF", classes );
        return (UnionClass) as( UnionClass.class );
    }

    /** 
     * <p>Answer a view of this class as an complement of the given class.</p>
     * @param cls An ontology classs that will be operand of the complement
     * @return This ontology class, converted to an complement of the given class 
     */
    public ComplementClass convertToComplementClass( Resource cls ) {
        setPropertyValue( getProfile().COMPLEMENT_OF(), "COMPLEMENT_OF", cls );
        return (ComplementClass) as( ComplementClass.class );
    }

    /** 
     * <p>Answer a view of this class as an resriction on the given property.</p>
     * @param prop A property this is the subject of a property restriction class expression
     * @return This ontology class, converted to a restriction on the given property 
     */
    public Restriction convertToRestriction( Property prop ) {
        if (!hasRDFType( getProfile().RESTRICTION(), "RESTRICTION", false )) {
            setRDFType( getProfile().RESTRICTION() );
        }
        setPropertyValue( getProfile().ON_PROPERTY(), "ON_PROPERTY", prop );
        return (Restriction) as( Restriction.class );
    }


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

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

