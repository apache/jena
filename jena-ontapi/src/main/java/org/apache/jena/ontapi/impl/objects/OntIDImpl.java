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
import org.apache.jena.ontapi.model.OntID;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;

import java.util.Set;
import java.util.stream.Stream;

/**
 * An Ontology ID Implementation.
 */
@SuppressWarnings("WeakerAccess")
public class OntIDImpl extends OntObjectImpl implements OntID {

    public OntIDImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    @Override
    public String getVersionIRI() {
        Set<String> res = Iterators.takeAsSet(listProperties(OWL2.versionIRI)
                        .mapWith(Statement::getObject)
                        .filterKeep(RDFNode::isURIResource)
                        .mapWith(RDFNode::asResource)
                        .mapWith(Resource::getURI),
                2
        );
        return res.size() == 1 ? res.iterator().next() : null;
    }

    @Override
    public OntIDImpl setVersionIRI(String uri) throws OntJenaException {
        if (uri != null && isAnon()) {
            throw new OntJenaException.IllegalArgument("Attempt to add version IRI (" + uri +
                    ") to anonymous ontology (" + asNode().toString() + ").");
        }
        removeAll(OWL2.versionIRI);
        if (uri != null) {
            addProperty(OWL2.versionIRI, getModel().createResource(uri));
        }
        return this;
    }

    @Override
    public OntIDImpl addImport(String uri) throws OntJenaException {
        if (OntJenaException.notNull(uri, "Null uri specified.").equals(getURI())) {
            throw new OntJenaException.IllegalArgument("Can't import itself: " + uri);
        }
        addImportResource(getModel().createResource(uri));
        return this;
    }

    @Override
    public OntIDImpl removeImport(String uri) {
        Resource r = getModel().createResource(OntJenaException.notNull(uri, "Null uri specified."));
        removeImportResource(r);
        return this;
    }

    @Override
    public Stream<String> imports() {
        return Iterators.asStream(listImportResources().mapWith(Resource::getURI), getCharacteristics());
    }

    public ExtendedIterator<Resource> listImportResources() {
        return listObjects(OWL2.imports)
                .filterKeep(RDFNode::isURIResource)
                .mapWith(RDFNode::asResource);
    }

    public void addImportResource(Resource uri) {
        addProperty(OWL2.imports, uri);
    }

    public void removeImportResource(Resource uri) {
        getModel().remove(this, OWL2.imports, uri);
    }

    @Override
    public String toString() {
        String iri = asNode().toString();
        String ver = getVersionIRI();
        if (ver != null) {
            return iri + "(" + ver + ")";
        }
        return iri;
    }
}
