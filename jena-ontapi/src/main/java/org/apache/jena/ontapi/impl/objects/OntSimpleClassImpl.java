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
import org.apache.jena.ontapi.impl.OntGraphModelImpl;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntEntity;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntList;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.model.OntProperty;
import org.apache.jena.ontapi.model.OntRelationalProperty;
import org.apache.jena.ontapi.model.OntStatement;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL2;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * {@code owl:Class} Implementation.
 * Instance of this class as a class with unknown nature is only available in a spec with corresponding permissions
 * ({@link OntModelControls}).
 * Specialized classes have their own implementations ({@link NamedImpl} or {@link OntClassImpl}).
 */
@SuppressWarnings("WeakerAccess")
public class OntSimpleClassImpl extends OntObjectImpl implements OntClass {

    public OntSimpleClassImpl(Node n, EnhGraph eg) {
        super(n, eg);
    }

    @Override
    public Optional<OntStatement> findRootStatement() {
        return getOptionalRootStatement(this, OWL2.Class);
    }

    @Override
    public Class<? extends OntClass> objectType() {
        return OntClass.class;
    }

    @Override
    public OntIndividual.Anonymous createIndividual() {
        return OntClassImpl.createAnonymousIndividual(getModel(), this);
    }

    @Override
    public OntIndividual.Named createIndividual(String uri) {
        return OntClassImpl.createNamedIndividual(getModel(), this, uri);
    }

    @Override
    public Stream<OntClass> superClasses(boolean direct) {
        return OntClassImpl.superClasses(this, direct);
    }

    @Override
    public Stream<OntClass> subClasses(boolean direct) {
        return OntClassImpl.subClasses(this, direct);
    }

    @Override
    public Stream<OntIndividual> individuals(boolean direct) {
        return OntClassImpl.individuals(this, direct);
    }

    @Override
    public boolean hasDeclaredProperty(OntProperty property, boolean direct) {
        return OntClassImpl.testDomain(this, property, direct);
    }

    @Override
    public Stream<OntProperty> declaredProperties(boolean direct) {
        return OntClassImpl.declaredProperties(this, direct);
    }

    @Override
    public boolean isHierarchyRoot() {
        return OntClassImpl.isHierarchyRoot(this);
    }

    @Override
    public OntList<OntRelationalProperty> createHasKey(Collection<OntObjectProperty> ope, Collection<OntDataProperty> dpe) {
        Stream<OntRelationalProperty> stream = Stream.concat(ope.stream(), dpe.stream());
        return OntClassImpl.createHasKey(getModel(), this, stream);
    }

    @Override
    public OntStatement addHasKeyStatement(OntRelationalProperty... properties) {
        return OntClassImpl.createHasKey(getModel(), this, Arrays.stream(properties)).getMainStatement();
    }

    @Override
    public Stream<OntList<OntRelationalProperty>> hasKeys() {
        return OntClassImpl.listHasKeys(getModel(), this);
    }

    @Override
    public OntSimpleClassImpl removeHasKey(Resource list) throws OntJenaException.IllegalArgument {
        OntClassImpl.removeHasKey(getModel(), this, list);
        return this;
    }

    @Override
    public boolean isDisjoint(Resource candidate) {
        return OntClassImpl.isDisjoint(this, candidate);
    }

    @Override
    public Stream<OntClass> disjointClasses() {
        return OntClassImpl.disjointClasses(getModel(), this);
    }

    @Override
    public Stream<OntClass> equivalentClasses() {
        return OntClassImpl.equivalentClasses(getModel(), this);
    }

    @Override
    public boolean hasSuperClass(OntClass clazz, boolean direct) {
        return OntClassImpl.hasSuperClass(this, clazz, direct);
    }

    @Override
    public OntClass addDisjointClass(OntClass other) {
        OntClassImpl.addDisjoint(getModel(), this, other);
        return this;
    }

    @Override
    public OntStatement addDisjointWithStatement(OntClass other) {
        return OntClassImpl.addDisjointWithStatement(getModel(), this, other);
    }

    @Override
    public OntClass removeDisjointClass(Resource other) {
        OntClassImpl.removeDisjoint(getModel(), this, other);
        return this;
    }

