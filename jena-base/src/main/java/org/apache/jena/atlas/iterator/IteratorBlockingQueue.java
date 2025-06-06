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

package org.apache.jena.atlas.iterator;

import java.util.Iterator ;
import java.util.NoSuchElementException ;
import java.util.concurrent.BlockingQueue ;

/** Iterator over a blocking queue until queue end seen */

public class IteratorBlockingQueue<T> implements Iterator<T> {
    private BlockingQueue<T> queue;
    private boolean finished = false;
    private T slot = null;
    private T endMarker;

    public IteratorBlockingQueue(BlockingQueue<T> queue, T endMarker) {
        this.queue = queue;
        this.endMarker = endMarker;
    }

    @Override
    public boolean hasNext() {
        if ( finished )
            return false;
        if ( slot != null )
            return true;
        try {
            slot = queue.take();
            if ( slot == endMarker ) {
                finished = true;
                slot = null;
                return false;
            }
            return true;

        } catch (InterruptedException ex) {
            ex.printStackTrace();

        }
        return false;
    }

    @Override
    public T next() {
        if ( !hasNext() )
            throw new NoSuchElementException();
        T item = slot;
        slot = null;
        return item;
    }
}
