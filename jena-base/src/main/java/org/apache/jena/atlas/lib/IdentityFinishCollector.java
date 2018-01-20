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

package org.apache.jena.atlas.lib;

import static java.util.stream.Collector.Characteristics.CONCURRENT;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;
import static java.util.stream.Collector.Characteristics.UNORDERED;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;

import org.apache.jena.ext.com.google.common.collect.ImmutableSet;

public interface IdentityFinishCollector<T, A> extends Collector<T, A, A> {

    @Override
    default Function<A, A> finisher() {
        return Function.identity();
    }

    @Override
    default Set<Characteristics> characteristics() {
        return ImmutableSet.of(IDENTITY_FINISH);
    }

    public interface UnorderedIdentityFinishCollector<T, A> extends IdentityFinishCollector<T, A> {

        @Override
        default Set<Characteristics> characteristics() {
            return ImmutableSet.of(UNORDERED, IDENTITY_FINISH);
        }
    }

    public interface ConcurrentUnorderedIdentityFinishCollector<T, A> extends UnorderedIdentityFinishCollector<T, A> {

        @Override
        default Set<Characteristics> characteristics() {
            return ImmutableSet.of(CONCURRENT, UNORDERED, IDENTITY_FINISH);
        }

    }
}
