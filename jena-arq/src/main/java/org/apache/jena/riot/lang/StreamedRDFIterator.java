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
    private boolean finished = false;
    private T next = null;
    private PrefixMap prefixes = new PrefixMap();

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

    @Override
    public final boolean hasNext() {
        if (this.next != null) {
            return true;
        } else if (this.finished && buffer.isEmpty()) {
            return false;
        } else {
            this.getNext();
            return this.hasNext();
        }
    }

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
            if (this.finished)
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

    @Override
    public final T next() {
        if (this.next != null) {
            T t = this.next;
            this.next = null;
            return t;
        } else if (this.finished && buffer.isEmpty()) {
            throw new NoSuchElementException();
        } else {
            this.getNext();
            return this.next;
        }
    }

    @Override
    public final void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void start() {
        // No-op
    }

    @Override
    public void base(String base) {
        // Base URIs are ignored
    }

    @Override
    public void prefix(String prefix, String iri) {
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
        this.finished = true;
    }

}