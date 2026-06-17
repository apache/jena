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

import static org.apache.jena.sparql.util.graph.GraphUtils.getResourceValue;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.vector.VectorDatasetFactory;
import org.apache.jena.query.vector.VectorIndex;
import org.apache.jena.rdf.model.Resource;

public class VectorDatasetAssembler extends AssemblerBase {
    @Override
    public Dataset open(Assembler a, Resource root, Mode mode) {
        Resource datasetResource = getResourceValue(root, VectorVocab.pDataset);
        Resource indexResource = getResourceValue(root, VectorVocab.pIndex);
        Dataset dataset = (Dataset)a.open(datasetResource);
        VectorIndex index = (VectorIndex)a.open(indexResource);
        return VectorDatasetFactory.create(dataset, index, true);
    }
}
