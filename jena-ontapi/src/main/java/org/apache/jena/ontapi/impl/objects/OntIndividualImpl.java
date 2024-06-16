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
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntNegativeAssertion;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.model.OntStatement;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An {@link OntIndividual} implementation, both for anonymous and named individuals.
 */
@SuppressWarnings("WeakerAccess")
public abstract class OntIndividualImpl extends OntObjectImpl implements OntIndividual {

    public OntIndividualImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    public static Anonymous createAnonymousIndividual(RDFNode node) {
        if (OntJenaException.notNull(node, "Null node.").canAs(Anonymous.class))
            return node.as(Anonymous.class);
        if (node.isAnon()) {
            return new AnonymousImpl(node.asNode(), (EnhGraph) node.getModel());
        }
        throw new OntJenaException.Conversion(node + " can't be presented as an anonymous individual");
    }

    /**
     * Returns a {@code Stream} of all class-types,
     * including their super-classes if the parameter {@code direct} is {@code false}.
     *
     * @param direct if {@code true} returns only direct types
     * @return a {@code Stream} of all {@link OntClass class}-types
     */
    @Override
    public Stream<OntClass> classes(boolean direct) {
        return classes(this, direct);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean hasOntClass(OntClass clazz, boolean direct) {
        if (direct) {
            Property reasonerProperty = reasonerProperty(getModel(), RDF.type);
            if (reasonerProperty != null) {
                return getModel().contains(this, reasonerProperty, clazz);
            }
        }
        AtomicBoolean isIndividual = new AtomicBoolean(true);
        return HierarchySupport.contains(
                this,
                (OntObject) clazz,
                it -> (Stream<OntObject>) ((Stream<?>) listClassesFor(it, isIndividual)),
                direct,
                OntGraphModelImpl.configValue(getModel(), OntModelControls.USE_BUILTIN_HIERARCHY_SUPPORT)
        );
    }

    @Override
    public Stream<OntIndividual> sameIndividuals() {
        if (!OntGraphModelImpl.configValue(getModel(), OntModelControls.USE_OWL_INDIVIDUAL_SAME_AS_FEATURE)) {
            return Stream.empty();
        }
        return objects(OWL2.sameAs, OntIndividual.class);
    }

    @Override
    public OntStatement addSameAsStatement(OntIndividual other) {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_INDIVIDUAL_SAME_AS_FEATURE, "owl:sameAs");
        return addStatement(OWL2.sameAs, other);
    }

