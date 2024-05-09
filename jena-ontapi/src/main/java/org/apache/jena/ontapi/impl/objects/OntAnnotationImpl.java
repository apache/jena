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
import org.apache.jena.ontapi.impl.OntGraphModelImpl;
import org.apache.jena.ontapi.model.OntAnnotation;
import org.apache.jena.ontapi.model.OntAnnotationProperty;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.model.OntStatement;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.ontapi.utils.StdModels;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The implementation of {@link OntAnnotation Annotation} {@link OntObject Ontology Object}.
 * Note: the search is carried out only for the root annotations:
 * the result of snippet {@code model.ontObjects(OntAnnotation.class)} would not contain the nested annotations.
 */
@SuppressWarnings("WeakerAccess")
public class OntAnnotationImpl extends OntObjectImpl implements OntAnnotation {
    public static final Set<Property> REQUIRED_PROPERTIES = Set.of(OWL2.annotatedSource,
            OWL2.annotatedProperty, OWL2.annotatedTarget);
    public static final Set<Property> SPEC = Stream.concat(Stream.of(RDF.type), REQUIRED_PROPERTIES.stream())
            .collect(Collectors.toUnmodifiableSet());
    /**
     * The first are annotations with the most numerous assertions and children,
     * the remaining comparison operations are not so important,
     * but the provided order should be preserved after graph reload.
     */
    public static final Comparator<OntAnnotation> DEFAULT_ANNOTATION_COMPARATOR = (left, right) -> {
        Set<OntStatement> leftSet = listRelatedStatements(left).toSet();
        Set<OntStatement> rightSet = listRelatedStatements(right).toSet();
        int res = Integer.compare(leftSet.size(), rightSet.size());
        while (res == 0) {
            OntStatement s1 = removeMin(leftSet, StdModels.STATEMENT_COMPARATOR_IGNORE_BLANK);
            OntStatement s2 = removeMin(rightSet, StdModels.STATEMENT_COMPARATOR_IGNORE_BLANK);
            res = StdModels.STATEMENT_COMPARATOR_IGNORE_BLANK.compare(s1, s2);
            if (leftSet.isEmpty() || rightSet.isEmpty()) break;
        }
        return -res;
    };
    public static final Set<Resource> EXTRA_ROOT_TYPES =
            Set.of(OWL2.AllDisjointClasses, OWL2.AllDisjointProperties, OWL2.AllDifferent, OWL2.NegativePropertyAssertion);
    public static final List<Resource> ROOT_TYPES = Stream.concat(
            Stream.of(OWL2.Axiom, OWL2.Annotation),
            EXTRA_ROOT_TYPES.stream()).toList();

    public OntAnnotationImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    /**
     * Creates a new annotation b-node resource with the given type and base statement.
     *
     * @param model {@link Model}
     * @param base  base ont-statement
     * @param type  owl:Axiom or owl:Annotation
     * @return {@link OntAnnotation} the anonymous resource with a specified type.
     */
    public static OntAnnotation createAnnotation(Model model, Statement base, Resource type) {
        Resource res = Objects.requireNonNull(model).createResource();
        if (!model.contains(Objects.requireNonNull(base))) {
            throw new OntJenaException.IllegalArgument("Can't find " + StdModels.toString(base));
        }
        res.addProperty(RDF.type, type);
        res.addProperty(OWL2.annotatedSource, base.getSubject());
        res.addProperty(OWL2.annotatedProperty, base.getPredicate());
        res.addProperty(OWL2.annotatedTarget, base.getObject());
        return res.as(OntAnnotation.class);
    }

    private static <S> S removeMin(Set<S> notEmptySet,
                                   @SuppressWarnings("SameParameterValue") Comparator<? super S> comparator)
            throws IllegalStateException {
        S res = notEmptySet.stream().min(comparator).orElseThrow(IllegalStateException::new);
        if (!notEmptySet.remove(res)) throw new IllegalStateException();
        return res;
    }

    /**
     * Lists annotation assertions plus sub-annotation root statements.
     *
     * @param annotation {@link OntAnnotation}
     * @return {@link ExtendedIterator} of {@link OntStatement}s
     */
    public static ExtendedIterator<OntStatement> listRelatedStatements(OntAnnotation annotation) {
        OntAnnotationImpl a = (OntAnnotationImpl) annotation;
        return a.listAssertions().andThen(a.listDescendants().mapWith(OntObject::getMainStatement));
    }

    @Override
    public ExtendedIterator<OntStatement> listSpec() {
        //return SPEC.stream().map(this::getRequiredProperty);
        return listStatements().filterKeep(s -> SPEC.contains(s.getPredicate()) || s.isAnnotationAssertion());
    }

    @Override
    public OntStatement getBase() {
        if (!hasType(OWL2.Axiom) && !hasType(OWL2.Annotation)) {
            return null;
        }
        Resource s = getRequiredObject(OWL2.annotatedSource, Resource.class);
        Property p = getRequiredObject(OWL2.annotatedProperty, Property.class);
        RDFNode o = getRequiredObject(OWL2.annotatedTarget, RDFNode.class);
        return getModel().createStatement(s, p, o);
    }

    @Override
    public ExtendedIterator<OntStatement> listAssertions() {
        return listStatements().filterKeep(s -> !SPEC.contains(s.getPredicate()) && s.isAnnotationAssertion());
    }

    @Override
    public ExtendedIterator<OntStatement> listAnnotations() {
        return listAssertions();
    }

    @Override
    public Stream<OntAnnotation> descendants() {
        return Iterators.asStream(listDescendants(), getCharacteristics());
    }

    /**
     * Returns an iterator over all descendants of this ont-annotation resource.
     *
     * @return {@link ExtendedIterator} of {@link OntAnnotation}s
     */
    public ExtendedIterator<OntAnnotation> listDescendants() {
        OntGraphModelImpl m = getModel();
        return listAnnotatedSources()
                .mapWith(s -> m.findNodeAs(((OntStatementImpl) s).getSubjectNode(), OntAnnotation.class))
                .filterDrop(Objects::isNull);
    }

    protected ExtendedIterator<OntStatement> listAnnotatedSources() {
        return getModel().listOntStatements(null, OWL2.annotatedSource, this);
    }

    @Override
    public OntStatement addAnnotation(OntAnnotationProperty property, RDFNode value) {
        return getModel().add(this, property, value).createStatement(this, property, value);
    }

    @Override
    public Class<? extends OntObject> objectType() {
        return OntAnnotation.class;
    }

}
