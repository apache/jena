/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.rdflink;

import java.util.Objects;

import org.apache.jena.rdflink.RDFLinkDatasetBuilder;
import org.apache.jena.rdfconnection.Isolation;
import org.apache.jena.sparql.core.DatasetGraph;

/**
 * Builder for RDFLink over a local dataset.
 */

public class RDFLinkDatasetBuilder  {
    private DatasetGraph dataset = null;
    private Isolation isolation = Isolation.NONE;

    public static RDFLink connect(DatasetGraph dsg) { return new RDFLinkDataset(dsg, Isolation.NONE); }
    public static RDFLink connect(DatasetGraph dsg, Isolation isolation) { return new RDFLinkDataset(dsg, isolation); }

    public static RDFLinkDatasetBuilder newBuilder() {
        return new RDFLinkDatasetBuilder();
    }

    private RDFLinkDatasetBuilder() {}

    public RDFLinkDatasetBuilder dataset(DatasetGraph dataset) {
        Objects.requireNonNull(dataset);
        this.dataset = dataset;
        return this;
    }

    public RDFLinkDatasetBuilder isolation(Isolation isolation) {
        Objects.requireNonNull(isolation);
        this.isolation = isolation;
        return this;
    }

    public RDFLink build() {
        Objects.requireNonNull(dataset, "No dataset for RDFLinkDataset");
        return new RDFLinkDataset(dataset, isolation);
    }

}

