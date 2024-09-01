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

package org.apache.jena.ontapi.impl.factories;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontapi.OntJenaException;
import org.apache.jena.ontapi.OntModelControls;
import org.apache.jena.ontapi.common.BaseEnhNodeFactoryImpl;
import org.apache.jena.ontapi.common.EnhNodeFactory;
import org.apache.jena.ontapi.common.EnhNodeFilter;
import org.apache.jena.ontapi.common.EnhNodeFinder;
import org.apache.jena.ontapi.common.OntConfig;
import org.apache.jena.ontapi.common.WrappedEnhNodeFactory;
import org.apache.jena.ontapi.impl.objects.OntObjectPropertyImpl;
import org.apache.jena.ontapi.impl.objects.OntSimplePropertyImpl;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class OntProperties {
    public static final EnhNodeFinder NEGATIVE_PROPERTY_ASSERTION_FINDER = new EnhNodeFinder.ByType(OWL2.NegativePropertyAssertion);
    public static final EnhNodeFilter NEGATIVE_PROPERTY_ASSERTION_FILTER = EnhNodeFilter.ANON
            .and(new EnhNodeFilter.HasPredicate(OWL2.sourceIndividual))
            .and(new EnhNodeFilter.HasPredicate(OWL2.assertionProperty));
    private static final EnhNodeFactory NAMED_OBJECT_PROPERTY_FACTORY_REFERENCE = WrappedEnhNodeFactory.of(OntObjectProperty.Named.class);
    private static final EnhNodeFactory ANONYMOUS_OBJECT_PROPERTY_FACTORY_REFERENCE = WrappedEnhNodeFactory.of(OntObjectProperty.Inverse.class);

    public static Factory createFactory(OntConfig config, boolean withInverseObjectProperty) {
        List<Node> objectPropertyTypes = new ArrayList<>();
        List<Node> allPropertyTypes = new ArrayList<>();
        objectPropertyTypes.add(OWL2.ObjectProperty.asNode());
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_INVERSE_FUNCTIONAL_FEATURE)) {
            objectPropertyTypes.add(OWL2.InverseFunctionalProperty.asNode());
        }
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_REFLEXIVE_FEATURE)) {
            objectPropertyTypes.add(OWL2.ReflexiveProperty.asNode());
        }
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_IRREFLEXIVE_FEATURE)) {
            objectPropertyTypes.add(OWL2.IrreflexiveProperty.asNode());
        }
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_SYMMETRIC_FEATURE)) {
            objectPropertyTypes.add(OWL2.SymmetricProperty.asNode());
        }
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_ASYMMETRIC_FEATURE)) {
            objectPropertyTypes.add(OWL2.AsymmetricProperty.asNode());
        }
        if (config.getBoolean(OntModelControls.USE_OWL_PROPERTY_TRANSITIVE_FEATURE)) {
            objectPropertyTypes.add(OWL2.TransitiveProperty.asNode());
        }
        allPropertyTypes.add(RDF.Property.asNode());
        allPropertyTypes.add(OWL2.AnnotationProperty.asNode());
        allPropertyTypes.add(OWL2.DatatypeProperty.asNode());
        allPropertyTypes.add(OWL2.FunctionalProperty.asNode());
        allPropertyTypes.addAll(objectPropertyTypes);
        return new Factory(
                allPropertyTypes.stream().toList(),
                objectPropertyTypes.stream().toList(),
                withInverseObjectProperty
        );
    }

    public static class ObjectPropertyExpressionFactory extends BaseEnhNodeFactoryImpl {
        @Override
        public ExtendedIterator<EnhNode> iterator(EnhGraph eg) {
            return Iterators.concat(
                    NAMED_OBJECT_PROPERTY_FACTORY_REFERENCE.iterator(eg),
                    ANONYMOUS_OBJECT_PROPERTY_FACTORY_REFERENCE.iterator(eg)
            );
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg) {
            if (node.isURI()) {
                return NAMED_OBJECT_PROPERTY_FACTORY_REFERENCE.canWrap(node, eg);
            }
            return ANONYMOUS_OBJECT_PROPERTY_FACTORY_REFERENCE.canWrap(node, eg);
        }

        @Override
        public EnhNode createInstance(Node node, EnhGraph eg) {
            if (node.isURI()) {
                return NAMED_OBJECT_PROPERTY_FACTORY_REFERENCE.createInstance(node, eg);
            }
            return ANONYMOUS_OBJECT_PROPERTY_FACTORY_REFERENCE.createInstance(node, eg);
        }

        @Override
        public EnhNode wrap(Node node, EnhGraph eg) {
            if (node.isURI())
                return NAMED_OBJECT_PROPERTY_FACTORY_REFERENCE.wrap(node, eg);
            if (node.isBlank())
                return ANONYMOUS_OBJECT_PROPERTY_FACTORY_REFERENCE.wrap(node, eg);
            throw new OntJenaException.Conversion("Can't convert node " + node + " to Object Property Expression.");
        }
    }

    public static class AnonymousObjectPropertyFactory extends BaseEnhNodeFactoryImpl {
        private static final Node OWL_INVERSE_OF = OWL2.inverseOf.asNode();

        @Override
        public ExtendedIterator<EnhNode> iterator(EnhGraph eg) {
            return listTriples(Node.ANY, eg)
                    .filterKeep(x -> x.getSubject().isBlank())
                    .mapWith(x -> createInstance(x.getSubject(), eg));
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg) {
            return node.isBlank() && Iterators.findFirst(listTriples(node, eg)).isPresent();
        }

        @Override
        public EnhNode createInstance(Node node, EnhGraph eg) {
            return new OntObjectPropertyImpl.InversePropertyImpl(node, eg);
        }

        private ExtendedIterator<Triple> listTriples(Node node, EnhGraph eg) {
            // "_:x owl:inverseOf PN":
            return eg.asGraph().find(node, OWL_INVERSE_OF, Node.ANY)
                    .filterKeep(x -> NAMED_OBJECT_PROPERTY_FACTORY_REFERENCE.canWrap(x.getObject(), eg));
        }
    }

    /**
     * Generic factory for any OntProperty including {@code rdf:Property}.
     * It does not care about punnings.
     */
    public static class Factory extends BaseEnhNodeFactoryImpl {

        private final List<Node> propertyTypes;
        private final List<Node> objectPropertyTypes;
        private final boolean allowInverseObjectProperty;

        private Factory(List<Node> propertyTypes, List<Node> objectPropertyTypes, boolean allowInverseObjectProperty) {
            this.propertyTypes = Objects.requireNonNull(propertyTypes);
            this.objectPropertyTypes = Objects.requireNonNull(objectPropertyTypes);
            this.allowInverseObjectProperty = allowInverseObjectProperty;
        }

        @Override
        public ExtendedIterator<EnhNode> iterator(EnhGraph eg) {
            ExtendedIterator<Node> named = Iterators.distinct(
                    Iterators.flatMap(
                            WrappedIterator.create(propertyTypes.iterator()),
                            type -> eg.asGraph().find(Node.ANY, RDF.type.asNode(), type)
                    ).mapWith(Triple::getSubject).filterKeep(Node::isURI)
            );
            if (!allowInverseObjectProperty) {
                return named.mapWith(it -> createInstance(it, eg));
            }
            ExtendedIterator<Node> anonymous = Iterators.distinct(
                    eg.asGraph().find(Node.ANY, OWL2.inverseOf.asNode(), Node.ANY).mapWith(triple -> {
                        if (!triple.getSubject().isBlank()) {
                            return null;
                        }
                        if (!isNamedObjectProperty(triple.getObject(), eg)) {
                            return null;
                        }
                        return triple.getSubject();
                    }).filterKeep(Objects::nonNull)
            );
            return Iterators.concat(named, anonymous).mapWith(it -> createInstance(it, eg));
        }

        @Override
        public EnhNode createInstance(Node node, EnhGraph eg) {
            return new OntSimplePropertyImpl(node, eg);
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg) {
            if (node.isURI()) {
                for (Node type : propertyTypes) {
                    if (eg.asGraph().contains(node, RDF.type.asNode(), type)) {
                        return true;
                    }
                }
            }
            if (allowInverseObjectProperty && node.isBlank()) {
                // "_:x owl:inverseOf PN":
                return isInverseObjectProperty(node, eg);
            }
            return false;
        }

        private boolean isInverseObjectProperty(Node node, EnhGraph eg) {
            return Iterators.findFirst(
                    eg.asGraph().find(node, OWL2.inverseOf.asNode(), Node.ANY)
                            .mapWith(Triple::getObject)
                            .filterKeep(it -> isNamedObjectProperty(it, eg))
            ).isPresent();
        }

        private boolean isNamedObjectProperty(Node node, EnhGraph eg) {
            if (!node.isURI()) {
                return false;
            }
            for (Node type : objectPropertyTypes) {
                if (eg.asGraph().contains(node, RDF.type.asNode(), type)) {
                    return true;
                }
            }
            return false;
        }
    }

}
