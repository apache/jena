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

package org.apache.jena.system.progress;

import java.util.Iterator;

import org.apache.jena.atlas.iterator.IteratorWrapper;

/**
 * Send ticks to a {@link ProgressMonitor} as an iterator moves forward.
 */

public class ProgressIterator<T> extends IteratorWrapper<T> {

    private final ProgressMonitor monitor;

    public ProgressIterator(Iterator<T> other, ProgressMonitor monitor) {
        super(other);
        this.monitor = monitor;
    }

    @Override
    public T next() {
        T t = super.next();
        monitor.tick();
        return t;
    }
}
