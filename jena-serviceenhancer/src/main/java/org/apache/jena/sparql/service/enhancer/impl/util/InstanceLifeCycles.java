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

package org.apache.jena.sparql.service.enhancer.impl.util;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class InstanceLifeCycles {
    public static class InstanceLifeCycleImpl<T>
        implements InstanceLifeCycle<T>, Serializable
    {
        private static final long serialVersionUID = 1L;
        protected Supplier<T> creator;
        protected Consumer<? super T> closer;

        public InstanceLifeCycleImpl(Supplier<T> creator, Consumer<? super T> closer) {
            this.creator = creator;
            this.closer = closer;
        }

        @Override
        public T newInstance() {
            return this.creator.get();
        }

        @Override
        public void closeInstance(T inst) {
            closer.accept(inst);
        }
    }

    public static <T> InstanceLifeCycle<T> of(Supplier<T> creator, Consumer<? super T> closer) {
        return new InstanceLifeCycleImpl<>(creator, closer);
    }

    public static <T> InstanceLifeCycle<T> enclose(InstanceLifeCycle<T> lifeCycle, Runnable beforeAction, Runnable afterAction) {
        return of(() -> {
            if (beforeAction != null) {
                beforeAction.run();
            }
            T r = lifeCycle.newInstance();
            return r;
        }, inst -> {
            try {
                lifeCycle.closeInstance(inst);
            } finally {
                if (afterAction != null) {
                    afterAction.run();
                }
            }
        });
    }

    public static <O, I> InstanceLifeCycle<Entry<O, I>> enclose(InstanceLifeCycle<O> outer, InstanceLifeCycle<I> inner) {
        return of(() -> {
            O o = outer.newInstance();
            I i;
            try {
                i = inner.newInstance();
            } catch (Exception e) {
                // On error creating the inner instance close the outer one
                outer.closeInstance(o);
                throw new RuntimeException(e);
            }
            return Map.entry(o, i);
        },
        e -> {
            O o = e.getKey();
            I i = e.getValue();
            try {
                outer.closeInstance(o);
            } finally {
                inner.closeInstance(i);
            }
        });
    }
}
