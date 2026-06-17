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

import org.apache.lucene.index.VectorSimilarityFunction;

public enum VectorSimilarity {
    COSINE(VectorSimilarityFunction.COSINE),
    DOT_PRODUCT(VectorSimilarityFunction.DOT_PRODUCT),
    EUCLIDEAN(VectorSimilarityFunction.EUCLIDEAN),
    MAXIMUM_INNER_PRODUCT(VectorSimilarityFunction.MAXIMUM_INNER_PRODUCT);

    private final VectorSimilarityFunction luceneFunction;

    VectorSimilarity(VectorSimilarityFunction luceneFunction) {
        this.luceneFunction = luceneFunction;
    }

    public VectorSimilarityFunction luceneFunction() {
        return luceneFunction;
    }

    public static VectorSimilarity fromName(String name) {
        if (name == null)
            return COSINE;
        return switch (name.toLowerCase()) {
            case "cosine" -> COSINE;
            case "dotproduct", "dot_product", "dot-product" -> DOT_PRODUCT;
            case "euclidean" -> EUCLIDEAN;
            case "maximuminnerproduct", "maximum_inner_product", "maximum-inner-product", "maxinnerproduct" -> MAXIMUM_INNER_PRODUCT;
            default -> throw new VectorException("Unknown vector similarity: " + name);
        };
    }
}
