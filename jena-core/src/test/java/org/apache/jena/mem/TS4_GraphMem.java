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

package org.apache.jena.mem;

import org.apache.jena.mem.collection.*;
import org.apache.jena.mem.iterator.IteratorOfJenaSetsTest;
import org.apache.jena.mem.iterator.SparseArrayIteratorTest;
import org.apache.jena.mem.pattern.PatternClassifierTest;
import org.apache.jena.mem.spliterator.ArraySpliteratorTest;
import org.apache.jena.mem.spliterator.ArraySubSpliteratorTest;
import org.apache.jena.mem.spliterator.SparseArraySpliteratorTest;
import org.apache.jena.mem.spliterator.SparseArraySubSpliteratorTest;
import org.apache.jena.mem.store.fast.FastArrayBunchTest;
import org.apache.jena.mem.store.fast.FastHashedTripleBunchTest;
import org.apache.jena.mem.store.fast.FastTripleStoreTest;
import org.apache.jena.mem.store.legacy.*;
import org.apache.jena.mem.store.roaring.RoaringBitmapTripleIteratorTest;
import org.apache.jena.mem.store.roaring.RoaringTripleStoreTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    // spliterator/
    SparseArraySubSpliteratorTest.class,
    ArraySubSpliteratorTest.class,
    ArraySpliteratorTest.class,
    SparseArraySpliteratorTest.class,

    // iterator/
    IteratorOfJenaSetsTest.class,
    SparseArrayIteratorTest.class,

    // collection/
    FastHashMapTest.class,
    FastHashMapTest2.class,
    HashCommonMapTest.class,
    HashCommonSetTest.class,
    FastHashSetTest.class,
    FastHashSetTest2.class,

    // store/fast
    FastTripleStoreTest.class,
    FastArrayBunchTest.class,
    FastHashedTripleBunchTest.class,

    // store/roaring
    RoaringTripleStoreTest.class,
    RoaringBitmapTripleIteratorTest.class,

    // store/legacy
    ArrayBunchTest.class,
    LegacyTripleStoreTest.class,
    NodeToTriplesMapMemTest.class,
    FieldFilterTest.class,
    HashedTripleBunchTest.class,

    // pattern/
    PatternClassifierTest.class,

    // --
    GraphMemLegacyTest.class,
    GraphMemFastTest.class,
    GraphMemRoaringTest.class,
    GraphMemTest.class
} )
public class TS4_GraphMem {}
