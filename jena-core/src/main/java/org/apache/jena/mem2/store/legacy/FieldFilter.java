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
package org.apache.jena.mem2.store.legacy;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import java.util.function.Predicate;

/**
 * A class that encapsulates a filter on fields on a triple.
 * <p>
 * The filter is a predicate that takes a triple and returns true if it passes
 * the filter and false otherwise.
 * </p>
 */
public class FieldFilter {

    public static final FieldFilter EMPTY = new FieldFilter();

    private final Predicate<Triple> filter;

    private final boolean hasFilter;

    private FieldFilter(Predicate<Triple> filter) {
        this.filter = filter;
        this.hasFilter = true;
    }

    private FieldFilter() {
        this.filter = null;
        this.hasFilter = false;
    }

    public static FieldFilter filterOn(Triple.Field f1, Node n1, Triple.Field f2, Node n2) {
        if (n1.isConcrete()) {
            if (n2.isConcrete()) {
                return new FieldFilter(t -> n1.equals(f1.getField(t)) && n2.equals(f2.getField(t)));
            }
            return new FieldFilter(t -> n1.equals(f1.getField(t)));
        } else if (n2.isConcrete()) {
            return new FieldFilter(t -> n2.equals(f2.getField(t)));
        }
        return FieldFilter.EMPTY;
    }

    public boolean hasFilter() {
        return hasFilter;
    }

    public Predicate<Triple> getFilter() {
        return filter;
    }
}
