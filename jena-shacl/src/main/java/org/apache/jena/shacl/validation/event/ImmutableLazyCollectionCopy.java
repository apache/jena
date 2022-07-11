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

package org.apache.jena.shacl.validation.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ImmutableLazyCollectionCopy<T> {
    private final Collection<T> original;
    private final AtomicReference<Collection<T>> copy = new AtomicReference<>();

    public ImmutableLazyCollectionCopy(Collection<T> original) {
        this.original = original;
    }

    public Collection<T> get(){
        return copy.updateAndGet(existingCopy -> existingCopy == null ? Collections.unmodifiableCollection(new ArrayList<>(original)) : existingCopy);
    }

    public String toString(){
        return Optional.ofNullable(copy.get()).orElse(original).toString();
    }

}
