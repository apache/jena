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


package org.apache.jena.sparql.service.enhancer.impl;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.service.enhancer.impl.BatchQueryRewriter.SubstitutionStrategy;

public class BatchQueryRewriterBuilder {
    protected OpServiceInfo serviceInfo;
    protected Var idxVar;
    protected boolean sequentialUnion;
    protected boolean orderRetainingUnion;
    protected boolean omitEndMarker;
    protected SubstitutionStrategy substitutionStrategy;

    public BatchQueryRewriterBuilder(OpServiceInfo serviceInfo, Var idxVar) {
        super();
        this.serviceInfo = serviceInfo;
        this.idxVar = idxVar;
    }

    public boolean isSequentialUnion() {
        return sequentialUnion;
    }

    public BatchQueryRewriterBuilder setSequentialUnion(boolean linearUnion) {
        this.sequentialUnion = linearUnion;
        return this;
    }

    public boolean isOrderRetainingUnion() {
        return orderRetainingUnion;
    }

    public BatchQueryRewriterBuilder setOrderRetainingUnion(boolean orderRetainingUnion) {
        this.orderRetainingUnion = orderRetainingUnion;
        return this;
    }

    public boolean isOmitEndMarker() {
        return omitEndMarker;
    }

    public BatchQueryRewriterBuilder setOmitEndMarker(boolean omitEndMarker) {
        this.omitEndMarker = omitEndMarker;
        return this;
    }

    public BatchQueryRewriterBuilder setSubstitutionStrategy(SubstitutionStrategy substitutionStrategy) {
        this.substitutionStrategy = substitutionStrategy;
        return this;
    }

    public SubstitutionStrategy getSubstitutionStrategy() {
        return substitutionStrategy;
    }

    public static BatchQueryRewriterBuilder from(OpServiceInfo serviceInfo, Var idxVar) {
        return new BatchQueryRewriterBuilder(serviceInfo, idxVar);
    }

    public BatchQueryRewriter build() {
        SubstitutionStrategy finalSubstitutionStrategy = substitutionStrategy == null
                ? SubstitutionStrategy.SUBSTITUTE
                : substitutionStrategy;

        return new BatchQueryRewriter(serviceInfo, idxVar, sequentialUnion, orderRetainingUnion, omitEndMarker, finalSubstitutionStrategy);
    }
}
