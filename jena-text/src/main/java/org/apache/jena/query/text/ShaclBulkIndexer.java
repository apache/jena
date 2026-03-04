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
import org.apache.jena.graph.Triple;
import org.apache.jena.query.text.ShaclIndexMapping.*;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.eval.PathEval;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bulk indexer for SHACL entity-per-document mode.
 * <p>
 * Iterates over all entities in the dataset that match SHACL index profiles,
 * builds Lucene documents, and writes them to the index. Designed for use
 * after bulk-loading data with {@code tdb2.tdbloader}, which bypasses the
 * normal {@link ShaclTextDocProducer} change listener.
 * <p>
 * Usage:
 * <pre>
 *   ShaclBulkIndexer indexer = new ShaclBulkIndexer(datasetGraph, textIndex, mapping);
 *   indexer.index();
 * </pre>
 */
public class ShaclBulkIndexer {
    private static final Logger log = LoggerFactory.getLogger(ShaclBulkIndexer.class);

    private static final Node RDF_TYPE = RDF.type.asNode();

    private final DatasetGraph baseDataset;
    private final TextIndexLucene textIndex;
    private final ShaclIndexMapping mapping;

    private long entityCount = 0;
    private long batchSize = 10000;
    private long commitInterval = 100000;

    public ShaclBulkIndexer(DatasetGraph baseDataset, TextIndex textIndex, ShaclIndexMapping mapping) {
        this.baseDataset = baseDataset;
        if (!(textIndex instanceof TextIndexLucene)) {
            throw new TextIndexException("ShaclBulkIndexer requires a TextIndexLucene instance");
        }
        this.textIndex = (TextIndexLucene) textIndex;
        this.mapping = mapping;
    }

    public void setBatchSize(long batchSize) {
        this.batchSize = batchSize;
    }

    public void setCommitInterval(long commitInterval) {
        this.commitInterval = commitInterval;
    }

    public long getEntityCount() {
        return entityCount;
    }

    /**
     * Index all entities matching SHACL profiles.
     * <p>
     * For each profile, finds all subjects with a matching {@code rdf:type},
     * builds an Entity from the graph triples, and writes to the Lucene index.
     * Commits periodically based on {@code commitInterval}.
     */
    public void index() {
        entityCount = 0;
        Graph defaultGraph = baseDataset.getDefaultGraph();

        // Track which (entityUri, profile) pairs we've already indexed
        Set<String> indexed = new HashSet<>();

        for (IndexProfile profile : mapping.getProfiles()) {
            for (Node targetClass : profile.getTargetClasses()) {
                log.info("Indexing profile {} class {}", profile.getShapeNode(), targetClass);
                long profileCount = 0;

                // Discover entities from default graph
                profileCount += indexEntities(defaultGraph, defaultGraph,
                    targetClass, profile, indexed);

                // Discover entities from named graphs via quad-level iteration
                Iterator<Quad> quadIter = baseDataset.find(
                    Node.ANY, Node.ANY, RDF_TYPE, targetClass);
                Graph unionGraph = baseDataset.getUnionGraph();
                while (quadIter.hasNext()) {
                    Quad quad = quadIter.next();
                    Node subject = quad.getSubject();
                    String entityUri = TextQueryFuncs.subjectToString(subject);

                    String dedupKey = entityUri + "|" + profile.getShapeNode().toString();
                    if (!indexed.add(dedupKey)) {
                        continue;
                    }

                    Entity entity = buildEntity(unionGraph, subject, entityUri, profile);
                    textIndex.updateEntityForProfile(entity, profile);
                    entityCount++;
                    profileCount++;

                    if (commitInterval > 0 && entityCount % commitInterval == 0) {
                        textIndex.commit();
                        log.info("  Committed at {} entities", entityCount);
                    }
                }

                log.info("  Indexed {} entities for class {}", profileCount, targetClass);
            }
        }

        textIndex.commit();
        log.info("Bulk indexing complete: {} entities indexed", entityCount);
    }

    /**
     * Index entities from a graph, using the provided lookup graph for field values.
     */
    private long indexEntities(Graph discoveryGraph, Graph lookupGraph,
                               Node targetClass, IndexProfile profile,
                               Set<String> indexed) {
        long count = 0;
        ExtendedIterator<Triple> typeTriples = discoveryGraph.find(Node.ANY, RDF_TYPE, targetClass);
        try {
            while (typeTriples.hasNext()) {
                Triple t = typeTriples.next();
                Node subject = t.getSubject();
                String entityUri = TextQueryFuncs.subjectToString(subject);

                String dedupKey = entityUri + "|" + profile.getShapeNode().toString();
                if (!indexed.add(dedupKey)) {
                    continue;
                }

                Entity entity = buildEntity(lookupGraph, subject, entityUri, profile);
                textIndex.updateEntityForProfile(entity, profile);
                entityCount++;
                count++;

                if (commitInterval > 0 && entityCount % commitInterval == 0) {
                    textIndex.commit();
                    log.info("  Committed at {} entities", entityCount);
                }
            }
        } finally {
            typeTriples.close();
        }
        return count;
    }

    /**
     * Build an Entity by reading all relevant triples for the subject.
     * Uses PathEval for complex paths (sequence, inverse); direct triple match for simple predicates.
     */
    private Entity buildEntity(Graph graph, Node subject, String entityUri, IndexProfile profile) {
        Entity entity = new Entity(entityUri, null);

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
                    ExtendedIterator<Triple> triples = graph.find(subject, predicate, Node.ANY);
                    try {
                        while (triples.hasNext()) {
                            Node obj = triples.next().getObject();
                            Object value = nodeToValue(obj, fieldDef.getFieldType());
                            if (value != null) {
                                entity.addValue(fieldDef.getFieldName(), value);
                            }
                        }
                    } finally {
                        triples.close();
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
            return obj.getURI();
        }
        return null;
    }
}
