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

package org.apache.jena.sparql.exec.tracker;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/** Throwable tracker that only stores the first encountered exception. */
public class ThrowableTrackerFirst
    implements ThrowableTracker
{
    protected Throwable throwable = null;

    @Override
    public void report(Throwable throwable) {
        if (this.throwable == null) {
            this.throwable = throwable;
        }
        // Ignore any throwables after the first
    }

    @Override
    public Iterator<Throwable> getThrowables() {
        return throwable == null ? Collections.emptyIterator() : List.of(throwable).iterator();
    }
}
