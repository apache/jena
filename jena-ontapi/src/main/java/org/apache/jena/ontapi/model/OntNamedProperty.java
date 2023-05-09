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

package org.apache.jena.ontapi.model;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

/**
 * Named Ontology property: {@link OntAnnotationProperty}, {@link OntDataProperty} and {@link OntObjectProperty.Named}.
 *
 * @param <P> subtype of {@link OntNamedProperty}
 */
public interface OntNamedProperty<P extends OntNamedProperty<P>> extends OntEntity, Property {

    /**
     * @see Property#isProperty()
     */
    @Override
    default boolean isProperty() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default P addComment(String txt) {
        return addComment(txt, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default P addComment(String txt, String lang) {
        return annotate(getModel().getRDFSComment(), txt, lang);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default P addLabel(String txt) {
        return addLabel(txt, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default P addLabel(String txt, String lang) {
        return annotate(getModel().getRDFSLabel(), txt, lang);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default P annotate(OntAnnotationProperty predicate, String txt, String lang) {
        return annotate(predicate, getModel().createLiteral(txt, lang));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    default P annotate(OntAnnotationProperty predicate, RDFNode value) {
        addAnnotation(predicate, value);
        return (P) this;
    }
}
