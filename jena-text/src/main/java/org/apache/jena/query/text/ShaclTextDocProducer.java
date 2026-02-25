/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.query.text;

import java.util.*;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.text.ShaclIndexMapping.*;
import org.apache.jena.query.text.changes.TextQuadAction;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.eval.PathEval;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entity-per-document change listener for SHACL-driven index profiles.
 * <p>
 * When a relevant triple changes, this producer rebuilds the entire Lucene document
 * for the affected entity by reading all triples from the base dataset.
 * <p>
 * The base dataset is already updated when {@code change()} fires because
 * {@code DatasetGraphTextMonitor.add()} calls {@code super.add()} before {@code record()}.
 */
public class ShaclTextDocProducer implements TextDocProducer {
    private static final Logger log = LoggerFactory.getLogger(ShaclTextDocProducer.class);

    private static final Node RDF_TYPE = RDF.type.asNode();

    private final DatasetGraph baseDataset;
    private final TextIndexLucene indexer;
    private final ShaclIndexMapping mapping;

    private final ThreadLocal<Boolean> inTransaction = ThreadLocal.withInitial(() -> Boolean.FALSE);

    public ShaclTextDocProducer(DatasetGraph baseDataset, TextIndex textIndex, ShaclIndexMapping mapping) {
        this.baseDataset = baseDataset;
        if (!(textIndex instanceof TextIndexLucene)) {
            throw new TextIndexException("ShaclTextDocProducer requires a TextIndexLucene instance");
        }
        this.indexer = (TextIndexLucene) textIndex;
        this.mapping = mapping;
    }

    @Override
    public void start() {
        inTransaction.set(true);
    }

    @Override
    public void finish() {
        inTransaction.set(false);
    }

    @Override
    public void reset() {}

    @Override
    public void change(TextQuadAction qaction, Node g, Node s, Node p, Node o) {
        if (qaction != TextQuadAction.ADD && qaction != TextQuadAction.DELETE)
            return;

        if (RDF_TYPE.equals(p)) {
            // rdf:type change → may add/remove entity from a profile
            handleTypeChange(qaction, s, o);
        } else if (mapping.isRelevantPredicate(p)) {
            // Relevant data predicate → rebuild the entity's documents
            rebuildEntityDocuments(s);
        }
        // Else: irrelevant predicate, ignore

        if (!inTransaction.get()) {
            indexer.commit();
        }
    }

    private void handleTypeChange(TextQuadAction qaction, Node subject, Node typeNode) {
        List<IndexProfile> profiles = mapping.getProfilesForClass(typeNode);
        if (profiles.isEmpty()) {
            return; // Not a type we care about
        }

        if (qaction == TextQuadAction.ADD) {
            // New type assertion → rebuild docs for matching profiles
            rebuildEntityDocuments(subject);
        } else if (qaction == TextQuadAction.DELETE) {
            // Type removed — check if entity still has this type in the dataset
            // If not, delete the corresponding profile document(s)
            rebuildEntityDocuments(subject);
        }
    }

    /**
     * Rebuild all Lucene documents for the given entity by reading its current state
     * from the base dataset.
     */
    private void rebuildEntityDocuments(Node subject) {
        String entityUri = TextQueryFuncs.subjectToString(subject);
        log.trace("rebuildEntityDocuments: {}", entityUri);

        // Get rdf:type values for the entity
        Set<Node> types = new HashSet<>();
        Iterator<Node> typeIter = baseDataset.getDefaultGraph().find(subject, RDF_TYPE, Node.ANY)
            .mapWith(t -> t.getObject());
        while (typeIter.hasNext()) {
            types.add(typeIter.next());
        }

        // Find matching profiles
        Set<IndexProfile> matchedProfiles = new LinkedHashSet<>();
        for (Node type : types) {
            matchedProfiles.addAll(mapping.getProfilesForClass(type));
        }

        if (matchedProfiles.isEmpty()) {
            // No matching profiles — delete any existing docs for this entity
            indexer.deleteEntityByUri(entityUri);
            return;
        }

        // For each matching profile, build an Entity and update the index
        for (IndexProfile profile : matchedProfiles) {
            Entity entity = buildEntity(subject, entityUri, profile);
            indexer.updateEntityForProfile(entity, profile);
        }
    }

    /**
     * Build an Entity by reading all relevant triples for the subject from the base dataset.
     * Uses PathEval for complex paths (sequence, inverse); direct triple match for simple predicates.
     */
    private Entity buildEntity(Node subject, String entityUri, IndexProfile profile) {
        Entity entity = new Entity(entityUri, null);
        Graph graph = baseDataset.getDefaultGraph();

        for (FieldDef fieldDef : profile.getFields()) {
            Path path = fieldDef.getPath();
            if (path != null && fieldDef.hasComplexPath()) {
                // Complex path — use PathEval
                Iterator<Node> values = PathEval.eval(graph, subject, path, null);
                while (values.hasNext()) {
                    Node obj = values.next();
                    Object value = nodeToValue(obj, fieldDef.getFieldType());
                    if (value != null) {
                        entity.addValue(fieldDef.getFieldName(), value);
                    }
                }
            } else {
                // Simple predicate(s) — direct triple match (fast path)
                for (Node predicate : fieldDef.getPredicates()) {
                    Iterator<Node> objects = graph
                        .find(subject, predicate, Node.ANY)
                        .mapWith(t -> t.getObject());
                    while (objects.hasNext()) {
                        Node obj = objects.next();
                        Object value = nodeToValue(obj, fieldDef.getFieldType());
                        if (value != null) {
                            entity.addValue(fieldDef.getFieldName(), value);
                        }
                    }
                }
            }
        }

        return entity;
    }

    private Object nodeToValue(Node obj, FieldType fieldType) {
        if (obj.isLiteral()) {
            switch (fieldType) {
                case INT:
                    try { return Integer.parseInt(obj.getLiteralLexicalForm()); }
                    catch (NumberFormatException e) { return null; }
                case LONG:
                    try { return Long.parseLong(obj.getLiteralLexicalForm()); }
                    catch (NumberFormatException e) { return null; }
                case DOUBLE:
                    try { return Double.parseDouble(obj.getLiteralLexicalForm()); }
                    catch (NumberFormatException e) { return null; }
                default:
                    return obj.getLiteralLexicalForm();
            }
        } else if (obj.isURI()) {
            // URI objects get stored as their URI string (useful for keyword fields)
            return obj.getURI();
        }
        return null;
    }
}
