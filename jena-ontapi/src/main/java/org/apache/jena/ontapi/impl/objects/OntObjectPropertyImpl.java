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
import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Node;
import org.apache.jena.ontapi.OntJenaException;
import org.apache.jena.ontapi.OntModelControls;
import org.apache.jena.ontapi.impl.OntGraphModelImpl;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntList;
import org.apache.jena.ontapi.model.OntNegativeAssertion;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.model.OntProperty;
import org.apache.jena.ontapi.model.OntStatement;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Implementation of the Object Property Expression abstraction.
 * Named instances should have a {@link OWL2#ObjectProperty owl:ObjectProperty} type declarations.
 * Anonymous instances should have {@link OWL2#inverseOf owl:inverseOf} predicate.
 */
@SuppressWarnings("WeakerAccess")
public abstract class OntObjectPropertyImpl extends OntPropertyImpl implements OntObjectProperty {

    public OntObjectPropertyImpl(Node n, EnhGraph g) {
        super(n, g);
    }

    @Override
    public Stream<OntObjectProperty> superProperties(boolean direct) {
        return superProperties(this, OntObjectProperty.class, direct);
    }

    @Override
    public Stream<OntObjectProperty> subProperties(boolean direct) {
        return subProperties(this, OntObjectProperty.class, direct);
    }

    @Override
    public Stream<OntClass> declaringClasses(boolean direct) {
        return declaringClasses(this, direct);
    }

    @Override
    public boolean hasSuperProperty(OntProperty property, boolean direct) {
        return property.canAs(OntObjectProperty.class) &&
                OntPropertyImpl.hasSuperProperty(this, property.as(OntObjectProperty.class), OntObjectProperty.class, direct);
    }

    @Override
    public OntNegativeAssertion.WithObjectProperty addNegativeAssertion(OntIndividual source, OntIndividual target) {
        return OntNegativePropertyAssertionImpl.create(getModel(), source, this, target);
    }

    @Override
    public OntList<OntObjectProperty> createPropertyChain(Collection<OntObjectProperty> properties) {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_PROPERTY_CHAIN_AXIOM_FEATURE, "owl:propertyChainAxiom");
        return getModel().createOntList(this, OWL2.propertyChainAxiom, OntObjectProperty.class, properties.iterator());
    }

    @Override
    public Stream<OntList<OntObjectProperty>> propertyChains() {
        if (!OntGraphModelImpl.configValue(getModel(), OntModelControls.USE_OWL_PROPERTY_CHAIN_AXIOM_FEATURE)) {
            return Stream.empty();
        }
        return OntListImpl.stream(getModel(), this, OWL2.propertyChainAxiom, OntObjectProperty.class);
    }

    @Override
    public OntObjectPropertyImpl removePropertyChain(Resource rdfList) throws OntJenaException.IllegalArgument {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_PROPERTY_CHAIN_AXIOM_FEATURE, "owl:propertyChainAxiom");
        getModel().deleteOntList(this, OWL2.propertyChainAxiom, findPropertyChain(rdfList).orElse(null));
        return this;
    }

    @Override
    public Stream<OntObjectProperty> disjointProperties() {
        return disjointProperties(getModel(), OntObjectProperty.class, this);
    }

    @Override
    public OntStatement addPropertyDisjointWithStatement(OntObjectProperty other) {
        return addDisjointWith(getModel(), this, other);
    }

    @Override
    public OntObjectProperty removeDisjointProperty(Resource property) {
        removeDisjointWith(getModel(), this, property);
        return this;
    }

    @Override
    public Stream<OntObjectProperty> equivalentProperties() {
        return equivalentProperties(getModel(), OntObjectProperty.class, this);
    }

    @Override
    public OntStatement addEquivalentPropertyStatement(OntObjectProperty other) {
        return addEquivalentProperty(getModel(), this, other);
    }

    @Override
    public OntObjectProperty removeEquivalentProperty(Resource property) {
        removeEquivalentProperty(getModel(), this, property);
        return this;
    }

    @Override
    public boolean isFunctional() {
        return OntGraphModelImpl.configValue(getModel(), OntModelControls.USE_OWL_OBJECT_PROPERTY_FUNCTIONAL_FEATURE) &&
                hasType(OWL2.FunctionalProperty);
    }

    @Override
    public OntObjectPropertyImpl setFunctional(boolean functional) {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_OBJECT_PROPERTY_FUNCTIONAL_FEATURE, "owl:FunctionalProperty");
        return changeRDFType(OWL2.FunctionalProperty, functional);
    }

    @Override
    public OntStatement addFunctionalDeclaration() {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_OBJECT_PROPERTY_FUNCTIONAL_FEATURE, "owl:FunctionalProperty");
        return addStatement(RDF.type, OWL2.FunctionalProperty);
    }

    @Override
    public boolean isInverseFunctional() {
        return OntGraphModelImpl.configValue(getModel(), OntModelControls.USE_OWL_PROPERTY_INVERSE_FUNCTIONAL_FEATURE) &&
                hasType(OWL2.InverseFunctionalProperty);
    }

    @Override
    public OntObjectPropertyImpl setInverseFunctional(boolean inverseFunctional) {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_PROPERTY_INVERSE_FUNCTIONAL_FEATURE, "owl:InverseFunctionalProperty");
        return changeRDFType(OWL2.InverseFunctionalProperty, inverseFunctional);
    }

    @Override
    public OntStatement addInverseFunctionalDeclaration() {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_PROPERTY_INVERSE_FUNCTIONAL_FEATURE, "owl:InverseFunctionalProperty");
        return addStatement(RDF.type, OWL2.InverseFunctionalProperty);
    }

    @Override
    public boolean isSymmetric() {
        return OntGraphModelImpl.configValue(getModel(), OntModelControls.USE_OWL_PROPERTY_SYMMETRIC_FEATURE) &&
                hasType(OWL2.SymmetricProperty);
    }

    @Override
    public OntObjectPropertyImpl setSymmetric(boolean symmetric) {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_PROPERTY_SYMMETRIC_FEATURE, "owl:SymmetricProperty");
        return changeRDFType(OWL2.SymmetricProperty, symmetric);
    }

    @Override
    public OntStatement addSymmetricDeclaration() {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_PROPERTY_SYMMETRIC_FEATURE, "owl:SymmetricProperty");
        return addStatement(RDF.type, OWL2.SymmetricProperty);
    }

    @Override
    public boolean isAsymmetric() {
        return OntGraphModelImpl.configValue(getModel(), OntModelControls.USE_OWL_PROPERTY_ASYMMETRIC_FEATURE) &&
                hasType(OWL2.AsymmetricProperty);
    }

    @Override
    public OntObjectPropertyImpl setAsymmetric(boolean asymmetric) {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_PROPERTY_ASYMMETRIC_FEATURE, "owl:AsymmetricProperty");
        return changeRDFType(OWL2.AsymmetricProperty, asymmetric);
    }

    @Override
    public OntStatement addAsymmetricDeclaration() {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_PROPERTY_ASYMMETRIC_FEATURE, "owl:AsymmetricProperty");
        return addStatement(RDF.type, OWL2.AsymmetricProperty);
    }

    @Override
    public boolean isTransitive() {
        return OntGraphModelImpl.configValue(getModel(), OntModelControls.USE_OWL_PROPERTY_TRANSITIVE_FEATURE) &&
                hasType(OWL2.TransitiveProperty);
    }

    @Override
    public OntObjectPropertyImpl setTransitive(boolean transitive) {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_PROPERTY_TRANSITIVE_FEATURE, "owl:TransitiveProperty");
        return changeRDFType(OWL2.TransitiveProperty, transitive);
    }

    @Override
    public OntStatement addTransitiveDeclaration() {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_PROPERTY_TRANSITIVE_FEATURE, "owl:TransitiveProperty");
        return addStatement(RDF.type, OWL2.TransitiveProperty);
    }

    @Override
    public boolean isReflexive() {
        return OntGraphModelImpl.configValue(getModel(), OntModelControls.USE_OWL_PROPERTY_REFLEXIVE_FEATURE) &&
                hasType(OWL2.ReflexiveProperty);
    }

    @Override
    public OntObjectPropertyImpl setReflexive(boolean reflexive) {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_PROPERTY_REFLEXIVE_FEATURE, "owl:ReflexiveProperty");
        return changeRDFType(OWL2.ReflexiveProperty, reflexive);
    }

    @Override
    public OntStatement addReflexiveDeclaration() {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_PROPERTY_REFLEXIVE_FEATURE, "owl:ReflexiveProperty");
        return addStatement(RDF.type, OWL2.ReflexiveProperty);
    }

    @Override
    public boolean isIrreflexive() {
        return OntGraphModelImpl.configValue(getModel(), OntModelControls.USE_OWL_PROPERTY_IRREFLEXIVE_FEATURE) &&
                hasType(OWL2.IrreflexiveProperty);
    }

    @Override
    public OntObjectPropertyImpl setIrreflexive(boolean irreflexive) {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_PROPERTY_IRREFLEXIVE_FEATURE, "owl:IrreflexiveProperty");
        return changeRDFType(OWL2.IrreflexiveProperty, irreflexive);
    }

    @Override
    public OntStatement addIrreflexiveDeclaration() {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_PROPERTY_IRREFLEXIVE_FEATURE, "owl:IrreflexiveProperty");
        return addStatement(RDF.type, OWL2.IrreflexiveProperty);
    }

    @Override
    public Stream<OntObjectProperty> inverseProperties() {
        if (!OntGraphModelImpl.configValue(getModel(), OntModelControls.USE_OWL_INVERSE_OBJECT_PROPERTIES_FEATURE)) {
            return Stream.empty();
        }
        return objects(OWL2.inverseOf, OntObjectProperty.class);
    }

    @Override
    public OntStatement addInverseOfStatement(OntObjectProperty other) {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_INVERSE_OBJECT_PROPERTIES_FEATURE, "owl:inverseOf");
        return addStatement(OWL2.inverseOf, other);
    }

    @Override
    public OntObjectProperty removeInverseProperty(Resource other) {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_INVERSE_OBJECT_PROPERTIES_FEATURE, "owl:inverseOf");
        remove(OWL2.inverseOf, other);
        return this;
    }

    @Override
    protected OntObjectPropertyImpl changeRDFType(Resource type, boolean add) {
        super.changeRDFType(type, add);
        return this;
    }

    public static class NamedImpl extends OntObjectPropertyImpl implements Named {

        public NamedImpl(Node n, EnhGraph g) {
            super(checkNamed(n), g);
        }

        @Override
        public Inverse createInverse() {
            OntGraphModelImpl m = getModel();
            m.checkType(OntObjectProperty.Inverse.class);
            List<Node> nodes = m.localStatements(null, OWL2.inverseOf, this)
                    .map(OntStatement::getSubject)
                    .filter(RDFNode::isAnon)
                    .map(FrontsNode::asNode)
                    .distinct()
                    .toList();
            if (nodes.size() > 1) {
                throw new OntJenaException.IllegalState("More than one inverse-of object properties found: [" +
                        nodes + " owl:inverseOf " + this + "]");
            }
            Node n = nodes.isEmpty() ?
                    m.createResource().addProperty(OWL2.inverseOf, NamedImpl.this).asNode() :
                    nodes.get(0);
            return m.getNodeAs(n, Inverse.class);
        }

        @Override
        public boolean isBuiltIn() {
            return getModel().isBuiltIn(this);
        }

        @Override
        public Class<Named> objectType() {
            return Named.class;
        }

        @Override
        public Property inModel(Model m) {
            return getModel() == m ? this : m.createProperty(getURI());
        }

        @Override
        public Optional<OntStatement> findRootStatement() {
            return getOptionalRootStatement(this, OWL2.ObjectProperty);
        }

        @Override
        public int getOrdinal() {
            return OntStatementImpl.createProperty(node, enhGraph).getOrdinal();
        }
    }

    public static class InversePropertyImpl extends OntObjectPropertyImpl implements Inverse {

        public InversePropertyImpl(Node n, EnhGraph g) {
            super(n, g);
        }

        @Override
        public Optional<OntStatement> findRootStatement() {
            return Optional.of(getModel().createStatement(this, OWL2.inverseOf, getDirect()).asRootStatement());
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return Inverse.class;
        }

        @Override
        public Named getDirect() {
            OntGraphModelImpl m = getModel();
            List<Resource> res = Iterators.distinct(listObjects(OWL2.inverseOf, Resource.class)
                    .filterKeep(RDFNode::isURIResource)).toList();
            if (res.size() != 1)
                throw new OntJenaException.IllegalState("Expected one and only one owl:inverseOf statement, but found: [" +
                        this + " owl:inverseOf " + res + "]");
            return m.getNodeAs(res.get(0).asNode(), Named.class);
        }

        @Override
        public Property asProperty() {
            return getDirect().asProperty();
        }
    }

}

