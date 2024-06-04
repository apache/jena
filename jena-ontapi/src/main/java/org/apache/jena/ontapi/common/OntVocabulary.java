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

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.ontapi.OntJenaException;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.apache.jena.vocabulary.SWRL;
import org.apache.jena.vocabulary.SWRLB;
import org.apache.jena.vocabulary.XSD;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An Ontology Vocabulary.
 * This is a generic interface that simply maps an {@code uri}-key to a {@code Set} of
 * {@link Resource RDF Resources} or {@link Property RDF Properties},
 * that are defined in vocabulary schemas and represent some family determined by that key.
 * A schema is a java class containing public static final constants.
 * Schemas are usually located inside the packages
 * {@code org.apache.jena.vocabulary} and {@code org.apache.jena.vocabulary}.
 * There are two kinds of property/resources described by this vocabulary: system and builtin.
 * A system resource/property is simply a URI defined in any scheme.
 * A builtin resource/property is a URI with a known type that does not require explicit declaration.
 * Note that all methods of this interface return unmodifiable {@code Set}s.
 *
 * @see Impls
 */
public interface OntVocabulary {

    OntVocabulary EMPTY = new Impls.BaseImpl(Map.of());
    OntVocabulary RDFS = new Impls.RDFSImpl();
    OntVocabulary OWL2_FULL = new Impls.OWL2Impl(Impls.OWL2Impl.Type.FULL);
    OntVocabulary OWL2_EL = new Impls.OWL2Impl(Impls.OWL2Impl.Type.EL);
    OntVocabulary OWL2_QL = new Impls.OWL2Impl(Impls.OWL2Impl.Type.QL);
    OntVocabulary OWL2_RL = new Impls.OWL2Impl(Impls.OWL2Impl.Type.RL);
    OntVocabulary OWL1_FULL = new Impls.OWL1Impl(Impls.OWL1Impl.Type.FULL);
    OntVocabulary OWL1_LITE = new Impls.OWL1Impl(Impls.OWL1Impl.Type.LITE);
    OntVocabulary DC = new Impls.DCImpl();
    OntVocabulary SKOS = new Impls.SKOSImpl();
    OntVocabulary SWRL = new Impls.SWRLImpl();

    /**
     * Answers a {@code Set} of system/builtin {@link Resource}s for the specified URI-{@code key}.
     * A URI-{@code key} - is a schema URI that determines a family of desired resources.
     * For example, to get all resources a key {@link RDFS#Resource rdfs:Resource} should be used,
     * because it is a supertype of everything.
     *
     * @param key String, not {@code null}
     * @return a {@code Set} of {@link Resource}s (possibly empty)
     */
    Set<? extends Resource> get(String key);

    /**
     * Answers a {@code Set} of system/builtin {@link Resource}s for the specified URI-key.
     *
     * @param uri a URI-{@link Resource}, not {@code null}
     * @param <X> either {@link Resource} or {@link Property}
     * @return a {@code Set} of {@code X}s, not {@code null} but possibly empty
     */
    @SuppressWarnings("unchecked")
    default <X extends Resource> Set<X> get(Resource uri) {
        return (Set<X>) get(Objects.requireNonNull(uri.getURI()));
    }

    /**
     * Returns a collection of all built-in properties
     * with implicit {@code rdf:type} equal to {@link OWL2#AnnotationProperty owl:AnnotationProperty}.
     *
     * @return {@code Set} of {@link Property Properties}
     */
    default Set<Property> getBuiltinAnnotationProperties() {
        return get(OWL2.AnnotationProperty);
    }

    /**
     * Returns a collection of all built-in properties
     * with implicit {@code rdf:type} equal to {@link OWL2#DatatypeProperty owl:DatatypeProperty}.
     *
     * @return {@code Set} of {@link Property Properties}
     */
    default Set<Property> getBuiltinDatatypeProperties() {
        return get(OWL2.DatatypeProperty);
    }

