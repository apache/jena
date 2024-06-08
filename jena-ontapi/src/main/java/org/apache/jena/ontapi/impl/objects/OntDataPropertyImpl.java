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
import org.apache.jena.ontapi.OntModelControls;
import org.apache.jena.ontapi.impl.OntGraphModelImpl;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntNegativeAssertion;
import org.apache.jena.ontapi.model.OntProperty;
import org.apache.jena.ontapi.model.OntStatement;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * An ontology object implementation with declarative type {@link OWL2#DatatypeProperty owl:DatatypeProperty}.
 */
public class OntDataPropertyImpl extends OntPropertyImpl implements OntDataProperty {

    public OntDataPropertyImpl(Node n, EnhGraph g) {
        super(n, g);
    }

    @Override
    public Class<OntDataProperty> objectType() {
        return OntDataProperty.class;
    }

    @Override
    public Stream<OntDataProperty> superProperties(boolean direct) {
        return superProperties(this, OntDataProperty.class, direct);
    }

    @Override
    public Stream<OntDataProperty> subProperties(boolean direct) {
        return subProperties(this, OntDataProperty.class, direct);
    }

    @Override
    public Stream<OntClass> declaringClasses(boolean direct) {
        return declaringClasses(this, direct);
    }

    @Override
    public boolean hasSuperProperty(OntProperty property, boolean direct) {
        return property.canAs(OntDataProperty.class) &&
                OntPropertyImpl.hasSuperProperty(this, property.as(OntDataProperty.class), OntDataProperty.class, direct);
    }

    @Override
    public Stream<OntDataProperty> disjointProperties() {
        return OntPropertyImpl.disjointProperties(getModel(), OntDataProperty.class, this);
    }

    @Override
    public OntStatement addPropertyDisjointWithStatement(OntDataProperty other) {
        return OntPropertyImpl.addDisjointWith(getModel(), this, other);
    }

    @Override
    public OntDataProperty removeDisjointProperty(Resource property) {
        OntPropertyImpl.removeDisjointWith(getModel(), this, property);
        return this;
    }

    @Override
    public Stream<OntDataProperty> equivalentProperties() {
        return OntPropertyImpl.equivalentProperties(getModel(), OntDataProperty.class, this);
    }

    @Override
    public OntStatement addEquivalentPropertyStatement(OntDataProperty other) {
        return OntPropertyImpl.addEquivalentProperty(getModel(), this, other);
    }

    @Override
    public OntDataProperty removeEquivalentProperty(Resource property) {
        OntPropertyImpl.removeEquivalentProperty(getModel(), this, property);
        return this;
    }

    @Override
    public OntNegativeAssertion.WithDataProperty addNegativeAssertion(OntIndividual source, Literal target) {
        return OntNegativePropertyAssertionImpl.create(getModel(), source, this, target);
    }

    @Override
    public boolean isFunctional() {
        return OntGraphModelImpl.configValue(getModel(), OntModelControls.USE_OWL_DATA_PROPERTY_FUNCTIONAL_FEATURE) &&
                hasType(OWL2.FunctionalProperty);
    }

    @Override
    public OntDataPropertyImpl setFunctional(boolean functional) {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_DATA_PROPERTY_FUNCTIONAL_FEATURE, "owl:FunctionalProperty");
        changeRDFType(OWL2.FunctionalProperty, functional);
        return this;
    }

    @Override
    public OntStatement addFunctionalDeclaration() {
        OntGraphModelImpl.checkFeature(getModel(), OntModelControls.USE_OWL_DATA_PROPERTY_FUNCTIONAL_FEATURE, "owl:FunctionalProperty");
        return addStatement(RDF.type, OWL2.FunctionalProperty);
    }

    @Override
    public boolean isBuiltIn() {
        return getModel().isBuiltIn(this);
    }

    @Override
    public Property inModel(Model m) {
        return getModel() == m ? this : m.createProperty(getURI());
    }

    @Override
    public Optional<OntStatement> findRootStatement() {
        return getOptionalRootStatement(this, OWL2.DatatypeProperty);
    }

    @Override
    public int getOrdinal() {
        return OntStatementImpl.createProperty(node, enhGraph).getOrdinal();
    }
}