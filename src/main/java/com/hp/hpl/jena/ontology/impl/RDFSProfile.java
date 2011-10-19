/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian_dickinson@users.sourceforge.net
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            22-Jun-2003
 * Filename           $RCSfile: RDFSProfile.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2009-10-06 13:04:42 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;



// Imports
///////////////
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import java.util.*;


/**
 * <p>
 * Ontology language profile for working with RDFS ontologies.  RDFS is a (small)
 * sub-set of OWL, mostly.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:ian_dickinson@users.sourceforge.net" >email</a>)
 * @version CVS $Id: RDFSProfile.java,v 1.2 2009-10-06 13:04:42 ian_dickinson Exp $
 */
public class RDFSProfile
    extends AbstractProfile
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    @Override
    public String   NAMESPACE() {                   return RDFS.getURI(); }

    @Override
    public Resource CLASS() {                       return RDFS.Class; }
    @Override
    public Resource RESTRICTION() {                 return null; }
    @Override
    public Resource THING() {                       return null; }
    @Override
    public Resource NOTHING() {                     return null; }
    @Override
    public Resource PROPERTY() {                    return RDF.Property; }
    @Override
    public Resource OBJECT_PROPERTY() {             return null; }
    @Override
    public Resource DATATYPE_PROPERTY() {           return null; }
    @Override
    public Resource TRANSITIVE_PROPERTY() {         return null; }
    @Override
    public Resource SYMMETRIC_PROPERTY() {          return null; }
    @Override
    public Resource FUNCTIONAL_PROPERTY() {         return null; }
    @Override
    public Resource INVERSE_FUNCTIONAL_PROPERTY() { return null; }
    @Override
    public Resource ALL_DIFFERENT() {               return null; }
    @Override
    public Resource ONTOLOGY() {                    return null; }
    @Override
    public Resource DEPRECATED_CLASS() {            return null; }
    @Override
    public Resource DEPRECATED_PROPERTY() {         return null; }
    @Override
    public Resource ANNOTATION_PROPERTY() {         return null; }
    @Override
    public Resource ONTOLOGY_PROPERTY() {           return null; }
    @Override
    public Resource LIST() {                        return RDF.List; }
    @Override
    public Resource NIL() {                         return RDF.nil; }
    @Override
    public Resource DATARANGE() {                   return null; }


    @Override
    public Property EQUIVALENT_PROPERTY() {         return null; }
    @Override
    public Property EQUIVALENT_CLASS() {            return null; }
    @Override
    public Property DISJOINT_WITH() {               return null; }
    @Override
    public Property SAME_INDIVIDUAL_AS() {          return null; }
    @Override
    public Property SAME_AS() {                     return null; }
    @Override
    public Property DIFFERENT_FROM() {              return null; }
    @Override
    public Property DISTINCT_MEMBERS() {            return null; }
    @Override
    public Property UNION_OF() {                    return null; }
    @Override
    public Property INTERSECTION_OF() {             return null; }
    @Override
    public Property COMPLEMENT_OF() {               return null; }
    @Override
    public Property ONE_OF() {                      return null; }
    @Override
    public Property ON_PROPERTY() {                 return null; }
    @Override
    public Property ALL_VALUES_FROM() {             return null; }
    @Override
    public Property HAS_VALUE() {                   return null; }
    @Override
    public Property SOME_VALUES_FROM() {            return null; }
    @Override
    public Property MIN_CARDINALITY() {             return null; }
    @Override
    public Property MAX_CARDINALITY() {             return null; }
    @Override
    public Property CARDINALITY() {                 return null; }
    @Override
    public Property INVERSE_OF() {                  return null; }
    @Override
    public Property IMPORTS() {                     return null; }
    @Override
    public Property PRIOR_VERSION() {               return null; }
    @Override
    public Property BACKWARD_COMPATIBLE_WITH() {    return null; }
    @Override
    public Property INCOMPATIBLE_WITH() {           return null; }
    @Override
    public Property SUB_PROPERTY_OF() {             return RDFS.subPropertyOf; }
    @Override
    public Property SUB_CLASS_OF() {                return RDFS.subClassOf; }
    @Override
    public Property DOMAIN() {                      return RDFS.domain; }
    @Override
    public Property RANGE() {                       return RDFS.range; }
    @Override
    public Property FIRST() {                       return RDF.first; }
    @Override
    public Property REST() {                        return RDF.rest; }
    @Override
    public Property MIN_CARDINALITY_Q() {           return null; }
    @Override
    public Property MAX_CARDINALITY_Q() {           return null; }
    @Override
    public Property CARDINALITY_Q() {               return null; }
    @Override
    public Property HAS_CLASS_Q() {                 return null; }

    // Annotations
    @Override
    public Property VERSION_INFO() {                return null; }
    @Override
    public Property LABEL() {                       return RDFS.label; }
    @Override
    public Property COMMENT() {                     return RDFS.comment; }
    @Override
    public Property SEE_ALSO() {                    return RDFS.seeAlso; }
    @Override
    public Property IS_DEFINED_BY() {               return RDFS.isDefinedBy; }


    @Override
    protected Resource[][] aliasTable() {
        return new Resource[][] {
            {}
        };
    }

    /** The only first-class axiom type in OWL is AllDifferent */
    @Override
    public Iterator<Resource> getAxiomTypes() {
        return Arrays.asList(
            new Resource[] {
            }
        ).iterator();
    }

    /** The annotation properties of OWL */
    @Override
    public Iterator<Resource> getAnnotationProperties() {
        return Arrays.asList(
            new Resource[] {
                RDFS.label,
                RDFS.seeAlso,
                RDFS.comment,
                RDFS.isDefinedBy
            }
        ).iterator();
    }

    @Override
    public Iterator<Resource> getClassDescriptionTypes() {
        return Arrays.asList(
            new Resource[] {
                RDFS.Class            }
        ).iterator();
    }


    /**
     * <p>
     * Answer true if the given graph supports a view of this node as the given
     * language element, according to the semantic constraints of the profile.
     * If strict checking on the ontology model is turned off, this check is
     * skipped.
     * </p>
     *
     * @param n A node to test
     * @param g The enhanced graph containing <code>n</code>, which is assumed to
     * be an {@link OntModel}.
     * @param type A class indicating the facet that we are testing against.
     * @return True if strict checking is off, or if <code>n</code> can be
     * viewed according to the facet resource <code>res</code>
     */
    @Override
    public <T> boolean isSupported( Node n, EnhGraph g, Class<T> type ) {
        if (g instanceof OntModel) {
            OntModel m = (OntModel) g;

            if (!m.strictMode()) {
                // checking turned off
                return true;
            }
            else {
                // lookup the profile check for this resource
                SupportsCheck check = (SupportsCheck) s_supportsChecks.get( type );

                // a check must be defined for the test to succeed
                return (check == null)  || check.doCheck( n, g );
            }
        }
        else {
            return false;
        }
    }

    /**
     * <p>
     * Answer a descriptive string for this profile, for use in debugging and other output.
     * </p>
     * @return "OWL Full"
     */
    @Override
    public String getLabel() {
        return "RDFS";
    }

    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================

    /** Helper class for doing syntactic/semantic checks on a node */
    protected static class SupportsCheck
    {
        public boolean doCheck( Node n, EnhGraph g ) {
            return true;
        }
    }


    // Table of check data
    //////////////////////

    private static Object[][] s_supportsCheckTable = new Object[][] {
        // Resource (key),              check method
        {  OntClass.class,              new SupportsCheck() {
                                            @Override
                                            public boolean doCheck( Node n, EnhGraph g ) {
                                                return g.asGraph().contains( n, RDF.type.asNode(), RDFS.Class.asNode() ) ||
                                                       g.asGraph().contains( n, RDF.type.asNode(), RDFS.Datatype.asNode() ) ||
                                                       // These are common cases that we should support
                                                       n.equals( RDFS.Resource.asNode() ) ||
                                                       g.asGraph().contains( Node.ANY, RDFS.domain.asNode(), n ) ||
                                                       g.asGraph().contains( Node.ANY, RDFS.range.asNode(), n )
                                                       ;
                                            }
                                        }
        },
        {  RDFList.class,               new SupportsCheck() {
                                            @Override
                                            public boolean doCheck( Node n, EnhGraph g ) {
                                                return n.equals( RDF.nil.asNode() )  ||
                                                       g.asGraph().contains( n, RDF.type.asNode(), RDF.List.asNode() );
                                            }
                                        }
        },
        {  OntProperty.class,           new SupportsCheck() {
                                            @Override
                                            public boolean doCheck( Node n, EnhGraph g ) {
                                                return g.asGraph().contains( n, RDF.type.asNode(), RDF.Property.asNode() );
                                            }
                                        }
        },
    };


    // Static variables
    //////////////////////////////////

    /** Map from resource to syntactic/semantic checks that a node can be seen as the given facet */
    protected static HashMap<Object, Object> s_supportsChecks = new HashMap<Object, Object>();

    static {
        // initialise the map of supports checks from a table of static data
        for (int i = 0;  i < s_supportsCheckTable.length;  i++) {
            s_supportsChecks.put( s_supportsCheckTable[i][0], s_supportsCheckTable[i][1] );
        }
    }

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
    (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