    /**
     * Returns a collection of all built-in properties
     * with implicit {@code rdf:type} equal to {@link OWL2#ObjectProperty owl:ObjectProperty}.
     *
     * @return {@code Set} of {@link Property Properties}
     */
    default Set<Property> getBuiltinObjectProperties() {
        return get(OWL2.ObjectProperty);
    }

    /**
     * Returns a collection of all built-in uri-resources
     * with implicit {@code rdf:type} equal to {@link RDFS#Datatype rdfs:Datatype}.
     *
     * @return {@code Set} of {@link Resource Resources}
     */
    default Set<Resource> getBuiltinDatatypes() {
        return get(org.apache.jena.vocabulary.RDFS.Datatype);
    }

    /**
     * Returns a collection of all built-in uri resources
     * with implicit {@code rdf:type} equal to {@link OWL2#Class owl:Class}.
     *
     * @return {@code Set} of {@link Resource Resources}
     */
    default Set<Resource> getBuiltinClasses() {
        return get(OWL2.Class);
    }

    /**
     * Returns a collection of all built-in uri resources
     * with implicit {@code rdf:type} equal to {@link SWRL#Builtin swrl:Builtin}.
     *
     * @return {@code Set} of {@link Resource Resources}
     */
    default Set<Resource> getBuiltinSWRLs() {
        return get(org.apache.jena.vocabulary.SWRL.Builtin);
    }

    /**
     * Returns all reserved resources:
     * OWL entities cannot have an uri belonging to the return collection.
     *
     * @return {@code Set} of {@link Resource Resources}
     */
    default Set<Resource> getSystemResources() {
        return get(org.apache.jena.vocabulary.RDFS.Resource);
    }

    /**
     * Returns all reserved properties:
     * OWL2 ontology cannot contain assertion with predicate belonging to the return collection.
     *
     * @return {@code Set} of {@link Property Properties}
     */
    default Set<Property> getSystemProperties() {
        return get(RDF.Property);
    }

    /**
     * Creates a new instance of {@code OntVocabulary}, combining this and the given vocabularies.
     *
     * @param other {@link OntVocabulary}, not {@code null}
     * @return {@link OntVocabulary}
     */
    default OntVocabulary and(OntVocabulary other) {
        return Impls.create(this, Objects.requireNonNull(other));
    }

    /**
     * A factory-helper to work with {@link OntVocabulary} instances, that wrap constant-holders
     * from the packages {@code org.apache.jena.vocabulary}
     * and {@code org.apache.jena.vocabulary} (such as {@link OWL2}).
     * <p>
     * In ONT-API, a {@link OntVocabulary} singleton is used
     * to build {@link OntPersonality}
     * and, also, in {@code com.github.owlcs.ontapi.transforms} subsystem.
     */
    final class Impls {

        /**
         * Creates a fresh union vocabulary that combines the given ones.
         *
         * @param vocabularies an {@code Array} of {@link OntVocabulary}s
         * @return {@link OntVocabulary}
         * @see #create(String, Collection)
         */
        public static OntVocabulary create(OntVocabulary... vocabularies) {
            return new BaseImpl(Arrays.stream(vocabularies)
                    .map(Impls::asMap)
                    .flatMap(x -> x.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> {
                        Set<Resource> res = new HashSet<>(a);
                        res.addAll(b);
                        return res;
                    })));
        }

        /**
         * Creates a {@link OntVocabulary} that contains the specified mapping ({@code key -> Set}).
         *
         * @param key    a URI-{@link Resource}, not {@code null}
         * @param values an {@code Array} with {@link Resource}s to map, not {@code null}
         * @return a {@link OntVocabulary} with single (specified) mapping
         */
        public static OntVocabulary create(Resource key, Resource... values) {
            return create(key, Arrays.stream(values).collect(Collectors.toUnmodifiableSet()));
        }

