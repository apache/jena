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

package org.apache.jena.riot.lang;

import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean ;

import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.StreamRDF;

/**
 * Abstract implementation of a producer consumer RDF stream where the streamed
 * RDF feeds a buffer which is exposed via an iterator.
 * <p>
 * In order for this to work correctly the producer thread which is passing
 * information to the {@link StreamRDF} interface typically needs to be on a
 * separate thread. However this depends on how the producer and the consumer
 * are started, if the start of the producer does not block the main thread
 * the consumer can happily be on the main thread.  Generally speaking though
 * it is good practice to ensure that one of the actors on this class is
 * running on a different thread.
 * </p>
 * 
 * @param <T>
 */
public abstract class StreamedRDFIterator<T> implements RDFParserOutputIterator<T> {

    protected BlockingQueue<T> buffer;
    private AtomicBoolean started = new AtomicBoolean(false) ;
    private AtomicBoolean finished = new AtomicBoolean(false) ;
    private T next = null;
    private PrefixMap prefixes = new PrefixMap();
    private String baseIri;

    /**
     * Constant for default buffer size
     */
    public static final int DEFAULT_BUFFER_SIZE = 10000;

    /**
     * Creates a new streamed RDF iterator
     * <p>
     * Buffer size must be chosen carefully in order to avoid performance
     * problems, if you set the buffer size too low you will experience a lot of
     * blocked calls so it will take longer to consume the data from the
     * iterator. For best performance the buffer size should be at least 10% of
     * the expected input size though you may need to tune this depending on how
     * fast your consumer thread is.
     * </p>
     * <p>
     * The fair parameter controls whether the locking policy used for the
     * buffer is fair. When enabled this reduces throughput but also reduces the
     * chance of thread starvation.
     * </p>
     * 
     * @param bufferSize
     *            Buffer size
     * @param fair
     *            Whether the buffer should use a fair locking policy
     */
    public StreamedRDFIterator(int bufferSize, boolean fair) {
        this.buffer = new ArrayBlockingQueue<T>(bufferSize, fair);
    }

    /**
     * Returns whether further elements are available
     * @return True if more elements are available, false otherwise
     * @exception IllegalStateException Thrown if you try to read from the iterator before the stream has had the {@link #start()} method called on it
     */
    @Override
    public final boolean hasNext() {
        if (!this.started.get()) {
            throw new IllegalStateException("Tried to read from iterator before the Stream was started, please ensure that a producer thread has called start() on the stream before attempting to iterate over it");
        } else if (this.next != null) {
            return true;
        } else if (this.finished.get() && buffer.isEmpty()) {
            return false;
        } else {
            this.getNext();
            return this.hasNext();
        }
    }

    /**
     * Helper method for actually getting the next element
     */
    private void getNext() {
        // Use an incremental back off retry strategy to avoid starving the
        // producer thread which is trying to insert into the queue we
        // are attempting to consume
        // We keep a maximum on the back off amount as otherwise we can
        // wait unduly long for new elements to be available

        // The initial back off starts at 0 so we just retry immediately after
        // the first failure
        int backoff = 0;

        while (this.next == null) {
            // We use poll() in favour of the blocking take() as otherwise we
            // can hit a deadlock if we reach this line before finish() is
            // called whereby we would block indefinitely and never be able to
            // continue
            this.next = buffer.poll();

            // As soon as we see the finished signal has been set stop
            // waiting for more elements
            if (this.finished.get())
                break;

            // Back off
            if (backoff > 0) {
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException e) {
                    // Ignore and continue
                }
            }
            // Increment back off up to maximum
            if (backoff < 5) {
                backoff++;
            }
        }
    }

    /**
     * Gets the next element from the iterator
     * @return Next element
     * @exception IllegalStateException Thrown if you try to iterate before the stream has had the {@link #start()} method called
     * @exception NoSuchElementException Thrown if there are no further elements
     */
    @Override
    public final T next() {
        if (!this.started.get()) {
            throw new IllegalStateException("Tried to read from iterator before the Stream was started, please ensure that a producer thread has called start() on the stream before attempting to iterate over it");
        } else if (this.hasNext()) {
            T t = this.next;
            this.next = null;
            return t;
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public final void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void start() {
        if (this.started.get()) {
            throw new IllegalStateException("A StreamedRDFIterator is not reusable, please create a new instance");
        }
        this.started.set(true);
    }
    
    /**
     * Returns whether it is safe to start iterating, this is the case if the {@link #start()}
     * method of the stream has been called
     * @return True if safe to iterate
     */
    public final boolean canIterate() {
        return this.started.get();
    }

    @Override
    public final void base(String base) {
        this.baseIri = base;
    }
    
    /**
     * Gets the most recently seen Base IRI
     * @return Base IRI
     */
    public String getBaseIri() {
        return this.baseIri;
    }

    @Override
    public final void prefix(String prefix, String iri) {
        this.prefixes.add(prefix, iri);
    }
    
    /**
     * Gets the prefix map which contains the prefixes seen so far in the stream
     * @return Prefix Map
     */
    public PrefixMap getPrefixes() {
        return this.prefixes;
    }

    @Override
    public final void finish() {
        this.finished.set(true) ;
    }

}