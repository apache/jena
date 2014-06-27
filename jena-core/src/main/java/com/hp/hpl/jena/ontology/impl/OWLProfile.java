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
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.Polyadic;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.InfGraph;

import java.util.*;



/**
 * <p>
 * Ontology language profile implementation for the Full variant of the OWL 2002/07 language.
 * </p>
 */
public class OWLProfile
    extends AbstractProfile
{
    // Constants
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////


    // Constructors
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

    @Override
    public String   NAMESPACE() {                   return OWL.getURI(); }

    @Override
    public Resource CLASS() {                       return OWL.Class; }
    @Override
    public Resource RESTRICTION() {                 return OWL.Restriction; }
    @Override
    public Resource THING() {                       return OWL.Thing; }
    @Override
    public Resource NOTHING() {                     return OWL.Nothing; }
    @Override
    public Resource PROPERTY() {                    return RDF.Property; }
    @Override
    public Resource OBJECT_PROPERTY() {             return OWL.ObjectProperty; }
    @Override
    public Resource DATATYPE_PROPERTY() {           return OWL.DatatypeProperty; }
    @Override
    public Resource TRANSITIVE_PROPERTY() {         return OWL.TransitiveProperty; }
    @Override
    public Resource SYMMETRIC_PROPERTY() {          return OWL.SymmetricProperty; }
    @Override
    public Resource FUNCTIONAL_PROPERTY() {         return OWL.FunctionalProperty; }
    @Override
    public Resource INVERSE_FUNCTIONAL_PROPERTY() { return OWL.InverseFunctionalProperty; }
    @Override
    public Resource ALL_DIFFERENT() {               return OWL.AllDifferent; }
    @Override
    public Resource ONTOLOGY() {                    return OWL.Ontology; }
    @Override
    public Resource DEPRECATED_CLASS() {            return OWL.DeprecatedClass; }
    @Override
    public Resource DEPRECATED_PROPERTY() {         return OWL.DeprecatedProperty; }
    @Override
    public Resource ANNOTATION_PROPERTY() {         return OWL.AnnotationProperty; }
    @Override
    public Resource ONTOLOGY_PROPERTY() {           return OWL.OntologyProperty; }
    @Override
    public Resource LIST() {                        return RDF.List; }
    @Override
    public Resource NIL() {                         return RDF.nil; }
    @Override
    public Resource DATARANGE() {                   return OWL.DataRange; }


    @Override
    public Property EQUIVALENT_PROPERTY() {         return OWL.equivalentProperty; }
    @Override
    public Property EQUIVALENT_CLASS() {            return OWL.equivalentClass; }
    @Override
    public Property DISJOINT_WITH() {               return OWL.disjointWith; }
    @Override
    public Property SAME_INDIVIDUAL_AS() {          return null; }
    @Override
    public Property SAME_AS() {                     return OWL.sameAs; }
    @Override
    public Property DIFFERENT_FROM() {              return OWL.differentFrom; }
    @Override
    public Property DISTINCT_MEMBERS() {            return OWL.distinctMembers; }
    @Override
    public Property UNION_OF() {                    return OWL.unionOf; }
    @Override
    public Property INTERSECTION_OF() {             return OWL.intersectionOf; }
    @Override
    public Property COMPLEMENT_OF() {               return OWL.complementOf; }
    @Override
    public Property ONE_OF() {                      return OWL.oneOf; }
    @Override
    public Property ON_PROPERTY() {                 return OWL.onProperty; }
    @Override
    public Property ALL_VALUES_FROM() {             return OWL.allValuesFrom; }
    @Override
    public Property HAS_VALUE() {                   return OWL.hasValue; }
    @Override
    public Property SOME_VALUES_FROM() {            return OWL.someValuesFrom; }
    @Override
    public Property MIN_CARDINALITY() {             return OWL.minCardinality; }
    @Override
    public Property MAX_CARDINALITY() {             return OWL.maxCardinality; }
    @Override
    public Property CARDINALITY() {                 return OWL.cardinality; }
    @Override
    public Property INVERSE_OF() {                  return OWL.inverseOf; }
    @Override
    public Property IMPORTS() {                     return OWL.imports; }
    @Override
    public Property PRIOR_VERSION() {               return OWL.priorVersion; }
    @Override
    public Property BACKWARD_COMPATIBLE_WITH() {    return OWL.backwardCompatibleWith; }
    @Override
    public Property INCOMPATIBLE_WITH() {           return OWL.incompatibleWith; }
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
    public Property MIN_CARDINALITY_Q() {           return null; }      // qualified restrictions are not in the first version of OWL
    @Override
    public Property MAX_CARDINALITY_Q() {           return null; }
    @Override
    public Property CARDINALITY_Q() {               return null; }
    @Override
    public Property HAS_CLASS_Q() {                 return null; }

    // Annotations
    @Override
    public Property VERSION_INFO() {                return OWL.versionInfo; }
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
        };
    }

    /** The only first-class axiom type in OWL is AllDifferent */
    @Override
    public Iterator<Resource> getAxiomTypes() {
        return Arrays.asList(
            new Resource[] {
                OWL.AllDifferent
            }
        ).iterator();
    }

    /** The annotation properties of OWL */
    @Override
    public Iterator<Resource> getAnnotationProperties() {
        return Arrays.asList(
            new Resource[] {
                OWL.versionInfo,
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
                OWL.Class,
                OWL.Restriction
            }
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
                SupportsCheck check = getCheckTable().get( type );

                // a check must be defined for the test to succeed
                return (check != null)  && check.doCheck( n, g );
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
        return "OWL Full";
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

        /**
         * Return a set of all of the nodes that are the objects of <code>rdf:type</code>
         * triples whose subject is <code>n</code>
         * @param n A subject node
         * @param g A graph
         * @return All <code>rdf:type</code> nodes for <code>n</code> in <code>g</code>
         */
        public Set<Node> allTypes( Node n, Graph g) {
            Set<Node> types = new HashSet<>();
            for (ExtendedIterator<Triple> i = g.find( n, RDF.type.asNode(), Node.ANY ); i.hasNext(); ) {
                types.add( i.next().getObject() );
            }
            return types;
        }

        /**
         * Return true if there is any intersection between the nodes in <code>nodes</code>
         * and the nodes of the resources in <code>ref</code>.
         * @param nodes
         * @param ref
         * @return boolean
         */
        public boolean intersect( Set<Node> nodes, Resource[] ref ) {
            for (Resource r: ref) {
                if (nodes.contains( r.asNode() )) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Return true if the node <code>n</code> in graph <code>g</code> has one of the
         * types in <code>ref</code>
         */
        public boolean hasType( Node n, EnhGraph eg, Resource[] ref ) {
            // depending on the type of the underlying graph, it may or may not be advantageous
            // to get all types at once, or ask many separate queries. heuristically, we assume
            // that fine-grain queries to an inference graph is preferable, and all-at-once for
            // other types, including persistent stores

            Graph g = eg.asGraph();

            if (isInferenceGraph( g )) {
                for (Resource r: ref) {
                    if (g.contains( n, RDF.type.asNode(), r.asNode() )) {
                        return true;
                    }
                }
                return false;
            }
            else {
                return intersect( allTypes( n, g ), ref );
            }
        }

        /**
         * Return true if a given graph is an inference graph
         * @param g A graph
         * @return True if the graph is an inference graph, or is a union with an inference
         * base graph
         */
        public boolean isInferenceGraph( Graph g ) {
            return (g instanceof InfGraph) ||
                   (g instanceof Polyadic && ((Polyadic) g).getBaseGraph() instanceof InfGraph);
        }
    }


    // Table of check data
    //////////////////////

    private static Object[][] s_supportsCheckData = new Object[][] {
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
                                                return g.asGraph().contains( n, RDF.type.asNode(), OWL.InverseFunctionalProperty.asNode() );
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
        {  HasValueRestriction.class,   new SupportsCheck() {
                                            @Override
                                            public boolean doCheck( Node n, EnhGraph g ) {
                                                return g.asGraph().contains( n, RDF.type.asNode(), OWL.Restriction.asNode() ) &&
                                                       containsSome( g,n, OWL.hasValue ) &&
                                                       containsSome( g,n, OWL.onProperty );
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
                                                return n.isURI() || n.isBlank() ;
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
        }};

    // to allow concise reference in the code above.
    public static boolean containsSome( EnhGraph g, Node n, Property p ) {
        return AbstractProfile.containsSome( g, n, p );
    }


    // Static variables
    //////////////////////////////////

    /** Map from resource to syntactic/semantic checks that a node can be seen as the given facet */
    private static HashMap<Class<?>, SupportsCheck> s_supportsChecks = new HashMap<>();

    static {
        // initialise the map of supports checks from a table of static data
        for ( Object[] aS_supportsCheckData : s_supportsCheckData )
        {
            s_supportsChecks.put( (Class<?>) aS_supportsCheckData[0], (SupportsCheck) aS_supportsCheckData[1] );
        }
    }

    protected Map<Class<?>, SupportsCheck> getCheckTable() {
        return s_supportsChecks;
    }
}