        /**
         * Creates a {@link OntVocabulary} that contains the specified mapping ({@code key -> Set}).
         *
         * @param key    a URI-{@link Resource}, not {@code null}
         * @param values a {@code Collection} of {@link Resource}s to map, not {@code null}
         * @return a {@link OntVocabulary} with single (specified) mapping
         */
        public static OntVocabulary create(Resource key, Collection<? extends Resource> values) {
            return create(Objects.requireNonNull(key).getURI(), values);
        }

        /**
         * Creates a {@link OntVocabulary} that contains the specified mapping ({@code key -> Set}).
         *
         * @param key    {@code String}, a URI of resource-family, not {@code null}
         * @param values a {@code Collection} of {@link Resource}s to map, not {@code null}
         * @return a {@link OntVocabulary} with single mapping
         * @see #create(OntVocabulary...)
         */
        public static OntVocabulary create(String key, Collection<? extends Resource> values) {
            Map<String, Set<? extends Resource>> map = new HashMap<>();
            map.put(Objects.requireNonNull(key), toUnmodifiableSet(Objects.requireNonNull(values)));
            return new BaseImpl(map);
        }

        /**
         * Creates a {@link OntVocabulary} with mapping for system resource/properties.
         *
         * @param schemas an {@code Array} of schemas
         *                - constant-holders with {@link Resource} and {@link Property} public static final fields,
         *                not {@code null}
         * @return a {@link OntVocabulary} with mapping for system resources and properties
         * (keys: {@link RDFS#Resource rdfs:Resource} and {@link RDF#Property rdf:Property})
         */
        public static OntVocabulary create(Class<?>... schemas) {
            return new BaseImpl(getConstants(Property.class, schemas), getConstants(Resource.class, schemas));
        }

        private static Stream<Field> directFields(Class<?> vocabulary, Class<?> type) {
            return Arrays.stream(vocabulary.getDeclaredFields())
                    .filter(x -> Modifier.isPublic(x.getModifiers()))
                    .filter(x -> Modifier.isStatic(x.getModifiers()))
                    .filter(x -> type.equals(x.getType()));
        }

        private static Stream<Field> fields(Class<?> vocabulary, Class<?> type) {
            Stream<Field> res = directFields(vocabulary, type);
            return vocabulary.getSuperclass() != null ? Stream.concat(res, fields(vocabulary.getSuperclass(), type)) : res;
        }

        private static <T> Stream<T> constants(Class<?> vocabulary, Class<T> type) {
            return fields(vocabulary, type).map(x -> getValue(x, type)).filter(Objects::nonNull);
        }

        private static <T> T getValue(Field field, Class<T> type) {
            try {
                return type.cast(field.get(null));
            } catch (IllegalAccessException e) {
                throw new OntJenaException.IllegalState("Unable to get an object of the type " + type.getSimpleName() +
                        " from the field " + field.getName(), e);
            }
        }

        private static <T> Set<T> getConstants(Class<? extends T> type, Class<?>... vocabularies) {
            return Arrays.stream(vocabularies)
                    .flatMap(x -> constants(x, type))
                    .collect(Collectors.toUnmodifiableSet());
        }

        private static Map<String, Set<? extends Resource>> asMap(OntVocabulary voc) {
            if (voc instanceof BaseImpl) {
                return ((BaseImpl) voc).map;
            }
            Map<String, Set<? extends Resource>> res = new HashMap<>();
            Stream.of(OWL2.AnnotationProperty, OWL2.DatatypeProperty, OWL2.ObjectProperty,
                            org.apache.jena.vocabulary.RDFS.Datatype,
                            OWL2.Class,
                            org.apache.jena.vocabulary.SWRL.Builtin,
                            RDF.Property,
                            org.apache.jena.vocabulary.RDFS.Resource
                    )
                    .forEach(x -> res.put(x.getURI(), voc.get(x)));
            return res;
        }

        private static <X> Set<X> toUnmodifiableSet(Collection<X> input) {
            if (input instanceof Set && input.getClass().getName().equals("java.util.Collections$UnmodifiableSet")) {
                return (Set<X>) input;
            }
            return input.stream().peek(Objects::requireNonNull).collect(Collectors.toUnmodifiableSet());
        }

