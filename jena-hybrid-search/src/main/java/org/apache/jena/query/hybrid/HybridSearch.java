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

package org.apache.jena.query.hybrid;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.query.text.TextHit;
import org.apache.jena.query.text.TextIndex;
import org.apache.jena.query.vector.VectorHit;
import org.apache.jena.query.vector.VectorIndex;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class HybridSearch {
    public static final int DEFAULT_LIMIT = 10;
    public static final int DEFAULT_CANDIDATE_LIMIT = 100;
    public static final int DEFAULT_RRF_K = 60;
    public static final float DEFAULT_TEXT_WEIGHT = 1.0f;
    public static final float DEFAULT_VECTOR_WEIGHT = 1.0f;

    public static List<HybridHit> search(TextIndex textIndex, VectorIndex vectorIndex, Node property, String queryText, int limit,
                                         int candidateLimit, int rrfK, float textWeight, float vectorWeight) {
        int effectiveLimit = limit > 0 ? limit : DEFAULT_LIMIT;
        int effectiveCandidateLimit = candidateLimit > 0 ? candidateLimit : Math.max(DEFAULT_CANDIDATE_LIMIT, effectiveLimit);

        Map<Node, MutableHit> hits = new LinkedHashMap<>();
        List<TextHit> textHits = textHits(textIndex, property, queryText, effectiveCandidateLimit);
        for (int i = 0; i < textHits.size(); i++) {
            TextHit hit = textHits.get(i);
            MutableHit mutable = hits.computeIfAbsent(hit.getNode(), MutableHit::new);
            mutable.textRank = i + 1;
            mutable.textScore = hit.getScore();
        }

        List<VectorHit> vectorHits = vectorIndex.query(queryText, effectiveCandidateLimit);
        for (int i = 0; i < vectorHits.size(); i++) {
            VectorHit hit = vectorHits.get(i);
            MutableHit mutable = hits.computeIfAbsent(hit.getNode(), MutableHit::new);
            mutable.vectorRank = i + 1;
            mutable.vectorScore = hit.getScore();
        }

        List<HybridHit> fused = new ArrayList<>();
        for (MutableHit hit : hits.values()) {
            float score = 0;
            if (hit.textRank > 0)
                score += textWeight / (rrfK + hit.textRank);
            if (hit.vectorRank > 0)
                score += vectorWeight / (rrfK + hit.vectorRank);
            fused.add(new HybridHit(hit.node, score, hit.textRank, hit.vectorRank, hit.textScore, hit.vectorScore));
        }
        fused.sort(Comparator.comparing(HybridHit::getScore).reversed().thenComparing(h -> h.getNode().toString()));
        return fused.size() > effectiveLimit ? fused.subList(0, effectiveLimit) : fused;
    }

    private static List<TextHit> textHits(TextIndex textIndex, Node property, String queryText, int candidateLimit) {
        if (property == null)
            return textIndex.query(property, queryText, null, null, candidateLimit);
        Resource resource = ResourceFactory.createResource(property.getURI());
        return textIndex.query(List.of(resource), queryText, null, null, candidateLimit, null);
    }

    private static class MutableHit {
        private final Node node;
        private int textRank = -1;
        private int vectorRank = -1;
        private float textScore = Float.NaN;
        private float vectorScore = Float.NaN;

        private MutableHit(Node node) {
            this.node = node;
        }
    }
}
