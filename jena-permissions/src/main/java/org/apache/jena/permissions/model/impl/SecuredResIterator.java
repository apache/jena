/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.permissions.model.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.jena.permissions.model.SecuredModel;
import org.apache.jena.permissions.model.SecuredResource;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

public class SecuredResIterator implements ResIterator {

    /**
     * Maps a Resource to a secured resource
     *
     */
    private class PermResourceMap implements Function<Resource, Resource> {
        private final SecuredModel securedModel;

        /**
         * Constructor.
         * 
         * @param securedModel the secured model in which the resources will be created.
         */
        public PermResourceMap(final SecuredModel securedModel) {
            this.securedModel = securedModel;
        }

        @Override
        public SecuredResource apply(final Resource o) {
            return SecuredResourceImpl.getInstance(securedModel, o);
        }
    }

    private final ExtendedIterator<Resource> iter;

    /**
     * Constructor.
     * 
     * @param securedModel The model in which resources will be constructed
     * @param wrapped      the Resource iterator.
     */
    public SecuredResIterator(final SecuredModel securedModel, final ExtendedIterator<Resource> wrapped) {

        final PermResourceMap map1 = new PermResourceMap(securedModel);
        iter = wrapped.mapWith(map1);
    }

    @Override
    public <X extends Resource> ExtendedIterator<Resource> andThen(final Iterator<X> other) {
        return iter.andThen(other);
    }

    @Override
    public void close() {
        iter.close();
    }

    @Override
    public ExtendedIterator<Resource> filterDrop(final Predicate<Resource> f) {
        return iter.filterDrop(f);
    }

    @Override
    public ExtendedIterator<Resource> filterKeep(final Predicate<Resource> f) {
        return iter.filterKeep(f);
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public <U> ExtendedIterator<U> mapWith(final Function<Resource, U> map1) {
        return iter.mapWith(map1);
    }

    @Override
    public Resource next() {
        return iter.next();
    }

    @Override
    public Resource nextResource() {
        return next();
    }

    @Override
    public void remove() {
        iter.remove();
    }

    @Override
    public Resource removeNext() {
        return iter.removeNext();
    }

    @Override
    public List<Resource> toList() {
        return iter.toList();
    }

    @Override
    public Set<Resource> toSet() {
        return iter.toSet();
    }
}
