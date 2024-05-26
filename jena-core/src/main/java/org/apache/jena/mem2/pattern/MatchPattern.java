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

package org.apache.jena.mem2.pattern;

/**
 * A pattern for matching triples.
 * The pattern is defined by the wildcard positions for the subject, predicate and object.
 */
public enum MatchPattern {
    /**
     * Match a triple with a concrete subject, predicate and object.
     */
    SUB_PRE_OBJ,
    /**
     * Match a triple with a concrete subject and predicate, and a wildcard object.
     */
    SUB_PRE_ANY,
    /**
     * Match a triple with a concrete subject and object, and a wildcard predicate.
     */
    SUB_ANY_OBJ,
    /**
     * Match a triple with a concrete subject, and wildcard predicate and object.
     */
    SUB_ANY_ANY,
    /**
     * Match a triple with a concrete predicate and object, and a wildcard subject.
     */
    ANY_PRE_OBJ,
    /**
     * Match a triple with a concrete predicate, and wildcard subject and object.
     */
    ANY_PRE_ANY,
    /**
     * Match a triple with a concrete object, and wildcard subject and predicate.
     */
    ANY_ANY_OBJ,
    /**
     * Match a triple with a wildcard subject, predicate and object.
     */
    ANY_ANY_ANY
}
