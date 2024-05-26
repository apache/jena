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

package org.apache.jena.ontapi.common;

import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Node;
import org.apache.jena.ontapi.OntModelControls;
import org.apache.jena.ontapi.model.OntAnnotationProperty;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntDataRange;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A standard personality mode to manage punnings.
 *
 * @see <a href="https://www.w3.org/TR/owl2-new-features/#F12:_Punning">2.4.1 F12: Punning</a>
 */
public enum PunningsMode {
    /**
     * For OWL1 DL.
     * OWL1 DL required a strict separation between the names of, e.g., classes and individuals.
     * The difference between this mode
     * and {@code DL1} is that declarations {@code rdfs:Class} &amp;
     * {@code rdfs:Datatype} considered as OntClass declarations;
     * also, {@code owl:Restriction}
     * is considered as prohibited declaration for entities that aren't classes (properties, datarange, named individuals).
     * See {@link OntModelControls#USE_LEGACY_COMPATIBLE_NAMED_CLASS_FACTORY}
     */
    DL1_COMPATIBLE,
    /**
     * For OWL1 DL.
     * OWL1 DL required a strict separation between the names of, e.g., classes and individuals.
     *
     */
    DL1,
    /**
     * For OWL2 DL.
     * Personality with four kinds of restriction on a {@code rdf:type} intersection (i.e. "illegal punnings"):
     * <ul>
     * <li>Named owl:Class &lt;-&gt; Named rdfs:Datatype</li>
     * <li>Named owl:ObjectProperty &lt;-&gt; owl:DatatypeProperty</li>
     * <li>Named owl:ObjectProperty &lt;-&gt; owl:AnnotationProperty</li>
     * <li>owl:AnnotationProperty &lt;-&gt; owl:DatatypeProperty</li>
     * </ul>
     * each of the pairs above can't exist in the form of OWL-Entity in the same model at the same time.
     * From specification: "OWL 2 DL imposes certain restrictions:
     * it requires that a name cannot be used for both a class and a datatype and
     * that a name can only be used for one kind of property."
     */
    DL2,
    /**
     * Forbidden intersections of rdf-declarations:
     * <ul>
     * <li>Class &lt;-&gt; Datatype</li>
     * <li>ObjectProperty &lt;-&gt; DataProperty</li>
     * </ul>
     */
    DL_WEAK,
    /**
     * Allow any entity type intersections.
     */
    FULL,
    ;

    private static final Set<Resource> OWL2_ALL_OBJECT_PROPERTIES = Set.of(
            OWL2.ObjectProperty,
            OWL2.InverseFunctionalProperty,
            OWL2.ReflexiveProperty,
            OWL2.IrreflexiveProperty,
            OWL2.SymmetricProperty,
            OWL2.AsymmetricProperty,
            OWL2.TransitiveProperty
    );
    private static final Set<Resource> OWL1_ALL_OBJECT_PROPERTIES = Set.of(
            OWL2.ObjectProperty,
            OWL2.InverseFunctionalProperty,
            OWL2.SymmetricProperty,
            OWL2.TransitiveProperty
    );
    private static final Set<Resource> OWL2_OBJECT_PROPERTIES = Set.of(
            OWL2.ObjectProperty
    );
    private static final Set<Resource> DATATYPE_PROPERTIES = Set.of(OWL2.DatatypeProperty);
    private static final Set<Resource> ANNOTATION_PROPERTIES = Set.of(OWL2.AnnotationProperty);
    private static final Set<Resource> RDF_PROPERTIES = Set.of(RDF.Property);
    private static final Set<Resource> OWL2_CLASSES = Set.of(OWL2.Class);
    private static final Set<Resource> OWL1_CLASSES = Set.of(OWL2.Class);
    private static final Set<Resource> OWL1_ALL_CLASSES = Set.of(OWL2.Class, OWL2.Restriction, RDFS.Class, RDFS.Datatype);
    private static final Set<Resource> OWL2_DATATYPES = Set.of(RDFS.Datatype);
    private static final Set<Resource> OWL1_DATATYPES = Set.of(OWL2.DataRange);
    private static final Set<Resource> OWL2_INDIVIDUALS = Set.of(OWL2.NamedIndividual);

