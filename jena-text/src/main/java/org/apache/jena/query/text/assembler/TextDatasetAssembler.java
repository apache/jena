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

package org.apache.jena.query.text.assembler;

import static org.apache.jena.query.text.assembler.TextVocab.pDataset;
import static org.apache.jena.query.text.assembler.TextVocab.pIndex;
import static org.apache.jena.query.text.assembler.TextVocab.pIndexes;
import static org.apache.jena.query.text.assembler.TextVocab.pIndexId;
import static org.apache.jena.query.text.assembler.TextVocab.pTextDocProducer;
import static org.apache.jena.query.text.assembler.TextVocab.textDataset;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.text.*;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;
import org.apache.jena.sparql.util.ClsLoader;
import org.apache.jena.sparql.util.graph.GraphUtils;

public class TextDatasetAssembler extends DatasetAssembler implements Assembler {
    public static Resource getType() { return textDataset; }

    @Override
    public DatasetGraph createDataset(Assembler a, Resource root) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dataset open(Assembler a, Resource root, Mode mode) {
        Resource dataset = GraphUtils.getResourceValue(root, pDataset);
        Resource textDocProducerNode = GraphUtils.getResourceValue(root, pTextDocProducer);

        Dataset ds = (Dataset) a.open(dataset);

        // Check for multi-index (text:indexes) vs single-index (text:index)
        Statement indexesStmt = root.getProperty(pIndexes);
        Resource singleIndex = GraphUtils.getResourceValue(root, pIndex);

        if (indexesStmt != null && singleIndex != null) {
            throw new TextIndexException("Cannot specify both text:index and text:indexes on " + root);
        }

        if (indexesStmt != null) {
            return openMultiIndex(a, root, mode, ds, indexesStmt, textDocProducerNode);
        } else {
            return openSingleIndex(a, root, mode, ds, singleIndex, textDocProducerNode);
        }
    }

    private Dataset openSingleIndex(Assembler a, Resource root, Mode mode,
                                     Dataset ds, Resource indexResource, Resource textDocProducerNode) {
        if (indexResource == null) {
            throw new TextIndexException("No text:index property on " + root);
        }
        TextIndex textIndex = (TextIndex) a.open(indexResource);
        TextDocProducer textDocProducer = resolveDocProducer(a, textDocProducerNode, ds, textIndex);

        // In SHACL mode, auto-create ShaclTextDocProducer
        if (textDocProducer == null && textIndex instanceof TextIndexLucene luceneIndex) {
            if (luceneIndex.isShaclMode()) {
                textDocProducer = new ShaclTextDocProducer(
                    ds.asDatasetGraph(), textIndex, luceneIndex.getShaclMapping());
            }
        }

        Dataset dst = TextDatasetFactory.create(ds, textIndex, true, textDocProducer);
        AssemblerUtils.mergeContext(root, dst.getContext());
        return dst;
    }

    private Dataset openMultiIndex(Assembler a, Resource root, Mode mode,
                                    Dataset ds, Statement indexesStmt, Resource textDocProducerNode) {
        RDFNode indexesNode = indexesStmt.getObject();
        if (!indexesNode.canAs(RDFList.class)) {
            throw new TextIndexException("text:indexes must be an RDF list: " + indexesNode);
        }

        RDFList indexesList = indexesNode.as(RDFList.class);
        TextIndexRegistry registry = new TextIndexRegistry();
        List<TextDocProducer> producers = new ArrayList<>();

        for (RDFNode node : indexesList.asJavaList()) {
            if (!node.isResource()) {
                throw new TextIndexException("Each element of text:indexes must be a resource: " + node);
            }
            Resource indexRes = node.asResource();
            TextIndex textIndex = (TextIndex) a.open(indexRes);

            if (!(textIndex instanceof TextIndexLucene luceneIndex)) {
                throw new TextIndexException("Multi-index mode requires TextIndexLucene: " + indexRes);
            }

            // Read text:indexId
            Statement idStmt = indexRes.getProperty(pIndexId);
            String indexId;
            if (idStmt != null) {
                indexId = idStmt.getString();
            } else {
                indexId = indexRes.isURIResource() ? indexRes.getLocalName() : "index_" + registry.size();
            }

            registry.register(indexId, luceneIndex);

            // Create doc producer for this index
            if (luceneIndex.isShaclMode()) {
                producers.add(new ShaclTextDocProducer(
                    ds.asDatasetGraph(), luceneIndex, luceneIndex.getShaclMapping()));
            } else {
                producers.add(new TextDocProducerTriples(luceneIndex));
            }
        }

        TextDocProducer compositeProducer;
        if (textDocProducerNode != null) {
            compositeProducer = resolveDocProducer(a, textDocProducerNode, ds, registry.getDefault());
        } else {
            compositeProducer = new CompositeTextDocProducer(producers);
        }

        DatasetGraph dsg = TextDatasetFactory.create(
            ds.asDatasetGraph(), registry, true, compositeProducer);
        Dataset dst = org.apache.jena.query.DatasetFactory.wrap(dsg);
        AssemblerUtils.mergeContext(root, dst.getContext());
        return dst;
    }

    private TextDocProducer resolveDocProducer(Assembler a, Resource textDocProducerNode,
                                                Dataset ds, TextIndex textIndex) {
        if (textDocProducerNode == null) {
            return null;
        }

        Class<?> c = ClsLoader.loadClass(textDocProducerNode.getURI(), TextDocProducer.class);
        String className = textDocProducerNode.getURI().substring(ARQConstants.javaClassURIScheme.length());
        Constructor<?> dyadic = getConstructor(c, DatasetGraph.class, TextIndex.class);
        Constructor<?> monadic = getConstructor(c, TextIndex.class);

        try {
            if (dyadic != null) {
                return (TextDocProducer) dyadic.newInstance(ds.asDatasetGraph(), textIndex);
            } else if (monadic != null) {
                return (TextDocProducer) monadic.newInstance(textIndex);
            } else {
                Log.warn(ClsLoader.class, "Exception during instantiation '" + className +
                    "' no TextIndex or DatasetGraph,Index constructor");
            }
        } catch (Exception ex) {
            Log.warn(ClsLoader.class, "Exception during instantiation '" + className + "': " + ex.getMessage());
        }
        return null;
    }

    private static Constructor<?> getConstructor(Class<?> c, Class<?>... types) {
        try {
            return c.getConstructor(types);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
