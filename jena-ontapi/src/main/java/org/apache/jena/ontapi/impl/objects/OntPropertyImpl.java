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

package org.apache.jena.ontapi.impl.objects;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.ontapi.OntJenaException;
import org.apache.jena.ontapi.OntModelControls;
import org.apache.jena.ontapi.impl.HierarchySupport;
import org.apache.jena.ontapi.impl.OntGraphModelImpl;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntProperty;
import org.apache.jena.ontapi.model.OntRelationalProperty;
import org.apache.jena.ontapi.model.OntStatement;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDFS;

import java.util.Set;
import java.util.stream.Stream;

/**
 * Property Expression base impl-class.
 * No functionality, just a collection of factories related to all OWL property-expressions.
 */
@SuppressWarnings("WeakerAccess")
public abstract class OntPropertyImpl extends OntObjectImpl implements OntProperty {

    public OntPropertyImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    public static Stream<OntClass> declaringClasses(OntProperty property, boolean direct) {
        Set<OntClass> domains = HierarchySupport.allTreeNodesSetInclusive(
                () -> property.domains()
                        .filter(it -> it.canAs(OntClass.class))
                        .map(it -> it.as(OntClass.class)).filter(it -> !isReservedOrBuiltin(it)),
                clazz -> OntClassImpl.explicitSubClasses(clazz).filter(it -> !isReservedOrBuiltin(it))
        );
        if (domains.isEmpty()) {
            if (!direct) {
                return property.getModel().ontObjects(OntClass.class).filter(it -> !isReservedOrBuiltin(it));
            } else {
                return property.getModel().hierarchyRoots();
            }
        }
        return domains.stream().filter(clazz -> clazz.hasDeclaredProperty(property, direct));
    }

    public static <X extends OntProperty> Stream<X> subProperties(X property, Class<X> type, boolean direct) {
        if (direct) {
            Property reasonerProperty = reasonerProperty(property.getModel(), RDFS.subPropertyOf);
            if (reasonerProperty != null) {
                return explicitSubProperties(property, reasonerProperty, type).filter(x -> !property.equals(x));
            }
        }
        return HierarchySupport.treeNodes(
                property,
                it -> explicitSubProperties(it, RDFS.subPropertyOf, type),
                direct,
                OntGraphModelImpl.configValue(property.getModel(), OntModelControls.USE_BUILTIN_HIERARCHY_SUPPORT)
        );
    }

    public static <X extends OntProperty> Stream<X> superProperties(X property, Class<X> type, boolean direct) {
        if (direct) {
            Property reasonerProperty = reasonerProperty(property.getModel(), RDFS.subPropertyOf);
            if (reasonerProperty != null) {
                return explicitSuperProperties(property, reasonerProperty, type).filter(x -> !property.equals(x));
            }
        }
        return HierarchySupport.treeNodes(
                property,
                it -> explicitSuperProperties(it, RDFS.subPropertyOf, type),
                direct,
                OntGraphModelImpl.configValue(property.getModel(), OntModelControls.USE_BUILTIN_HIERARCHY_SUPPORT)
        );
    }

    public static <X extends OntProperty> boolean hasSuperProperty(X property, X candidateSuper, Class<X> type, boolean direct) {
        if (property.equals(candidateSuper)) {
            // every property is a sub-property of itself
            return true;
        }
        if (direct) {
            Property reasonerProperty = reasonerProperty(property.getModel(), RDFS.subPropertyOf);
            if (reasonerProperty != null) {
                return property.getModel().contains(property, reasonerProperty, candidateSuper);
            }
        }
        return HierarchySupport.contains(
                property,
                candidateSuper,
                it -> explicitSuperProperties(it, RDFS.subPropertyOf, type),
                direct,
                OntGraphModelImpl.configValue(property.getModel(), OntModelControls.USE_BUILTIN_HIERARCHY_SUPPORT)
        );
    }

    public static <X extends OntRelationalProperty> Stream<X> disjointProperties(OntGraphModelImpl m, Class<X> type, X property) {
        if (!OntGraphModelImpl.configValue(m, OntModelControls.USE_OWL2_PROPERTY_DISJOINT_WITH_FEATURE)) {
            return Stream.empty();
        }
        return property.objects(OWL2.propertyDisjointWith, type);
    }

    public static <X extends OntRelationalProperty> OntStatement addDisjointWith(OntGraphModelImpl m, X property, X other) {
        OntGraphModelImpl.checkFeature(m, OntModelControls.USE_OWL2_PROPERTY_DISJOINT_WITH_FEATURE, "owl:propertyDisjointWith");
        return property.addStatement(OWL2.propertyDisjointWith, other);
    }

    public static <X extends OntRelationalProperty> void removeDisjointWith(OntGraphModelImpl m, X property, Resource other) {
        OntGraphModelImpl.checkFeature(m, OntModelControls.USE_OWL2_PROPERTY_DISJOINT_WITH_FEATURE, "owl:propertyDisjointWith");
        property.remove(OWL2.propertyDisjointWith, other);
    }

    public static <X extends OntRelationalProperty> Stream<X> equivalentProperties(OntGraphModelImpl m, Class<X> type, X property) {
        if (!OntGraphModelImpl.configValue(m, OntModelControls.USE_OWL_PROPERTY_EQUIVALENT_FEATURE)) {
            return Stream.empty();
        }
        return property.objects(OWL2.equivalentProperty, type);
    }

    public static <X extends OntRelationalProperty> OntStatement addEquivalentProperty(OntGraphModelImpl m, X property, X other) {
        OntGraphModelImpl.checkFeature(m, OntModelControls.USE_OWL_PROPERTY_EQUIVALENT_FEATURE, "owl:equivalentProperty");
        return property.addStatement(OWL2.equivalentProperty, other);
    }

    public static <X extends OntRelationalProperty> void removeEquivalentProperty(OntGraphModelImpl m, X property, Resource other) {
        OntGraphModelImpl.checkFeature(m, OntModelControls.USE_OWL_PROPERTY_EQUIVALENT_FEATURE, "owl:equivalentProperty");
        property.remove(OWL2.equivalentProperty, other);
    }

    static <X extends OntProperty> Stream<X> explicitSubProperties(X property, Property predicate, Class<X> type) {
        return subjects(predicate, property, type);
    }

    static <X extends OntProperty> Stream<X> explicitSuperProperties(X property, Property predicate, Class<X> type) {
        return property.objects(predicate, type);
    }

    @Override
    public Property asProperty() {
        if (!isURIResource()) {
            throw new OntJenaException.IllegalState();
        }
        return as(Property.class);
    }

}
