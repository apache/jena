/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            7 Sept 2001
 * Filename           $RCSfile: DAMLHierarchy.java,v $
 * Revision           $Revision: 1.6 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2005-02-21 12:05:20 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml.impl;


// Imports
///////////////

import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.rdf.model.*;

import java.util.*;



/**
 * Represents knowledge about the class and property hierarchies in the DAML spec,
 * so that these can be available without forcing the user to import the DAML specification
 * into their model each time. Uses Singleton pattern.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLHierarchy.java,v 1.6 2005-02-21 12:05:20 andy_seaborne Exp $
 */
public class DAMLHierarchy
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////

    /** The singleton instance */
    private static DAMLHierarchy s_instance = new DAMLHierarchy();


    // Instance variables
    //////////////////////////////////

    /**
     * Map of DAML and RDF standard classes that are super-classes of other DAML standard classes.
     * Assumes each class has at most one super-class.
     */
    public Resource[][] DAML_STANDARD_CLASS_HIERARCHY = new Resource[][] {
        // sub-class                           super-class
        {DAML_OIL.Class,                       RDFS.Class},
        {DAML_OIL.Datatype,                    RDFS.Class},
        {DAML_OIL.Restriction,                 DAML_OIL.Class},
        {DAML_OIL.ObjectProperty,              RDF.Property},
        {DAML_OIL.DatatypeProperty,            RDF.Property},
        {DAML_OIL.TransitiveProperty,          DAML_OIL.ObjectProperty},
        {DAML_OIL.UnambiguousProperty,         DAML_OIL.ObjectProperty},
        {DAML_OIL.UniqueProperty,              RDF.Property},
        {DAML_OIL.List,                        RDF.Seq},
    };


    /**
     * Known equivalences between DAML and RDFS values
     */
    public Resource[][] DAML_STANDARD_EQUIVALENCES = new Resource[][] {
        {DAML_OIL.subClassOf,                   RDFS.subClassOf},
        {DAML_OIL.Literal,                      RDFS.Literal},
        {DAML_OIL.Property,                     RDF.Property},
        {DAML_OIL.type,                         RDF.type},
        {DAML_OIL.value,                        RDF.value},
        {DAML_OIL.subPropertyOf,                RDFS.subPropertyOf},
        {DAML_OIL.domain,                       RDFS.domain},
        {DAML_OIL.range,                        RDFS.range},
        {DAML_OIL.label,                        RDFS.label},
        {DAML_OIL.comment,                      RDFS.comment},
        {DAML_OIL.seeAlso,                      RDFS.seeAlso},
        {DAML_OIL.isDefinedBy,                  RDFS.isDefinedBy},
    };


    /** A list of well-known properties that are known a priori to be transitive */
    public Property[] TRANSITIVE_PROPERTIES = new Property[] {
        DAML_OIL.subClassOf,
        DAML_OIL.subPropertyOf,
        DAML_OIL.sameClassAs,
        DAML_OIL.sameIndividualAs,
        DAML_OIL.samePropertyAs,
        DAML_OIL.equivalentTo,

        RDFS.subClassOf,
        RDFS.subPropertyOf
    };

    /** Super-class hierarchy hashmap */
    protected Map m_classHierarchyMap = new HashMap();

    /** Flag to show initialisation of static structures has taken place */
    protected boolean m_initialised = false;

    /** Equivalance map */
    protected Map m_equivalenceMap = new HashMap();

    /** Table of transitive properties */
    protected Map m_transitiveProperties = new HashMap();



    // Constructors
    //////////////////////////////////

    /**
     * Constructor is private to enforce singleton design pattern.
     */
    private DAMLHierarchy() {
    }


    // External signature methods
    //////////////////////////////////

    /**
     * Answer the singleton instance.
     *
     * @return the unique DAMLHierarchy
     */
    public static DAMLHierarchy getInstance() {
        return s_instance;
    }


    /**
     * Answer true if the first URI represents a class in the standard DAML ontology
     * that is a super-class of the class denoted by the second URI.  This is used to
     * provide background knowledge from the standard DAML ontology without having
     * to explicitly import it each time.
     *
     * @param uri0 A class URI
     * @param uri1 A class URI
     * @return true if the class denoted by uri0 is a sub-class of the class denoted by uri1
     */
    public boolean isDAMLSubClassOf( String uri0, String uri1 ) {
        // only works for named classes, so if the uri's are null or empty (indicating
        // anonymous resources) we skip
        if ((uri0 == null)  ||  (uri1 == null)  ||
            (uri0.length() == 0)  ||  (uri1.length() == 0)) {
            return false;
        }

        // ensure that the structure has been initialised
        initialiseMaps();

        // first, is uri0 directly a sub-class of uri1?
        String superClass = (String) m_classHierarchyMap.get( uri0 );

        if (uri1.equals( superClass )) {
            // found a match
            return true;
        }
        else {
            // NOTE: assumes there is at most one super-class for each class, which is
            // true in current releases of DAML but may not remain true forever.
            return superClass == null ? false : isDAMLSubClassOf( superClass, uri1 );
        }
    }


    /**
     * Answer true if the given property is well-known to be transitive. That is,
     * it is transitive even though it is not marked as being in the class of
     * transitive properties defined by the TransitiveProperty class.  Note: this
     * test does not check for <code>rdf:type TransitiveProperty</code>, it <i>only</i>
     * tests the table of well-known transitive properties.
     *
     * @param res A resource
     * @return True if the resource is a Property, and it is a member of the set
     *         of well-known transitive properties.
     */
    public boolean isTransitiveProperty( Resource res ) {
        return (res instanceof Property  &&  m_transitiveProperties.containsKey( res ));
    }


    /**
     * Answer an iteration of those values that are equivalent to the given resource.
     *
     * @param res A resource, that may have known equivalent values
     * @return an iteration over the resource's equivalent values, which may be empty
     *         but will not be null.
     */
    public Iterator getEquivalentValues( Resource res ) {
        // ensure that the cache structure has been initialised
        initialiseMaps();

        LinkedList equivs = (LinkedList) m_equivalenceMap.get( res );
        return (equivs == null) ? new LinkedList().iterator() : equivs.iterator();
    }



    // Internal implementation methods
    //////////////////////////////////

    /**
     * Initialise the static structures we need to process class hierarchy queries.
     * Note that we don't do this as a static to avoid race conditions with setting
     * up the vocabularies.
     */
    private void initialiseMaps() {
        if (!m_initialised) {
            // initialise the class hierachy map for known standard DAML classes
            for (int i = 0;  i < DAML_STANDARD_CLASS_HIERARCHY.length;  i++) {
                String subClass   = DAML_STANDARD_CLASS_HIERARCHY[i][0].getURI();
                String superClass = DAML_STANDARD_CLASS_HIERARCHY[i][1].getURI();

                m_classHierarchyMap.put( subClass, superClass );
            }

            // initialise the equivalence map - note symmetric relation, so we have to do both directions
            for (int i = 0;  i < DAML_STANDARD_EQUIVALENCES.length;  i++) {
                // first do the forwards direction
                LinkedList l = (LinkedList) m_equivalenceMap.get( DAML_STANDARD_EQUIVALENCES[i][0] );

                if (l == null) {
                    // if no list of equivs yet, create one
                    l = new LinkedList();
                    m_equivalenceMap.put( DAML_STANDARD_EQUIVALENCES[i][0], l );
                }

                l.add( DAML_STANDARD_EQUIVALENCES[i][1] );

                // now do the reverse direction
                l = (LinkedList) m_equivalenceMap.get( DAML_STANDARD_EQUIVALENCES[i][1] );

                if (l == null) {
                    // if no list of equivs yet, create one
                    l = new LinkedList();
                    m_equivalenceMap.put( DAML_STANDARD_EQUIVALENCES[i][1], l );
                }

                l.add( DAML_STANDARD_EQUIVALENCES[i][0] );
            }

            // initialise the transitive properties map, for efficiency
            for (int i = 0;  i < TRANSITIVE_PROPERTIES.length;  i++) {
                m_transitiveProperties.put( TRANSITIVE_PROPERTIES[i], Boolean.TRUE );
            }

            m_initialised = true;
        }
    }



    //==============================================================================
    // Inner class definitions
    //==============================================================================


}


/*
    (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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

