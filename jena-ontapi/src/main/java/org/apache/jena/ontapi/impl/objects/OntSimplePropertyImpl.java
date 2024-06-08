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
import org.apache.jena.ontapi.model.OntClass;
import org.apache.jena.ontapi.model.OntEntity;
import org.apache.jena.ontapi.model.OntObject;
import org.apache.jena.ontapi.model.OntProperty;
import org.apache.jena.ontapi.model.OntStatement;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Simple direct {@link OntProperty} implementation.
 */
public class OntSimplePropertyImpl extends OntPropertyImpl implements OntProperty, OntEntity {

    public OntSimplePropertyImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    @Override
    public Class<? extends OntObject> objectType() {
        return OntProperty.class;
    }

    @Override
    public Optional<OntStatement> findRootStatement() {
        return getOptionalRootStatement(this, RDF.Property);
    }

    @Override
    public Stream<OntProperty> superProperties(boolean direct) {
        return superProperties(this, OntProperty.class, direct);
    }

    @Override
    public Stream<OntProperty> subProperties(boolean direct) {
        return subProperties(this, OntProperty.class, direct);
    }

    @Override
    public Stream<? extends Resource> domains() {
        return objects(RDFS.domain, Resource.class).filter(RDFNode::isURIResource);
    }

    @Override
    public Stream<? extends Resource> ranges() {
        return objects(RDFS.range, Resource.class).filter(RDFNode::isURIResource);
    }

    @Override
    public Stream<OntClass> declaringClasses(boolean direct) {
        return declaringClasses(this, direct);
    }

    @Override
    public boolean hasSuperProperty(OntProperty property, boolean direct) {
        return OntPropertyImpl.hasSuperProperty(this, property, OntProperty.class, direct);
    }

    @Override
    public Resource inModel(Model m) {
        return getModel() == m ? this : m.getRDFNode(asNode()).asResource();
    }

    @Override
    public boolean isBuiltIn() {
        return isURIResource() && getModel().isBuiltIn(this);
    }
}