        protected static class RDFSImpl extends BaseImpl {
            public static final Set<Property> ANNOTATION_PROPERTIES = Set.of(
                    org.apache.jena.vocabulary.RDFS.label,
                    org.apache.jena.vocabulary.RDFS.comment,
                    org.apache.jena.vocabulary.RDFS.seeAlso,
                    org.apache.jena.vocabulary.RDFS.isDefinedBy
            );
            private static final Class<?>[] VOCABULARIES = new Class<?>[]{RDF.class, RDFS.class};
            public static final Set<Property> PROPERTIES = getConstants(Property.class, VOCABULARIES);
            public static final Set<Resource> RESOURCES = getConstants(Resource.class, VOCABULARIES);

            protected RDFSImpl() {
                super(
                        /* annotation properties */ ANNOTATION_PROPERTIES,
                        /* datatype properties */ null,
                        /* object properties */ null,
                        /* named classes */ null,
                        /* datatypes */ null,
                        /* swrl */ null,
                        /* system properties */ PROPERTIES,
                        /* system resources */ RESOURCES
                );
            }
        }

        /**
         * Access to the {@link OWL2 OWL2} (including RDFS &amp; RDF &amp; XSD) vocabulary.
         */
        @SuppressWarnings("WeakerAccess")
        protected static class OWL2Impl extends BaseImpl {
            /**
             * The list of datatypes from owl-2 specification (35 types)
             * (see <a href="https://www.w3.org/TR/owl2-quick-reference/">Quick References, 3.1 Built-in Datatypes</a>).
             * It seems it is not full:
             */
            public static final Set<Resource> OWL2_DATATYPES = Set.of(
                    OWL2.real, OWL2.rational,
                    RDF.xmlLiteral, RDF.PlainLiteral, RDF.langString,
                    org.apache.jena.vocabulary.RDFS.Literal, XSD.xstring, XSD.normalizedString,
                    XSD.token, XSD.language, XSD.Name, XSD.NCName, XSD.NMTOKEN, XSD.decimal, XSD.integer,
                    XSD.xdouble, XSD.xfloat, XSD.xboolean,
                    XSD.nonNegativeInteger, XSD.nonPositiveInteger, XSD.positiveInteger, XSD.negativeInteger,
                    XSD.xlong, XSD.xint, XSD.xshort, XSD.xbyte,
                    XSD.unsignedLong, XSD.unsignedInt, XSD.unsignedShort, XSD.unsignedByte,
                    XSD.hexBinary, XSD.base64Binary,
                    XSD.anyURI, XSD.dateTime, XSD.dateTimeStamp
            );
            /**
             * @see <a href="https://www.w3.org/TR/owl2-profiles/#Entities">EL: Entities</a>
             * @see <a href="https://www.w3.org/TR/owl2-profiles/#Entities_2">QL: Entities</a>
             */
            public static final Set<Resource> OWL2_EL_QL_DATATYPES = Set.of(
                    OWL2.real, OWL2.rational,
                    RDF.xmlLiteral, RDF.PlainLiteral, RDF.langString,
                    org.apache.jena.vocabulary.RDFS.Literal, XSD.xstring, XSD.normalizedString,
                    XSD.token, XSD.Name, XSD.NCName, XSD.NMTOKEN, XSD.decimal, XSD.integer,
                    XSD.nonNegativeInteger,
                    XSD.hexBinary, XSD.base64Binary,
                    XSD.anyURI, XSD.dateTime, XSD.dateTimeStamp
            );
            /**
             * @see <a href="https://www.w3.org/TR/owl2-profiles/#Entities_3">RL: Entitites</a>
             */
            public static final Set<Resource> OWL2_RL_DATATYPES = Set.of(
                    RDF.xmlLiteral, RDF.PlainLiteral, RDF.langString,
                    org.apache.jena.vocabulary.RDFS.Literal, XSD.xstring, XSD.normalizedString,
                    XSD.token, XSD.language, XSD.Name, XSD.NCName, XSD.NMTOKEN, XSD.decimal, XSD.integer,
                    XSD.xdouble, XSD.xfloat, XSD.xboolean,
                    XSD.nonNegativeInteger, XSD.nonPositiveInteger, XSD.positiveInteger, XSD.negativeInteger,
                    XSD.xlong, XSD.xint, XSD.xshort, XSD.xbyte,
                    XSD.unsignedLong, XSD.unsignedInt, XSD.unsignedShort, XSD.unsignedByte,
                    XSD.hexBinary, XSD.base64Binary,
                    XSD.anyURI, XSD.dateTime, XSD.dateTimeStamp
            );
            public static final Set<Resource> ALL_KNOWN_DATATYPES = initOWL2BuiltInRDFDatatypes(TypeMapper.getInstance())
                    .stream()
                    .map(RDFDatatype::getURI)
                    .map(ResourceFactory::createResource)
                    .collect(Collectors.toUnmodifiableSet());
            public static final Set<Resource> OWL2_CLASSES = Set.of(OWL2.Nothing, OWL2.Thing);
            public static final Set<Property> OWL2_ANNOTATION_PROPERTIES = Set.of(
                    org.apache.jena.vocabulary.RDFS.label,
                    org.apache.jena.vocabulary.RDFS.comment,
                    org.apache.jena.vocabulary.RDFS.seeAlso,
                    org.apache.jena.vocabulary.RDFS.isDefinedBy,
                    OWL2.versionInfo,
                    OWL2.backwardCompatibleWith,
                    OWL2.priorVersion,
                    OWL2.incompatibleWith,
                    OWL2.deprecated);
            public static final Set<Property> OWL2_DATA_PROPERTIES = Set.of(OWL2.topDataProperty, OWL2.bottomDataProperty);
            public static final Set<Property> OWL2_OBJECT_PROPERTIES = Set.of(OWL2.topObjectProperty, OWL2.bottomObjectProperty);
            private static final Class<?>[] VOCABULARIES = new Class<?>[]{XSD.class, RDF.class, org.apache.jena.vocabulary.RDFS.class, OWL2.class};
            public static final Set<Property> PROPERTIES = getConstants(Property.class, VOCABULARIES);
            public static final Set<Resource> RESOURCES = getConstants(Resource.class, VOCABULARIES);

