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

package org.apache.jena.sparql.service.enhancer.claimingcache;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/** Essentially a typedef for {@code Ref<CompletableFuture<T>>} */
public interface RefFuture<T>
    extends RefDelegate<CompletableFuture<T>, Ref<CompletableFuture<T>>>
{
    default T await() {
        CompletableFuture<T> cf = get();
        T result;
        try {
            result = cf.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    RefFuture<T> acquire();

    /** Create a sub-reference to a transformed value of the CompletableFuture */
    // Result must be closed by caller
    default <U> RefFuture<U> acquireTransformed(Function<? super T, ? extends U> transform) {
        RefFuture<T> acquired = this.acquire();
        Object synchronizer = acquired.getSynchronizer();

        CompletableFuture<U> future = acquired.get().thenApply(transform);
        RefFuture<U> result = RefFutureImpl.wrap(RefImpl.create(future, synchronizer, acquired::close));
        return result;
    }

    default <U> RefFuture<U> acquireTransformedAndCloseThis(Function<? super T, ? extends U> transform) {
        RefFuture<U> result = acquireTransformed(transform);
        this.close();
        return result;
    }
}
