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
package org.apache.jena.ontology.impl;


// Imports
///////////////

import java.util.*;

import org.apache.jena.enhanced.* ;
import org.apache.jena.graph.* ;
import org.apache.jena.ontology.* ;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.vocabulary.* ;



/**
 * <p>
 * Ontology language profile implementation for the DL variant of the OWL 2002/07 language.
 * </p>
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


    /**
     * <p>
     * Answer a descriptive string for this profile, for use in debugging and other output.
     * </p>
     * @return "OWL DL"
     */
    @Override
    public String getLabel() {
        return "OWL DL";
    }


    // Internal implementation methods
    //////////////////////////////////

    protected static Object[][] s_supportsCheckData = new Object[][] {
            // Resource (key),              check method
            {  AllDifferent.class,          new SupportsCheck() {
                @Override
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.AllDifferent.asNode() );
                }
            }
            },
            {  AnnotationProperty.class,    new SupportsCheck() {
                @Override
                public boolean doCheck( Node n, EnhGraph g ) {
                    for (Iterator<Resource> i = ((OntModel) g).getProfile().getAnnotationProperties();  i.hasNext(); ) {
                        if (i.next().asNode().equals( n )) {
                            // a built-in annotation property
                            return true;
                        }
                    }
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.AnnotationProperty.asNode() );
                }
            }
            },
            {  OntClass.class,              new SupportsCheck() {
                @Override
                public boolean doCheck( Node n, EnhGraph eg ) {
                    Graph g = eg.asGraph();
                    return hasType( n, eg, new Resource[] {OWL.Class, OWL.Restriction, RDFS.Class, RDFS.Datatype} ) ||
                           // These are common cases that we should support
                           n.equals( OWL.Thing.asNode() ) ||
                           n.equals( OWL.Nothing.asNode() ) ||
                           g.contains( Node.ANY, RDFS.domain.asNode(), n ) ||
                           g.contains( Node.ANY, RDFS.range.asNode(), n ) ||
                           g.contains( n, OWL.intersectionOf.asNode(), Node.ANY ) ||
                           g.contains( n, OWL.unionOf.asNode(), Node.ANY ) ||
                           g.contains( n, OWL.complementOf.asNode(), Node.ANY )
                           ;
                }
            }
            },
            {  DatatypeProperty.class,      new SupportsCheck() {
                @Override
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.DatatypeProperty.asNode() );
                }
            }
            },
            {  ObjectProperty.class,        new SupportsCheck() {
                @Override
                public boolean doCheck( Node n, EnhGraph g ) {
                    return hasType( n, g, new Resource[] {OWL.ObjectProperty,OWL.TransitiveProperty,
                                                          OWL.SymmetricProperty, OWL.InverseFunctionalProperty} );
                }
            }
            },
            {  FunctionalProperty.class,    new SupportsCheck() {
                @Override
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.FunctionalProperty.asNode() );
                }
            }
            },
            {  InverseFunctionalProperty.class, new SupportsCheck() {
                @Override
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.InverseFunctionalProperty.asNode() ) &&
                    !g.asGraph().contains( n, RDF.type.asNode(), OWL.DatatypeProperty.asNode() );
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
                    return hasType( n, g, new Resource[] {RDF.Property, OWL.ObjectProperty, OWL.DatatypeProperty,
                                                          OWL.AnnotationProperty, OWL.TransitiveProperty,
                                                          OWL.SymmetricProperty, OWL.InverseFunctionalProperty,
                                                          OWL.FunctionalProperty} );
                }
            }
            },
            {  Ontology.class,              new SupportsCheck() {
                @Override
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.Ontology.asNode() );
                }
            }
            },
            {  Restriction.class,           new SupportsCheck() {
                @Override
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.Restriction.asNode() );
                }
            }
            },
            {  AllValuesFromRestriction.class,   new SupportsCheck() {
                @Override
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.Restriction.asNode() ) &&
                    containsSome( g, n, OWL.allValuesFrom ) &&
                    containsSome( g, n, OWL.onProperty );
                }
            }
            },
            {  SomeValuesFromRestriction.class,   new SupportsCheck() {
                @Override
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.Restriction.asNode() ) &&
                    containsSome( g,n, OWL.someValuesFrom ) &&
                    containsSome( g,n, OWL.onProperty );
                }
            }
            },
            {  HasValueRestriction.class,   new SupportsCheck() {
                @Override
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.Restriction.asNode() ) &&
                    containsSome( g, n, OWL.hasValue ) &&
                    containsSome( g, n, OWL.onProperty );
                }
            }
            },
            {  CardinalityRestriction.class,   new SupportsCheck() {
                @Override
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.Restriction.asNode() ) &&
                    containsSome( g, n, OWL.cardinality ) &&
                    containsSome( g, n, OWL.onProperty );
                }
            }
            },
            {  MinCardinalityRestriction.class,   new SupportsCheck() {
                @Override
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.Restriction.asNode() ) &&
                    containsSome( g, n, OWL.minCardinality ) &&
                    containsSome( g, n, OWL.onProperty );
                }
            }
            },
            {  MaxCardinalityRestriction.class,   new SupportsCheck() {
                @Override
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.Restriction.asNode() ) &&
                    containsSome( g, n, OWL.maxCardinality ) &&
                    containsSome( g, n, OWL.onProperty );
                }
            }
            },
            {  SymmetricProperty.class,     new SupportsCheck() {
                @Override
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.SymmetricProperty.asNode() ) &&
                    !g.asGraph().contains( n, RDF.type.asNode(), OWL.DatatypeProperty.asNode() );
                }
            }
            },
            {  TransitiveProperty.class,    new SupportsCheck() {
                @Override
                public boolean doCheck( Node n, EnhGraph g ) {
                    return g.asGraph().contains( n, RDF.type.asNode(), OWL.TransitiveProperty.asNode() ) &&
                    !g.asGraph().contains( n, RDF.type.asNode(), OWL.DatatypeProperty.asNode() );
                }
            }
            },
            {  Individual.class,    new SupportsCheck() {
                @Override
                public boolean doCheck( Node n, EnhGraph g ) {
                    if ( n.isURI() || n.isBlank() ) { 
                        return !hasType( n, g, new Resource[] {RDFS.Class, RDF.Property, OWL.Class,
                                                               OWL.ObjectProperty, OWL.DatatypeProperty, OWL.TransitiveProperty,
                                                               OWL.FunctionalProperty, OWL.InverseFunctionalProperty} );
                    }
                    else {
                        return false;
                    }
                }
            }
            },
            {  DataRange.class,    new SupportsCheck() {
                @Override
                public boolean doCheck( Node n, EnhGraph g ) {
                    return n.isBlank()  &&
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
    private static HashMap<Class<?>,SupportsCheck> s_supportsChecks = new HashMap<>();

    static {
        // initialise the map of supports checks from a table of static data
        for ( Object[] aS_supportsCheckData : s_supportsCheckData )
        {
            s_supportsChecks.put( (Class<?>) aS_supportsCheckData[0], (SupportsCheck) aS_supportsCheckData[1] );
        }
    }

    @Override
    protected Map<Class<?>,SupportsCheck> getCheckTable() {
        return s_supportsChecks;
    }

    //==============================================================================
    // Inner class definitions
    //==============================================================================


}