            protected OWL2Impl(Type type) {
                super(
                        OWL2_ANNOTATION_PROPERTIES,
                        type == Type.RL ? Set.of() : OWL2_DATA_PROPERTIES,
                        type == Type.RL ? Set.of() : OWL2_OBJECT_PROPERTIES,
                        OWL2_CLASSES,
                        datatypes(type),
                        /*SWRL*/ null,
                        PROPERTIES,
                        RESOURCES
                );
            }

            private static Set<Resource> datatypes(Type type) {
                if (type == Type.DL) {
                    return OWL2_DATATYPES;
                }
                if (type == Type.EL || type == Type.QL) {
                    return OWL2_EL_QL_DATATYPES;
                }
                if (type == Type.RL) {
                    return OWL2_RL_DATATYPES;
                }
                return ALL_KNOWN_DATATYPES;
            }

            private static Set<RDFDatatype> initOWL2BuiltInRDFDatatypes(TypeMapper types) {
                Stream.of(OWL2.real, OWL2.rational).forEach(d -> types.registerDatatype(new BaseDatatype(d.getURI())));
                OWL2_DATATYPES.forEach(iri -> types.getSafeTypeByName(iri.getURI()));
                Set<RDFDatatype> res = new HashSet<>();
                types.listTypes().forEachRemaining(res::add);
                return Collections.unmodifiableSet(res);
            }

            protected enum Type {
                FULL, DL, EL, QL, RL,
            }
        }

