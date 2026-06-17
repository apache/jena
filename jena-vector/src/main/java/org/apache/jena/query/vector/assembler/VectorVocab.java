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

package org.apache.jena.query.vector.assembler;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.system.Vocab;

public class VectorVocab {
    public static final String NS = "http://jena.apache.org/vector#";

    public static final String pfQuery = NS + "query";

    public static final Resource vectorDataset = Vocab.resource(NS, "VectorDataset");
    public static final Resource vectorIndex = Vocab.resource(NS, "VectorIndex");
    public static final Resource vectorIndexLucene = Vocab.resource(NS, "VectorIndexLucene");
    public static final Resource openAICompatibleEmbeddings = Vocab.resource(NS, "OpenAICompatibleEmbeddings");
    public static final Resource embeddingProvider = Vocab.resource(NS, "EmbeddingProvider");
    public static final Resource cosine = Vocab.resource(NS, "cosine");
    public static final Resource dotProduct = Vocab.resource(NS, "dotProduct");
    public static final Resource euclidean = Vocab.resource(NS, "euclidean");
    public static final Resource maximumInnerProduct = Vocab.resource(NS, "maximumInnerProduct");

    public static final Property pDataset = Vocab.property(NS, "dataset");
    public static final Property pIndex = Vocab.property(NS, "index");
    public static final Property pDirectory = Vocab.property(NS, "directory");
    public static final Property pDimension = Vocab.property(NS, "dimension");
    public static final Property pSimilarity = Vocab.property(NS, "similarity");
    public static final Property pTextPredicate = Vocab.property(NS, "textPredicate");
    public static final Property pEmbeddingProvider = Vocab.property(NS, "embeddingProvider");
    public static final Property pEndpoint = Vocab.property(NS, "endpoint");
    public static final Property pModel = Vocab.property(NS, "model");
    public static final Property pApiKeyEnv = Vocab.property(NS, "apiKeyEnv");
    public static final Property pBatchSize = Vocab.property(NS, "batchSize");
    public static final Property pClass = Vocab.property(NS, "class");
}
