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

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import org.apache.jena.sparql.service.enhancer.impl.util.StackTraceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a {@link Ref}.
 */
public class RefImpl<T>
    implements Ref<T>
{
    private static final Logger logger = LoggerFactory.getLogger(RefImpl.class);

    protected boolean traceAcquisitions = true;

    protected T value;

    /**
     * The release action is run once immediately when the isAlive() state changes to false.
     * The release action cannot 'revive' a reference as the reference is already 'dead'.
     *
     * The release action differs depending on how a reference was created:
     * On the root reference, the releaseAction releases the wrapped resource
     * On a child reference, the releaseAction releases itself (the child) from the parent one.
     *
     */
    protected AutoCloseable releaseAction;

    // TODO Would it be worthwhile to add a pre-release action that is run immediately before
    //      a ref would become dead?
    // protected AutoCloseable preReleaseAction;

    /**
     * Object on which to synchronize on before any change of state of this reference.
     * This allows for e.g. synchronizing on a {@code Map<K, Reference<V>}, such that
     * closing a reference removes the map entry before it can be accessed and conversely,
     * synchronizing on the map prevents the reference from becoming released.
     */
    protected Object synchronizer;

    protected Object comment; // An attribute which can be used for debugging reference chains
    protected RefImpl<T> parent;
    protected volatile boolean isClosed = false;

    protected StackTraceElement[] acquisitionStackTrace;
    protected StackTraceElement[] closeStackTrace;
    protected StackTraceElement[] closeTriggerStackTrace;

    // A child ref is active as long as its close() method has not been called
    // The WeakHashMap nature may 'hide' entries whose key is about to be GC'd.
    // This can lead to the situation that childRefs.isEmpty() may true even
    // if there are active child refs (whose close method has not yet been called)

    // TODO The map is only for debugging / reporting - remove?
    protected Map<Ref<T>, Object> childRefs = new WeakHashMap<>();
    protected volatile int activeChildRefs = 0;

    public RefImpl(
            RefImpl<T> parent,
            T value,
            Object synchronizer,
            AutoCloseable releaseAction,
            Object comment) {
        super();
        this.parent = parent;
        this.value = value;
        this.releaseAction = releaseAction;
        this.synchronizer = synchronizer == null ? this : synchronizer;
        this.comment = comment;

        if (traceAcquisitions) {
            acquisitionStackTrace = StackTraceUtils.getStackTraceIfEnabled();
        }
    }

    /**
     * Note: Actually this method should be replaced with an approach using Java 9 Cleaner
     * however I couldn't get the cleaner to run.
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void finalize() throws Throwable {
        try {
            if (!isClosed) {
                synchronized (synchronizer) {
                    if (!isClosed) {
                        String msg = "Ref released by GC rather than user logic - indicates resource leak."
                                + "Acquired at " + StackTraceUtils.toString(acquisitionStackTrace);
                        logger.warn(msg);

                        close();
                    }
                }
            }
        } finally {
            super.finalize();
        }
    }

    public Object getComment() {
        return comment;
    }

    @Override
    public Object getSynchronizer() {
        return synchronizer;
    }

    @Override
    public T get() {
        if (isClosed) {
            String msg = "Cannot get value of a closed reference:\n"
                    + "Acquired at " + StackTraceUtils.toString(acquisitionStackTrace) + "\n"
                    + "Closed at " + StackTraceUtils.toString(closeStackTrace) + "\n"
                    + "Close Triggered at " + StackTraceUtils.toString(closeTriggerStackTrace);
            logger.warn(msg);

            throw new RuntimeException("Cannot get value of a closed reference");
        }

        return value;
    }

    @Override
    public Ref<T> acquire(Object comment) {
        synchronized (synchronizer) {
            if (!isAlive()) {
                String msg = "Cannot acquire from a reference with status 'isAlive=false'"
                        + "\nClose triggered at: " + StackTraceUtils.toString(closeTriggerStackTrace);
                throw new RuntimeException(msg);
            }

            // A bit of ugliness to allow the reference to release itself
            @SuppressWarnings("rawtypes")
            Ref[] tmp = new Ref[1];
            tmp[0] = new RefImpl<>(this, value, synchronizer, () -> release(tmp[0]), comment);

            @SuppressWarnings("unchecked")
            Ref<T> result = tmp[0];
            childRefs.put(result, comment);
            ++activeChildRefs;
            //activeChildRefs.incrementAndGet();
            return result;
        }
    }

    protected void release(Object childRef) {
        boolean isContained = childRefs.containsKey(childRef);
        if (isContained) {
            childRefs.remove(childRef);
            --activeChildRefs;
        } else {
            throw new RuntimeException("An unknown reference requested to release itself. Should not happen");
        }

        checkRelease();
    }

    @Override
    public boolean isAlive() {
        boolean result;
        result = !isClosed || activeChildRefs != 0;
        return result;
    }

    @Override
    public void close() {
        synchronized (synchronizer) {
            if (isClosed) {
                String msg = "Reference was already closed." +
                        "\nReleased at: " + StackTraceUtils.toString(closeStackTrace) +
                        "\nAcquired at: " + StackTraceUtils.toString(acquisitionStackTrace);

                logger.debug(msg);
                // Alternatively throw new RuntimeException(msg)?
            } else {
                if (traceAcquisitions) {
                    closeStackTrace = StackTraceUtils.getStackTraceIfEnabled();
                }

                isClosed = true;

                checkRelease();
            }
        }
    }

    protected void checkRelease() {

        if (!isAlive()) {
            if (traceAcquisitions) {
                closeTriggerStackTrace = StackTraceUtils.getStackTraceIfEnabled();
            }

            if (releaseAction != null) {
                try {
                    releaseAction.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static <T extends AutoCloseable> Ref<T> fromCloseable(T value, Object synchronizer) {
        return create(value, synchronizer, value);
    }

    /** Create method where the close action is created from a provided lambda that accepts the value */
    public static <T> Ref<T> create2(T value, Object synchronizer, Consumer<? super T> closer) {
        return create(value, synchronizer, () -> closer.accept(value), null);
    }

    public static <T> Ref<T> create(T value, Object synchronizer, AutoCloseable releaseAction) {
        return create(value, synchronizer, releaseAction, null);
    }

    public static <T> Ref<T> create(T value, Object synchronizer, AutoCloseable releaseAction, Object comment) {
        return new RefImpl<>(null, value, synchronizer, releaseAction, comment);
    }

    public static <T> Ref<T> createClosed() {
        RefImpl<T> result = new RefImpl<>(null, null, null, null, null);
        result.isClosed = true;
        return result;
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @SuppressWarnings("resource")
    @Override
    public Ref<T> getRootRef() {
        RefImpl<T> result = this;
        while (result.parent != null) {
            result = result.parent;
        }
        return result;
    }

    @Override
    public StackTraceElement[] getAcquisitionStackTrace() {
        return acquisitionStackTrace;
    }

    @Override
    public StackTraceElement[] getCloseStackTrace() {
        return closeStackTrace;
    }

    @Override
    public StackTraceElement[] getCloseTriggerStackTrace() {
        return closeTriggerStackTrace;
    }

    @Override
    public String toString() {
        String result = String.format("Ref %s, active(self, #children)=(%b, %d), aquired at %s",
                comment, !isClosed, activeChildRefs, StackTraceUtils.toString(acquisitionStackTrace));
        return result;
    }
}
