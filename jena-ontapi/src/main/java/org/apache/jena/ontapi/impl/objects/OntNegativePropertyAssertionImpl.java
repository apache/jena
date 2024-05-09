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
import org.apache.jena.ontapi.impl.OntGraphModelImpl;
import org.apache.jena.ontapi.model.OntDataProperty;
import org.apache.jena.ontapi.model.OntIndividual;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.model.OntNegativeAssertion;
import org.apache.jena.ontapi.model.OntObjectProperty;
import org.apache.jena.ontapi.model.OntRelationalProperty;
import org.apache.jena.ontapi.model.OntStatement;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;

import java.util.Optional;

/**
 * Implementation of the Negative Property Assertion.
 */
@SuppressWarnings("WeakerAccess")
public abstract class OntNegativePropertyAssertionImpl<P extends OntRelationalProperty, T extends RDFNode>
        extends OntObjectImpl implements OntNegativeAssertion<P, T> {

    public OntNegativePropertyAssertionImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    public static WithDataProperty create(OntGraphModelImpl model,
                                          OntIndividual source,
                                          OntDataProperty property,
                                          Literal target) {
        Resource res = create(model, source).addProperty(OWL2.assertionProperty, property)
                .addProperty(OWL2.targetValue, target);
        return model.getNodeAs(res.asNode(), WithDataProperty.class);
    }

    public static WithObjectProperty create(OntGraphModelImpl model,
                                            OntIndividual source,
                                            OntObjectProperty property,
                                            OntIndividual target) {
        Resource res = create(model, source)
                .addProperty(OWL2.assertionProperty, property)
                .addProperty(OWL2.targetIndividual, target);
        return model.getNodeAs(res.asNode(), WithObjectProperty.class);
    }

    private static Resource create(OntModel model, OntIndividual source) {
        Resource res = model.createResource();
        res.addProperty(RDF.type, OWL2.NegativePropertyAssertion);
        res.addProperty(OWL2.sourceIndividual, source);
        return res;
    }

    @Override
    public Optional<OntStatement> findRootStatement() {
        return getRequiredRootStatement(this, OWL2.NegativePropertyAssertion);
    }

    @Override
    public ExtendedIterator<OntStatement> listSpec() {
        return Iterators.concat(super.listSpec(), listRequired(OWL2.sourceIndividual, OWL2.assertionProperty, targetPredicate()));
    }

    abstract Class<P> propertyClass();

    abstract Property targetPredicate();

    @Override
    public OntIndividual getSource() {
        return getRequiredObject(OWL2.sourceIndividual, OntIndividual.class);
    }

    @Override
    public P getProperty() {
        return getRequiredObject(OWL2.assertionProperty, propertyClass());
    }

    public static class ObjectAssertionImpl extends OntNegativePropertyAssertionImpl<OntObjectProperty, OntIndividual> implements WithObjectProperty {
        public ObjectAssertionImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        Class<OntObjectProperty> propertyClass() {
            return OntObjectProperty.class;
        }

        @Override
        Property targetPredicate() {
            return OWL2.targetIndividual;
        }

        @Override
        public Class<WithObjectProperty> objectType() {
            return WithObjectProperty.class;
        }


        @Override
        public OntIndividual getTarget() {
            return getRequiredObject(targetPredicate(), OntIndividual.class);
        }

    }

    public static class DataAssertionImpl extends OntNegativePropertyAssertionImpl<OntDataProperty, Literal> implements WithDataProperty {
        public DataAssertionImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        Class<OntDataProperty> propertyClass() {
            return OntDataProperty.class;
        }

        @Override
        Property targetPredicate() {
            return OWL2.targetValue;
        }

        @Override
        public Class<WithDataProperty> objectType() {
            return WithDataProperty.class;
        }


        @Override
        public Literal getTarget() {
            return getRequiredObject(targetPredicate(), Literal.class);
        }
    }
}
