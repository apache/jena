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

package org.apache.jena.irix;

/**
 * Provider: an implementation of a factory for IRIs.
 * This is not an application interface - is the plugin for the provider to jena.
 *
 * @see IRIxResolver
 */
public interface IRIProvider {
    /**
     * Create an IRI, throw {@link IRIException} if the string does not conform to the grammar.
     */
    public IRIx create(String iri) throws IRIException;

//    /**
//     * Create an IRI, throw {@link IRIException} if the string does not conform to the grammar.
//     * If there provider has additional errors and warnings about the IRI, these are signalled
//     * using the two consumer functions.
//     * This operation may still throw IRIException if the IRI string is unacceptable.
//     */
//    public void check(String iri, Consumer<String> errors, Consumer<String> warnings) throws IRIException;

    /**
     * Create an IRI, throw {@link IRIException} if the string does not conform to the grammar
     * or violates additional rules of the provider.
     */
    public void check(String iriStr) throws IRIException;

    /**
     * Run in strict mode - the exact definition of "strict" depends on the provider.
     * When strict a provider should implement to the letter of the specifications,
     * including URI-scheme rules. This strictness should be documented.
     */
    public void strictMode(String scheme, boolean runStrict);

    /*
     * Return the state of strict mode for the given scheme.
     */
    public boolean isStrictMode(String scheme);
}