    @Override
    public OntIndividual removeSameIndividual(Resource other) {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_INDIVIDUAL_SAME_AS_FEATURE, "owl:sameAs");
        remove(OWL2.sameAs, other);
        return this;
    }

    @Override
    public Stream<OntIndividual> differentIndividuals() {
        if (!OntGraphModelImpl.configValue(getModel(), OntModelControls.USE_OWL_INDIVIDUAL_DIFFERENT_FROM_FEATURE)) {
            return Stream.empty();
        }
        return objects(OWL2.differentFrom, OntIndividual.class);
    }

    @Override
    public OntStatement addDifferentFromStatement(OntIndividual other) {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_INDIVIDUAL_DIFFERENT_FROM_FEATURE, "owl:differentFrom");
        return addStatement(OWL2.differentFrom, other);
    }

    @Override
    public OntIndividual removeDifferentIndividual(Resource other) {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_INDIVIDUAL_DIFFERENT_FROM_FEATURE, "owl:differentFrom");
        remove(OWL2.differentFrom, other);
        return this;
    }

    /**
     * Lists all right parts from class assertion statements where this individual is at subject position.
     *
     * @return {@link ExtendedIterator} over all direct {@link OntClass class}-types
     */
    public ExtendedIterator<OntClass> listClasses() {
        return listObjects(RDF.type, OntClass.class);
    }

    @SuppressWarnings("unchecked")
    static Stream<OntClass> classes(OntObject individual, boolean direct) {
        if (direct) {
            Property reasonerProperty = reasonerProperty(individual.getModel(), RDF.type);
            if (reasonerProperty != null) {
                return individual
                        .objects(reasonerProperty, OntClass.class)
                        .filter(OntClass::canAsAssertionClass)
                        .map(OntClass::asAssertionClass);
            }
        }
        AtomicBoolean isIndividual = new AtomicBoolean(true);
        Stream<?> res = HierarchySupport.treeNodes(individual,
                it -> (Stream<OntObject>) ((Stream<?>) listClassesFor(it, isIndividual)),
                direct,
                OntGraphModelImpl.configValue(individual.getModel(), OntModelControls.USE_BUILTIN_HIERARCHY_SUPPORT)
        );

        return ((Stream<OntClass>) res)
                .filter(OntClass::canAsAssertionClass).map(OntClass::asAssertionClass);
    }

    static Stream<OntClass> listClassesFor(OntObject resource, AtomicBoolean isFirstLevel) {
        if (isFirstLevel.get()) {
            isFirstLevel.set(false);
            return resource
                    .objects(RDF.type, OntClass.class)
                    .filter(OntClass::canAsAssertionClass)
                    .map(OntClass::asAssertionClass);
        }
        return OntClassImpl.explicitSuperClasses(RDFS.subClassOf, resource);
    }

    @Override
    public boolean isLocal() {
        Optional<OntStatement> root = findRootStatement();
        return (root.isPresent() && root.get().isLocal()) || hasLocalClassAssertions();
    }

    protected boolean hasLocalClassAssertions() {
        return Iterators.findFirst(listClassAssertions().filterKeep(OntStatement::isLocal)).isPresent();
    }

    /**
     * Lists all class assertion statements.
     *
     * @return {@link ExtendedIterator} over all class assertions.
     */
    public ExtendedIterator<OntStatement> listClassAssertions() {
        return listStatements(RDF.type).filterKeep(s -> s.getObject().canAs(OntClass.class));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Stream<OntNegativeAssertion> negativeAssertions() {
        return Iterators.asStream(listNegativeAssertions(), getCharacteristics());
    }

    @SuppressWarnings("rawtypes")
    public ExtendedIterator<OntNegativeAssertion> listNegativeAssertions() {
        return listSubjects(OWL2.sourceIndividual, OntNegativeAssertion.class);
    }

    @Override
    protected Set<OntStatement> getContent() {
        Set<OntStatement> res = super.getContent();
        listNegativeAssertions().forEachRemaining(x -> res.addAll(((OntObjectImpl) x).getContent()));
        return res;
    }

    /**
     * Represents a named individual.
     * Note: it may not have {@link OntObject#getMainStatement()} statement.
     */
    public static class NamedImpl extends OntIndividualImpl implements Named {
        public NamedImpl(Node n, EnhGraph m) {
            super(checkNamed(n), m);
        }

        @Override
        public Optional<OntStatement> findRootStatement() {
            return Optional.of(getModel().createStatement(this, RDF.type, OWL2.NamedIndividual).asRootStatement())
                    .filter(r -> getModel().contains(r));
        }

        @Override
        public boolean isBuiltIn() {
            return false;
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return Named.class;
        }

        @Override
        public NamedImpl detachClass(Resource clazz) {
            OntGraphModelImpl m = getModel();
            m.listOntStatements(this, RDF.type, clazz)
                    .filterDrop(s -> OWL2.NamedIndividual.equals(s.getObject()))
                    .toList()
                    .forEach(s -> m.remove(s.clearAnnotations()));
            return this;
        }
    }

    /**
     * See description to the interface {@link Anonymous}.
     * The current implementation allows treating b-node as anonymous individual
     * in any case except the following cases:
     * <ul>
     * <li>it is a subject in statement "_:x rdf:type s", where "s" is not a class expression ("C").</li>
     * <li>it is a subject in statement "_:x @predicate @any", where @predicate is from reserved vocabulary
     * but not object, data or annotation built-in property
     * and not owl:sameAs and owl:differentFrom.</li>
     * <li>it is an object in statement "@any @predicate _:x", where @predicate is from reserved vocabulary
     * but not object, data or annotation built-in property
     * and not owl:sameAs, owl:differentFrom, owl:hasValue, owl:sourceIndividual and rdf:first.</li>
     * </ul>
     * <p>
     * for notations see <a href="https://www.w3.org/TR/owl2-quick-reference/">OWL2 Quick Refs</a>
     */
    @SuppressWarnings("javadoc")
    public static class AnonymousImpl extends OntIndividualImpl implements Anonymous {

        public AnonymousImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public boolean isLocal() {
            return hasLocalClassAssertions();
        }

        @Override
        public Optional<OntStatement> findRootStatement() {
            return Optional.empty();
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return Anonymous.class;
        }

        @Override
        public AnonymousImpl detachClass(Resource clazz) {
            Set<OntClass> classes = classes().collect(Collectors.toSet());
            if (clazz == null && !classes.isEmpty()) {
                throw new OntJenaException.IllegalState("Detaching classes is prohibited: " +
                        "the anonymous individual (" + this + ") should contain at least one class assertion, " +
                        "otherwise it can be lost");
            }
            if (classes.size() == 1 && classes.iterator().next().equals(clazz)) {
                throw new OntJenaException.IllegalState("Detaching class (" + clazz + ") is prohibited: " +
                        "it is a single class assertion for the individual " + this + ".");
            }
            remove(RDF.type, clazz);
            return this;
        }

    }
}
