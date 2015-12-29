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

/**
 * Tuples.
 * <p>
 * A Tuple is a fixed length sequence of the objects of the same type. They are
 * immutable and provide value-based {@code hashCode} and {@code .equals()}.
 * <p>
 * There are space-saving implementations for tuples of length 0 to small N and
 * a general purpose implementation.
 * <ul>
 * <li>{@code Tuple} -- the interface 
 * <li>{@code TupleFactory} -- creates {@code Tuples} 
 * <li>{@code TupleMap} -- provides transformations of order of elements
 * </ul>
 */

package org.apache.jena.atlas.lib.tuple;
