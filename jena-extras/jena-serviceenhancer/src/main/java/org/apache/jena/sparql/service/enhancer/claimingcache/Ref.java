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

import java.util.function.Function;

/**
 * Interface for nested references.
 * References allow for sharing an entity across several clients and
 * deferring the release of that entity's resources immediately to the point
 * in time when the last reference is released. The main use case is for memory paging
 * such that if several threads request the same page only one physical buffer is handed out
 * from a cache - conversely, as long as a page is still in used by a client, cache eviction
 * and synchronization may be suppressed.
 *
 * Terminology:
 * <ul>
 *   <li>A reference is <b>closed</b> when {@link #close()} was called; <b>open</b> otherwise.</li>
 *   <li>A reference is <b>alive</b> when it is <b>open</b> and/or any of the child refs acquired from it are still <b>alive</b>.</li>
 *   <li>A reference is <b>released</b> (dead) as soon it is no longer alive. This immediately triggers its release action.</li>
 * <ul>
 *
 * Implementation note: At present the alive-check and release action are assumed to run synchronously. As such there
 * is no transition phase ('dying' or 'releasing'). This could be added in the future.</li>
 *
 * @param <T> The value type stored in this reference.
 */
public interface Ref<T>
    extends AutoCloseable
{
    /**
     * Get the root reference
     */
    Ref<T> getRootRef();

    /**
     * Get the referent only iff this ref instance has not yet been closed.
     * This method fails for closed alive refs.
     * A closed reference is alive if it has unclosed child references.
     *
     * For most use cases the referent should be accessed using this method.
     *
     * @return The referent
     */
    T get();

    /**
     * Return the object on which reference acquisition, release and the close action
     * are synchronized on.
     */
    Object getSynchronizer();

    /**
     * Acquire a new reference with a given comment object
     * Acquiration fails if isAlive() returns false
     */
    Ref<T> acquire(Object purpose);

    default Ref<T> acquire() {
        return acquire(null);
    }

    /**
     * A reference may itself be closed, but references to it may keep it alive
     *
     * @return true iff either this reference is not closed or there exists any acquired reference.
     */
    boolean isAlive();

    /**
     * Check whether this reference is closed
     */
    boolean isClosed();

    // Overrides the throws declaration of Autoclose
    @Override
    void close();

    /** Optional operation. References may expose where they were acquired. */
    StackTraceElement[] getAcquisitionStackTrace();

    /** Optional operation. References may expose where they were closed was called. */
    StackTraceElement[] getCloseStackTrace();

    /** Optional operation. References may expose where they were close was triggered upon release. */
    StackTraceElement[] getCloseTriggerStackTrace();

    /**
     * Return a Ref with a new referent obtained by mapping this ref's value with mapper.
     * Closing the returned Ref closes the original one. Synchronizes on the same object as this ref.
     */
    @SuppressWarnings("resource") // Result must be closed by caller
    default <X> Ref<X> acquireMapped(Function<? super T, ? extends X> mapper) {
        Ref<T> base = acquire();
        X mapped = mapper.apply(base.get());
        Ref<X> result = RefImpl.create(mapped, base.getSynchronizer(), base);
        return result;
    }
}
