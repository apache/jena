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

package org.apache.jena.iri;

/**
 * This interface is used for
 * making new {@link IRI} objects.
 * It is used for making IRIs in two ways:
 * <ol>
 * <li>Without
 * resolving against a base (by the class {@link IRIFactory}). 
 * <li>By resolving against a base (by the interface
 * {@link IRI}).
 * </ol>
 * Which properties of the IRIs result in errors or
 * warnings is determined by the 
 * current settings of the underlying {@link IRIFactory},
 * which is the factory object being used in the first
 * case, or the factory object used to create the base
 * IRI in the second case.
 */
public interface IRIFactoryI {

    /**
     * Make a new IRI object (possibly
     * including IRI resolution),
     * and check it for violations
     * of the standards being enforced by the factory.
     * This method both allows IRI resolution
     * against a base, and for creating a new
     * IRI using a different factory, 
     * with different conformance settings,
     * implementing a different URI or IRI standard,
     * or variant thereof.
     * @param i The IRI to use.
     * @return A new IRI object.
     * @throws IRIException If a violation of
     * the standards being enforced by the factory 
     * has been detected, and this violation is 
     * classified by the factory as an error.
     */
    IRI construct(IRI i) throws IRIException;
    /**
     * Make a new IRI object (possibly
     * including IRI resolution),
     * and check it for violations
     * of the standards being enforced by the factory.
     * @param s The IRI to use.
     * @return A new IRI object.
     * @throws IRIException If a violation of
     * the standards being enforced by the factory 
     * has been detected, and this violation is 
     * classified by the factory as an error.
     */
    IRI construct(String s) throws IRIException;
    /**
     * Make a new IRI object (possibly
     * including IRI resolution),
     * and check it for violations
     * of the standards being enforced by the factory.
     * This method both allows IRI resolution
     * against a base, and for creating a new
     * IRI using a different factory, with different
     *  conformance settings,
     * implementing a different URI or IRI standard,
     * or variant thereof.
     *  This method does not throw exceptions, but
     *  records all errors and warnings found
     *  to be queried later using {@link IRI#hasViolation(boolean)}
     *  and {@link IRI#violations(boolean)}.
     * @param i The IRI to use.
     * @return A new IRI object.
     * 
     */
    IRI create(IRI i);
    /**
     * Make a new IRI object (possibly
     * including IRI resolution),
     * and check it for violations
     * of the standards being enforced by the factory.
     *  This method does not throw exceptions, but
     *  records all errors and warnings found
     *  to be queried later using {@link IRI#hasViolation(boolean)}
     *  and {@link IRI#violations(boolean)}.
     * @param s The IRI to use.
     * @return A new IRI object.
     * 
     */
    IRI create(String s);

}
