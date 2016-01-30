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

package org.apache.jena.sparql.core.mem;

import static java.util.Arrays.stream;
import static java.util.EnumSet.of;
import static org.apache.jena.sparql.core.mem.TupleSlot.*;

import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Forms for triple indexes.
 *
 */
public enum TripleTableForm implements Supplier<TripleTable>, Predicate<Set<TupleSlot>> {

    /**
     * Subject-predicate-object.
     */
    SPO(of(SUBJECT, PREDICATE), SUBJECT),
    /**
     * Predicate-object-subject.
     */
    POS(of(PREDICATE, OBJECT), PREDICATE),
    /**
     * Object-subject-predicate.
     */
    OSP(of(OBJECT, SUBJECT), OBJECT);
    
    private TripleTableForm(final Set<TupleSlot> tp, final TupleSlot op) {
        this.twoPrefix = tp;
        this.onePrefix = of(op);
    }
    
    @Override
    public TripleTable get() {
        return new PMapTripleTable(name());
    }

    /**
     * Prefixes of the pattern for this table form.
     */
    public final Set<TupleSlot> twoPrefix, onePrefix;

    /**
     * @param pattern
     * @return whether this index form avoids traversal for a query of this pattern
     */
    @Override
    public boolean test(final Set<TupleSlot> pattern) {
        return twoPrefix.equals(pattern) || onePrefix.equals(pattern) || pattern.size() == 3;
    }

    /**
     * @param pattern
     * @return the most appropriate choice of index form for that query
     */
    public static TripleTableForm chooseFrom(final Set<TupleSlot> pattern) {
        return tableForms().filter(f -> f.test(pattern)).findFirst().orElse(SPO);
    }

    /**
     * @return a stream of these table forms
     */
    public static Stream<TripleTableForm> tableForms() {
        return stream(values());
    }
}
