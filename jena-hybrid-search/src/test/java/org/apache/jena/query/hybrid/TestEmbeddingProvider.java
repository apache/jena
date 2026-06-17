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

import java.util.List;

import org.apache.jena.query.vector.EmbeddingProvider;

public class TestEmbeddingProvider implements EmbeddingProvider {
    @Override
    public List<float[]> embed(List<String> inputs) {
        return inputs.stream().map(TestEmbeddingProvider::vectorFor).toList();
    }

    private static float[] vectorFor(String text) {
        String t = text.toLowerCase();
        if (t.contains("feline") || t.contains("cat") || t.contains("kitten"))
            return new float[] { 1, 0, 0 };
        if (t.contains("vehicle") || t.contains("car"))
            return new float[] { 0, 1, 0 };
        return new float[] { 0, 0, 1 };
    }
}
