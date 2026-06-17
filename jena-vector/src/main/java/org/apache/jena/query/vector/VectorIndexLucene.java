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

package org.apache.jena.query.vector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.KnnFloatVectorField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.KnnFloatVectorQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;

public class VectorIndexLucene implements VectorIndex {
    private static final String FIELD_UID = "uid";
    private static final String FIELD_SUBJECT = "subject";
    private static final String FIELD_VECTOR = "vector";

    private final Directory directory;
    private final EmbeddingProvider embeddingProvider;
    private final Node textPredicate;
    private final int dimension;
    private final VectorSimilarity similarity;
    private volatile IndexWriter indexWriter;

    public VectorIndexLucene(Directory directory, EmbeddingProvider embeddingProvider, Node textPredicate, int dimension, VectorSimilarity similarity) {
        this.directory = directory;
        this.embeddingProvider = embeddingProvider;
        this.textPredicate = textPredicate;
        this.dimension = dimension;
        this.similarity = similarity == null ? VectorSimilarity.COSINE : similarity;
        openIndexWriter();
    }

    @Override
    public void prepareCommit() {
        try {
            indexWriter.prepareCommit();
        } catch (IOException e) {
            throw new VectorException("prepareCommit", e);
        }
    }

    @Override
    public void commit() {
        try {
            indexWriter.flush();
            indexWriter.commit();
        } catch (IOException e) {
            throw new VectorException("commit", e);
        }
    }

    @Override
    public void rollback() {
        IndexWriter writer = indexWriter;
        indexWriter = null;
        try {
            writer.rollback();
        } catch (IOException e) {
            throw new VectorException("rollback", e);
        }
        openIndexWriter();
    }

    @Override
    public void add(Node graph, Node subject, Node predicate, String text) {
        if (!textPredicate.equals(predicate))
            return;
        float[] vector = embeddingProvider.embed(List.of(text)).get(0);
        checkDimension(vector);
        Document doc = new Document();
        String uid = uid(graph, subject, predicate);
        doc.add(new StringField(FIELD_UID, uid, Field.Store.YES));
        doc.add(new StoredField(FIELD_SUBJECT, encodeNode(subject)));
        doc.add(new KnnFloatVectorField(FIELD_VECTOR, vector, similarity.luceneFunction()));
        try {
            indexWriter.updateDocument(new Term(FIELD_UID, uid), doc);
        } catch (IOException e) {
            throw new VectorException("add", e);
        }
    }

    @Override
    public void delete(Node graph, Node subject, Node predicate) {
        if (!textPredicate.equals(predicate))
            return;
        try {
            indexWriter.deleteDocuments(new Term(FIELD_UID, uid(graph, subject, predicate)));
        } catch (IOException e) {
            throw new VectorException("delete", e);
        }
    }

    @Override
    public List<VectorHit> query(String text, int limit) {
        float[] vector = embeddingProvider.embed(List.of(text)).get(0);
        return query(vector, limit);
    }

    @Override
    public List<VectorHit> query(float[] vector, int limit) {
        checkDimension(vector);
        int k = limit > 0 ? limit : 10;
        try (DirectoryReader reader = DirectoryReader.open(indexWriter)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs topDocs = searcher.search(new KnnFloatVectorQuery(FIELD_VECTOR, vector, k), k);
            List<VectorHit> hits = new ArrayList<>(topDocs.scoreDocs.length);
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.storedFields().document(scoreDoc.doc);
                hits.add(new VectorHit(decodeNode(doc.get(FIELD_SUBJECT)), scoreDoc.score));
            }
            return hits;
        } catch (IOException e) {
            throw new VectorException("query", e);
        }
    }

    @Override
    public Node getTextPredicate() {
        return textPredicate;
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public void close() {
        try {
            indexWriter.close();
        } catch (IOException e) {
            throw new VectorException("close", e);
        }
    }

    private void openIndexWriter() {
        try {
            indexWriter = new IndexWriter(directory, new IndexWriterConfig());
            indexWriter.commit();
        } catch (IOException e) {
            throw new VectorException("openIndexWriter", e);
        }
    }

    private void checkDimension(float[] vector) {
        if (vector.length != dimension)
            throw new VectorException("Expected embedding dimension " + dimension + " but got " + vector.length);
    }

    private static String uid(Node graph, Node subject, Node predicate) {
        return encodeNode(canonicalGraph(graph)) + "\n" + encodeNode(subject) + "\n" + encodeNode(predicate);
    }

    private static Node canonicalGraph(Node graph) {
        if (graph == null || org.apache.jena.sparql.core.Quad.isDefaultGraph(graph))
            return org.apache.jena.sparql.core.Quad.defaultGraphNodeGenerated;
        return graph;
    }

    private static String encodeNode(Node node) {
        return node == null ? "" : NodeFmtLib.strTTL(node);
    }

    private static Node decodeNode(String encoded) {
        if (encoded.startsWith("<") && encoded.endsWith(">"))
            return NodeFactory.createURI(encoded.substring(1, encoded.length() - 1));
        throw new VectorException("Only URI subjects are supported in vector index results: " + encoded);
    }
}
