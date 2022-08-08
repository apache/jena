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

import java.util.Objects;

/**
 * A key object capturing the id of an input binding and an id for a
 * corresponding consecutive range of data which needs to be accessed/retrieved.
 */
public class SliceKey
    implements Comparable<SliceKey>
{
    protected int inputId;
    protected int rangeId;

    public SliceKey(int inputId, int rangeId) {
        super();
        this.inputId = inputId;
        this.rangeId = rangeId;
    }

    public int getInputId() {
        return inputId;
    }

    public int getRangeId() {
        return rangeId;
    }

    @Override
    public int compareTo(SliceKey o) {
        int result = inputId == o.inputId
                ? o.rangeId - rangeId
                : o.inputId - inputId;
        return result;
    }

    @Override
    public String toString() {
        return "SliceKey [inputId=" + inputId + ", rangeId=" + rangeId + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputId, rangeId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SliceKey other = (SliceKey) obj;
        return inputId == other.inputId && rangeId == other.rangeId;
    }
}
