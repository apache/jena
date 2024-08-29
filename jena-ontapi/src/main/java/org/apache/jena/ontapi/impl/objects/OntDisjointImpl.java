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
import org.apache.jena.ontapi.common.OntEnhNodeFactories;
import org.apache.jena.ontapi.impl.OntGraphModelImpl;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntDisjoint;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.model.OntRelationalProperty;
import org.apache.jena.ontapi.model.OntStatement;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Implementation for anonymous {@code owl:AllDisjointProperties}, {@code owl:AllDisjointClasses}, {@code owl:AllDifferent} sections.
 */
@SuppressWarnings("WeakerAccess")
public abstract class OntDisjointImpl<O extends OntObject> extends OntObjectImpl implements OntDisjoint<O> {

    public OntDisjointImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    public static Classes createDisjointClasses(OntGraphModelImpl model, Stream<OntClass> classes) {
        return create(model, OWL2.AllDisjointClasses, Classes.class, OntClass.class, classes, OWL2.members);
    }

    /**
     * Creates blank node {@code _:x rdf:type owl:AllDifferent. _:x owl:members (a1 ... an).}
     * <p>
     * Note: the predicate is {@link OWL2#members owl:members},
     * not {@link OWL2#distinctMembers owl:distinctMembers} (but the last one is correct also)
     * It is chosen as the preferred from considerations of uniformity.
     *
     * @param model       {@link OntGraphModelImpl}
     * @param individuals stream of {@link OntIndividual}
     * @return {@link Individuals}
     * @see <a href="https://www.w3.org/TR/owl2-quick-reference/#Additional_Vocabulary_in_OWL_2_RDF_Syntax">4.2 Additional Vocabulary in OWL 2 RDF Syntax</a>
     */
    @SuppressWarnings("javadoc")
    public static Individuals createDifferentIndividuals(OntGraphModelImpl model, Stream<OntIndividual> individuals) {
        Property membersPredicate = OntGraphModelImpl.configValue(model, OntModelControls.USE_OWL1_DISTINCT_MEMBERS_PREDICATE_FEATURE) ?
                OWL2.distinctMembers :
                OWL2.members;
        return create(model, OWL2.AllDifferent, Individuals.class, OntIndividual.class, individuals, membersPredicate);
    }

    public static ObjectProperties createDisjointObjectProperties(OntGraphModelImpl model, Stream<OntObjectProperty> properties) {
        return create(model, OWL2.AllDisjointProperties, ObjectProperties.class, OntObjectProperty.class, properties, OWL2.members);
    }

    public static DataProperties createDisjointDataProperties(OntGraphModelImpl model, Stream<OntDataProperty> properties) {
        return create(model, OWL2.AllDisjointProperties, DataProperties.class, OntDataProperty.class, properties, OWL2.members);
    }

    public static <R extends OntDisjoint<?>, E extends OntObject> R create(OntGraphModelImpl model,
                                                                           Resource type,
                                                                           Class<R> resultType,
                                                                           Class<E> memberType,
                                                                           Stream<E> members,
                                                                           Property membersPredicate) {
        OntJenaException.notNull(members, "Null " + OntEnhNodeFactories.viewAsString(memberType) + " members stream.");
        RDFList items = model.createList(members
                .peek(x -> OntJenaException.notNull(x,
                        "OntDisjoint: Null " + OntEnhNodeFactories.viewAsString(memberType) + " is specified"))
                .iterator());
        Resource res = model.createResource()
                .addProperty(RDF.type, type)
                .addProperty(membersPredicate, items);
        return model.getNodeAs(res.asNode(), resultType);
    }

    @Override
    public Optional<OntStatement> findRootStatement() {
        return getRequiredRootStatement(this, getResourceType());
    }

    protected Property getPredicate() {
        return OWL2.members;
    }

    protected abstract Class<O> getComponentType();

    protected abstract Resource getResourceType();

    @Override
    public Stream<O> members() {
        return Iterators.asStream(listMembers());
    }

    public ExtendedIterator<O> listMembers() {
        return getList().listMembers();
    }

    @Override
    public OntListImpl<O> getList() {
        return getModel().asOntList(getRequiredObject(getPredicate(), RDFList.class),
                this, getPredicate(), getComponentType());
    }

    @Override
    public ExtendedIterator<OntStatement> listSpec() {
        return Iterators.concat(super.listSpec(), getList().listContent());
    }

    public static class ClassesImpl extends OntDisjointImpl<OntClass> implements Classes {
        public ClassesImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return Classes.class;
        }

