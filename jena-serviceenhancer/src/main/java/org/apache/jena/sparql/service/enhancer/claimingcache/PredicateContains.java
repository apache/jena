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

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

/** Predicate to test for containment in a collection. */
public class PredicateContains<T>
    implements Predicate<T>
{
    private Collection<T> collection;

    public PredicateContains(Collection<T> collection) {
        super();
        this.collection = Objects.requireNonNull(collection);
    }

    @Override
    public boolean test(T t) {
        boolean result = collection.contains(t);
        return result;
    }

    @Override
    public String toString() {
        return collection.toString();
    }
}