    static Map<Class<? extends OntObject>, Set<Node>> toMap(PunningsMode mode) {
        Map<Class<? extends OntObject>, Set<Set<Resource>>> res;
        if (PunningsMode.DL1_COMPATIBLE == mode) {
            res = OWL1_DL(true);
        } else if (PunningsMode.DL1 == mode) {
            res = OWL1_DL(false);
        } else if (PunningsMode.DL2 == mode) {
            res = OWL2_DL();
        } else if (PunningsMode.DL_WEAK == mode) {
            res = OWL2_DL_WEAK();
        } else if (PunningsMode.FULL == mode) {
            res = FULL();
        } else {
            throw new IllegalStateException();
        }
        return res.entrySet().stream().collect(
                Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        it -> it.getValue().stream()
                                .flatMap(Collection::stream)
                                .map(FrontsNode::asNode)
                                .collect(Collectors.toUnmodifiableSet())
                )
        );
    }

    private static Map<Class<? extends OntObject>, Set<Set<Resource>>> OWL1_DL(boolean compatible) {
        Set<Resource> classes = compatible ? OWL1_ALL_CLASSES : OWL1_CLASSES;
        return Map.of(
                OntAnnotationProperty.class, Set.of(
                        OWL1_ALL_OBJECT_PROPERTIES, DATATYPE_PROPERTIES, classes
                ),
                OntObjectProperty.Named.class, Set.of(
                        DATATYPE_PROPERTIES, ANNOTATION_PROPERTIES, classes
                ),
                OntDataProperty.class, Set.of(
                        OWL1_ALL_OBJECT_PROPERTIES, ANNOTATION_PROPERTIES, classes
                ),
                OntDataRange.Named.class, Set.of(
                        OWL1_DATATYPES // no named classes but still require
                ),
                OntClass.Named.class, Set.of(
                        ANNOTATION_PROPERTIES, OWL1_ALL_OBJECT_PROPERTIES, DATATYPE_PROPERTIES
                ),
                OntIndividual.Named.class, Set.of(
                        ANNOTATION_PROPERTIES, OWL1_ALL_OBJECT_PROPERTIES, DATATYPE_PROPERTIES, classes, RDF_PROPERTIES
                )
        );
    }

    private static Map<Class<? extends OntObject>, Set<Set<Resource>>> OWL2_DL() {
        return Map.of(
                OntAnnotationProperty.class, Set.of(
                        OWL2_ALL_OBJECT_PROPERTIES, DATATYPE_PROPERTIES
                ),
                OntObjectProperty.Named.class, Set.of(
                        DATATYPE_PROPERTIES, ANNOTATION_PROPERTIES
                ),
                OntDataProperty.class, Set.of(
                        OWL2_ALL_OBJECT_PROPERTIES, ANNOTATION_PROPERTIES
                ),
                OntDataRange.Named.class, Set.of(
                        OWL2_CLASSES
                ),
                OntClass.Named.class, Set.of(
                        OWL2_DATATYPES
                ),
                OntIndividual.Named.class, Set.of()
        );
    }

    private static Map<Class<? extends OntObject>, Set<Set<Resource>>> OWL2_DL_WEAK() {
        return Map.of(
                OntAnnotationProperty.class, Set.of(),
                OntObjectProperty.Named.class, Set.of(DATATYPE_PROPERTIES),
                OntDataProperty.class, Set.of(OWL2_OBJECT_PROPERTIES),
                OntDataRange.Named.class, Set.of(OWL2_CLASSES),
                OntClass.Named.class, Set.of(OWL2_DATATYPES),
                OntIndividual.Named.class, Set.of()
        );
    }

    private static Map<Class<? extends OntObject>, Set<Set<Resource>>> FULL() {
        return Map.of(
                OntAnnotationProperty.class, Set.of(),
                OntObjectProperty.Named.class, Set.of(),
                OntDataProperty.class, Set.of(),
                OntDataRange.Named.class, Set.of(),
                OntClass.Named.class, Set.of(),
                OntIndividual.Named.class, Set.of()
        );
    }
}
