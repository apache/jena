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

import org.apache.jena.graph.Node;

public class HybridHit {
    private final Node node;
    private final float score;
    private final int textRank;
    private final int vectorRank;
    private final float textScore;
    private final float vectorScore;

    public HybridHit(Node node, float score, int textRank, int vectorRank, float textScore, float vectorScore) {
        this.node = node;
        this.score = score;
        this.textRank = textRank;
        this.vectorRank = vectorRank;
        this.textScore = textScore;
        this.vectorScore = vectorScore;
    }

    public Node getNode() { return node; }
    public float getScore() { return score; }
    public int getTextRank() { return textRank; }
    public int getVectorRank() { return vectorRank; }
    public float getTextScore() { return textScore; }
    public float getVectorScore() { return vectorScore; }
}