        /**
         * Access to the {@link OWL OWL1} (including RDFS &amp; RDF &amp; XSD) vocabulary.
         */
        @SuppressWarnings("WeakerAccess")
        protected static class OWL1Impl extends BaseImpl {
            private static final Set<Resource> OWL1_DATATYPES =
                    Set.of(RDF.xmlLiteral, RDF.PlainLiteral, RDF.langString,
                            org.apache.jena.vocabulary.RDFS.Literal, XSD.xstring, XSD.normalizedString,
                            XSD.token, XSD.language, XSD.Name, XSD.NCName, XSD.NMTOKEN, XSD.decimal, XSD.integer,
                            XSD.xdouble, XSD.xfloat, XSD.xboolean,
                            XSD.nonNegativeInteger, XSD.nonPositiveInteger, XSD.positiveInteger, XSD.negativeInteger,
                            XSD.xlong, XSD.xint, XSD.xshort, XSD.xbyte,
                            XSD.unsignedLong, XSD.unsignedInt, XSD.unsignedShort, XSD.unsignedByte,
                            XSD.hexBinary, XSD.base64Binary,
                            XSD.anyURI, XSD.dateTime, XSD.dateTimeStamp
                    );
            public static final Set<Resource> ALL_KNOWN_DATATYPES = initOWL1BuiltInRDFDatatypes(TypeMapper.getInstance())
                    .stream()
                    .map(RDFDatatype::getURI)
                    .map(ResourceFactory::createResource)
                    .collect(Collectors.toUnmodifiableSet());
            public static final Set<Resource> OWL1_FULL_CLASSES = Set.of(
                    OWL.Nothing, OWL.Thing
            );
            public static final Set<Resource> OWL1_LITE_CLASSES = Set.of(
                    OWL.Thing
            );
            public static final Set<Property> OWL1_ANNOTATION_PROPERTIES = Set.of(
                    org.apache.jena.vocabulary.RDFS.label,
                    org.apache.jena.vocabulary.RDFS.comment,
                    org.apache.jena.vocabulary.RDFS.seeAlso,
                    org.apache.jena.vocabulary.RDFS.isDefinedBy,
                    OWL.versionInfo,
                    OWL.backwardCompatibleWith,
                    OWL.priorVersion,
                    OWL.incompatibleWith
            );
            public static final Set<Property> OWL1_DATA_PROPERTIES = Set.of();
            public static final Set<Property> OWL1_OBJECT_PROPERTIES = Set.of();
            private static final Class<?>[] VOCABULARIES = new Class<?>[]{
                    XSD.class,
                    RDF.class,
                    org.apache.jena.vocabulary.RDFS.class,
                    OWL.class};
            public static final Set<Property> PROPERTIES = getConstants(Property.class, VOCABULARIES);
            public static final Set<Resource> RESOURCES = getConstants(Resource.class, VOCABULARIES);

            protected OWL1Impl(Type type) {
                super(
                        OWL1_ANNOTATION_PROPERTIES,
                        OWL1_DATA_PROPERTIES,
                        OWL1_OBJECT_PROPERTIES,
                        classes(type),
                        datatypes(type),
                        /*SWRL*/ null,
                        PROPERTIES,
                        RESOURCES
                );
            }

            private static Set<Resource> classes(Type type) {
                if (type == Type.LITE) {
                    return OWL1_LITE_CLASSES;
                }
                return OWL1_FULL_CLASSES;
            }

            private static Set<Resource> datatypes(Type type) {
                if (type == Type.FULL) {
                    return ALL_KNOWN_DATATYPES;
                }
                return OWL1_DATATYPES;
            }

            private static Set<RDFDatatype> initOWL1BuiltInRDFDatatypes(TypeMapper types) {
                OWL1_DATATYPES.forEach(iri -> types.getSafeTypeByName(iri.getURI()));
                Set<String> exclude = OWL2Impl.OWL2_DATATYPES.stream()
                        .filter(it -> !OWL1_DATATYPES.contains(it))
                        .map(Resource::getURI)
                        .collect(Collectors.toUnmodifiableSet());
                Set<RDFDatatype> res = new HashSet<>();
                types.listTypes().forEachRemaining(it -> {
                    if (!exclude.contains(it.getURI())) {
                        res.add(it);
                    }
                });
                return Collections.unmodifiableSet(res);
            }