        @Override
        protected Class<OntClass> getComponentType() {
            return OntClass.class;
        }

        @Override
        protected Resource getResourceType() {
            return OWL2.AllDisjointClasses;
        }
    }

    /**
     * {@code DisjointClasses := 'DisjointClasses' '(' axiomAnnotations subClassExpression subClassExpression { subClassExpression } ')'}
     */
    public static class QLRLClassesImpl extends OntDisjointImpl<OntClass> implements Classes {
        public QLRLClassesImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return Classes.class;
        }

        @Override
        protected Class<OntClass> getComponentType() {
            return OntClass.class;
        }

        @Override
        protected Resource getResourceType() {
            return OWL2.AllDisjointClasses;
        }

        @Override
        public Stream<OntClass> members() {
            return getList().members().filter(OntClass::canAsDisjointClass).map(OntClass::asDisjointClass);
        }
    }

    public static class IndividualsImpl extends OntDisjointImpl<OntIndividual> implements Individuals {
        private final boolean useMembers;
        private final boolean useDistinctMembers;

        public IndividualsImpl(Node n, EnhGraph m, boolean useMembers, boolean useDistinctMembers) {
            super(n, m);
            this.useMembers = useMembers;
            this.useDistinctMembers = useDistinctMembers;
        }

        @Override
        public ExtendedIterator<OntIndividual> listMembers() {
            return Iterators.flatMap(lists(), OntListImpl::listMembers);
        }

        @Override
        public ExtendedIterator<OntStatement> listSpec() {
            return Iterators.concat(super.listSpec(), Iterators.flatMap(lists(), OntListImpl::listContent));
        }

        public ExtendedIterator<Property> listPredicates() {
            if (useDistinctMembers) {
                return Iterators.of(getAlternativePredicate());
            }
            if (useMembers) {
                return Iterators.of(getPredicate());
            } else {
                return Iterators.of(getPredicate(), getAlternativePredicate());
            }
        }

        public ExtendedIterator<OntListImpl<OntIndividual>> lists() {
            return listPredicates()
                    .mapWith(this::findList)
                    .filterKeep(Optional::isPresent)
                    .mapWith(Optional::get);
        }

        @Override
        public OntListImpl<OntIndividual> getList() {
            if (useDistinctMembers) {
                return findList(getAlternativePredicate())
                        .orElseThrow(() -> new OntJenaException.IllegalState("Can't find owl:distinctMembers"));
            }
            if (useMembers) {
                return findList(getPredicate())
                        .orElseThrow(() -> new OntJenaException.IllegalState("Can't find owl:members"));
            }
            Optional<OntListImpl<OntIndividual>> p = findList(getPredicate());
            Optional<OntListImpl<OntIndividual>> a = findList(getAlternativePredicate());
            if (p.isPresent() && a.isPresent()) {
                return p.get();
            }
            if (p.isPresent()) {
                return p.get();
            }
            if (a.isPresent()) {
                return a.get();
            }
            throw new OntJenaException.IllegalState("Can't find owl:members or owl:distinctMembers");
        }

        public Optional<OntListImpl<OntIndividual>> findList(Property predicate) {
            if (!hasProperty(predicate)) return Optional.empty();
            return Optional.of(getModel().asOntList(getRequiredObject(predicate, RDFList.class),
                    this, predicate, getComponentType()));
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return Individuals.class;
        }

        protected Property getAlternativePredicate() {
            return OWL2.distinctMembers;
        }

        @Override
        protected Class<OntIndividual> getComponentType() {
            return OntIndividual.class;
        }

        @Override
        protected Resource getResourceType() {
            return OWL2.AllDifferent;
        }
    }

    public abstract static class PropertiesImpl<P extends OntRelationalProperty>
            extends OntDisjointImpl<P> implements Properties<P> {

        public PropertiesImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        protected Resource getResourceType() {
            return OWL2.AllDisjointProperties;
        }
    }

    public static class ObjectPropertiesImpl extends PropertiesImpl<OntObjectProperty> implements ObjectProperties {
        public ObjectPropertiesImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return ObjectProperties.class;
        }

        @Override
        protected Class<OntObjectProperty> getComponentType() {
            return OntObjectProperty.class;
        }
    }

    public static class DataPropertiesImpl extends PropertiesImpl<OntDataProperty> implements DataProperties {
        public DataPropertiesImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return DataProperties.class;
        }

        @Override
        protected Class<OntDataProperty> getComponentType() {
            return OntDataProperty.class;
        }
    }
}
