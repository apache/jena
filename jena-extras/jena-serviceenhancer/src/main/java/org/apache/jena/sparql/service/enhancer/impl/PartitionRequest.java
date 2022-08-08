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

package org.apache.jena.sparql.service.enhancer.impl;

/** Helper class to capture a range of data w.r.t. a partition key (typically a binding) and assign it an id */
public class PartitionRequest<I>
{
    protected long outputId;
    protected I partitionKey;
    protected long offset;
    protected long limit;

    public PartitionRequest(
            long outputId,
            I partition,
            long offset,
            long limit) {
        super();
        this.outputId = outputId;
        this.partitionKey = partition;
        this.offset = offset;
        this.limit = limit;
    }

    public long getOutputId() {
        return outputId;
    }

    public I getPartitionKey() {
        return partitionKey;
    }

    public long getOffset() {
        return offset;
    }

    public long getLimit() {
        return limit;
    }

    public boolean hasOffset() {
        return offset > 0;
    }

    public boolean hasLimit() {
        return limit >= 0 && limit < Long.MAX_VALUE;
    }
}