            public enum Type {
                FULL, DL, LITE,
            }
        }

        /**
         * Access to {@link DC} vocabulary.
         */
        protected static class DCImpl extends BaseImpl {
            public static final Set<Property> ALL_PROPERTIES = getConstants(Property.class, org.apache.jena.vocabulary.DC.class);

            protected DCImpl() {
                super(
                        /* annotation properties */ ALL_PROPERTIES,
                        /* datatype properties */ null,
                        /* object properties */ null,
                        /* named classes */ null,
                        /* datatypes */ null,
                        /* swrl */ null,
                        /* system properties */ ALL_PROPERTIES,
                        /* system resources */ null
                );
            }
        }

        /**
         * Access to {@link SKOS} vocabulary.
         */
        @SuppressWarnings("WeakerAccess")
        protected static class SKOSImpl extends BaseImpl {
            public static final Set<Property> ANNOTATION_PROPERTIES =
                    Set.of(org.apache.jena.vocabulary.SKOS.altLabel,
                            org.apache.jena.vocabulary.SKOS.changeNote, org.apache.jena.vocabulary.SKOS.definition,
                            org.apache.jena.vocabulary.SKOS.editorialNote, org.apache.jena.vocabulary.SKOS.example, org.apache.jena.vocabulary.SKOS.hiddenLabel, org.apache.jena.vocabulary.SKOS.historyNote,
                            org.apache.jena.vocabulary.SKOS.note, org.apache.jena.vocabulary.SKOS.prefLabel, org.apache.jena.vocabulary.SKOS.scopeNote);
            public static final Set<Property> OBJECT_PROPERTIES =
                    Set.of(org.apache.jena.vocabulary.SKOS.broadMatch, org.apache.jena.vocabulary.SKOS.broader, org.apache.jena.vocabulary.SKOS.broaderTransitive,
                            org.apache.jena.vocabulary.SKOS.closeMatch, org.apache.jena.vocabulary.SKOS.exactMatch, org.apache.jena.vocabulary.SKOS.hasTopConcept, org.apache.jena.vocabulary.SKOS.inScheme,
                            org.apache.jena.vocabulary.SKOS.mappingRelation, org.apache.jena.vocabulary.SKOS.member, org.apache.jena.vocabulary.SKOS.memberList, org.apache.jena.vocabulary.SKOS.narrowMatch,
                            org.apache.jena.vocabulary.SKOS.narrower, org.apache.jena.vocabulary.SKOS.narrowerTransitive, org.apache.jena.vocabulary.SKOS.related,
                            org.apache.jena.vocabulary.SKOS.relatedMatch, org.apache.jena.vocabulary.SKOS.semanticRelation, org.apache.jena.vocabulary.SKOS.topConceptOf);
            /**
             * NOTE: In the OWLAPI-api {@code org.semanticweb.owlapi.vocab.SKOSVocabulary} there is also skos:TopConcept
             * But in fact there is no such resource in the <a href="https://www.w3.org/2009/08/skos-reference/skos.htm">specification</a>.
             */
            public static final Set<Resource> CLASSES =
                    Set.of(org.apache.jena.vocabulary.SKOS.Collection, org.apache.jena.vocabulary.SKOS.Concept, org.apache.jena.vocabulary.SKOS.ConceptScheme, org.apache.jena.vocabulary.SKOS.OrderedCollection);

            public static final Set<Property> PROPERTIES = getConstants(Property.class, org.apache.jena.vocabulary.SKOS.class);
            public static final Set<Resource> RESOURCES = getConstants(Resource.class, org.apache.jena.vocabulary.SKOS.class);


