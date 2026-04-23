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

package org.apache.jena.sparql.service.enhancer.claimingcache;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Predicate;

import com.google.common.collect.Range;

/** Predicate to match by a range. */
public class PredicateRange<T extends Comparable<T>>
    implements Predicate<T>, Serializable
{
    private static final long serialVersionUID = 1L;
    private final Range<T> range;

    public PredicateRange(Range<T> range) {
        super();
        this.range = Objects.requireNonNull(range);
    }

    @Override
    public boolean test(T t) {
        boolean result = range.contains(t);
        return result;
    }

    @Override
    public String toString() {
        return range.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(range);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PredicateRange<?> other = (PredicateRange<?>) obj;
        return Objects.equals(range, other.range);
    }
}