    @Override
    public OntStatement addEquivalentClassStatement(OntClass other) {
        return OntClassImpl.addEquivalentClass(getModel(), this, other);
    }

    @Override
    public OntClass removeEquivalentClass(Resource other) {
        OntClassImpl.removeEquivalentClass(getModel(), this, other);
        return this;
    }

    /**
     * A version of Named class for RL specification.
     * @see <a href="https://www.w3.org/TR/owl2-profiles/#OWL_2_RL_2">OWL2 RL</a>
     */
    public static class RLNamedImpl extends NamedImpl {

        public RLNamedImpl(Node n, EnhGraph eg) {
            super(n, eg);
        }

        @Override
        public OntClass asSubClass() {
            if (OWL2.Thing.equals(this)) {
                throw new OntJenaException.Unsupported("Specification does not allow this class to be a subclass");
            }
            return this;
        }

        @Override
        public boolean canAsSubClass() {
            return !OWL2.Thing.equals(this);
        }

        @Override
        public OntClass asSuperClass() {
            if (OWL2.Thing.equals(this)) {
                throw new OntJenaException.Unsupported("Specification does not allow this class to be a superclass");
            }
            return this;
        }

        @Override
        public boolean canAsSuperClass() {
            return !OWL2.Thing.equals(this);
        }

        @Override
        public OntClass asEquivalentClass() {
            if (OWL2.Thing.equals(this)) {
                throw new OntJenaException.Unsupported("Specification does not allow this class to be an equivalent class");
            }
            return this;
        }

        @Override
        public boolean canAsEquivalentClass() {
            return !OWL2.Thing.equals(this);
        }

        @Override
        public OntClass asDisjointClass() {
            if (OWL2.Thing.equals(this)) {
                throw new OntJenaException.Unsupported("Specification does not allow this class to be a disjoint class");
            }
            return this;
        }

        @Override
        public boolean canAsDisjointClass() {
            return !OWL2.Thing.equals(this);
        }
    }

    /**
     * Primary (named) class ({@code <uri> a owl:Class}).
     * This is also {@link OntEntity}.
     * Note:
     * In jena OWL1, class expressions, such as {@link OntClass.ComplementOf}
     * can also be named.
     */
    @SuppressWarnings("javadoc")
    public static class NamedImpl extends OntSimpleClassImpl implements OntClass.Named {

        public NamedImpl(Node n, EnhGraph eg) {
            super(checkNamed(n), eg);
        }

        @Override
        public OntClass.Named asNamed() {
            return this;
        }

        @Override
        public Class<Named> objectType() {
            return Named.class;
        }

        @Override
        public boolean isBuiltIn() {
            return getModel().isBuiltIn(this);
        }

        @Override
        public Stream<OntList<OntClass>> disjointUnions() {
            if (!OntGraphModelImpl.configValue(getModel(), OntModelControls.USE_OWL2_NAMED_CLASS_DISJOINT_UNION_FEATURE)) {
                return Stream.empty();
            }
            return OntListImpl.stream(getModel(), this, OWL2.disjointUnionOf, OntClass.class);
        }

        @Override
        public OntList<OntClass> createDisjointUnion(Collection<OntClass> classes) {
            OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL2_NAMED_CLASS_DISJOINT_UNION_FEATURE, "owl:disjointUnionOf");
            return getModel().createOntList(this, OWL2.disjointUnionOf, OntClass.class,
                    Objects.requireNonNull(classes).stream().distinct().iterator());
        }

        @Override
        public OntClass.Named removeDisjointUnion(Resource rdfList) throws OntJenaException.IllegalArgument {
            OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL2_NAMED_CLASS_DISJOINT_UNION_FEATURE, "owl:disjointUnionOf");
            getModel().deleteOntList(this, OWL2.disjointUnionOf, findDisjointUnion(rdfList).orElse(null));
            return this;
        }

        @Override
        public OntClass.Named removeDisjointClass(Resource other) {
            super.removeDisjointClass(other);
            return this;
        }

        @Override
        public OntClass.Named removeEquivalentClass(Resource other) {
            super.removeEquivalentClass(other);
            return this;
        }

        @Override
        public OntClass.Named addDisjointClass(OntClass other) {
            super.addDisjointClass(other);
            return this;
        }
    }
}
