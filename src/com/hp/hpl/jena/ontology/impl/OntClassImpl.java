/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            27-Mar-2003
 * Filename           $RCSfile: OntClassImpl.java,v $
 * Revision           $Revision: 1.7 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-04-28 15:44:08 $
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
import com.hp.hpl.jena.ontology.path.*;
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
 * @version CVS $Id: OntClassImpl.java,v 1.7 2003-04-28 15:44:08 ian_dickinson Exp $
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
                throw new OntologyException( "Cannot convert node " + n.toString() + " to OntClass");
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

    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>subClassOf</code>
     * property of a class description. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the imports of an ontology element
     */
    public PathSet p_subClassOf() {
        return asPathSet( getProfile().SUB_CLASS_OF() );
    }
    
    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>equivalentClass</code>
     * property of a class description. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the imports of an ontology element
     */
    public PathSet p_equivalentClass() {
        return asPathSet( getProfile().EQUIVALENT_CLASS() );
    }
    
    /**
     * <p>
     * Answer an {@link PathSet accessor} for the 
     * <code>disjointWith</code>
     * property of a class description. The accessor
     * can be used to perform a variety of operations, including getting and setting the value.
     * </p>
     * 
     * @return An abstract accessor for the imports of an ontology element
     */
    public PathSet p_disjointWith() {
        return asPathSet( getProfile().DISJOINT_WITH() );
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
     * for which this class is a sub-class. Will generate the
     * closure of the iteration over the super-class relationship.
     * <p>
     * 
     * @return an iterator over the resources representing this class's super-classes.
     */
    public Iterator getSuperClasses() {
        return getSuperClasses( true );
    }


    /**
     * <p>
     * Answer an iterator over the class descriptions
     * that mention this class as one of its super-classes.  Will iterate over the
     * closure of the sub-class relationship.
     * </p>
     * 
     * @return an iterator over the resources representing this class's sub-classes.
     */
    public Iterator getSubClasses() {
        return getSubClasses( true );
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
     * <p>
     * Answer true if the given class is a sub-class of this class.
     * </p>
     * 
     * @param cls A resource representing a class
     * @return True if this class is a super-class of the given class <code>cls</code>.
     */
    public boolean hasSubClass( Resource cls ) {
        boolean found = false;
        Iterator i = null;
        
        try {
            i = getSubClasses();
            while (!found && i.hasNext()) {
                found = cls.equals( i.next() );           
            }
        }
        finally {
            if (i instanceof ClosableIterator) {
                ((ClosableIterator) i).close();
            }
        }
        
        return found;
    }


    /**
     * <p>
     * Answer true if the given class is a super-class of this class.
     * </p>
     * 
     * @param cls A resource representing a class
     * @return True if this class is a sub-class of the given class <code>cls</code>.
     */
    public boolean hasSuperClass( Resource cls ) {
        boolean found = false;
        Iterator i = null;
        
        try {
            i = getSuperClasses();
            while (!found && i.hasNext()) {
                found = cls.equals( i.next() );           
            }
        }
        finally {
            if (i instanceof ClosableIterator) {
                ((ClosableIterator) i).close();
            }
        }
        
        return found;
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

