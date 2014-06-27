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
package com.hp.hpl.jena.ontology.impl;



// Imports
///////////////
import java.util.* ;

import com.hp.hpl.jena.enhanced.EnhGraph ;
import com.hp.hpl.jena.enhanced.EnhNode ;
import com.hp.hpl.jena.enhanced.Implementation ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.ontology.* ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.reasoner.InfGraph ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.Filter ;
import com.hp.hpl.jena.util.iterator.UniqueFilter;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;
import com.hp.hpl.jena.vocabulary.OWL ;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.vocabulary.RDFS ;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary ;


/**
 * <p>
 * Implementation of the ontology abstraction representing ontology classes.
 * </p>
 */
public class OntClassImpl
    extends OntResourceImpl
    implements OntClass
{
    // Constants
    //////////////////////////////////

    /* LDP never returns properties in these namespaces */
    private static final String[] IGNORE_NAMESPACES = new String[] {
            OWL.NS,
            RDF.getURI(),
            RDFS.getURI(),
            ReasonerVocabulary.RBNamespace
    };


    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating OntClass facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    @SuppressWarnings("hiding")
    public static Implementation factory = new Implementation() {
        @Override
        public EnhNode wrap( Node n, EnhGraph eg ) {
            if (canWrap( n, eg )) {
                return new OntClassImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n.toString() + " to OntClass: it does not have rdf:type owl:Class or equivalent");
            }
        }

        @Override
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being an OntClass facet if it has rdf:type owl:Class or equivalent
            Profile profile = (eg instanceof OntModel) ? ((OntModel) eg).getProfile() : null;
            return (profile != null)  &&  profile.isSupported( node, eg, OntClass.class );
        }
    };


    // Instance variables
    //////////////////////////////////



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
    }

    // External signature methods
    //////////////////////////////////

    // subClassOf

    /**
     * <p>Assert that this class is sub-class of the given class. Any existing
     * statements for <code>subClassOf</code> will be removed.</p>
     * @param cls The class that this class is a sub-class of
     * @exception ProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.
     */
    @Override
    public void setSuperClass( Resource cls ) {
        setPropertyValue( getProfile().SUB_CLASS_OF(), "SUB_CLASS_OF", cls );
    }

    /**
     * <p>Add a super-class of this class.</p>
     * @param cls A class that is a super-class of this class.
     * @exception ProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.
     */
    @Override
    public void addSuperClass( Resource cls ) {
        addPropertyValue( getProfile().SUB_CLASS_OF(), "SUB_CLASS_OF", cls );
    }

    /**
     * <p>Answer a class that is the super-class of this class. If there is
     * more than one such class, an arbitrary selection is made.</p>
     * @return A super-class of this class
     * @exception ProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.
     */
    @Override
    public OntClass getSuperClass() {
        return objectAs( getProfile().SUB_CLASS_OF(), "SUB_CLASS_OF", OntClass.class );
    }

    /**
     * <p>Answer an iterator over all of the classes that are declared to be super-classes of
     * this class. Each element of the iterator will be an {@link OntClass}.</p>
     * @return An iterator over the super-classes of this class.
     * @exception ProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<OntClass> listSuperClasses() {
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
     * @exception ProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<OntClass> listSuperClasses( boolean direct ) {
        return listDirectPropertyValues( getProfile().SUB_CLASS_OF(), "SUB_CLASS_OF", OntClass.class, getProfile().SUB_CLASS_OF(), direct, false )
                .filterDrop( new SingleEqualityFilter<OntClass>( this ) ).filterKeep( new UniqueFilter<OntClass>());
    }

    /**
     * <p>Answer true if the given class is a super-class of this class.</p>
     * @param cls A class to test.
     * @return True if the given class is a super-class of this class.
     * @exception ProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.
     */
    @Override
    public boolean hasSuperClass( Resource cls ) {
        return hasSuperClass( cls, false );
    }

    /**
     * <p>Answer true if this class has any super-class in the model. Note that
     * when using a reasoner, all OWL classes have owl:Thing as a super-class.</p>
     * @return True if this class has any known super-class.
     * @exception ProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.
     */
    @Override
    public boolean hasSuperClass() {
        return getSuperClass() != null;
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
     * @exception ProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.
     */
    @Override
    public boolean hasSuperClass( Resource cls, boolean direct ) {
        if (!direct) {
            // don't need any special case, we just get the property
            return hasPropertyValue( getProfile().SUB_CLASS_OF(), "SUB_CLASS_OF", cls );
        }
        else {
            // we want the direct, not general relationship
            // first try to find an inf graph that can do the work for us
            InfGraph ig = null;
            if (getGraph() instanceof InfGraph) {
                ig = (InfGraph) getGraph();
            }
            else if (getGraph() instanceof OntModel) {
                OntModel m = (OntModel) getGraph();
                if (m.getGraph() instanceof InfGraph) {
                    ig = (InfGraph) m.getGraph();
                }
            }

            if (ig != null && ig.getReasoner().supportsProperty( ReasonerVocabulary.directSubClassOf )) {
                // we can look this up directly
                return hasPropertyValue( ReasonerVocabulary.directSubClassOf, "direct sub-class", cls );
            }
            else {
                // otherwise, not an inf-graph or the given inf-graph does not support direct directly (:-)
                return hasSuperClassDirect(cls);
            }
        }
    }

    /**
     * <p>Remove the given class from the super-classes of this class.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls A class to be removed from the super-classes of this class
     * @exception ProfileException If the {@link Profile#SUB_CLASS_OF()} class is not supported in the current language profile.
     */
    @Override
    public void removeSuperClass( Resource cls ) {
        removePropertyValue( getProfile().SUB_CLASS_OF(), "SUB_CLASS_OF", cls );
    }

    /**
     * <p>Assert that this class is super-class of the given class. Any existing
     * statements for <code>subClassOf</code> on <code>prop</code> will be removed.</p>
     * @param cls The class that is a sub-class of this class
     * @exception ProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.
     */
    @Override
    public void setSubClass( Resource cls ) {
        // first we have to remove all of the inverse sub-class links
        checkProfile( getProfile().SUB_CLASS_OF(), "SUB_CLASS_OF" );
        for (StmtIterator i = getModel().listStatements( null, getProfile().SUB_CLASS_OF(), this );  i.hasNext(); ) {
            i.removeNext();
        }

        cls.as( OntClass.class ).addSuperClass( this );
    }

    /**
     * <p>Add a sub-class of this class.</p>
     * @param cls A class that is a sub-class of this class.
     * @exception ProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.
     */
    @Override
    public void addSubClass( Resource cls ) {
        cls.as( OntClass.class ).addSuperClass( this );
    }

    /**
     * <p>Answer a class that is the sub-class of this class. If there is
     * more than one such class, an arbitrary selection is made. If
     * there is no such class, return null.</p>
     * @return A sub-class of this class or null
     * @exception ProfileException If the {@link Profile#SUB_CLASS_OF()}
     * property is not supported in the current language profile.
     */
    @Override
    public OntClass getSubClass() {
        checkProfile( getProfile().SUB_CLASS_OF(), "SUB_CLASS_OF" );
        StmtIterator i = getModel().listStatements( null, getProfile().SUB_CLASS_OF(), this );
        try {
            if (i.hasNext()) {
                return i.nextStatement()
                                   .getSubject()
                                   .as( OntClass.class );
            }
            else {
                return null;
            }
        }
        finally {
            i.close();
        }
    }

    /**
     * <p>Answer an iterator over all of the classes that are declared to be sub-classes of
     * this class. Each element of the iterator will be an {@link OntClass}.</p>
     * @return An iterator over the sub-classes of this class.
     * @exception ProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<OntClass> listSubClasses() {
        return listSubClasses( false );
    }

    /**
     * <p>
     * Answer an iterator over the classes that are declared to be sub-classes of
     * this class. Each element of the iterator will be an {@link OntClass}. The
     * distinguishing extra parameter for this method is the flag <code>direct</code>
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
     * @exception ProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<OntClass> listSubClasses( boolean direct ) {
        return listDirectPropertyValues( getProfile().SUB_CLASS_OF(), "SUB_CLASS_OF", OntClass.class, getProfile().SUB_CLASS_OF(), direct, true )
                .filterDrop( new SingleEqualityFilter<OntClass>( this ) ).filterKeep( new UniqueFilter<OntClass>());
    }


    /**
     * <p>Answer true if the given class is a sub-class of this class.</p>
     * @param cls A class to test.
     * @return True if the given class is a sub-class of this class.
     * @exception ProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.
     */
    @Override
    public boolean hasSubClass( Resource cls ) {
        return hasSubClass( cls, false );
    }

    /**
     * <p>Answer true if this class has any sub-class in the model. Note that
     * when using a reasoner, all OWL classes have owl:Nothing as a sub-class.</p>
     * @return True if this class has any known sub-class.
     * @exception ProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.
     */
    @Override
    public boolean hasSubClass() {
        return getSubClass() != null;
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
     * @exception ProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.
     */
    @Override
    public boolean hasSubClass( Resource cls, boolean direct ) {
        if (getModel() instanceof OntModel &&
            (cls.getModel() == null || !(cls.getModel() instanceof OntModel)))
        {
            // could be outside an ontmodel if a constant
            cls = cls.inModel( getModel() );
        }
        return cls.as( OntClass.class ).hasSuperClass( this, direct );
    }

    /**
     * <p>Remove the given class from the sub-classes of this class.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls A class to be removed from the sub-classes of this class
     * @exception ProfileException If the {@link Profile#SUB_CLASS_OF()} class is not supported in the current language profile.
     */
    @Override
    public void removeSubClass( Resource cls ) {
        (cls.as( OntClass.class)).removeSuperClass( this );
    }


    // equivalentClass

    /**
     * <p>Assert that the given class is equivalent to this class. Any existing
     * statements for <code>equivalentClass</code> will be removed.</p>
     * @param cls The class that this class is a equivalent to.
     * @exception ProfileException If the {@link Profile#EQUIVALENT_CLASS()} property is not supported in the current language profile.
     */
    @Override
    public void setEquivalentClass( Resource cls ) {
        setPropertyValue( getProfile().EQUIVALENT_CLASS(), "EQUIVALENT_CLASS", cls );
    }

    /**
     * <p>Add a class that is equivalent to this class.</p>
     * @param cls A class that is equivalent to this class.
     * @exception ProfileException If the {@link Profile#EQUIVALENT_CLASS()} property is not supported in the current language profile.
     */
    @Override
    public void addEquivalentClass( Resource cls ) {
        addPropertyValue( getProfile().EQUIVALENT_CLASS(), "EQUIVALENT_CLASS", cls );
    }

    /**
     * <p>Answer a class that is equivalent to this class. If there is
     * more than one such class, an arbitrary selection is made.</p>
     * @return A class equivalent to this class
     * @exception ProfileException If the {@link Profile#EQUIVALENT_CLASS()} property is not supported in the current language profile.
     */
    @Override
    public OntClass getEquivalentClass() {
        return objectAs( getProfile().EQUIVALENT_CLASS(), "EQUIVALENT_CLASS", OntClass.class );
    }

    /**
     * <p>Answer an iterator over all of the classes that are declared to be equivalent classes to
     * this class. Each element of the iterator will be an {@link OntClass}.</p>
     * @return An iterator over the classes equivalent to this class.
     * @exception ProfileException If the {@link Profile#EQUIVALENT_CLASS()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<OntClass> listEquivalentClasses() {
        return listAs( getProfile().EQUIVALENT_CLASS(), "EQUIVALENT_CLASS", OntClass.class ).filterKeep( new UniqueFilter<OntClass>());
    }

    /**
     * <p>Answer true if the given class is equivalent to this class.</p>
     * @param cls A class to test for
     * @return True if the given property is equivalent to this class.
     * @exception ProfileException If the {@link Profile#EQUIVALENT_CLASS()} property is not supported in the current language profile.
     */
    @Override
    public boolean hasEquivalentClass( Resource cls ) {
        return hasPropertyValue( getProfile().EQUIVALENT_CLASS(), "EQUIVALENT_CLASS", cls );
    }

    /**
     * <p>Remove the statement that this class and the given class are
     * equivalent.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls A class that may be declared to be equivalent to this class, and which is no longer equivalent
     * @exception ProfileException If the {@link Profile#EQUIVALENT_CLASS()} property is not supported in the current language profile.
     */
    @Override
    public void removeEquivalentClass( Resource cls ) {
        removePropertyValue( getProfile().EQUIVALENT_CLASS(), "EQUIVALENT_CLASS", cls );
    }

    // disjointWith

    /**
     * <p>Assert that this class is disjoint with the given class. Any existing
     * statements for <code>disjointWith</code> will be removed.</p>
     * @param cls The property that this class is disjoint with.
     * @exception ProfileException If the {@link Profile#DISJOINT_WITH()} property is not supported in the current language profile.
     */
    @Override
    public void setDisjointWith( Resource cls ) {
        setPropertyValue( getProfile().DISJOINT_WITH(), "DISJOINT_WITH", cls );
    }

    /**
     * <p>Add a class that this class is disjoint with.</p>
     * @param cls A class that has no instances in common with this class.
     * @exception ProfileException If the {@link Profile#DISJOINT_WITH()} property is not supported in the current language profile.
     */
    @Override
    public void addDisjointWith( Resource cls ) {
        addPropertyValue( getProfile().DISJOINT_WITH(), "DISJOINT_WITH", cls );
    }

    /**
     * <p>Answer a class with which this class is disjoint. If there is
     * more than one such class, an arbitrary selection is made.</p>
     * @return A class disjoint with this class
     * @exception ProfileException If the {@link Profile#DISJOINT_WITH()} property is not supported in the current language profile.
     */
    @Override
    public OntClass getDisjointWith() {
        return objectAs( getProfile().DISJOINT_WITH(), "DISJOINT_WITH", OntClass.class );
    }

    /**
     * <p>Answer an iterator over all of the classes that this class is declared to be disjoint with.
     * Each element of the iterator will be an {@link OntClass}.</p>
     * @return An iterator over the classes disjoint with this class.
     * @exception ProfileException If the {@link Profile#DISJOINT_WITH()} property is not supported in the current language profile.
     */
    @Override
    public ExtendedIterator<OntClass> listDisjointWith() {
        return listAs( getProfile().DISJOINT_WITH(), "DISJOINT_WITH", OntClass.class ).filterKeep( new UniqueFilter<OntClass>() ) ;
    }

    /**
     * <p>Answer true if this class is disjoint with the given class.</p>
     * @param cls A class to test
     * @return True if the this class is disjoint with the the given class.
     * @exception ProfileException If the {@link Profile#DISJOINT_WITH()} property is not supported in the current language profile.
     */
    @Override
    public boolean isDisjointWith( Resource cls ) {
        return hasPropertyValue( getProfile().DISJOINT_WITH(), "DISJOINT_WITH", cls );
    }

    /**
     * <p>Remove the statement that this class and the given class are
     * disjoint.  If this statement
     * is not true of the current model, nothing happens.</p>
     * @param cls A class that may be declared to be disjoint with this class, and which is no longer disjoint
     * @exception ProfileException If the {@link Profile#DISJOINT_WITH()} property is not supported in the current language profile.
     */
    @Override
    public void removeDisjointWith( Resource cls ) {
        removePropertyValue( getProfile().DISJOINT_WITH(), "DISJOINT_WITH", cls );
    }


    // other utility methods

    /**
     * Equivalent to calling {@link #listDeclaredProperties(boolean)} with
     * default value <code>direct = false</code>.
     *
     */
    @Override
    public ExtendedIterator<OntProperty> listDeclaredProperties() {
        return listDeclaredProperties( false );
    }


    /**
     * <p>Return an iterator over the properties associated with a frame-like
     * view of this class. This captures an intuitive notion of the <em>
     * properties of a class</em>. This can be useful in presenting an ontology
     * class in a user interface, for example by automatically constructing a
     * form to instantiate instances of the class. The properties in the frame-like
     * view of the class are determined by comparing the domain of properties in
     * this class's {@link OntModel} with the class itself.  See:
     * <a href="/documentation/notes/rdf-frames.html">Presenting RDF as frames</a>
     * for more details.
     * </p>
     * <p>
     * Note that many cases of determining whether a
     * property is associated with a class depends on RDFS or OWL reasoning.
     * This method may therefore return complete results only in models that
     * have an attached reasoner.
     * </p>
     *
     * @param direct If <code>true</code>, restrict the properties returned to those directly
     * associated with this class. If <code>false</code>, the properties of super-classes of
     * this class will not be listed among the declared properties of this class.
     * @return An iteration of the properties that are associated with this class
     * by their domain.
     */
    @Override
    public ExtendedIterator<OntProperty> listDeclaredProperties( boolean direct ) {
        // first collect the candidate properties
        Set<RDFNode> candSet = new HashSet<>();

        // if the attached model does inference, it will potentially find more of these
        // than a non-inference model
        for (Iterator<Statement> i = listAllProperties(); i.hasNext(); ) {
            candSet.add( i.next().getSubject().as( Property.class ) );
        }

        // now we iterate over the candidates and check that they match all domain constraints
        List<RDFNode> cands = new ArrayList<>();
        cands.addAll( candSet );
        for (int j = cands.size() -1; j >= 0; j--) {
            Property cand = (Property) cands.get( j );
            if (!hasDeclaredProperty( cand, direct )) {
                cands.remove( j );
            }
        }

        // return the results, using the ont property facet
        return WrappedIterator.create( cands.iterator() )
                              .mapWith( new AsMapper<>( OntProperty.class ) );
    }


    /**
     * <p>Answer true if the given property is one of the declared properties
     * of this class. For details, see {@link #listDeclaredProperties(boolean)}.</p>
     * @param p A property to test
     * @param direct If true, only direct associations between classes and properties
     * are considered
     * @return True if <code>p</code> is one of the declared properties of
     * this class
     */
    @Override
    public boolean hasDeclaredProperty( Property p, boolean direct ) {
        return testDomain( p, direct );
    }


    /**
     * <p>Answer an iterator over the individuals in the model that have this
     * class among their types.<p>
     *
     * @return An iterator over those instances that have this class as one of
     *         the classes to which they belong
     */
    @Override
    public ExtendedIterator<Individual> listInstances() {
        return listInstances( false );
    }


    /**
     * <p>Answer an iterator over the individuals in the model that have this
     * class among their types, optionally excluding sub-classes of this class.<p>
     *
     * @param  direct If true, only direct instances are counted (i.e. not instances
     * of sub-classes of this class)
     * @return An iterator over those instances that have this class as one of
     *         the classes to which they belong
     */
    @Override
    public ExtendedIterator<Individual> listInstances( final boolean direct ) {
        return getModel()
                .listStatements( null, RDF.type, this )
                .mapWith( new SubjectAsMapper<>( Individual.class ) )
                .filterKeep( new Filter<Individual>() {
                    @Override
                    public boolean accept( Individual o ) {
                        // if direct, ignore the sub-class typed resources
                        return o.hasRDFType( OntClassImpl.this, direct );
                    }} ).filterKeep( new UniqueFilter<Individual>());
    }


    /**
     * <p>Answer a new individual that has this class as its <code>rdf:type</code></p>
     * @return A new anonymous individual that is an instance of this class
     */
    @Override
    public Individual createIndividual() {
        return ((OntModel) getModel()).createIndividual( this );
    }


    /**
     * <p>Answer a new individual that has this class as its <code>rdf:type</code></p>
     * @param uri The URI of the new individual
     * @return A new named individual that is an instance of this class
     */
    @Override
    public Individual createIndividual( String uri ) {
        return ((OntModel) getModel()).createIndividual( uri, this );
    }


    /**
     * <p>Remove the given individual from the set of instances that are members of
     * this class. This is effectively equivalent to the {@link Individual#removeOntClass} method,
     * but invoked via the class resource rather than via the individual resource.</p>
     * @param individual A resource denoting an individual that is no longer to be a member
     * of this class
     */
    @Override
    public void dropIndividual( Resource individual ) {
        getModel().remove( individual, RDF.type, this );
    }


    /**
     * <p>Answer true if this class is one of the roots of the class hierarchy.
     * This will be true if either (i) this class has <code>owl:Thing</code>
     * as a direct super-class, or (ii) it has
     * no declared super-classes (including anonymous class expressions).</p>
     * @return True if this class is the root of the class hierarchy in the
     * model it is attached to
     */
    @Override
    public boolean isHierarchyRoot() {
        // sanity check - :Nothing is never a root class
        if (equals( getProfile().NOTHING() )) {
            return false;
        }

        // the only super-classes of a root class are the various aliases
        // of Top, or itself

        /**
            Note: moved the initialisation of i outside the try-catch, otherwise an
            exception in listSuperClasses [eg a broken Graph implementation] will
            avoid i's initialisation but still run i.close, generating a mysterious
            NullPointerException. Signed, Mr Burnt Spines.
         */
        ExtendedIterator<OntClass> i = listSuperClasses( true );
        try {

            while (i.hasNext()) {
                Resource sup = i.next();
                if (!(sup.equals( getProfile().THING() ) ||
                      sup.equals( RDFS.Resource ) ||
                      sup.equals( this )))
                {
                    // a super that indicates this is not a root class
                    return false;
                }
            }
        }
        finally {
            i.close();
        }

        return true;
    }


    // access to facets
    /**
     * <p>Answer a view of this class as an enumerated class</p>
     * @return This class, but viewed as an EnumeratedClass node
     * @exception ConversionException if the class cannot be converted to an enumerated class
     * given the lanuage profile and the current state of the underlying model.
     */
    @Override
    public EnumeratedClass asEnumeratedClass() {
        return as( EnumeratedClass.class );
    }

    /**
     * <p>Answer a view of this class as a union class</p>
     * @return This class, but viewed as a UnionClass node
     * @exception ConversionException if the class cannot be converted to a union class
     * given the lanuage profile and the current state of the underlying model.
     */
    @Override
    public UnionClass asUnionClass()  {
        return as( UnionClass.class );
    }

    /**
     * <p>Answer a view of this class as an intersection class</p>
     * @return This class, but viewed as an IntersectionClass node
     * @exception ConversionException if the class cannot be converted to an intersection class
     * given the lanuage profile and the current state of the underlying model.
     */
    @Override
    public IntersectionClass asIntersectionClass()  {
        return as( IntersectionClass.class );
    }

    /**
     * <p>Answer a view of this class as a complement class</p>
     * @return This class, but viewed as a ComplementClass node
     * @exception ConversionException if the class cannot be converted to a complement class
     * given the lanuage profile and the current state of the underlying model.
     */
    @Override
    public ComplementClass asComplementClass() {
        return as( ComplementClass.class );
    }

    /**
     * <p>Answer a view of this class as a restriction class expression</p>
     * @return This class, but viewed as a Restriction node
     * @exception ConversionException if the class cannot be converted to a restriction
     * given the lanuage profile and the current state of the underlying model.
     */
    @Override
    public Restriction asRestriction() {
        return as( Restriction.class );
    }


    // sub-type testing

    /**
     * <p>Answer true if this class is an enumerated class expression</p>
     * @return True if this is an enumerated class expression
     */
    @Override
    public boolean isEnumeratedClass() {
        checkProfile( getProfile().ONE_OF(), "ONE_OF" );
        return hasProperty( getProfile().ONE_OF() );
    }

    /**
     * <p>Answer true if this class is a union class expression</p>
     * @return True if this is a union class expression
     */
    @Override
    public boolean isUnionClass() {
        checkProfile( getProfile().UNION_OF(), "UNION_OF" );
        return hasProperty( getProfile().UNION_OF() );
    }

    /**
     * <p>Answer true if this class is an intersection class expression</p>
     * @return True if this is an intersection class expression
     */
    @Override
    public boolean isIntersectionClass() {
        checkProfile( getProfile().INTERSECTION_OF(), "INTERSECTION_OF" );
        return hasProperty( getProfile().INTERSECTION_OF() );
    }

    /**
     * <p>Answer true if this class is a complement class expression</p>
     * @return True if this is a complement class expression
     */
    @Override
    public boolean isComplementClass() {
        checkProfile( getProfile().COMPLEMENT_OF(), "COMPLEMENT_OF" );
        return hasProperty( getProfile().COMPLEMENT_OF() );
    }

    /**
     * <p>Answer true if this class is a property restriction</p>
     * @return True if this is a restriction
     */
    @Override
    public boolean isRestriction() {
        checkProfile( getProfile().RESTRICTION(), "RESTRICTION" );
        return hasProperty( getProfile().ON_PROPERTY() ) ||
               hasProperty( RDF.type, getProfile().RESTRICTION() );
    }


    // conversion operations

    /**
     * <p>Answer a view of this class as an enumeration of the given individuals.</p>
     * @param individuals A list of the individuals that will comprise the permitted values of this
     * class converted to an enumeration
     * @return This ontology class, converted to an enumeration of the given individuals
     */
    @Override
    public EnumeratedClass convertToEnumeratedClass( RDFList individuals ) {
        setPropertyValue( getProfile().ONE_OF(), "ONE_OF", individuals );
        return as( EnumeratedClass.class );
    }

    /**
     * <p>Answer a view of this class as an intersection of the given classes.</p>
     * @param classes A list of the classes that will comprise the operands of the intersection
     * @return This ontology class, converted to an intersection of the given classes
     */
    @Override
    public IntersectionClass convertToIntersectionClass( RDFList classes ) {
        setPropertyValue( getProfile().INTERSECTION_OF(), "INTERSECTION_OF", classes );
        return as( IntersectionClass.class );
    }

    /**
     * <p>Answer a view of this class as a union of the given classes.</p>
     * @param classes A list of the classes that will comprise the operands of the union
     * @return This ontology class, converted to an union of the given classes
     */
    @Override
    public UnionClass convertToUnionClass( RDFList classes ) {
        setPropertyValue( getProfile().UNION_OF(), "UNION_OF", classes );
        return as( UnionClass.class );
    }

    /**
     * <p>Answer a view of this class as an complement of the given class.</p>
     * @param cls An ontology classs that will be operand of the complement
     * @return This ontology class, converted to an complement of the given class
     */
    @Override
    public ComplementClass convertToComplementClass( Resource cls ) {
        setPropertyValue( getProfile().COMPLEMENT_OF(), "COMPLEMENT_OF", cls );
        return as( ComplementClass.class );
    }

    /**
     * <p>Answer a view of this class as an resriction on the given property.</p>
     * @param prop A property this is the subject of a property restriction class expression
     * @return This ontology class, converted to a restriction on the given property
     */
    @Override
    public Restriction convertToRestriction( Property prop ) {
        if (!hasRDFType( getProfile().RESTRICTION(), "RESTRICTION", false )) {
            setRDFType( getProfile().RESTRICTION() );
        }
        setPropertyValue( getProfile().ON_PROPERTY(), "ON_PROPERTY", prop );
        return as( Restriction.class );
    }


    // Internal implementation methods
    //////////////////////////////////

    /**
     * <p>Answer true if this class has the given class as a direct super-class, without using
     * extra help from the reasoner.</p>
     * @param cls The class to test
     * @return True if the cls is a direct super-class of this class
     */
    protected boolean hasSuperClassDirect(Resource cls) {
        // we manually compute the maximal lower elements - this could be expensive in general
        //return ResourceUtils.maximalLowerElements( listSuperClasses(), getProfile().SUB_CLASS_OF(), false ).contains( cls );

        ExtendedIterator<OntClass> i = listDirectPropertyValues( getProfile().SUB_CLASS_OF(), "subClassOf", OntClass.class,
                                                       getProfile().SUB_CLASS_OF(), true, false );
        try {
            while (i.hasNext()) {
                if (cls.equals( i.next() )) {
                    return true;
                }
            }
        }
        finally {
            i.close();
        }

        return false;
    }


    /**
     * <p>Answer true if this class lies with the domain of p<p>
     * @param p
     * @param direct If true, only consider direct associations with domain
     * @return True if this class in the domain of property <code>p</code>
     */
    protected boolean testDomain( Property p, boolean direct ) {
        // we ignore any property in the OWL, etc namespace
        String namespace = p.getNameSpace();
        for ( String IGNORE_NAMESPACE : IGNORE_NAMESPACES )
        {
            if ( namespace.equals( IGNORE_NAMESPACE ) )
            {
                return false;
            }
        }

        // check for global props, that have no specific domain constraint
        boolean isGlobal = true;

        // flag for detecting the direct case
        boolean seenDirect = false;

        for (StmtIterator i = getModel().listStatements( p, getProfile().DOMAIN(), (RDFNode) null ); i.hasNext();  ) {
            Resource domain = i.nextStatement().getResource();

            // there are some well-known values we ignore
            if (!(domain.equals( getProfile().THING() ) || domain.equals( RDFS.Resource ))) {
                // not a generic domain
                isGlobal = false;

                if (domain.equals( this )) {
                    // if this class is actually in the domain (as opposed to one of this class's
                    // super-classes), then we've detected the direct property case
                    seenDirect = true;
                }
                else if (!canProveSuperClass( domain )) {
                    // there is a class in the domain of p that is not a super-class of this class
                    return false;
                }
            }
        }

        if (direct) {
            // if we're looking for direct props, we must either have seen the direct case
            // or it's a global prop and this is a root class
            return seenDirect || (isGlobal && isHierarchyRoot());
        }
        else {
            // not direct, we must either found a global or a super-class prop
            // otherwise the 'return false' above would have kicked in
            return true;
        }
    }


    /**
     * <p>Answer an iterator over all of the properties in this model
     * @return An iterator over {@link OntProperty}
     */
    protected ExtendedIterator<Statement> listAllProperties() {
        OntModel mOnt = (OntModel) getModel();
        Profile prof = mOnt.getProfile();

        ExtendedIterator<Statement> pi = mOnt.listStatements( null, RDF.type, getProfile().PROPERTY() );

        // check reasoner capabilities - major performance improvement for inf models
        if (mOnt.getReasoner() != null) {
            Model caps = mOnt.getReasoner().getReasonerCapabilities();
            if (caps.contains( null, ReasonerVocabulary.supportsP, OWL.ObjectProperty) )
            {
                // we conclude that the reasoner can do the necessary work to infer that
                // all owl:ObjectProperty, owl:DatatypeProperty, etc, are rdf:Property resources
                return pi;
            }
        }

        // otherwise, we manually check the other property types
        if (prof.OBJECT_PROPERTY() != null) {
            pi = pi.andThen( mOnt.listStatements( null, RDF.type, prof.OBJECT_PROPERTY() ) );
        }
        if (prof.DATATYPE_PROPERTY() != null) {
            pi = pi.andThen( mOnt.listStatements( null, RDF.type, prof.DATATYPE_PROPERTY() ) );
        }
        if (prof.FUNCTIONAL_PROPERTY() != null) {
            pi = pi.andThen( mOnt.listStatements( null, RDF.type, prof.FUNCTIONAL_PROPERTY() ) );
        }
        if (prof.INVERSE_FUNCTIONAL_PROPERTY() != null) {
            pi = pi.andThen( mOnt.listStatements( null, RDF.type, prof.INVERSE_FUNCTIONAL_PROPERTY() ) );
        }
        if (prof.SYMMETRIC_PROPERTY() != null) {
            pi = pi.andThen( mOnt.listStatements( null, RDF.type, prof.SYMMETRIC_PROPERTY() ) );
        }
        if (prof.TRANSITIVE_PROPERTY() != null) {
            pi = pi.andThen( mOnt.listStatements( null, RDF.type, prof.TRANSITIVE_PROPERTY() ) );
        }
        if (prof.ANNOTATION_PROPERTY() != null) {
            pi = pi.andThen( mOnt.listStatements( null, RDF.type, prof.ANNOTATION_PROPERTY() ) );
        }

        return pi;
    }

    /**
     * <p>Answer true if we can demonstrate that this class has the given super-class.
     * If this model has a reasoner, this is equivalent to asking if the sub-class
     * relation holds. Otherwise, we simulate basic reasoning by searching upwards
     * through the class hierarchy.</p>
     * @param sup A super-class to test for
     * @return True if we can show that sup is a super-class of thsi class
     */
    protected boolean canProveSuperClass( Resource sup ) {
        OntModel om = (OntModel) getModel();
        if (om.getReasoner() != null) {
            if (om.getReasoner()
                  .getReasonerCapabilities().contains( null, ReasonerVocabulary.supportsP, RDFS.subClassOf ))
            {
                // this reasoner does transitive closure on sub-classes, so we just ask
                return hasSuperClass( sup );
            }
        }

        // otherwise, we have to search upwards through the class hierarchy
        Set<OntClass> seen = new HashSet<>();
        List<OntClass> queue = new ArrayList<>();
        queue.add( this );

        while (!queue.isEmpty()) {
            OntClass c = queue.remove( 0 );
            if (!seen.contains( c )) {
                seen.add( c );

                if (c.equals( sup )) {
                    // found the super class
                    return true;
                }
                else {
                    // queue the supers
                    for (Iterator<OntClass> i = c.listSuperClasses(); i.hasNext(); ) {
                        queue.add( i.next() );
                    }
                }
            }
        }

        // to get here, we didn't find the class we were looking for
        return false;
    }

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
