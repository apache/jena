/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            10 Feb 2003
 * Filename           $RCSfile: OWLDLProfile.java,v $
 * Revision           $Revision: 1.10 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-08-12 22:30:49 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002, 2003, Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;
import com.hp.hpl.jena.rdf.model.*;


// Imports
///////////////
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.ontology.*;

import java.util.*;



/**
 * <p>
 * Ontology language profile implementation for the DL variant of the OWL 2002/07 language.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OWLDLProfile.java,v 1.10 2004-08-12 22:30:49 ian_dickinson Exp $
 */
public class OWLDLProfile
    extends OWLProfile
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
	public Property SAME_AS() {                     return null; }


    /**
     * <p>
     * Answer a descriptive string for this profile, for use in debugging and other output.
     * </p>
     * @return "OWL DL"
     */
    public String getLabel() {
        return "OWL DL";
    }
    

    // Internal implementation methods
    //////////////////////////////////

    protected static Object[][] s_supportsCheckData = new Object[][] {
            // Resource (key),              check method
            {  AllDifferent.class,          new SupportsCheck() {
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.AllDifferent.asNode() );
                }
            }
            },
            {  AnnotationProperty.class,    new SupportsCheck() {
                public boolean doCheck( Node n, EnhGraph g ) {
                    for (Iterator i = ((OntModel) g).getProfile().getAnnotationProperties();  i.hasNext(); ) {
                        if (((Resource) i.next()).asNode().equals( n )) {
                            // a built-in annotation property
                            return true;
                        }
                    }
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.AnnotationProperty.asNode() );
                }
            }
            },
            {  OntClass.class,              new SupportsCheck() {
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.Class.asNode() ) ||
                    g.asGraph().contains( n, RDF.type.asNode(), OWL.Restriction.asNode() ) || 
                    g.asGraph().contains( n, RDF.type.asNode(), RDFS.Class.asNode() );
                }
            }
            },
            {  DatatypeProperty.class,      new SupportsCheck() {
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.DatatypeProperty.asNode() );
                }
            }
            },
            {  ObjectProperty.class,        new SupportsCheck() {
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.ObjectProperty.asNode() ) ||
                    g.asGraph().contains( n, RDF.type.asNode(), OWL.TransitiveProperty.asNode() ) || 
                    g.asGraph().contains( n, RDF.type.asNode(), OWL.SymmetricProperty.asNode() ) || 
                    g.asGraph().contains( n, RDF.type.asNode(), OWL.InverseFunctionalProperty.asNode() );
                }
            }
            },
            {  FunctionalProperty.class,    new SupportsCheck() {
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.FunctionalProperty.asNode() );
                }
            }
            },
            {  InverseFunctionalProperty.class, new SupportsCheck() {
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.InverseFunctionalProperty.asNode() ) &&
                    !g.asGraph().contains( n, RDF.type.asNode(), OWL.DatatypeProperty.asNode() );
                }
            }
            },
            {  RDFList.class,               new SupportsCheck() {
                public boolean doCheck( Node n, EnhGraph g ) {
                    return n.equals( RDF.nil.asNode() )  ||
                    g.asGraph().contains( n, RDF.type.asNode(), RDF.List.asNode() );
                }
            }
            },
            {  OntProperty.class,           new SupportsCheck() {
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), RDF.Property.asNode() ) ||
                    g.asGraph().contains( n, RDF.type.asNode(), OWL.ObjectProperty.asNode() ) ||
                    g.asGraph().contains( n, RDF.type.asNode(), OWL.DatatypeProperty.asNode() ) ||
                    g.asGraph().contains( n, RDF.type.asNode(), OWL.AnnotationProperty.asNode() ) ||
                    g.asGraph().contains( n, RDF.type.asNode(), OWL.TransitiveProperty.asNode() ) || 
                    g.asGraph().contains( n, RDF.type.asNode(), OWL.SymmetricProperty.asNode() ) || 
                    g.asGraph().contains( n, RDF.type.asNode(), OWL.FunctionalProperty.asNode() ) || 
                    g.asGraph().contains( n, RDF.type.asNode(), OWL.InverseFunctionalProperty.asNode() ); 
                }
            }
            },
            {  Ontology.class,              new SupportsCheck() {
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.Ontology.asNode() );
                }
            }
            },
            {  Restriction.class,           new SupportsCheck() {
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.Restriction.asNode() );
                }
            }
            },
            {  AllValuesFromRestriction.class,   new SupportsCheck() {
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.Restriction.asNode() ) &&
                    containsSome( g, n, OWL.allValuesFrom ) &&
                    containsSome( g, n, OWL.onProperty );
                }
            }
            },
            {  SomeValuesFromRestriction.class,   new SupportsCheck() {
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.Restriction.asNode() ) &&
                    containsSome( g,n, OWL.someValuesFrom ) &&
                    containsSome( g,n, OWL.onProperty );
                }
            }
            },
            {  HasValueRestriction.class,   new SupportsCheck() {
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.Restriction.asNode() ) &&
                    containsSome( g, n, OWL.hasValue ) &&
                    containsSome( g, n, OWL.onProperty );
                }
            }
            },
            {  CardinalityRestriction.class,   new SupportsCheck() {
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.Restriction.asNode() ) &&
                    containsSome( g, n, OWL.cardinality ) &&
                    containsSome( g, n, OWL.onProperty );
                }
            }
            },
            {  MinCardinalityRestriction.class,   new SupportsCheck() {
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.Restriction.asNode() ) &&
                    containsSome( g, n, OWL.minCardinality ) &&
                    containsSome( g, n, OWL.onProperty );
                }
            }
            },
            {  MaxCardinalityRestriction.class,   new SupportsCheck() {
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.Restriction.asNode() ) &&
                    containsSome( g, n, OWL.maxCardinality ) &&
                    containsSome( g, n, OWL.onProperty ); 
                }
            }
            },
            {  SymmetricProperty.class,     new SupportsCheck() {
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.SymmetricProperty.asNode() ) &&
                    !g.asGraph().contains( n, RDF.type.asNode(), OWL.DatatypeProperty.asNode() );
                }
            }
            },
            {  TransitiveProperty.class,    new SupportsCheck() {
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.TransitiveProperty.asNode() ) &&
                    !g.asGraph().contains( n, RDF.type.asNode(), OWL.DatatypeProperty.asNode() );
                }
            }
            },
            {  Individual.class,    new SupportsCheck() {
                public boolean doCheck( Node n, EnhGraph eg ) {
                    if (n instanceof Node_URI || n instanceof Node_Blank) {
                        // necessary to be a uri or bNode, but not sufficient
                        Graph g = eg.asGraph();
                        
                        // this check filters out OWL-full entailments from the OWL-rule reasoner
                        return !(g.contains( n, RDF.type.asNode(), RDFS.Class.asNode() ) ||
                                g.contains( n, RDF.type.asNode(), RDF.Property.asNode() ));                            
                    }
                    else {
                        return false;
                    }
                }
            }
            },
            {  DataRange.class,    new SupportsCheck() {
                public boolean doCheck( Node n, EnhGraph g ) {
                    return n instanceof Node_Blank  &&
                           g.asGraph().contains( n, RDF.type.asNode(), OWL.DataRange.asNode() );
                }
            }
            }
            };
    

    // to allow concise reference in the code above.
    public static boolean containsSome( EnhGraph g, Node n, Property p ) {
        return AbstractProfile.containsSome( g, n, p ); 
    }
    
    /** Map from resource to syntactic/semantic checks that a node can be seen as the given facet */
    private static HashMap s_supportsChecks = new HashMap();
    
    static {
        // initialise the map of supports checks from a table of static data
        for (int i = 0;  i < s_supportsCheckData.length;  i++) {
            s_supportsChecks.put( s_supportsCheckData[i][0], s_supportsCheckData[i][1] );
        }
    }

    protected Map getCheckTable() {
        return s_supportsChecks;
    }
    
    //==============================================================================
    // Inner class definitions
    //==============================================================================


}




/*
    (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
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

