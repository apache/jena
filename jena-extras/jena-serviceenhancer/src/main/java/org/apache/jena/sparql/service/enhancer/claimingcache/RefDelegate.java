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

/** Interface with default methods that delegate Ref's methods */
public interface RefDelegate<T, R extends Ref<T>>
    extends Ref<T>
{
    R getDelegate();

    @Override
    default Ref<T> getRootRef() {
        return getDelegate().getRootRef();
    }

    @Override
    default T get() {
        return getDelegate().get();
    }

    @Override
    default Ref<T> acquire(Object purpose) {
        return getDelegate().acquire(purpose);
    }

    @Override
    default boolean isAlive() {
        return getDelegate().isAlive();
    }

    @Override
    default boolean isClosed() {
        return getDelegate().isClosed();
    }

    @Override
    default void close() {
        getDelegate().close();
    }

    @Override
    default Object getSynchronizer() {
        return getDelegate().getSynchronizer();
    }

    @Override
    default StackTraceElement[] getAcquisitionStackTrace() {
        return getDelegate().getAcquisitionStackTrace();
    }

    @Override
    default StackTraceElement[] getCloseStackTrace() {
        return getDelegate().getCloseStackTrace();
    }

    @Override
    default StackTraceElement[] getCloseTriggerStackTrace() {
        return getDelegate().getCloseTriggerStackTrace();
    }
}
