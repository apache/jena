/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            27-Mar-2003
 * Filename           $RCSfile: OntClassImpl.java,v $
 * Revision           $Revision: 1.12 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-05-30 18:47:51 $
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
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.*;

import java.util.Iterator;


/**
 * <p>
 * Implementation for the ontology abstraction representing ontology classes.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OntClassImpl.java,v 1.12 2003-05-30 18:47:51 ian_dickinson Exp $
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
     * this class. Each element of the iterator will be an {@link #OntClass}.</p>
     * @return An iterator over the super-classes of this class.
     * @exception OntProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.   
     */ 
    public Iterator listSuperClasses() {
        return listAs( getProfile().SUB_CLASS_OF(), "SUB_CLASS_OF", OntClass.class );
    }

    /**
     * <p>Answer true if the given class is a super-class of this class.</p>
     * @param cls A class to test.
     * @return True if the given class is a super-class of this class.
     * @exception OntProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.   
     */
    public boolean hasSuperClass( Resource cls ) {
        return hasPropertyValue( getProfile().SUB_CLASS_OF(), "SUB_CLASS_OF", cls );
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
     * this class. Each element of the iterator will be an {@link #OntClass}.</p>
     * @return An iterator over the sub-classes of this class.
     * @exception OntProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.   
     */ 
    public Iterator listSubClasses() {
        checkProfile( getProfile().SUB_CLASS_OF(), "SUB_CLASS_OF" );
        return WrappedIterator.create( getModel().listStatements( null, getProfile().SUB_CLASS_OF(), this ) )
               .mapWith( new SubjectAsMapper( OntClass.class ) );
    }

    /**
     * <p>Answer true if the given class is a sub-class of this class.</p>
     * @param cls A class to test.
     * @return True if the given class is a sub-class of this class.
     * @exception OntProfileException If the {@link Profile#SUB_CLASS_OF()} property is not supported in the current language profile.   
     */
    public boolean hasSubClass( Resource cls ) {
        return ((OntClass) cls.as( OntClass.class )).hasSuperClass( this );
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
     * this class. Each element of the iterator will be an {@link #OntClass}.</p>
     * @return An iterator over the classes equivalent to this class.
     * @exception OntProfileException If the {@link Profile#EQUIVALENT_CLASS()} property is not supported in the current language profile.   
     */ 
    public Iterator listEquivalentClasses() {
        return listAs( getProfile().EQUIVALENT_CLASS(), "EQUIVALENT_CLASS", OntClass.class );
    }

    /**
     * <p>Answer true if the given class is equivalent to this class.</p>
     * @param class A class to test for
     * @return True if the given property is equivalent to this class.
     * @exception OntProfileException If the {@link Profile#EQUIVALENT_CLASS()} property is not supported in the current language profile.   
     */
    public boolean hasEquivalentClass( Resource cls ) {
        return hasPropertyValue(  getProfile().EQUIVALENT_CLASS(), "EQUIVALENT_CLASS", cls );
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
     * Each element of the iterator will be an {@link #OntClass}.</p>
     * @return An iterator over the classes disjoint with this class.
     * @exception OntProfileException If the {@link Profile#DISJOINT_WITH()} property is not supported in the current language profile.   
     */ 
    public Iterator listDisjointWith() {
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
     * <p>
     * Answer an iterator over the class descriptions
     * that mention this class as one of its super-classes.
     * </p>
     * <p>
     * TODO: the closed parameter is ignored at the current time
     * </p>
     * 
     * @param closed If true, close the iteration over the sub-class relation: i&#046;e&#046;
     *               return the sub-classes of the sub-classes, etc.
     * @return an iterator over the resources representing this class's sub-classes
     */
    public Iterator getSubClasses( boolean closed ) {
        // ensure we have an extended iterator of statements  _x rdfs:subClassOf this
        Iterator i = getModel().listStatements( null, RDFS.subClassOf, this );
        ExtendedIterator ei = (i instanceof ExtendedIterator) ? (ExtendedIterator) i : WrappedIterator.create( i );
        
        // alias defined?
        if (getProfile().hasAliasFor( RDFS.subClassOf )) {
            ei = ei.andThen( WrappedIterator.create( getModel().listStatements( null, (Property) getProfile().getAliasFor( RDFS.subClassOf ), this ) ) );
        }
        
        // we only want the subjects of the statements
        return ei.mapWith( new Map1() { public Object map1( Object x ) { return ((Statement) x).getSubject().as( OntClass.class ); } } );
    }


    /**
     * <p>
     * Answer an iterator over the class descriptions
     * for which this class is a sub-class. 
     * </p>
     * <p>
     * TODO: the closed parameter is ignored at the current time
     * </p>
     * 
     * @param closed If true, close the iteration over the super-class relation: i&#046;e&#046;
     *               return the super-classes of the super-classes, etc.
     * @return an iterator over the resources representing this class's sub-classes.
     */
    public Iterator getSuperClasses( boolean closed ) {
        // ensure we have an extended iterator of statements  this rdfs:subClassOf _x
        Iterator i = getModel().listStatements( this, RDFS.subClassOf, (RDFNode) null );
        ExtendedIterator ei = (i instanceof ExtendedIterator) ? (ExtendedIterator) i : WrappedIterator.create( i );
        
        // alias defined?
        if (getProfile().hasAliasFor( RDFS.subClassOf )) {
            ei = ei.andThen( WrappedIterator.create( getModel().listStatements( this, (Property) getProfile().getAliasFor( RDFS.subClassOf ), (RDFNode) null ) ) );
        }
        
        // we only want the subjects of the statements
        return ei.mapWith( new Map1() { public Object map1( Object x ) { return ((Statement) x).getSubject().as( OntClass.class ); } } );
    }


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
    public EnumeratedClass convertToEnumeratedClass( OntList individuals ) {
        setPropertyValue( getProfile().ONE_OF(), "ONE_OF", individuals );
        return (EnumeratedClass) as( EnumeratedClass.class );
    }

    /** 
     * <p>Answer a view of this class as an intersection of the given classes.</p>
     * @param classes A list of the classes that will comprise the operands of the intersection
     * @return This ontology class, converted to an intersection of the given classes 
     */
    public IntersectionClass convertToIntersectionClass( OntList classes ) {
        setPropertyValue( getProfile().INTERSECTION_OF(), "INTERSECTION_OF", classes );
        return (IntersectionClass) as( IntersectionClass.class );
    }

    /** 
     * <p>Answer a view of this class as a union of the given classes.</p>
     * @param classes A list of the classes that will comprise the operands of the union
     * @return This ontology class, converted to an union of the given classes 
     */
    public UnionClass convertToUnionClass( OntList classes ) {
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
        if (!hasRDFType( getProfile().RESTRICTION(), "RESTRICTION")) {
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

