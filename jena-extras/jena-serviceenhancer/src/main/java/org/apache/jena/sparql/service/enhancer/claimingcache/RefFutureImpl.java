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

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefFutureImpl<T>
    extends RefDelegateBase<CompletableFuture<T>, Ref<CompletableFuture<T>>>
    implements RefFuture<T>
{
    private static final Logger logger = LoggerFactory.getLogger(RefFutureImpl.class);

    public RefFutureImpl(Ref<CompletableFuture<T>> delegate) {
        super(delegate);
    }

    @Override
    public RefFuture<T> acquire() {
        return wrap(getDelegate().acquire());
    }

    /**
     * A simple wrapping of an instance of {@code Ref<CompletableFuture<T>>}
     * as a more handy instance of {@code RefFuture<T>}.
     * All methods of the returned RefFuture delegate to the original Ref.
     *
     * Note, that {@code RefFuture<T>} is a sub-interface of
     * {@code Ref<CompletableFuture<T>>}.
     */
    public static <T> RefFuture<T> wrap(Ref<CompletableFuture<T>> delegate) {
        return new RefFutureImpl<>(delegate);
    }

    /** Wrap an existing ref with completed future */
    public static <T> RefFuture<T> fromRef(Ref<T> ref) {
        RefFuture<T> result = RefFutureImpl.fromFuture(CompletableFuture.completedFuture(ref), ref.getSynchronizer());
        return result;
    }

    /** Create a ref that upon close cancels the future or closes the ref when it is available s*/
    public static <T> RefFuture<T> fromFuture(CompletableFuture<Ref<T>> future, Object synchronizer) {
      return wrap(RefImpl.create(future.thenApply(Ref::get), synchronizer, () -> cancelFutureOrCloseRef(future), null));
    }

    public static void cancelFutureOrCloseRef(CompletableFuture<? extends Ref<?>> future) {
        cancelFutureOrCloseValue(future, Ref::close);
    }

    /** Registers a whenComplete action that closes the value if loaded. Then immediately attempts to cancel the future. */
    public static <T> void cancelFutureOrCloseValue(CompletableFuture<T> future, Consumer<? super T> valueCloseAction) {

        AtomicBoolean closeActionRun = new AtomicBoolean(false);

        BiConsumer<T, Throwable> closeAction = (value, t) -> {
            // Beware of short circuit evaluation of getAndSet!
            if (!closeActionRun.getAndSet(true) && value != null && valueCloseAction != null) {
                valueCloseAction.accept(value);
            }

            if (t != null) {
                logger.warn("Exception encountered during close", t);
            }
        };

        CompletableFuture<T> derived = future.whenComplete(closeAction);

        try {
            if (!derived.isDone()) {
                future.cancel(true);
                // Wait for exception (possibly due to cancel) or normal completion
                derived.get();
            }
        } catch (CancellationException | InterruptedException | ExecutionException e) {
            logger.warn("Exception raised during close", e);
        }
    }
}