            protected SKOSImpl() {
                super(
                        /* annotation properties */ ANNOTATION_PROPERTIES,
                        /* datatype properties */ null,
                        /* object properties */ OBJECT_PROPERTIES,
                        /* named classes */ CLASSES,
                        /* datatypes */ null,
                        /* swrl */ null,
                        /* system properties */ PROPERTIES,
                        /* system resources */ RESOURCES
                );
            }
        }

        /**
         * For SWRL modeling.
         *
         * @see SWRL
         * @see SWRLB
         */
        protected static class SWRLImpl extends BaseImpl {
            public static final Set<Resource> BUILTINS = getConstants(Property.class, SWRLB.class);
            private static final Class<?>[] VOCABULARIES = new Class<?>[]{SWRL.class, SWRLB.class};
            public static final Set<Property> PROPERTIES = getConstants(Property.class, VOCABULARIES);
            public static final Set<Resource> RESOURCES = getConstants(Resource.class, VOCABULARIES);

            protected SWRLImpl() {
                super(
                        /* annotation properties */ null,
                        /* datatype properties */ null,
                        /* object properties */ null,
                        /* named classes */ null,
                        /* datatypes */ null,
                        /* swrl */ BUILTINS,
                        /* system properties */ PROPERTIES,
                        /* system resources */ RESOURCES
                );
            }
        }

        /**
         * The base implementation.
         */
        public static class BaseImpl implements OntVocabulary {
            private final Map<String, Set<? extends Resource>> map;

            private BaseImpl(Set<Property> properties,
                             Set<Resource> resources) {
                this(
                        /* annotation properties */ null,
                        /* datatype properties */ null,
                        /* object properties */ null,
                        /* named classes */ null,
                        /* datatypes */ null,
                        /* swrl */ null,
                        /* system properties */ properties,
                        /* system resources */ resources
                );
            }

            protected BaseImpl(Set<Property> annotationProperties,
                               Set<Property> dataProperties,
                               Set<Property> objectProperties,
                               Set<Resource> classes,
                               Set<Resource> datatypes,
                               Set<Resource> swrlBuiltins,
                               Set<Property> allProperties,
                               Set<Resource> allResources) {
                this.map = collectBuiltIns(
                        annotationProperties,
                        dataProperties,
                        objectProperties,
                        classes,
                        datatypes,
                        swrlBuiltins,
                        allProperties,
                        allResources);
            }

            private static Map<String, Set<? extends Resource>> collectBuiltIns(Set<Property> annotationProperties,
                                                                                Set<Property> dataProperties,
                                                                                Set<Property> objectProperties,
                                                                                Set<Resource> classes,
                                                                                Set<Resource> datatypes,
                                                                                Set<Resource> swrlBuiltins,
                                                                                Set<Property> allProperties,
                                                                                Set<Resource> allResources) {
                return Stream.of(
                        pair(OWL2.AnnotationProperty, annotationProperties),
                        pair(OWL2.DatatypeProperty, dataProperties),
                        pair(OWL2.ObjectProperty, objectProperties),
                        pair(OWL2.Class, classes),
                        pair(org.apache.jena.vocabulary.RDFS.Datatype, datatypes),
                        pair(org.apache.jena.vocabulary.SWRL.Builtin, swrlBuiltins),
                        pair(RDF.Property, allProperties),
                        pair(org.apache.jena.vocabulary.RDFS.Resource, allResources)
                ).filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(
                        it -> it.getKey().getURI(),
                        it -> Set.copyOf(it.getValue()))
                );
            }

            private static Map.Entry<Resource, Set<? extends Resource>> pair(Resource key, Set<? extends Resource> value) {
                return value == null ? null : Map.entry(key, value);
            }

            protected BaseImpl(Map<String, Set<? extends Resource>> map) {
                this.map = Map.copyOf(Objects.requireNonNull(map));
            }

            @Override
            public Set<? extends Resource> get(String key) {
                return map.getOrDefault(key, Collections.emptySet());
            }
        }
    }
}
