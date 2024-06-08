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
import org.apache.jena.ontapi.model.OntAnnotationProperty;
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntProperty;
import org.apache.jena.ontapi.model.OntStatement;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDFS;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Implementation of Annotation Property abstraction
 * (a URI-{@link Resource} with {@link OWL2#AnnotationProperty owl:AnnotationProperty} type).
 */
public class OntAnnotationPropertyImpl extends OntPropertyImpl implements OntAnnotationProperty {

    public OntAnnotationPropertyImpl(Node n, EnhGraph g) {
        super(n, g);
    }

    @Override
    public Class<OntAnnotationProperty> objectType() {
        return OntAnnotationProperty.class;
    }

    @Override
    public Stream<OntAnnotationProperty> superProperties(boolean direct) {
        return superProperties(this, OntAnnotationProperty.class, direct);
    }

    @Override
    public Stream<OntAnnotationProperty> subProperties(boolean direct) {
        return subProperties(this, OntAnnotationProperty.class, direct);
    }

    @Override
    public OntStatement addDomainStatement(Resource domain) {
        return addStatement(RDFS.domain, checkNamed(domain));
    }

    @Override
    public OntStatement addRangeStatement(Resource range) {
        return addStatement(RDFS.range, checkNamed(range));
    }

    @Override
    public Stream<Resource> domains() {
        return objects(RDFS.domain, Resource.class).filter(RDFNode::isURIResource);
    }

    @Override
    public Stream<Resource> ranges() {
        return objects(RDFS.range, Resource.class).filter(RDFNode::isURIResource);
    }

    @Override
    public Stream<OntClass> declaringClasses(boolean direct) {
        return declaringClasses(this, direct);
    }

    @Override
    public boolean hasSuperProperty(OntProperty property, boolean direct) {
        return property.canAs(OntAnnotationProperty.class) &&
                OntPropertyImpl.hasSuperProperty(this, property.as(OntAnnotationProperty.class), OntAnnotationProperty.class, direct);
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
        return getOptionalRootStatement(this, OWL2.AnnotationProperty);
    }

    @Override
    public int getOrdinal() {
        return OntStatementImpl.createProperty(node, enhGraph).getOrdinal();
    }
}
