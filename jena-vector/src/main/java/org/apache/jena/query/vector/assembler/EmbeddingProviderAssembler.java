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

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.query.vector.EmbeddingProvider;
import org.apache.jena.query.vector.VectorException;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.util.graph.GraphUtils;

public class EmbeddingProviderAssembler extends AssemblerBase {
    @Override
    public EmbeddingProvider open(Assembler a, Resource root, Mode mode) {
        String className = GraphUtils.getStringValue(root, VectorVocab.pClass);
        try {
            Object instance = Class.forName(className).getConstructor().newInstance();
            if (!(instance instanceof EmbeddingProvider))
                throw new VectorException(className + " is not an EmbeddingProvider");
            return (EmbeddingProvider)instance;
        } catch (ReflectiveOperationException e) {
            throw new VectorException("Failed to create embedding provider " + className, e);
        }
    }
}
