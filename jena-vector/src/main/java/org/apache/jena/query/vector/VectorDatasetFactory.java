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

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.vector.assembler.VectorVocab;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sys.JenaSystem;

public class VectorDatasetFactory {
    static { JenaSystem.init(); }

    public static Dataset create(String assemblerFile) {
        return (Dataset)AssemblerUtils.build(assemblerFile, VectorVocab.vectorDataset);
    }

    public static Dataset create(Dataset base, VectorIndex vectorIndex) {
        return create(base, vectorIndex, false);
    }

    public static Dataset create(Dataset base, VectorIndex vectorIndex, boolean closeIndexOnDSGClose) {
        return DatasetFactory.wrap(create(base.asDatasetGraph(), vectorIndex, closeIndexOnDSGClose));
    }

    public static DatasetGraph create(DatasetGraph base, VectorIndex vectorIndex) {
        return create(base, vectorIndex, false);
    }

    public static DatasetGraph create(DatasetGraph base, VectorIndex vectorIndex, boolean closeIndexOnDSGClose) {
        DatasetGraph dsg = new DatasetGraphVector(base, vectorIndex, closeIndexOnDSGClose);
        dsg.getContext().set(VectorQuery.vectorIndex, vectorIndex);
        return dsg;
    }
}
