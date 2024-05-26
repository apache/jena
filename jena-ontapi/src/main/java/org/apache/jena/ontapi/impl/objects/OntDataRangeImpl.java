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
import org.apache.jena.ontapi.model.OntDataRange;
import org.apache.jena.ontapi.model.OntFacetRestriction;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.model.OntStatement;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Implementation for Data Range Expressions.
 */
@SuppressWarnings("WeakerAccess")
public class OntDataRangeImpl extends OntObjectImpl implements OntDataRange {

    public OntDataRangeImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    private static Resource create(OntGraphModelImpl model) {
        Resource type = OntGraphModelImpl.configValue(model, OntModelControls.USE_OWL1_DATARANGE_DECLARATION_FEATURE) ?
                OWL2.DataRange :
                RDFS.Datatype;
        return model.createResource().addProperty(RDF.type, type);
    }

    public static OneOf createOneOf(OntGraphModelImpl model, Stream<Literal> values) {
        OntJenaException.notNull(values, "Null values stream.");
        RDFList items = model.createList(values
                .peek(f -> OntJenaException.notNull(f, "OntDR: null literal.")).iterator());
        Resource res = create(model).addProperty(OWL2.oneOf, items);
        return model.getNodeAs(res.asNode(), OneOf.class);
    }

    public static Restriction createRestriction(OntGraphModelImpl model, Named dataType, Stream<OntFacetRestriction> values) {
        OntJenaException.notNull(dataType, "Null data-type.");
        OntJenaException.notNull(values, "Null values stream.");
        RDFList items = model.createList(values
                .peek(f -> OntJenaException.notNull(f, "OntDR: null faced restriction."))
                .iterator());
        Resource res = create(model)
                .addProperty(OWL2.onDatatype, dataType)
                .addProperty(OWL2.withRestrictions, items);
        return model.getNodeAs(res.asNode(), Restriction.class);
    }

    public static ComplementOf createComplementOf(OntGraphModelImpl model, OntDataRange other) {
        OntJenaException.notNull(other, "Null data range.");
        Resource res = create(model).addProperty(OWL2.datatypeComplementOf, other);
        return model.getNodeAs(res.asNode(), ComplementOf.class);
    }

    public static UnionOf createUnionOf(OntGraphModelImpl model, Stream<OntDataRange> values) {
        OntJenaException.notNull(values, "Null values stream.");
        RDFList items = model.createList(values
                .peek(f -> OntJenaException.notNull(f, "OntDR: null data range."))
                .iterator());
        Resource res = create(model)
                .addProperty(OWL2.unionOf, items);
        return model.getNodeAs(res.asNode(), UnionOf.class);
    }

    public static IntersectionOf createIntersectionOf(OntGraphModelImpl model, Stream<OntDataRange> values) {
        OntJenaException.notNull(values, "Null values stream.");
        RDFList items = model.createList(values
                .peek(f -> OntJenaException.notNull(f, "OntDR: null data range."))
                .iterator());
        Resource res = create(model).addProperty(OWL2.intersectionOf, items);
        return model.getNodeAs(res.asNode(), IntersectionOf.class);
    }

    @Override
    public Optional<OntStatement> findRootStatement() {
        Resource type = OntGraphModelImpl.configValue(getModel(), OntModelControls.USE_OWL1_DATARANGE_DECLARATION_FEATURE) ?
                OWL2.DataRange :
                RDFS.Datatype;
        return getRequiredRootStatement(this, type);
    }

    public static class ComplementOfImpl extends OntDataRangeImpl implements ComplementOf {
        public ComplementOfImpl(Node n, EnhGraph m) {
            super(n, m);
        }

        @Override
        public OntDataRange getValue() {
            return getRequiredObject(OWL2.datatypeComplementOf, OntDataRange.class);
        }

        @Override
        public ExtendedIterator<OntStatement> listSpec() {
            return Iterators.concat(super.listSpec(), listRequired(OWL2.datatypeComplementOf));
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return ComplementOf.class;
        }

        @Override
        public ComplementOf setValue(OntDataRange value) {
            Objects.requireNonNull(value);
            removeAll(OWL2.datatypeComplementOf).addProperty(OWL2.datatypeComplementOf, value);
            return this;
        }
    }

    public static class OneOfImpl extends CombinationImpl<Literal> implements OneOf {
        public OneOfImpl(Node n, EnhGraph m) {
            super(n, m, OWL2.oneOf, Literal.class);
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return OneOf.class;
        }
    }

    public static class RestrictionImpl extends CombinationImpl<OntFacetRestriction> implements Restriction {
        public RestrictionImpl(Node n, EnhGraph m) {
            super(n, m, OWL2.withRestrictions, OntFacetRestriction.class);
        }

        @Override
        public Class<Restriction> objectType() {
            return Restriction.class;
        }

        @Override
        public Named getValue() {
            return getRequiredObject(OWL2.onDatatype, Named.class);
        }

        @Override
        public RestrictionImpl setValue(Named value) {
            Objects.requireNonNull(value);
            removeAll(OWL2.onDatatype).addProperty(OWL2.onDatatype, value);
            return this;
        }

        @Override
        public ExtendedIterator<OntStatement> listSpec() {
            return Iterators.concat(listDeclaration(), listRequired(OWL2.onDatatype), withRestrictionsSpec());
        }

        public ExtendedIterator<OntStatement> withRestrictionsSpec() {
            return Iterators.flatMap(getList().listContent(), s -> {
                if (!s.getObject().canAs(OntFacetRestriction.class)) {
                    return Iterators.of(s);
                }
                return Iterators.of(s, s.getObject().as(OntFacetRestriction.class).getMainStatement());
            });
        }

    }

    public static class UnionOfImpl extends CombinationImpl<OntDataRange> implements UnionOf {
        public UnionOfImpl(Node n, EnhGraph m) {
            super(n, m, OWL2.unionOf, OntDataRange.class);
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return UnionOf.class;
        }
    }

    public static class IntersectionOfImpl extends CombinationImpl<OntDataRange> implements IntersectionOf {

        public IntersectionOfImpl(Node n, EnhGraph m) {
            super(n, m, OWL2.intersectionOf, OntDataRange.class);
        }

        @Override
        public Class<? extends OntObject> objectType() {
            return IntersectionOf.class;
        }
    }

    /**
     * An abstract superclass for {@link OneOf}, {@link Restriction}, {@link UnionOf}, {@link IntersectionOf}.
     *
     * @param <N> {@link RDFNode}
     */
    protected static abstract class CombinationImpl<N extends RDFNode> extends OntDataRangeImpl implements Combination<N> {
        protected final Property predicate;
        protected final Class<N> type;

        protected CombinationImpl(Node n, EnhGraph m, Property predicate, Class<N> type) {
            super(n, m);
            this.predicate = OntJenaException.notNull(predicate, "Null predicate.");
            this.type = OntJenaException.notNull(type, "Null view.");
        }

        @Override
        public ExtendedIterator<OntStatement> listSpec() {
            return Iterators.concat(listDeclaration(), getList().listContent());
        }

        public ExtendedIterator<OntStatement> listDeclaration() {
            return super.listSpec();
        }

        @Override
        public OntListImpl<N> getList() {
            return getModel().asOntList(getRequiredObject(predicate, RDFList.class), this, predicate, true, null, type);
        }
    }

}
